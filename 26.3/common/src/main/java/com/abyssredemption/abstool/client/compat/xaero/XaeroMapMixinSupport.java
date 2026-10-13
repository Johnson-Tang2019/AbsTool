package com.abyssredemption.abstool.client.compat.xaero;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Loader adapters provide resources; both loaders use the same optional map gate. */
public abstract class XaeroMapMixinSupport implements IMixinConfigPlugin {
    private boolean supported;
    protected abstract Optional<XaeroWorldMapContract.ResourceReader> mapResources();
    public final void onLoad(String mixinPackage) {
        supported = false;
        try {
            var resources = mapResources();
            if (resources.isEmpty()) return;
            String reason = XaeroWorldMapContract.rejection(resources.get());
            supported = reason.isEmpty();
            if (!supported) org.slf4j.LoggerFactory.getLogger("abstool").warn("Xaero map integration unavailable: {}", reason);
        } catch (IOException | RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger("abstool").warn("Cannot inspect optional Xaero map interfaces", failure);
        }
    }
    public final boolean shouldApplyMixin(String target, String mixin) { return supported; }
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
