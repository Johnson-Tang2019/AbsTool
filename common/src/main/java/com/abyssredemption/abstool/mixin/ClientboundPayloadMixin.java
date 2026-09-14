package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.furnace.FurnaceNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundPayloadMixin {
    @Shadow @Final @Mutable public static StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomPayloadPacket> GAMEPLAY_STREAM_CODEC;
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void abstool$observe(CallbackInfo ci) {
        GAMEPLAY_STREAM_CODEC = FurnaceNetwork.incoming(GAMEPLAY_STREAM_CODEC);
    }

    @Inject(method = "handle(Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void abstool$jade(net.minecraft.network.protocol.common.ClientCommonPacketListener listener, CallbackInfo ci) {
        if (FurnaceNetwork.handleJadeReply(((ClientboundCustomPayloadPacket) (Object) this).payload())) ci.cancel();
    }
}
