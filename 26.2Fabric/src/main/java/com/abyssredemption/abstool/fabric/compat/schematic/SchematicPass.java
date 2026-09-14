package com.abyssredemption.abstool.fabric.compat.schematic;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import fi.dy.masa.litematica.render.schematic.ChunkRenderBatchDraw;
import fi.dy.masa.litematica.render.schematic.ChunkRenderLayers;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Consumes only this frame's upstream draw records; owns no persistent GPU buffers. */
public final class SchematicPass {
    public static void draw(ChunkRenderBatchDraw batch, ChunkSectionLayerGroup group, GpuSampler sampler) {
        boolean level = ImmediateState.isRenderingLevel;
        boolean bypass = ImmediateState.bypass;
        ImmediateState.isRenderingLevel = false;
        ImmediateState.bypass = true;
        try {
            var client = Minecraft.getInstance();
            var target = client.gameRenderer.mainRenderTarget();
            var indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
            var indexBuffer = batch.maxIndicesRequired() == 0 ? null : indices.getBuffer(batch.maxIndicesRequired());
            try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "abstool:schematic/" + group.name(), target.getColorTextureView(), Optional.empty(),
                    target.getDepthTextureView(), OptionalDouble.empty())) {
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("ChunkFix", batch.chunkFixUBO());
                pass.bindTexture("Sampler0", batch.atlasTexture(), sampler);
                pass.bindTexture("Sampler2", client.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                for (var layer : group.layers()) {
                    var draws = batch.drawData().get(layer);
                    if (draws.isEmpty()) continue;
                    var pair = ChunkRenderLayers.PIPELINE_MAP.get(batch.renderTranslucent() ? ChunkSectionLayer.TRANSLUCENT : layer);
                    pass.setPipeline(batch.renderCollidingBlocks() ? pair.getRight() : pair.getLeft());
                    pass.drawMultipleIndexed(layer == ChunkSectionLayer.TRANSLUCENT ? draws.reversed() : draws,
                            indexBuffer, indices.type(), List.of("DynamicTransforms"), batch.dynamicTransforms());
                    SchematicAdapter.drawn();
                    SchematicAdapter.frameIndices += draws.stream().mapToLong(draw -> draw.indexCount()).sum();
                }
            }
        } catch (RuntimeException error) {
            SchematicAdapter.failed(error);
        } finally {
            ImmediateState.bypass = bypass;
            ImmediateState.isRenderingLevel = level;
        }
    }
}
