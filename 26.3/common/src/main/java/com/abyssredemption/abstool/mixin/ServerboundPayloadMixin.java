package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.furnace.FurnaceNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundPayloadMixin {
    @Shadow @Final @Mutable public static StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> STREAM_CODEC;
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void abstool$encode(CallbackInfo ci) {
        STREAM_CODEC = FurnaceNetwork.outgoing(STREAM_CODEC);
    }
}
