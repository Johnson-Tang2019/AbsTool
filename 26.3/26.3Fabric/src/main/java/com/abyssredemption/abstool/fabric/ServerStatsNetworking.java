package com.abyssredemption.abstool.fabric;

import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
import com.abyssredemption.abstool.client.serverstats.performance.PerformanceClient;
import com.abyssredemption.abstool.client.serverstats.performance.PerformancePayloads;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.network.WaypointPayloads;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** Registers the same optional statistics protocol used by the NeoForge client. */
public final class ServerStatsNetworking {
    private ServerStatsNetworking() {}
    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(HelloRequestPayload.TYPE, HelloRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(OverviewRequestPayload.TYPE, OverviewRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TrendRequestPayload.TYPE, TrendRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(LeaderboardRequestPayload.TYPE, LeaderboardRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.StartRequest.TYPE, PerformancePayloads.StartRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.StatusRequest.TYPE, PerformancePayloads.StatusRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.StopRequest.TYPE, PerformancePayloads.StopRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.SummaryRequest.TYPE, PerformancePayloads.SummaryRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.ChunkPageRequest.TYPE, PerformancePayloads.ChunkPageRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.PlayerPageRequest.TYPE, PerformancePayloads.PlayerPageRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.SlowTickPageRequest.TYPE, PerformancePayloads.SlowTickPageRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PerformancePayloads.ChunkDetailRequest.TYPE, PerformancePayloads.ChunkDetailRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.SyncRequest.TYPE, WaypointPayloads.SyncRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.CreateRequest.TYPE, WaypointPayloads.CreateRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.UpdateRequest.TYPE, WaypointPayloads.UpdateRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.DeleteRequest.TYPE, WaypointPayloads.DeleteRequest.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HelloResponsePayload.TYPE, HelloResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OverviewResponsePayload.TYPE, OverviewResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TrendResponsePayload.TYPE, TrendResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LeaderboardResponsePayload.TYPE, LeaderboardResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatsErrorPayload.TYPE, StatsErrorPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.StartResponse.TYPE, PerformancePayloads.StartResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.StatusResponse.TYPE, PerformancePayloads.StatusResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.StopResponse.TYPE, PerformancePayloads.StopResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.SummaryResponse.TYPE, PerformancePayloads.SummaryResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.ChunkPageResponse.TYPE, PerformancePayloads.ChunkPageResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.PlayerPageResponse.TYPE, PerformancePayloads.PlayerPageResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.SlowTickPageResponse.TYPE, PerformancePayloads.SlowTickPageResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PerformancePayloads.ChunkDetailResponse.TYPE, PerformancePayloads.ChunkDetailResponse.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Snapshot.TYPE, WaypointPayloads.Snapshot.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Delta.TYPE, WaypointPayloads.Delta.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Permissions.TYPE, WaypointPayloads.Permissions.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.MutationResult.TYPE, WaypointPayloads.MutationResult.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Error.TYPE, WaypointPayloads.Error.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(HelloResponsePayload.TYPE, (p, c) -> ServerStatsClient.onHello(p));
        ClientPlayNetworking.registerGlobalReceiver(OverviewResponsePayload.TYPE, (p, c) -> ServerStatsClient.onOverview(p));
        ClientPlayNetworking.registerGlobalReceiver(TrendResponsePayload.TYPE, (p, c) -> ServerStatsClient.onTrend(p));
        ClientPlayNetworking.registerGlobalReceiver(LeaderboardResponsePayload.TYPE, (p, c) -> ServerStatsClient.onBoard(p));
        ClientPlayNetworking.registerGlobalReceiver(StatsErrorPayload.TYPE, (p, c) -> c.client().execute(() -> ServerStatsClient.onError(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.StartResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onStart(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.StatusResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onStatus(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.StopResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onStop(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.SummaryResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onSummary(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.ChunkPageResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onChunks(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.PlayerPageResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onPlayers(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.SlowTickPageResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onSlowTicks(p)));
        ClientPlayNetworking.registerGlobalReceiver(PerformancePayloads.ChunkDetailResponse.TYPE, (p,c) -> c.client().execute(() -> PerformanceClient.onDetail(p)));
        ClientPlayNetworking.registerGlobalReceiver(WaypointPayloads.Snapshot.TYPE, (p,c) -> c.client().execute(() -> SharedWaypointClient.snapshot(p)));
        ClientPlayNetworking.registerGlobalReceiver(WaypointPayloads.Delta.TYPE, (p,c) -> c.client().execute(() -> SharedWaypointClient.delta(p)));
        ClientPlayNetworking.registerGlobalReceiver(WaypointPayloads.Permissions.TYPE, (p,c) -> c.client().execute(() -> SharedWaypointClient.permissions(p)));
        ClientPlayNetworking.registerGlobalReceiver(WaypointPayloads.MutationResult.TYPE, (p,c) -> c.client().execute(() -> SharedWaypointClient.mutationResult(p)));
        ClientPlayNetworking.registerGlobalReceiver(WaypointPayloads.Error.TYPE, (p,c) -> c.client().execute(() -> SharedWaypointClient.error(p)));
        ServerStatsClient.install(ClientPlayNetworking::send, ClientPlayNetworking::canSend);
        SharedWaypointClient.install(ClientPlayNetworking::send, ClientPlayNetworking::canSend);
        ClientTickEvents.END_CLIENT_TICK.register(ServerStatsClient::tick);
    }
}
