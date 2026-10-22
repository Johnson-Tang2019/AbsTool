package com.abyssredemption.abstool.client.mobmerge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/** Opaque, depth-tested native text with its own shadow; never changes model lighting. */
public final class MobMergeLabels {
    private MobMergeLabels() {}
    public static void submit(SubmitNodeCollector collector,PoseStack poses,Vec3 attachment,int offset,Component text,CameraRenderState camera) {
        if(attachment==null) return;
        poses.pushPose();
        poses.translate(attachment.x,attachment.y+0.5,attachment.z);
        poses.rotate(camera.orientation);
        poses.scale(0.025f,-0.025f,0.025f);
        float x=-Minecraft.getInstance().font.width(text)/2f;
        collector.submitText(poses,x,offset,text.getVisualOrderText(),true,Font.DisplayMode.NORMAL,
                0xF000F0,0xFFFFFFFF,0xA0000000,0);
        poses.popPose();
    }
}
