package com.abyssredemption.abstool.neoforge;

import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ServerStatsNetworking {
    private ServerStatsNetworking() {}
    public static void register(RegisterPayloadHandlersEvent event) {
        var r = event.registrar("2").optional();
        r.playToServer(HelloRequestPayload.TYPE, HelloRequestPayload.CODEC, (p, c) -> {});
        r.playToServer(OverviewRequestPayload.TYPE, OverviewRequestPayload.CODEC, (p, c) -> {});
        r.playToServer(TrendRequestPayload.TYPE, TrendRequestPayload.CODEC, (p, c) -> {});
        r.playToServer(LeaderboardRequestPayload.TYPE, LeaderboardRequestPayload.CODEC, (p, c) -> {});
        r.playToClient(HelloResponsePayload.TYPE, HelloResponsePayload.CODEC, (p, c) -> ServerStatsClient.onHello(p));
        r.playToClient(OverviewResponsePayload.TYPE, OverviewResponsePayload.CODEC, (p, c) -> ServerStatsClient.onOverview(p));
        r.playToClient(TrendResponsePayload.TYPE, TrendResponsePayload.CODEC, (p, c) -> ServerStatsClient.onTrend(p));
        r.playToClient(LeaderboardResponsePayload.TYPE, LeaderboardResponsePayload.CODEC, (p, c) -> ServerStatsClient.onBoard(p));
        r.playToClient(StatsErrorPayload.TYPE, StatsErrorPayload.CODEC, (p, c) -> ServerStatsClient.onError(p));
    }
}
