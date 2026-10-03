// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.client;

import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

final class VaultRenderTypes {
    static final RenderType SEE_THROUGH_LINES = createSeeThroughLines();

    private VaultRenderTypes() {
    }

    private static RenderType createSeeThroughLines() {
        RenderPipeline vanillaLines = RenderPipelines.LINES;
        RenderPipeline.Snippet lineSnippet = new RenderPipeline.Snippet(
                vanillaLines.getShaders(),
                Optional.of(vanillaLines.getShaderDefines()),
                Optional.of(vanillaLines.getBindGroupLayouts()),
                vanillaLines.getColorTargetStates().toArray(com.mojang.renderpearl.api.pipeline.ColorTargetState[]::new),
                vanillaLines.getColorTargetStates().size(),
                Optional.empty(),
                Optional.of(vanillaLines.getPolygonMode()),
                Optional.of(vanillaLines.isCull()),
                vanillaLines.getVertexFormatBindings().toArray(com.mojang.renderpearl.api.vertex.VertexFormat[]::new),
                Optional.of(vanillaLines.getPrimitiveTopology()),
                vanillaLines.pushConstantSize()
        );

        RenderPipeline pipeline = RenderPipeline.builder(lineSnippet)
                .withLocation(Identifier.fromNamespaceAndPath("abstool", "pipeline/see_through_lines"))
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                .build();
        // Iris selects shaders by pipeline identity. A derived vanilla pipeline
        // needs the same mapping, while retaining our independent depth state.
        try {
            Class.forName("net.irisshaders.iris.pipeline.IrisPipelines")
                    .getMethod("copyPipeline", RenderPipeline.class, RenderPipeline.class)
                    .invoke(null, vanillaLines, pipeline);
        } catch (ClassNotFoundException ignored) {
            // Iris is optional.
        } catch (ReflectiveOperationException | LinkageError error) {
            com.abyssredemption.abstool.AbsTool.LOGGER.warn("Unable to register tracking lines with Iris", error);
        }
        RenderSetup setup = RenderSetup.builder(pipeline)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .createRenderSetup();
        return RenderType.create("abstool_vault_see_through_lines", setup);
    }
}
