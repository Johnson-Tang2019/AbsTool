package com.abyssredemption.abstool.fabric.compat.schematic.mixin;

import com.abyssredemption.abstool.fabric.compat.schematic.SchematicAdapter;
import com.abyssredemption.abstool.fabric.compat.schematic.SchematicPass;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import fi.dy.masa.litematica.render.schematic.ChunkRenderBatchDraw;

@Mixin(ChunkRenderBatchDraw.class)
public class SchematicDrawMixin {
    @WrapMethod(method = "draw")
    private void collectSchematic(ChunkSectionLayerGroup group, GpuSampler sampler, ProfilerFiller profiler, Operation<Void> original) {
        if (!SchematicAdapter.active()) { original.call(group, sampler, profiler); return; }
        if (!net.irisshaders.iris.api.v0.IrisApi.getInstance().isRenderingShadowPass()) {
            var batch = (ChunkRenderBatchDraw)(Object)this;
            SchematicAdapter.enqueue(group, () -> SchematicPass.draw(batch, group, sampler));
        }
    }
}
