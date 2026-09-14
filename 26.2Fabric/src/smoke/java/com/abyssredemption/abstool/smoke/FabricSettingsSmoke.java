package com.abyssredemption.abstool.smoke;

import com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory;
import com.terraformersmc.modmenu.ModMenu;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricSettingsSmoke implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SettingsSmoke smoke = new SettingsSmoke();
        ClientTickEvents.END_CLIENT_TICK.register(client -> smoke.tick(client, parent -> {
            if (FabricLoader.getInstance().isModLoaded("modmenu")) {
                if (!ModMenu.hasConfigScreen("abstool")) throw new AssertionError("Mod Menu entry missing");
                return ModMenu.getConfigScreen("abstool", parent);
            }
            return ClothConfigScreenFactory.create(parent);
        }));
    }
}
