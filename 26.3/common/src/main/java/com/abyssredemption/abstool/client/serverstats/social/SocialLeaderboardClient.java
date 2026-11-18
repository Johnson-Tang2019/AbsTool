package com.abyssredemption.abstool.client.serverstats.social;

import com.abyssredemption.abstool.client.serverstats.ServerStatsAvailability;
import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.export.ServerStatsExport;
import com.abyssredemption.abstool.client.serverstats.network.ProtocolConstants;
import com.abyssredemption.abstool.client.serverstats.network.payload.StatsErrorPayload;

public final class SocialLeaderboardClient {
    private static final SocialLeaderboardState STATE=new SocialLeaderboardState();
    private static boolean capability;
    private static long lastAttempt;
    private SocialLeaderboardClient() {}
    public static SocialLeaderboardState state() { return STATE; }
    public static void clear() { capability=false; lastAttempt=0; STATE.clear(); }
    public static void hello(int capabilities) { capability=(capabilities&ProtocolConstants.CAP_SOCIAL_LEADERBOARDS)!=0; if(!capability) STATE.clear(); }
    public static boolean supported() {
        return capability && ServerStatsClient.state().availability()==ServerStatsAvailability.READY && ServerStatsClient.canSend(SocialPayloads.Request.TYPE.id());
    }
    public static void tick() { STATE.tick(System.currentTimeMillis()); }
    public static void request(int board,int page,boolean force) {
        long now=System.currentTimeMillis();
        if(ServerStatsExport.exporting() || !supported() || STATE.pending(board,page) || !force && STATE.fresh(board,page,now) || now-lastAttempt<600) return;
        long id=ServerStatsClient.nextRequestId();
        if(!STATE.expect(id,board,page,now)) return;
        lastAttempt=now; ServerStatsClient.send(new SocialPayloads.Request(2,id,board,page));
    }
    public static void onResponse(SocialPayloads.Response response) {
        ServerStatsExport.social(response);
        STATE.accept(response,System.currentTimeMillis());
    }
    public static boolean onError(StatsErrorPayload error) {
        return STATE.error(error.requestId(),error.protocolVersion(),"server."+error.errorCode()) || error.errorCode()>=40 && error.errorCode()<=45;
    }
}
