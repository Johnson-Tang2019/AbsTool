package com.abyssredemption.abstool.fabric.compat.schematic.mixin;

import com.abyssredemption.abstool.fabric.compat.schematic.SchematicAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.irisshaders.iris.pipeline.IrisRenderingPipeline", remap = false)
public class SchematicLevelMixin {
    @Inject(method = "beginLevelRendering", at = @At("HEAD"))
    private void beginSchematic(CallbackInfo ci) { SchematicAdapter.resetFrame(); }
    @Inject(method = "finalizeLevelRendering", at = @At("RETURN"))
    private void compositeSchematic(CallbackInfo ci) { SchematicAdapter.endLevel(); }
}
