package com.abyssredemption.abstool.fabric.compat.schematic;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.schematic.SchematicShaderConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.api.v0.IrisApi;
import com.abyssredemption.abstool.client.schematic.SchematicShaderStatus;
import net.minecraft.client.Minecraft;
import fi.dy.masa.litematica.render.LitematicaRenderer;

public final class SchematicAdapter {
    private static boolean previous;
    private static long draws;
    private static long nextLog;
    public static long frameIndices;
    private static String failure;
    private static final java.util.EnumMap<net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup, Runnable> pending =
            new java.util.EnumMap<>(net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup.class);
    public static void enqueue(net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup group, Runnable draw) { pending.put(group, draw); }
    public static void endLevel() {
        try { for (var draw : pending.values()) draw.run(); }
        finally { pending.clear(); }
    }
    public static void resetFrame() { pending.clear(); frameIndices = 0; }
    public static void failed(RuntimeException error) {
        if (failure == null) com.abyssredemption.abstool.AbsTool.LOGGER.error("Schematic compatibility draw failed", error);
        failure = error.getClass().getSimpleName() + ": " + error.getMessage();
    }
    public static void tick(Minecraft client) {
        if (ConfigManager.get().schematicShaderCompat.mode == SchematicShaderConfig.Mode.OFF) failure = null;
        if (client.level == null) { pending.clear(); draws = 0; }
        boolean enabled = active();
        if (enabled != previous) {
            if (client.level != null) LitematicaRenderer.getInstance().getWorldRenderer().loadRenderers(null);
            previous = enabled;
        }
        if (ConfigManager.get().schematicShaderCompat.debug && System.nanoTime() >= nextLog) {
            nextLog = System.nanoTime() + 5_000_000_000L;
            com.abyssredemption.abstool.AbsTool.LOGGER.info("Schematic shader: {}", status());
        }
    }
    public static void drawn() { draws++; }
    public static SchematicShaderStatus.Snapshot status() {
        var state = ConfigManager.get().schematicShaderCompat.mode == SchematicShaderConfig.Mode.OFF ? SchematicShaderStatus.State.OFF
                : failure != null ? SchematicShaderStatus.State.ERROR
                : !IrisApi.getInstance().isShaderPackInUse() ? SchematicShaderStatus.State.SHADERS_OFF
                : active() ? SchematicShaderStatus.State.ACTIVE : SchematicShaderStatus.State.UNSUPPORTED_BACKEND;
        return new SchematicShaderStatus.Snapshot(state, "Experimental local schematic pass; Litematica " + installedVersion("litematica")
                + " / MaLiLib " + installedVersion("malilib") + " / Iris " + installedVersion("iris")
                + " / Sodium " + installedVersion("sodium") + "; "
                + RenderSystem.getDevice().getDeviceInfo().backendName() + (failure == null ? "" : "; " + failure), draws);
    }
    private static String installedVersion(String id) {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(id).orElseThrow()
                .getMetadata().getVersion().getFriendlyString();
    }
    public static boolean active() {
        return SchematicMixinPlugin.supported
                && failure == null
                && ConfigManager.get().schematicShaderCompat.mode == SchematicShaderConfig.Mode.AUTO
                && IrisApi.getInstance().isShaderPackInUse()
                && RenderSystem.getDevice().getDeviceInfo().backendName().equals("OpenGL");
    }
}
