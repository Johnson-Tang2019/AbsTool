package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.mobmerge.MobMerge;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class MobMergeFrameMixin {
    @Inject(method="extract",at=@At("HEAD"))
    private void abstool$prepareGroups(DeltaTracker delta,Camera camera,float tick,CallbackInfo callback) {
        MobMerge.prepare(delta);
    }
}
