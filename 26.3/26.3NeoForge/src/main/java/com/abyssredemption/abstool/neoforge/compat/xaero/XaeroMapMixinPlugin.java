package com.abyssredemption.abstool.neoforge.compat.xaero;

import com.abyssredemption.abstool.client.compat.xaero.XaeroMapMixinSupport;
import com.abyssredemption.abstool.client.compat.xaero.XaeroWorldMapContract;
import java.util.Optional;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

public final class XaeroMapMixinPlugin extends XaeroMapMixinSupport {
    protected Optional<XaeroWorldMapContract.ResourceReader> mapResources() {
        var loader = FMLLoader.getCurrentOrNull();
        if (loader == null || loader.getDist() != Dist.CLIENT) return Optional.empty();
        var mods = loader.getLoadingModList();
        if (mods == null || mods.getModFileById("xaerominimap") == null) return Optional.empty();
        var map = mods.getModFileById("xaeroworldmap");
        if (map == null) return Optional.empty();
        return Optional.of(resource -> map.getFile().getContents().readFile(resource));
    }
}
