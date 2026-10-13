package com.abyssredemption.abstool.fabric.compat.xaero;

import com.abyssredemption.abstool.client.compat.xaero.XaeroMapMixinSupport;
import com.abyssredemption.abstool.client.compat.xaero.XaeroWorldMapContract;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/** Apply optional map mixins when their required interfaces are present. */
public final class XaeroMapMixinPlugin extends XaeroMapMixinSupport {
    protected Optional<XaeroWorldMapContract.ResourceReader> mapResources() {
        var loader = FabricLoader.getInstance();
        if (loader.getEnvironmentType() != EnvType.CLIENT || !loader.isModLoaded("xaerominimap")) return Optional.empty();
        return loader.getModContainer("xaeroworldmap").map(map -> resource -> java.nio.file.Files.readAllBytes(
                map.findPath(resource).orElseThrow(() -> new java.io.IOException("Missing " + resource))));
    }
}
