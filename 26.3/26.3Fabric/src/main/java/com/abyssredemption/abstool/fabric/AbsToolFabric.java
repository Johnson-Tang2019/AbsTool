package com.abyssredemption.abstool.fabric;

import com.abyssredemption.abstool.AbsTool;
import net.fabricmc.api.ModInitializer;

public final class AbsToolFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AbsTool.init();
    }
}
