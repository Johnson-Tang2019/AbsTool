package com.abyssredemption.abstool.fabric.compat.shulker.mixin;

import com.abyssredemption.abstool.fabric.compat.shulker.DominantItemHint;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.tweakermore.impl.mc_tweaks.shulkerBoxItemContentHint.ShulkerBoxItemContentHintRenderer;
import me.fallenbreath.tweakermore.impl.mc_tweaks.shulkerBoxItemContentHint.ShulkerBoxItemContentHintCommon.Info;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShulkerBoxItemContentHintRenderer.class, remap = false)
public abstract class ShulkerRendererMixin {
    // Override only the icon/text decisions. The third read still governs the mixed-box bar.
    @WrapOperation(method = "render", at = {
            @At(value = "FIELD", target = "Lme/fallenbreath/tweakermore/impl/mc_tweaks/shulkerBoxItemContentHint/ShulkerBoxItemContentHintCommon$Info;allItemSame:Z", ordinal = 0),
            @At(value = "FIELD", target = "Lme/fallenbreath/tweakermore/impl/mc_tweaks/shulkerBoxItemContentHint/ShulkerBoxItemContentHintCommon$Info;allItemSame:Z", ordinal = 1)
    }, require = 2)
    private static boolean abstool$useDominantIcon(Info info, Operation<Boolean> original) {
        return original.call(info) || DominantItemHint.replacesEllipsis(info);
    }
}
