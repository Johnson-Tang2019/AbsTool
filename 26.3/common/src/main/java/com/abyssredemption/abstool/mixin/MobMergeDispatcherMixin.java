package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.mobmerge.MobMerge;
import com.abyssredemption.abstool.client.mobmerge.MobMergeState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class MobMergeDispatcherMixin {
    @Inject(method="extractEntity",at=@At("RETURN"))
    private void abstool$extractGroup(Entity entity,float tick,CallbackInfoReturnable<EntityRenderState> callback) {
        MobMerge.decorate(entity,callback.getReturnValue(),tick);
    }
    @Inject(method="shouldRender",at=@At("HEAD"),cancellable=true)
    private void abstool$cullDuplicates(Entity entity,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z,float tick,CallbackInfoReturnable<Boolean> callback) {
        if(MobMerge.hidden(entity)) callback.setReturnValue(false);
    }
    @Inject(method="submit",at=@At("HEAD"),cancellable=true)
    private void abstool$skipDuplicateSubmission(EntityRenderState state,net.minecraft.client.renderer.state.level.CameraRenderState camera,double x,double y,double z,
            com.mojang.blaze3d.vertex.PoseStack poses,net.minecraft.client.renderer.SubmitNodeCollector collector,CallbackInfo callback) {
        if(((MobMergeState)state).abstool$mergedAway()) callback.cancel();
    }
}
