package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.screenshot.ScreenshotClipboard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screenshot.class)
public abstract class ScreenshotClipboardMixin {
    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V"), require = 1)
    private static void abstool$copySavedImage(NativeImage image, File file, Operation<Void> original) throws IOException {
        original.call(image, file);
        ScreenshotClipboard.saved(file);
    }
}
