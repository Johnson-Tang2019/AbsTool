package com.abyssredemption.abstool.client.serverstats.network.payload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
public record HelloRequestPayload(int protocolVersion) implements CustomPacketPayload {
 public static final Type<HelloRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("absservertool","hello_request"));
 public static final StreamCodec<RegistryFriendlyByteBuf,HelloRequestPayload> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.protocolVersion),b->new HelloRequestPayload(b.readVarInt()));
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
