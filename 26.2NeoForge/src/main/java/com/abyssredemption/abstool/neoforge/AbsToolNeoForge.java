package com.abyssredemption.abstool.neoforge;

import com.abyssredemption.abstool.AbsTool;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

@Mod(AbsTool.MOD_ID)
public final class AbsToolNeoForge {
    public AbsToolNeoForge(IEventBus modEventBus) {
        AbsTool.init();
    }
}
