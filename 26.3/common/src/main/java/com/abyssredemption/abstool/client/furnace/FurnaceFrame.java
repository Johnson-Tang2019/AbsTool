package com.abyssredemption.abstool.client.furnace;

import com.abyssredemption.abstool.client.vault.client.VaultFrame;
import com.abyssredemption.abstool.client.vault.client.VaultRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record FurnaceFrame(VaultFrame boxes, List<Label> labels) {
    public static final FurnaceFrame EMPTY = new FurnaceFrame(VaultFrame.EMPTY, List.of());
    public FurnaceFrame { labels = List.copyOf(labels); }
    public record Label(int x, int y, int z, String text, int color) {}

    public static void submit(FurnaceFrame frame, CameraRenderState camera, OptionsRenderState options, SubmitNodeCollector collector) {
        if (frame == null) return;
        VaultRenderer.submit(frame.boxes, camera, options, collector);
        for (Label label : frame.labels) {
            PoseStack pose = new PoseStack();
            pose.translate(label.x + 0.5 - camera.pos.x, label.y + 1.25 - camera.pos.y, label.z + 0.5 - camera.pos.z);
            collector.order(102).submitNameTag(pose, Vec3.ZERO, 0,
                    Component.literal(label.text).withColor(label.color & 0xFFFFFF), true, 0xF000F0, camera);
        }
    }
}
