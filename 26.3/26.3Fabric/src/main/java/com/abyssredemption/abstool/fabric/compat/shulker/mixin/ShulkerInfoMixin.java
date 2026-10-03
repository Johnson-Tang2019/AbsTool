package com.abyssredemption.abstool.fabric.compat.shulker.mixin;

import com.abyssredemption.abstool.fabric.compat.shulker.DominantItemHint;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.fallenbreath.tweakermore.impl.mc_tweaks.shulkerBoxItemContentHint.ShulkerBoxItemContentHintCommon;
import me.fallenbreath.tweakermore.impl.mc_tweaks.shulkerBoxItemContentHint.ShulkerBoxItemContentHintCommon.Info;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShulkerBoxItemContentHintCommon.class, remap = false)
public abstract class ShulkerInfoMixin {
    @ModifyReturnValue(method = "prepareInformation", at = @At("RETURN"))
    private static Info abstool$dominantItem(Info info, ItemStack box) {
        return DominantItemHint.prepare(info, box);
    }
}
