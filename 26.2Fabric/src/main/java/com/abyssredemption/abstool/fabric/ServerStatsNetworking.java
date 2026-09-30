package com.abyssredemption.abstool.fabric;

import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
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
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.SyncRequest.TYPE, WaypointPayloads.SyncRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.CreateRequest.TYPE, WaypointPayloads.CreateRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.UpdateRequest.TYPE, WaypointPayloads.UpdateRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WaypointPayloads.DeleteRequest.TYPE, WaypointPayloads.DeleteRequest.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HelloResponsePayload.TYPE, HelloResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OverviewResponsePayload.TYPE, OverviewResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TrendResponsePayload.TYPE, TrendResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LeaderboardResponsePayload.TYPE, LeaderboardResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatsErrorPayload.TYPE, StatsErrorPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Snapshot.TYPE, WaypointPayloads.Snapshot.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Delta.TYPE, WaypointPayloads.Delta.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Permissions.TYPE, WaypointPayloads.Permissions.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.MutationResult.TYPE, WaypointPayloads.MutationResult.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WaypointPayloads.Error.TYPE, WaypointPayloads.Error.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(HelloResponsePayload.TYPE, (p, c) -> ServerStatsClient.onHello(p));
        ClientPlayNetworking.registerGlobalReceiver(OverviewResponsePayload.TYPE, (p, c) -> ServerStatsClient.onOverview(p));
        ClientPlayNetworking.registerGlobalReceiver(TrendResponsePayload.TYPE, (p, c) -> ServerStatsClient.onTrend(p));
        ClientPlayNetworking.registerGlobalReceiver(LeaderboardResponsePayload.TYPE, (p, c) -> ServerStatsClient.onBoard(p));
        ClientPlayNetworking.registerGlobalReceiver(StatsErrorPayload.TYPE, (p, c) -> ServerStatsClient.onError(p));
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
