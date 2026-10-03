package com.abyssredemption.abstool.client.serverstats.network.payload;
import net.minecraft.network.RegistryFriendlyByteBuf; import net.minecraft.network.codec.StreamCodec; import net.minecraft.network.protocol.common.custom.CustomPacketPayload; import net.minecraft.resources.Identifier;
public record LeaderboardRequestPayload(int protocolVersion,long requestId,int leaderboardType,int page) implements CustomPacketPayload {
 public static final Type<LeaderboardRequestPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("absservertool","leaderboard_request"));
 public static final StreamCodec<RegistryFriendlyByteBuf,LeaderboardRequestPayload> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.protocolVersion);b.writeVarLong(p.requestId);b.writeVarInt(p.leaderboardType);b.writeVarInt(p.page);},b->new LeaderboardRequestPayload(b.readVarInt(),b.readVarLong(),b.readVarInt(),b.readVarInt()));
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
