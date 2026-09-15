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
            new Target("litematica", "fi/dy/masa/litematica/render/schematic/ChunkRenderBatchDraw.class", "642cc4fc3261f401a9b5e195d1f1f82d6365375c9ffcb517b0e94e8fdaf1c2ea"),
            new Target("litematica", "fi/dy/masa/litematica/render/schematic/BufferBuilderCache.class", "9975594db617dfc75ead78293afec2baaaa24b4f364199a53d1b5703f394b79a"),
            new Target("litematica", "fi/dy/masa/litematica/render/schematic/WorldRendererSchematic.class", "a559bf7220cc75896ad486428640087218e494655eb2640b17cd83e398f30805"),
            new Target("iris", "net/irisshaders/iris/pipeline/IrisRenderingPipeline.class", "bc9352887ef09159c4680b759b498c7bc80531660b4f4ac893f3f060392064c4"),
            new Target("iris", "net/irisshaders/iris/vertices/ImmediateState.class", "c602600bbe058ec604264703521cb55a081d095a4068a0c97e947d9f235ee383"),
            new Target("iris", "net/irisshaders/iris/mixin/MixinRenderPipeline.class", "b64627a9d80d21cf95a7ca4c6f5355137c8671412365812e89526ac07d50a89c"),
            new Target("malilib", "fi/dy/masa/malilib/render/MaLiLibPipelines.class", "1dffc4e9137489dbdb1cd5208dfcf5370dafd0898e16955ae571d0c81c3e4b01"));
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
                String expected = target.sha256();
                if (target.mod().equals("iris") && Set.of("1.11.1+mc26.2", "1.11.2+mc26.2").contains(installed.get("iris"))) {
                    expected = switch (target.resource()) {
                        case "net/irisshaders/iris/pipeline/IrisRenderingPipeline.class" -> "92442d66f2750e2e45e751f005dd53bb99867e04ec6bfb6ea22a41bf144c7097";
                        case "net/irisshaders/iris/mixin/MixinRenderPipeline.class" -> "eb1f13bfbf0d626b0d8984111120ef4c18cfa66e7ec9c7ac5e63d1abe4026d7d";
                        default -> expected;
                    };
                }
                if (target.mod().equals("litematica") && installed.get("litematica").equals("0.28.3")
                        && target.resource().endsWith("/WorldRendererSchematic.class")) {
                    expected = "bd953e93bdb137540917657efa947c4de043d298992857c7ef2c0f388c0c631f";
                }
                if (!actual.equals(expected)) { reason = "Unsupported bytecode: " + target.resource(); return; }
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
