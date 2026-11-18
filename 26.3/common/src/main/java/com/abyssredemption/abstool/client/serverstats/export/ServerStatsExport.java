package com.abyssredemption.abstool.client.serverstats.export;

import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.export.ExcelWorkbook;
import com.abyssredemption.abstool.client.serverstats.ServerStatsAvailability;
import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.model.ClientLeaderboardPage;
import com.abyssredemption.abstool.client.serverstats.network.LeaderboardType;
import com.abyssredemption.abstool.client.serverstats.network.ProtocolConstants;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
import com.abyssredemption.abstool.client.serverstats.social.SocialLeaderboardClient;
import com.abyssredemption.abstool.client.serverstats.social.SocialPayloads;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Collects one authorized page at a time using the ordinary statistics protocol. */
public final class ServerStatsExport {
    private static final int MAX_PAGES = 1000;
    private static final int MAX_ROWS = 50_000;
    private static final int INITIAL_DELAY = 750;
    private static final int RESPONSE_TIMEOUT = 15_000;
    private static Job job;
    private static boolean writing;
    private static Path exportedPath;
    private static Component status = Component.empty();

    private ServerStatsExport() {}

    public static boolean exporting() { return job != null; }
    public static boolean busy() { return exporting() || writing; }
    public static Component status() { return status; }
    public static Path lastFile() { return exportedPath; }

    public static void start() {
        var state = ServerStatsClient.state();
        var client = Minecraft.getInstance();
        if (busy() || client.getConnection() == null || state.availability() != ServerStatsAvailability.READY) return;
        var server = client.getCurrentServer();
        var next = new Job(client.gameDirectory.toPath().resolve("abstool-exports"),
                server == null ? text("local_world").getString() : server.name);
        next.info("exported_at", Instant.now().toString());
        next.info("timezone", state.timezone());
        next.info("tracking_started", state.trackingStartedDate());
        int caps = state.capabilities();
        if ((caps & ProtocolConstants.CAP_OVERVIEW) != 0 && ServerStatsClient.canSend(OverviewRequestPayload.TYPE.id())) {
            next.queue.add(new Query("overview", (id, page) -> new OverviewRequestPayload(ProtocolConstants.VERSION, id)));
        }
        for (int period = 0; period < 2; period++) {
            int kind = period;
            int bit = period == 0 ? ProtocolConstants.CAP_DAILY_TREND : ProtocolConstants.CAP_WEEKLY_TREND;
            int count = period == 0 ? ProtocolConstants.MAX_DAILY_TREND : ProtocolConstants.MAX_WEEKLY_TREND;
            if ((caps & bit) != 0 && ServerStatsClient.canSend(TrendRequestPayload.TYPE.id())) {
                next.queue.add(new Query(period == 0 ? "daily" : "weekly",
                        (id, page) -> new TrendRequestPayload(ProtocolConstants.VERSION, id, kind, count)));
            }
        }
        if (ServerStatsClient.canSend(LeaderboardRequestPayload.TYPE.id())) {
            for (var type : LeaderboardType.values()) {
                if (state.supports(type)) {
                    next.queue.add(new Query(boardKey(type), (id, page) ->
                            new LeaderboardRequestPayload(ProtocolConstants.VERSION, id, type.networkId(), page)));
                }
            }
        }
        if (SocialLeaderboardClient.supported()) {
            next.queue.add(socialQuery(0, "pigeons"));
            next.queue.add(socialQuery(3, "wanderers"));
        }
        if (next.queue.isEmpty()) {
            fail("unsupported");
            return;
        }
        job = next;
        advance();
    }

    private static String boardKey(LeaderboardType type) { return type.name().toLowerCase(Locale.ROOT); }
    private static Query socialQuery(int board, String key) {
        return new Query(key, (id, page) -> new SocialPayloads.Request(ProtocolConstants.VERSION, id, board, page));
    }

    public static void tick() {
        var current = job;
        if (current == null) return;
        long now = System.nanoTime();
        if (current.id != 0) {
            if (now >= current.deadline) fail("timeout");
            return;
        }
        if (now < current.sendAt) return;
        current.id = ServerStatsClient.nextRequestId();
        current.deadline = now + RESPONSE_TIMEOUT * 1_000_000L;
        try {
            var request = current.query.packet().make(current.id, current.page);
            if (!ServerStatsClient.canSend(request.type().id())) {
                fail("disconnected");
                return;
            }
            ServerStatsClient.send(request);
        } catch (RuntimeException error) {
            AbsTool.LOGGER.warn("Could not request statistics for Excel export", error);
            fail("disconnected");
        }
    }

    public static void disconnected() {
        if (job != null) fail("disconnected");
    }

    private static boolean matches(long id) { return job != null && job.id != 0 && job.id == id; }
    private static boolean expects(String key) { return key.equals(job.query.key()); }

    public static boolean error(StatsErrorPayload error) {
        if (!matches(error.requestId()) && !(error.requestId() == 0 && error.errorCode() == 4 && job != null && job.id != 0)) {
            return false;
        }
        if ((error.errorCode() == 4 || error.errorCode() == 44) && job.delay < 6000) {
            job.delay *= 2;
            schedule();
        } else if ((error.errorCode() == 40 || error.errorCode() == 41) && job.page == 1
                && (expects("pigeons") || expects("wanderers"))) {
            job.info.add(List.of(text(expects("pigeons") ? "pigeons" : "wanderers").getString(),
                    text("unavailable_board").getString()));
            advance();
        } else {
            fail("server_error", error.errorCode());
        }
        return true;
    }

    public static void overview(OverviewResponsePayload data) {
        if (!matches(data.requestId())) return;
        if (!expects("overview")) { fail("changed"); return; }
        job.tables.add(table("overview", "period,period_key,active,time,placements,deaths,complete", List.of(
                List.of(text("today").getString(), data.dateKey(), data.dau(), minutes(data.todayPlayTimeTicks()),
                        data.todayPlacements(), data.todayDeaths(), !data.todayPartial()),
                List.of(text("this_week").getString(), data.weekKey(), data.wau(), minutes(data.weekPlayTimeTicks()),
                        data.weekPlacements(), data.weekDeaths(), !data.weekPartial()))));
        job.info("online", data.onlinePlayers());
        job.info("known", data.knownPlayers());
        advance();
    }

    public static void trend(TrendResponsePayload data) {
        if (!matches(data.requestId())) return;
        if (data.periodType() < 0 || data.periodType() > 1) { fail("changed"); return; }
        String key = data.periodType() == 0 ? "daily" : "weekly";
        if (!expects(key)) { fail("changed"); return; }
        List<List<?>> rows = new ArrayList<>();
        for (var entry : data.entries()) {
            rows.add(List.of(entry.key(), entry.activePlayers(), minutes(entry.playTimeTicks()), entry.placements(), entry.deaths(), entry.complete()));
        }
        job.tables.add(table(key, "period_key,active,time,placements,deaths,complete", rows));
        advance();
    }

    public static void board(long id, ClientLeaderboardPage data) {
        if (!matches(id)) return;
        String key = boardKey(data.type());
        if (!expects(key) || !acceptPage(data.page(), data.totalPages(), data.totalEntries())) { fail("changed"); return; }
        boolean playtime = data.type().name().endsWith("_PLAYTIME");
        for (var entry : data.entries()) {
            if (!acceptPlayer(entry.rank(), entry.uuid())) { fail("changed"); return; }
            Number value;
            if (playtime) value = minutes(entry.value());
            else value = entry.value();
            job.rows.add(List.of(entry.rank(), entry.playerName(), value, entry.online()));
        }
        finishPage(key, playtime ? "rank,player,time,online" : "rank,player,count,online");
    }

    public static void social(SocialPayloads.Response data) {
        if (data.protocolVersion() != ProtocolConstants.VERSION || !matches(data.requestId())) return;
        String key = data.boardType() == 0 ? "pigeons" : "wanderers";
        if ((data.boardType() != 0 && data.boardType() != 3) || !expects(key)
                || !acceptPage(data.page(), data.totalPages(), data.totalEntries())) { fail("changed"); return; }
        for (var entry : data.entries()) {
            if (!acceptPlayer(entry.rank(), entry.uuid())) { fail("changed"); return; }
            job.rows.add(List.of(entry.rank(), entry.playerName(), Component.translatable("abstool.social.subtype." + entry.subtype()).getString(),
                    entry.score(), instant(entry.lastSeenAtEpochMillis()), minutes(entry.windowPlayTimeTicks()),
                    entry.onFootDistanceCm() / 100000.0, entry.observableActions(), entry.activeDays(), entry.shortVisits(), entry.online(),
                    instant(data.windowStartEpochMillis()), instant(data.windowEndEpochMillis()), data.complete()));
        }
        finishPage(key, "rank,player,subtype,score,last_seen,time,distance,actions,active_days,visits,online,window_start,window_end,complete");
    }

    private static boolean acceptPage(int page, int pages, int entries) {
        if (page != job.page || pages < page || entries < 0) return false;
        if (page == 1) {
            job.pages = pages;
            job.entries = entries;
        }
        return pages <= MAX_PAGES && entries <= MAX_ROWS && pages == job.pages && entries == job.entries;
    }

    private static boolean acceptPlayer(int rank, UUID uuid) {
        return rank == job.rows.size() + 1 && rank <= job.entries && job.players.add(uuid);
    }

    private static void finishPage(String key, String columns) {
        if (job.page < job.pages) {
            job.page++;
            progress();
            schedule();
        } else if (job.rows.size() != job.entries) {
            fail("changed");
        } else {
            job.tables.add(table(key, columns, job.rows));
            advance();
        }
    }

    private static ExcelWorkbook.Table table(String key, String columns, List<List<?>> rows) {
        var headers = java.util.Arrays.stream(columns.split(",")).map(column -> text(column).getString()).toList();
        return new ExcelWorkbook.Table(text(key).getString(), headers, List.copyOf(rows));
    }

    private static void advance() {
        job.query = job.queue.poll();
        if (job.query == null) {
            write();
            return;
        }
        job.page = 1;
        job.pages = 1;
        job.rows = new ArrayList<>();
        job.players.clear();
        progress();
        schedule();
    }

    private static void progress() { status = text("collecting", text(job.query.key()), job.page, job.pages); }
    private static void schedule() {
        job.id = 0;
        job.sendAt = System.nanoTime() + job.delay * 1_000_000L;
    }

    private static void write() {
        var ready = job;
        var client = Minecraft.getInstance();
        ready.tables.addFirst(table("info", "field,value", ready.info));
        job = null;
        writing = true;
        status = text("writing");
        CompletableFuture.supplyAsync(() -> ExcelWorkbook.save(ready.directory, "server-stats", ready.tables)).whenComplete((path, error) ->
                client.execute(() -> {
                    writing = false;
                    if (error != null) {
                        AbsTool.LOGGER.warn("Could not export server statistics", error);
                        fail("write_failed");
                    } else {
                        exportedPath = path;
                        notifyStatus(text("saved", path.getFileName().toString()));
                    }
                }));
    }

    private static void fail(String key, Object... arguments) {
        job = null;
        notifyStatus(text(key, arguments));
    }

    private static void notifyStatus(Component message) {
        status = message;
        SystemToast.add(Minecraft.getInstance().gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                text("button"), message);
    }

    private static Component text(String key, Object... arguments) {
        return Component.translatable("abstool.stats.export." + key, arguments);
    }
    private static double minutes(long ticks) { return ticks / 1200.0; }
    private static String instant(long epoch) { return epoch == 0 ? "" : Instant.ofEpochMilli(epoch).toString(); }

    @FunctionalInterface
    private interface Request { CustomPacketPayload make(long id, int page); }
    private record Query(String key, Request packet) {}

    private static final class Job {
        final Path directory;
        final ArrayDeque<Query> queue = new ArrayDeque<>();
        final List<ExcelWorkbook.Table> tables = new ArrayList<>();
        final List<List<?>> info = new ArrayList<>();
        final HashSet<UUID> players = new HashSet<>();
        Query query;
        List<List<?>> rows;
        int page, pages, entries;
        long id, deadline, sendAt;
        int delay = INITIAL_DELAY;

        Job(Path directory, String server) {
            this.directory = directory;
            info("server", server);
        }

        void info(String key, Object value) { info.add(List.of(text(key).getString(), value)); }
    }
}
