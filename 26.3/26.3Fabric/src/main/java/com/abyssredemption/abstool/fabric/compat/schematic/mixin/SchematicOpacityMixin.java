package com.abyssredemption.abstool.fabric.compat.schematic.mixin;

import com.abyssredemption.abstool.fabric.compat.schematic.SchematicAdapter;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "fi.dy.masa.litematica.render.schematic.WorldRendererSchematic", remap = false)
public class SchematicOpacityMixin {
    @ModifyExpressionValue(method = "prepareBlockLayers", at = @At(value = "NEW", target = "(FFFF)Lorg/joml/Vector4f;"))
    private Vector4f multiplyOpacity(Vector4f color) {
        if (SchematicAdapter.active()) color.w *= (float) ConfigManager.get().schematicShaderCompat.opacityMultiplier;
        return color;
    }
}
