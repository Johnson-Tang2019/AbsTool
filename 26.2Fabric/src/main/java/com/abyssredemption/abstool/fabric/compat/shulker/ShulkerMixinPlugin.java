package com.abyssredemption.abstool.fabric.compat.shulker;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ShulkerMixinPlugin implements IMixinConfigPlugin {
    public static boolean supported;
    private static final Map<String, String> HASHES = Map.of(
            "me/fallenbreath/tweakermore/impl/mc_tweaks/shulkerBoxItemContentHint/ShulkerBoxItemContentHintCommon.class", "fcb721cb2dd912ec58480d32319a3d71b0881591f9ac917bcc70390bfe7b24c3",
            "me/fallenbreath/tweakermore/impl/mc_tweaks/shulkerBoxItemContentHint/ShulkerBoxItemContentHintRenderer.class", "20332fff59d4cd21639a240a1f2314cf5fb9f5aff1b2bca751e2c9b6f1d4de98");
    public void onLoad(String mixinPackage) {
        var loader = FabricLoader.getInstance();
        if (loader.getEnvironmentType() != EnvType.CLIENT || !loader.isModLoaded("tweakermore")) return;
        try {
            var mod = loader.getModContainer("tweakermore").orElseThrow();
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            for (var entry : HASHES.entrySet()) {
                var path = mod.findPath(entry.getKey()).orElseThrow();
                String hash = java.util.HexFormat.of().formatHex(digest.digest(java.nio.file.Files.readAllBytes(path)));
                if (!hash.equals(entry.getValue())) return;
            }
            supported = true;
        } catch (Exception failure) {
            org.slf4j.LoggerFactory.getLogger("abstool").warn("Cannot inspect TweakerMore hint renderer", failure);
        }
    }
    public boolean shouldApplyMixin(String target, String mixin) { return supported; }
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
