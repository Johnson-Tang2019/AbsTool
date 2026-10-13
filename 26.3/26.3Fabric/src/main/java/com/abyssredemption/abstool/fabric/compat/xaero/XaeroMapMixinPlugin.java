package com.abyssredemption.abstool.fabric.compat.xaero;

import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Only load classes referencing the optional map on the verified client version. */
public final class XaeroMapMixinPlugin implements IMixinConfigPlugin {
    private boolean supported;
    public void onLoad(String mixinPackage) {
        var loader = FabricLoader.getInstance();
        supported = loader.getEnvironmentType() == EnvType.CLIENT && loader.isModLoaded("xaerominimap")
                && loader.getModContainer("xaeroworldmap").map(mod ->
                    mod.getMetadata().getVersion().getFriendlyString().equals("1.46.4")).orElse(false);
    }
    public boolean shouldApplyMixin(String target, String mixin) { return supported; }
    public String getRefMapperConfig() { return null; }
    public void acceptTargets(Set<String> mine, Set<String> others) {}
    public List<String> getMixins() { return null; }
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
