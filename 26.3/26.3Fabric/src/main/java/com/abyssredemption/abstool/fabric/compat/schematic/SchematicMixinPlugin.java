package com.abyssredemption.abstool.fabric.compat.schematic;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SchematicMixinPlugin implements IMixinConfigPlugin {
    private record Target(String mod, String resource, String sha256) {}
    private static final List<Target> TARGETS = List.of(
            new Target("litematica", "fi/dy/masa/litematica/render/schematic/ChunkRenderBatchDraw.class", "c262fb1bbca678e488bd718ac6d5dd6fe608cede02008813e777466c93b0ae0a"),
            new Target("litematica", "fi/dy/masa/litematica/render/schematic/BufferBuilderCache.class", "403d16787f8e9a065d457140787b6ebd1571c85057c750a9ae116df7a11fddd7"),
            new Target("litematica", "fi/dy/masa/litematica/render/schematic/WorldRendererSchematic.class", "10f393fba33f5830574fa29a577131b7738f213aca89fddc2e82c2bfa179474c"),
            new Target("iris", "net/irisshaders/iris/pipeline/IrisRenderingPipeline.class", "53a667ae4114da83f10a19246898afb4ca5e2c1599595a53878559cfa03cee88"),
            new Target("iris", "net/irisshaders/iris/vertices/ImmediateState.class", "c602600bbe058ec604264703521cb55a081d095a4068a0c97e947d9f235ee383"),
            new Target("iris", "net/irisshaders/iris/mixin/MixinRenderPipeline.class", "c2ce6a173032e9578cf7eb6a22d3cb6505851273e7bc46c59e72344a69d573cf"),
            new Target("malilib", "fi/dy/masa/malilib/render/MaLiLibPipelines.class", "9e576350ef03845b8e0abf5dfdf0aa802655233dd42eaaab97cc8e5d819d090f"));
    public static boolean supported;
    public static String reason = "Not initialized";
    public void onLoad(String mixinPackage) {
        var loader = FabricLoader.getInstance();
        var installed = new java.util.HashMap<String, String>();
        SchematicVersions.PINNED.keySet().forEach(id -> loader.getModContainer(id).ifPresent(
                mod -> installed.put(id, mod.getMetadata().getVersion().getFriendlyString())));
        reason = SchematicVersions.rejection(installed, loader.getEnvironmentType() == EnvType.CLIENT);
        if (!reason.isEmpty()) return;
        // Hash original class resources, never load optional targets at discovery time.
        // This also rejects modified binaries reusing a recognized version string.
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            for (var target : TARGETS) {
                var path = loader.getModContainer(target.mod()).orElseThrow().findPath(target.resource()).orElseThrow();
                String actual = java.util.HexFormat.of().formatHex(digest.digest(java.nio.file.Files.readAllBytes(path)));
                if (!actual.equals(target.sha256())) { reason = "Unsupported bytecode: " + target.resource(); return; }
            }
            supported = true;
            reason = "Supported pinned combination";
        } catch (IOException | java.security.NoSuchAlgorithmException | java.util.NoSuchElementException e) {
            reason = "Cannot inspect optional dependency: " + e.getMessage();
        }
    }
    public boolean shouldApplyMixin(String target, String mixin) { return supported; }
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
