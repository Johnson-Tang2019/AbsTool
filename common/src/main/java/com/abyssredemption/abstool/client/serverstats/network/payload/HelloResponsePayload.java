package com.abyssredemption.abstool.client.serverstats.network.payload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
public record HelloResponsePayload(int protocolVersion, String serverModVersion, int capabilities, int pageSize, String trackingStartedDate, String timezone, boolean todayPartial) implements CustomPacketPayload {
 public static final Type<HelloResponsePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("absservertool","hello_response"));
 public static final StreamCodec<RegistryFriendlyByteBuf,HelloResponsePayload> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.protocolVersion);b.writeUtf(p.serverModVersion,32);b.writeVarInt(p.capabilities);b.writeVarInt(p.pageSize);b.writeUtf(p.trackingStartedDate,16);b.writeUtf(p.timezone,64);b.writeBoolean(p.todayPartial);},b->new HelloResponsePayload(b.readVarInt(),b.readUtf(32),b.readVarInt(),b.readVarInt(),b.readUtf(16),b.readUtf(64),b.readBoolean()));
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
