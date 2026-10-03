package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.elytra.ElytraLaunch;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ElytraWorldLifecycleMixin {
    @Inject(method = {"setLevel", "clearClientLevel"}, at = @At("HEAD"))
    private void abstool$clearLaunch(CallbackInfo ci) { ElytraLaunch.reset(); }
}
