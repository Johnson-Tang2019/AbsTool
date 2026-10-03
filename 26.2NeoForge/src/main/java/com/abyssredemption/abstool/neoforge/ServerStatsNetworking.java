package com.abyssredemption.abstool.neoforge;

import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
import com.abyssredemption.abstool.client.serverstats.performance.PerformanceClient;
import com.abyssredemption.abstool.client.serverstats.performance.PerformancePayloads;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.network.WaypointPayloads;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ServerStatsNetworking {
    private ServerStatsNetworking() {}
    public static void register(RegisterPayloadHandlersEvent event) {
        var r = event.registrar("2").optional();
        r.playToServer(HelloRequestPayload.TYPE, HelloRequestPayload.CODEC, (p, c) -> {});
        r.playToServer(OverviewRequestPayload.TYPE, OverviewRequestPayload.CODEC, (p, c) -> {});
        r.playToServer(TrendRequestPayload.TYPE, TrendRequestPayload.CODEC, (p, c) -> {});
        r.playToServer(LeaderboardRequestPayload.TYPE, LeaderboardRequestPayload.CODEC, (p, c) -> {});
        r.playToServer(PerformancePayloads.StartRequest.TYPE, PerformancePayloads.StartRequest.CODEC, (p,c) -> {});
        r.playToServer(PerformancePayloads.StatusRequest.TYPE, PerformancePayloads.StatusRequest.CODEC, (p,c) -> {});
        r.playToServer(PerformancePayloads.StopRequest.TYPE, PerformancePayloads.StopRequest.CODEC, (p,c) -> {});
        r.playToServer(PerformancePayloads.SummaryRequest.TYPE, PerformancePayloads.SummaryRequest.CODEC, (p,c) -> {});
        r.playToServer(PerformancePayloads.ChunkPageRequest.TYPE, PerformancePayloads.ChunkPageRequest.CODEC, (p,c) -> {});
        r.playToServer(PerformancePayloads.PlayerPageRequest.TYPE, PerformancePayloads.PlayerPageRequest.CODEC, (p,c) -> {});
        r.playToServer(PerformancePayloads.SlowTickPageRequest.TYPE, PerformancePayloads.SlowTickPageRequest.CODEC, (p,c) -> {});
        r.playToServer(PerformancePayloads.ChunkDetailRequest.TYPE, PerformancePayloads.ChunkDetailRequest.CODEC, (p,c) -> {});
        r.playToServer(WaypointPayloads.SyncRequest.TYPE, WaypointPayloads.SyncRequest.CODEC, (p,c) -> {});
        r.playToServer(WaypointPayloads.CreateRequest.TYPE, WaypointPayloads.CreateRequest.CODEC, (p,c) -> {});
        r.playToServer(WaypointPayloads.UpdateRequest.TYPE, WaypointPayloads.UpdateRequest.CODEC, (p,c) -> {});
        r.playToServer(WaypointPayloads.DeleteRequest.TYPE, WaypointPayloads.DeleteRequest.CODEC, (p,c) -> {});
        r.playToClient(HelloResponsePayload.TYPE, HelloResponsePayload.CODEC, (p, c) -> ServerStatsClient.onHello(p));
        r.playToClient(OverviewResponsePayload.TYPE, OverviewResponsePayload.CODEC, (p, c) -> ServerStatsClient.onOverview(p));
        r.playToClient(TrendResponsePayload.TYPE, TrendResponsePayload.CODEC, (p, c) -> ServerStatsClient.onTrend(p));
        r.playToClient(LeaderboardResponsePayload.TYPE, LeaderboardResponsePayload.CODEC, (p, c) -> ServerStatsClient.onBoard(p));
        r.playToClient(StatsErrorPayload.TYPE, StatsErrorPayload.CODEC, (p, c) -> c.enqueueWork(() -> ServerStatsClient.onError(p)));
        r.playToClient(PerformancePayloads.StartResponse.TYPE, PerformancePayloads.StartResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onStart(p)));
        r.playToClient(PerformancePayloads.StatusResponse.TYPE, PerformancePayloads.StatusResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onStatus(p)));
        r.playToClient(PerformancePayloads.StopResponse.TYPE, PerformancePayloads.StopResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onStop(p)));
        r.playToClient(PerformancePayloads.SummaryResponse.TYPE, PerformancePayloads.SummaryResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onSummary(p)));
        r.playToClient(PerformancePayloads.ChunkPageResponse.TYPE, PerformancePayloads.ChunkPageResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onChunks(p)));
        r.playToClient(PerformancePayloads.PlayerPageResponse.TYPE, PerformancePayloads.PlayerPageResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onPlayers(p)));
        r.playToClient(PerformancePayloads.SlowTickPageResponse.TYPE, PerformancePayloads.SlowTickPageResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onSlowTicks(p)));
        r.playToClient(PerformancePayloads.ChunkDetailResponse.TYPE, PerformancePayloads.ChunkDetailResponse.CODEC, (p,c) -> c.enqueueWork(() -> PerformanceClient.onDetail(p)));
        r.playToClient(WaypointPayloads.Snapshot.TYPE, WaypointPayloads.Snapshot.CODEC, (p,c) -> c.enqueueWork(() -> SharedWaypointClient.snapshot(p)));
        r.playToClient(WaypointPayloads.Delta.TYPE, WaypointPayloads.Delta.CODEC, (p,c) -> c.enqueueWork(() -> SharedWaypointClient.delta(p)));
        r.playToClient(WaypointPayloads.Permissions.TYPE, WaypointPayloads.Permissions.CODEC, (p,c) -> c.enqueueWork(() -> SharedWaypointClient.permissions(p)));
        r.playToClient(WaypointPayloads.MutationResult.TYPE, WaypointPayloads.MutationResult.CODEC, (p,c) -> c.enqueueWork(() -> SharedWaypointClient.mutationResult(p)));
        r.playToClient(WaypointPayloads.Error.TYPE, WaypointPayloads.Error.CODEC, (p,c) -> c.enqueueWork(() -> SharedWaypointClient.error(p)));
    }
}
