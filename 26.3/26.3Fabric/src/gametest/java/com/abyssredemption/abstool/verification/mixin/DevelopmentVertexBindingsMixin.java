package com.abyssredemption.abstool.verification.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Arrays;

/** Test-only correction of Iris 1.11.4's short array for vanilla's 16-slot validation. */
@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class DevelopmentVertexBindingsMixin {
    @WrapOperation(method = "validateDraw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline;getVertexFormatBindings()[Lcom/mojang/blaze3d/vertex/VertexFormat;"))
    private VertexFormat[] completeValidationSlots(RenderPipeline pipeline, Operation<VertexFormat[]> original) {
        VertexFormat[] bindings = original.call(pipeline);
        return Boolean.getBoolean("abstool.schematicTests") && bindings.length < 16
                ? Arrays.copyOf(bindings, 16) : bindings;
    }
}
