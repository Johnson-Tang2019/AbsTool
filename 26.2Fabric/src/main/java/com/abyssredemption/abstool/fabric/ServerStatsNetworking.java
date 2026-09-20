package com.abyssredemption.abstool.fabric;

import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
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
        PayloadTypeRegistry.clientboundPlay().register(HelloResponsePayload.TYPE, HelloResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OverviewResponsePayload.TYPE, OverviewResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TrendResponsePayload.TYPE, TrendResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LeaderboardResponsePayload.TYPE, LeaderboardResponsePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatsErrorPayload.TYPE, StatsErrorPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(HelloResponsePayload.TYPE, (p, c) -> ServerStatsClient.onHello(p));
        ClientPlayNetworking.registerGlobalReceiver(OverviewResponsePayload.TYPE, (p, c) -> ServerStatsClient.onOverview(p));
        ClientPlayNetworking.registerGlobalReceiver(TrendResponsePayload.TYPE, (p, c) -> ServerStatsClient.onTrend(p));
        ClientPlayNetworking.registerGlobalReceiver(LeaderboardResponsePayload.TYPE, (p, c) -> ServerStatsClient.onBoard(p));
        ClientPlayNetworking.registerGlobalReceiver(StatsErrorPayload.TYPE, (p, c) -> ServerStatsClient.onError(p));
        ServerStatsClient.install(ClientPlayNetworking::send, ClientPlayNetworking::canSend);
        ClientTickEvents.END_CLIENT_TICK.register(ServerStatsClient::tick);
    }
}
