package com.abyssredemption.abstool.smoke;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "abstool_smoke", dist = Dist.CLIENT)
public final class NeoForgeSettingsSmoke {
    private final SettingsSmoke smoke = new SettingsSmoke();

    public NeoForgeSettingsSmoke() {
        NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(ClientTickEvent.Post event) {
        smoke.tick(Minecraft.getInstance(), parent -> {
            var container = ModList.get().getModContainerById("abstool").orElseThrow();
            return container.getCustomExtension(IConfigScreenFactory.class).orElseThrow()
                    .createScreen(container, parent);
        });
    }
}
