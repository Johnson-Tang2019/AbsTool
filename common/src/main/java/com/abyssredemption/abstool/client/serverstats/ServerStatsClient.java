package com.abyssredemption.abstool.client.serverstats;

import com.abyssredemption.abstool.client.serverstats.model.*;
import com.abyssredemption.abstool.client.serverstats.network.*;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Shared statistics lifecycle; each loader supplies its networking transport. */
public final class ServerStatsClient {
    private static final ServerStatsState STATE = new ServerStatsState();
    private static Consumer<CustomPacketPayload> sender;
    private static Predicate<Identifier> canSend;
    private static Object connection;
    private static long nextId;
    private static int ticks;

    private ServerStatsClient() {}
    public static ServerStatsState state() { return STATE; }
    public static void install(Consumer<CustomPacketPayload> send, Predicate<Identifier> available) {
        sender = send;
        canSend = available;
    }
    public static void tick(Minecraft client) {
        if (connection != client.getConnection()) {
            connection = client.getConnection();
            STATE.clear();
            ticks = 0;
        }
        if (connection == null || sender == null) return;
        ticks++;
        if (STATE.availability() == ServerStatsAvailability.UNKNOWN && ticks % 20 == 0) {
            if (canSend.test(HelloRequestPayload.TYPE.id())) {
                STATE.availability(ServerStatsAvailability.CHECKING);
                sender.accept(new HelloRequestPayload(ProtocolConstants.VERSION));
            } else if (ticks >= 60) STATE.availability(ServerStatsAvailability.UNSUPPORTED);
        }
        if (STATE.availability() == ServerStatsAvailability.CHECKING && ticks >= 200) STATE.availability(ServerStatsAvailability.ERROR);
    }
    public static void onHello(HelloResponsePayload p) {
        if (p.protocolVersion() != ProtocolConstants.VERSION) {
            STATE.availability(ServerStatsAvailability.PROTOCOL_MISMATCH);
            return;
        }
        STATE.hello(p.capabilities(), p.trackingStartedDate(), p.timezone(), p.todayPartial());
        requestOverview(true);
    }
    public static void onOverview(OverviewResponsePayload p) {
        if (p.protocolVersion() != ProtocolConstants.VERSION || p.onlinePlayers() < 0 || p.knownPlayers() < 0 || p.dau() < 0 || p.wau() < 0) return;
        STATE.putOverview(new ServerOverview(p.dateKey(), p.weekKey(), p.onlinePlayers(), p.knownPlayers(), p.dau(), p.wau(), p.todayPlayTimeTicks(), p.weekPlayTimeTicks(), p.todayPlacements(), p.weekPlacements(), p.todayDeaths(), p.weekDeaths(), p.todayPartial(), p.weekPartial(), p.generatedAtMillis()), p.requestId());
    }
    public static void onTrend(TrendResponsePayload p) {
        if (p.protocolVersion() != ProtocolConstants.VERSION || p.entries().size() > ProtocolConstants.MAX_DAILY_TREND) return;
        STATE.putTrend(p.periodType(), p.entries(), p.requestId());
    }
    public static void onBoard(LeaderboardResponsePayload p) {
        if (p.protocolVersion() != ProtocolConstants.VERSION || p.totalPages() < 1 || p.page() < 1 || p.totalEntries() < 0 || p.entries().size() > ProtocolConstants.MAX_ENTRIES) return;
        var type = LeaderboardType.fromNetworkId(p.leaderboardType());
        if (type.isEmpty()) return;
        var entries = p.entries().stream().map(e -> new ClientLeaderboardEntry(e.rank(), e.uuid(), e.playerName().isBlank() ? "未知玩家" : e.playerName(), Math.max(0, e.value()), e.online())).toList();
        STATE.put(new ClientLeaderboardPage(type.get(), p.page(), p.totalPages(), p.totalEntries(), p.generatedAtEpochMillis(), p.contextLabel(), entries), p.requestId());
    }
    public static void onError(StatsErrorPayload payload) { STATE.availability(ServerStatsAvailability.ERROR); }
    public static void requestOverview(boolean force) {
        if (STATE.availability() == ServerStatsAvailability.READY && (force || STATE.overview() == null)) {
            long id = ++nextId;
            STATE.expect("overview", id);
            sender.accept(new OverviewRequestPayload(ProtocolConstants.VERSION, id));
        }
    }
    public static void requestTrend(int period, int count, boolean force) {
        if (STATE.availability() == ServerStatsAvailability.READY && (force || !STATE.freshTrend(period))) {
            long id = ++nextId;
            STATE.expect("trend:" + period, id);
            sender.accept(new TrendRequestPayload(ProtocolConstants.VERSION, id, period, count));
        }
    }
    public static void request(LeaderboardType type, int page, boolean force) {
        page = Math.max(1, page);
        if (STATE.availability() == ServerStatsAvailability.READY && STATE.supports(type) && (force || !STATE.fresh(type, page))) {
            long id = ++nextId;
            STATE.expect(type + ":" + page, id);
            sender.accept(new LeaderboardRequestPayload(ProtocolConstants.VERSION, id, type.networkId(), page));
        }
    }
}
