package com.abyssredemption.abstool.client.serverstats.performance;

import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.network.ProtocolConstants;
import com.abyssredemption.abstool.client.serverstats.network.payload.StatsErrorPayload;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-only profiler state. The server owns sampling, permissions, and reports. */
public final class PerformanceClient {
    private static final Map<Long, String> pending = new HashMap<>();
    private static final Map<Long, Long> pendingSince = new HashMap<>();
    private static final Map<String, Long> pageAttempt = new HashMap<>();
    private static final Map<Integer, PerformanceModels.Page<PerformanceModels.Chunk>> chunks = new HashMap<>();
    private static final Map<Integer, PerformanceModels.Page<PerformanceModels.Player>> players = new HashMap<>();
    private static final Map<Integer, PerformanceModels.Page<PerformanceModels.SlowTick>> slowTicks = new HashMap<>();
    private static boolean supported;
    private static PerformancePayloads.StatusResponse status;
    private static PerformanceModels.Summary summary;
    private static PerformanceModels.ChunkDetail detail;
    private static String error;
    private static long lastStatusRequest;

    private PerformanceClient() {}
    public static void clear() {
        supported = false; status = null; summary = null; detail = null; error = null;
        pending.clear(); pendingSince.clear(); pageAttempt.clear();
        chunks.clear(); players.clear(); slowTicks.clear(); lastStatusRequest = 0;
    }
    public static void hello(int capabilities) {
        supported = (capabilities & ProtocolConstants.CAP_PERFORMANCE_PROFILER) != 0;
        if (!supported) clear();
    }
    public static boolean supported() { return supported && ServerStatsClient.canSend(PerformancePayloads.StatusRequest.TYPE.id()); }
    public static PerformancePayloads.StatusResponse status() { return status; }
    public static PerformanceModels.Summary summary() { return summary; }
    public static PerformanceModels.ChunkDetail detail() { return detail; }
    public static String error() { return error; }
    public static boolean pending(String action) { return pending.containsValue(action); }
    public static PerformanceModels.Page<PerformanceModels.Chunk> chunks(int page) { return chunks.get(page); }
    public static PerformanceModels.Page<PerformanceModels.Player> players(int page) { return players.get(page); }
    public static PerformanceModels.Page<PerformanceModels.SlowTick> slowTicks(int page) { return slowTicks.get(page); }
    public static void opened() { requestStatus(); }
    public static void tick(Minecraft client) {
        long now = System.currentTimeMillis();
        pendingSince.entrySet().removeIf(entry -> {
            if (now - entry.getValue() < 6000) return false;
            if (pending.remove(entry.getKey()) != null) error = "timeout";
            return true;
        });
        if (client.gui.screen() instanceof PerformanceProfilerScreen
                && (status == null || status.state() != 0)
                && now - lastStatusRequest >= 2000) requestStatus();
    }
    private static void send(String action, CustomPacketPayload payload, long id) {
        pending.put(id, action);
        pendingSince.put(id, System.currentTimeMillis());
        error = null;
        ServerStatsClient.send(payload);
    }
    public static void requestStatus() {
        if (!supported() || pending("status")) return;
        long id = ServerStatsClient.nextRequestId();
        lastStatusRequest = System.currentTimeMillis();
        send("status", new PerformancePayloads.StatusRequest(ProtocolConstants.VERSION, id), id);
    }
    public static void start(int seconds) {
        if (!supported() || status == null || status.state() != 0 || pending("start") || pending("stop")) return;
        if (seconds < status.minimumDurationSeconds() || seconds <= 0
                || status.maximumDurationSeconds() > 0 && seconds > status.maximumDurationSeconds()) {
            error = "invalid_duration"; return;
        }
        long id = ServerStatsClient.nextRequestId();
        send("start", new PerformancePayloads.StartRequest(ProtocolConstants.VERSION, id, seconds), id);
    }
    public static void stop() {
        if (!supported() || status == null || status.state() != 1 || pending("stop") || pending("start")) return;
        long id = ServerStatsClient.nextRequestId();
        send("stop", new PerformancePayloads.StopRequest(ProtocolConstants.VERSION, id, status.sessionId()), id);
    }
    public static void requestSummary(long reportId) {
        if (!supported() || pending("summary")) return;
        long id = ServerStatsClient.nextRequestId();
        send("summary", new PerformancePayloads.SummaryRequest(ProtocolConstants.VERSION, id, reportId), id);
    }
    public static void requestPage(int tab, int page) {
        if (!supported() || summary == null || page < 1) return;
        String action = "page:" + tab + ":" + page;
        long now = System.currentTimeMillis();
        if (now - pageAttempt.getOrDefault(action, 0L) < 3000) return;
        if (pending(action) || tab == 1 && chunks.containsKey(page) || tab == 2 && players.containsKey(page)
                || tab == 3 && slowTicks.containsKey(page)) return;
        long id = ServerStatsClient.nextRequestId();
        CustomPacketPayload payload = switch (tab) {
            case 1 -> new PerformancePayloads.ChunkPageRequest(ProtocolConstants.VERSION, id, summary.reportId(), page);
            case 2 -> new PerformancePayloads.PlayerPageRequest(ProtocolConstants.VERSION, id, summary.reportId(), page);
            case 3 -> new PerformancePayloads.SlowTickPageRequest(ProtocolConstants.VERSION, id, summary.reportId(), page);
            default -> null;
        };
        if (payload != null) { pageAttempt.put(action, now); send(action, payload, id); }
    }
    public static void requestDetail(PerformanceModels.Chunk chunk) {
        if (!supported() || summary == null || pending("detail")) return;
        detail = null;
        long id = ServerStatsClient.nextRequestId();
        send("detail", new PerformancePayloads.ChunkDetailRequest(ProtocolConstants.VERSION, id,
                summary.reportId(), chunk.dimension(), chunk.x(), chunk.z()), id);
    }
    private static boolean accept(int version, long id, String action) {
        if (version != ProtocolConstants.VERSION) return false;
        pendingSince.remove(id);
        return action.equals(pending.remove(id));
    }
    public static void onStatus(PerformancePayloads.StatusResponse p) {
        if (!accept(p.version(), p.requestId(), "status") || p.state() < 0 || p.state() > 2
                || p.minimumDurationSeconds() < 1 || p.maximumDurationSeconds() != 0
                && p.maximumDurationSeconds() < p.minimumDurationSeconds()) return;
        status = p;
        if (p.state() == 0 && p.latestReportId() > 0 && (summary == null || summary.reportId() != p.latestReportId()))
            requestSummary(p.latestReportId());
    }
    public static void onStart(PerformancePayloads.StartResponse p) {
        if (!accept(p.version(), p.requestId(), "start") || p.sessionId() <= 0 || p.acceptedDurationSeconds() <= 0) return;
        status = new PerformancePayloads.StatusResponse(p.version(), p.requestId(), 1, p.sessionId(),
                status.defaultDurationSeconds(), status.minimumDurationSeconds(), status.maximumDurationSeconds(),
                p.acceptedDurationSeconds(), p.startedAtEpochMillis(), p.expectedEndEpochMillis(), 0,
                status.latestReportId());
    }
    public static void onStop(PerformancePayloads.StopResponse p) {
        if (!accept(p.version(), p.requestId(), "stop") || status == null || p.sessionId() != status.sessionId()) return;
        requestStatus();
        if (p.reportId() > 0) requestSummary(p.reportId());
    }
    public static void onSummary(PerformancePayloads.SummaryResponse p) {
        if (!accept(p.version(), p.requestId(), "summary") || !p.summary().valid()) return;
        if (summary == null || summary.reportId() != p.summary().reportId()) {
            chunks.clear(); players.clear(); slowTicks.clear(); pageAttempt.clear(); detail = null;
        }
        summary = p.summary();
    }
    private static <T> boolean validPage(PerformanceModels.Page<T> p) {
        return summary != null && p.reportId() == summary.reportId() && p.page() >= 1
                && p.totalPages() >= 1 && p.page() <= p.totalPages() && p.totalEntries() >= 0
                && p.entries().size() <= PerformanceModels.MAX_PAGE_ENTRIES;
    }
    public static void onChunks(PerformancePayloads.ChunkPageResponse p) {
        if (accept(p.version(), p.requestId(), "page:1:" + p.page().page()) && validPage(p.page())
                && p.page().entries().stream().allMatch(PerformanceModels.Chunk::valid)) chunks.put(p.page().page(), p.page());
    }
    public static void onPlayers(PerformancePayloads.PlayerPageResponse p) {
        if (accept(p.version(), p.requestId(), "page:2:" + p.page().page()) && validPage(p.page())
                && p.page().entries().stream().allMatch(PerformanceModels.Player::valid)) players.put(p.page().page(), p.page());
    }
    public static void onSlowTicks(PerformancePayloads.SlowTickPageResponse p) {
        if (accept(p.version(), p.requestId(), "page:3:" + p.page().page()) && validPage(p.page())) slowTicks.put(p.page().page(), p.page());
    }
    public static void onDetail(PerformancePayloads.ChunkDetailResponse p) {
        if (accept(p.version(), p.requestId(), "detail") && summary != null && p.reportId() == summary.reportId()
                && p.detail().chunk().valid()) detail = p.detail();
    }
    public static boolean onError(StatsErrorPayload p) {
        if (p.protocolVersion() != ProtocolConstants.VERSION) return false;
        String action = pending.remove(p.requestId());
        if (action == null) return false;
        pendingSince.remove(p.requestId());
        error = "server:" + p.errorCode();
        return true;
    }
}
