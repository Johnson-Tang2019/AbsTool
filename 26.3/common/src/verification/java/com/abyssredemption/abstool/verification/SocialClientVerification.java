package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.serverstats.social.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import java.nio.file.*;
import java.util.*;

public final class SocialClientVerification {
    private static int assertions;
    public static void main(String[] args) throws Exception {
        var state=new SocialLeaderboardState(); var response=fixture(300,3,1);
        check(!state.accept(response,0),"Unsolicited response ignored");
        state.expect(300,3,1,0); check(state.accept(response,1),"Expected response accepted");
        state.expect(301,0,1,1); state.expect(302,3,1,1);
        check(state.accept(fixture(301,0,1),2) && state.page(3,1).requestId()==300,"Tab switching keeps independent page cache");
        state.expect(303,3,1,2); check(!state.accept(fixture(302,3,1),3),"Older refresh cannot overwrite latest");
        check(state.accept(fixture(303,3,1),3),"Latest refresh accepted");
        state.expect(304,3,2,3); check(!state.accept(fixture(304,3,1),4),"Page mismatch ignored");
        state.clear(); check(!state.accept(fixture(304,3,2),4) && state.page(3,1)==null,"R server switch clears pending requests and cache");
        check(!state.error(300,2,"server.40"),"Foreign errors do not mutate state");
        state.expect(305,0,1,10); state.tick(6010); check(state.error(0,1).equals("timeout"),"Timeout state");
        check(!state.fresh(3,1,0),"Cleared page is not fresh");
        var request=new SocialPayloads.Request(2,300,3,2); var b=buffer();
        try {
            SocialPayloads.Request.CODEC.encode(b,request); byte[] bytes=new byte[b.readableBytes()]; b.getBytes(0,bytes); Files.write(Path.of("social-request.bin"),bytes);
            check(HexFormat.of().formatHex(bytes).equals("02ac020302"),"Request golden bytes");
            check(SocialPayloads.Request.CODEC.decode(b).equals(request) && !b.isReadable(),"Request exact decode");
        } finally { b.release(); }
        b=buffer(); try {
            SocialPayloads.Response.CODEC.encode(b,response); byte[] bytes=new byte[b.readableBytes()]; b.getBytes(0,bytes); Files.write(Path.of("social-response.bin"),bytes);
            check(SocialPayloads.Response.CODEC.decode(b).equals(response) && !b.isReadable(),"Response exact decode");
        } finally { b.release(); }
        System.out.println("SOCIAL_CLIENT_VERIFICATION_PASSED: "+assertions+" assertions; request correlation, independent tabs, reconnect clearing, timeouts and golden wire fixtures");
    }
    private static RegistryFriendlyByteBuf buffer() { return new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY); }
    private static SocialPayloads.Response fixture(long id,int board,int page) {
        long now=java.time.Instant.parse("2026-10-26T12:00:00Z").toEpochMilli();
        var entry=new SocialPayloads.Entry(1,UUID.fromString("00000000-0000-0000-0000-000000000001"),"中文玩家",56,board==3?3:1,now-86400000L,1,288000,500000,16,3,0,true);
        return new SocialPayloads.Response(2,id,board,page,Math.max(1,page),1,now,now-14*86400000L,now,true,List.of(entry));
    }
    private static void check(boolean condition,String message) { assertions++; if(!condition) throw new AssertionError(message); }
}
