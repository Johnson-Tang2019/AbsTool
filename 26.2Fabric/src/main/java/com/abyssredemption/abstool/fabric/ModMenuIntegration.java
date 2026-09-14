package com.abyssredemption.abstool.fabric;

import com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClothConfigScreenFactory::create;
    }
}
