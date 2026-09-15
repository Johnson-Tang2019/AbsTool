package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.elytra.ElytraLaunch;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ElytraLaunchMixin {
    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onInput(Lnet/minecraft/client/player/ClientInput;)V"))
    private void abstool$input(CallbackInfo ci) { ElytraLaunch.input((LocalPlayer)(Object)this); }
    @Inject(method = "tick", at = @At("RETURN"))
    private void abstool$afterMovement(CallbackInfo ci) { ElytraLaunch.afterMovement((LocalPlayer)(Object)this); }
}
