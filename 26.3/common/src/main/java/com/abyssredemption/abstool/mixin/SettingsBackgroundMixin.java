package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.waypoint.gui.SettingsAppearance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class SettingsBackgroundMixin {
    @Inject(method="extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",at=@At("HEAD"),cancellable=true)
    private void abstool$lightBackground(GuiGraphicsExtractor graphics,CallbackInfo callback) {
        var screen=(Screen)(Object)this;
        if(SettingsAppearance.translucent(screen)) {
            graphics.fill(0,0,screen.width,screen.height,0x24000000);
            callback.cancel();
        }
    }
    @Inject(method="extractBlurredBackground",at=@At("HEAD"),cancellable=true)
    private void abstool$clearBackdrop(GuiGraphicsExtractor graphics,CallbackInfo callback) {
        if(SettingsAppearance.translucent((Screen)(Object)this)) callback.cancel();
    }
}
