package com.abyssredemption.abstool.verification.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class FrameSamplesMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void sampleFrame(CallbackInfo ci) {
        if (Boolean.getBoolean("abstool.schematicTests")) com.abyssredemption.abstool.verification.FrameSamples.frame();
    }
}
