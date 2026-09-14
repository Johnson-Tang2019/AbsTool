package com.abyssredemption.abstool.fabric.compat.schematic.mixin;

import com.abyssredemption.abstool.fabric.compat.schematic.SchematicAdapter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.*;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "fi.dy.masa.litematica.render.schematic.BufferBuilderCache", remap = false)
public class SchematicBuffersMixin {
    @WrapOperation(method = "lambda$getBuilder$0", at = @At(value = "NEW", target = "com/mojang/blaze3d/vertex/BufferBuilder"))
    private static BufferBuilder stableFormat(ByteBufferBuilder allocator, PrimitiveTopology topology, VertexFormat format, Operation<BufferBuilder> original) {
        if (!SchematicAdapter.active()) return original.call(allocator, topology, format);
        boolean previous = ImmediateState.skipExtension.get();
        ImmediateState.skipExtension.set(true);
        try { return original.call(allocator, topology, DefaultVertexFormat.BLOCK); }
        finally { ImmediateState.skipExtension.set(previous); }
    }
}
