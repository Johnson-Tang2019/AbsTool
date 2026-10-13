package com.abyssredemption.abstool.fabric.compat.xaero;

import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Apply optional map mixins when their required interfaces are present. */
public final class XaeroMapMixinPlugin implements IMixinConfigPlugin {
    private boolean supported;
    public void onLoad(String mixinPackage) {
        var loader = FabricLoader.getInstance();
        supported = false;
        if (loader.getEnvironmentType() != EnvType.CLIENT || !loader.isModLoaded("xaerominimap")) return;
        var map = loader.getModContainer("xaeroworldmap");
        if (map.isEmpty()) return;
        try {
            String reason = XaeroWorldMapContract.rejection(resource -> java.nio.file.Files.readAllBytes(
                    map.get().findPath(resource).orElseThrow(() -> new java.io.IOException("Missing " + resource))));
            supported = reason.isEmpty();
            if (!supported) org.slf4j.LoggerFactory.getLogger("abstool").warn("Xaero map integration unavailable: {}", reason);
        } catch (java.io.IOException | RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger("abstool").warn("Cannot inspect optional Xaero map interfaces", failure);
        }
    }
    public boolean shouldApplyMixin(String target, String mixin) { return supported; }
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
