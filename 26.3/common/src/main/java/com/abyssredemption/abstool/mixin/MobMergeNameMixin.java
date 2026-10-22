package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.mobmerge.MobMergeLabels;
import com.abyssredemption.abstool.client.mobmerge.MobMergeState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep the normal entity-name method and Iris context hooks around our text submission. */
@Mixin(EntityRenderer.class)
public abstract class MobMergeNameMixin {
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    private void abstool$readableCount(SubmitNodeCollector collector,PoseStack poses,Vec3 attachment,int offset,Component text,boolean seeThrough,int light,
            CameraRenderState camera,Operation<Void> original,@Local(argsOnly=true) EntityRenderState state) {
        if(((MobMergeState)state).abstool$groupCount()>1 && text==state.nameTag) MobMergeLabels.submit(collector,poses,attachment,offset,text,camera);
        else original.call(collector,poses,attachment,offset,text,seeThrough,light,camera);
    }
}
