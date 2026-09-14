// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class VaultRenderer {
    public static void submit(VaultFrame frame, CameraRenderState cameraState,
                              OptionsRenderState options, SubmitNodeCollector collector) {
        if (frame == null || frame.targets().isEmpty()) return;
        Vec3 camera = cameraState.pos;
        Vector3f origin = getCrosshairOrigin(cameraState, options.bobView, options.damageTiltStrength);
        collector.order(100).submitCustomGeometry(new PoseStack(), VaultRenderTypes.SEE_THROUGH_LINES, (pose, buffer) -> {
            for (VaultFrame.Target target : frame.targets()) {
                drawBox(buffer, pose, target.x() - camera.x,
                        target.y() - camera.y, target.z() - camera.z, target.color());
            }
        });
        if (frame.tracers()) {
            collector.order(101).submitCustomGeometry(new PoseStack(), RenderTypes.lines(), (pose, buffer) -> {
                for (VaultFrame.Target target : frame.targets()) {
                    drawLine(buffer, pose, origin.x, origin.y, origin.z,
                            target.x() + 0.5 - camera.x, target.y() + 0.5 - camera.y,
                            target.z() + 0.5 - camera.z, frame.tracerColor());
                }
            });
        }
    }

    private static void drawBox(VertexConsumer buffer, PoseStack.Pose pose, double x, double y, double z, int color) {
        for (int a = 0; a <= 1; a++) {
            for (int b = 0; b <= 1; b++) {
                drawLine(buffer, pose, x, y + a, z + b, x + 1, y + a, z + b, color);
                drawLine(buffer, pose, x + a, y, z + b, x + a, y + 1, z + b, color);
                drawLine(buffer, pose, x + a, y + b, z, x + a, y + b, z + 1, color);
            }
        }
    }

    /** Return a camera-relative point on the crosshair ray, in front of the near plane. */
    public static Vector3f getCrosshairOrigin(
            net.minecraft.client.renderer.state.level.CameraRenderState cameraState,
            boolean bobView,
            double damageTiltStrength
    ) {
        if (cameraState.projectionMatrix == null || cameraState.viewRotationMatrix == null) {
            return new Vector3f(0.0F, -0.15F, 0.0F);
        }

        PoseStack bobStack = new PoseStack();
        applyHurtBob(cameraState, bobStack, damageTiltStrength);
        if (bobView) applyViewBob(cameraState, bobStack);

        // GameRenderer multiplies view bob into the projection matrix before rendering the
        // level. Meteor unprojects the screen center with that same matrix.
        Matrix4f projection = new Matrix4f(cameraState.projectionMatrix).mul(bobStack.last().pose());
        // Minecraft 26.2 uses reverse-Z: depth zero is the far plane. Mid-depth
        // keeps the tracer origin just in front of the camera instead of kilometers away.
        Vector4f center = new Vector4f(0.0F, 0.0F, 0.5F, 1.0F)
                .mul(projection.invert())
                .mul(new Matrix4f(cameraState.viewRotationMatrix).invert());
        center.div(center.w);
        return new Vector3f(center.x, center.y, center.z);
    }

    private static void applyHurtBob(
            net.minecraft.client.renderer.state.level.CameraRenderState cameraState,
            PoseStack matrices,
            double damageTiltStrength
    ) {
        var entity = cameraState.entityRenderState;
        if (!entity.isLiving) return;

        float hurt = entity.hurtTime;
        if (entity.isDeadOrDying) {
            float duration = Math.min(entity.deathTime, 20.0F);
            matrices.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (duration + 200.0F)));
        }
        if (hurt <= 0.0F || entity.hurtDuration <= 0) return;

        hurt /= entity.hurtDuration;
        hurt = Mth.sin(hurt * hurt * hurt * hurt * (float) Math.PI);
        matrices.mulPose(Axis.YP.rotationDegrees(-entity.hurtDir));
        matrices.mulPose(Axis.ZP.rotationDegrees((float) (-hurt * 14.0 * damageTiltStrength)));
        matrices.mulPose(Axis.YP.rotationDegrees(entity.hurtDir));
    }

    private static void applyViewBob(
            net.minecraft.client.renderer.state.level.CameraRenderState cameraState,
            PoseStack matrices
    ) {
        var entity = cameraState.entityRenderState;
        if (!entity.isPlayer) return;

        float walk = entity.backwardsInterpolatedWalkDistance;
        float bob = entity.bob;
        matrices.translate(
                Mth.sin(walk * (float) Math.PI) * bob * 0.5F,
                -Math.abs(Mth.cos(walk * (float) Math.PI) * bob),
                0.0F
        );
        matrices.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walk * (float) Math.PI) * bob * 3.0F));
        matrices.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(walk * (float) Math.PI - 0.2F) * bob) * 5.0F));
    }

    private static void drawLine(VertexConsumer consumer, PoseStack.Pose pose, double x1, double y1, double z1, double x2, double y2, double z2, int argb) {
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0.0F) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        Vector3f normal = new Vector3f(nx, ny, nz);
        consumer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(argb).setLineWidth(2.0F).setNormal(pose, normal);
        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(argb).setLineWidth(2.0F).setNormal(pose, normal);
    }

}
