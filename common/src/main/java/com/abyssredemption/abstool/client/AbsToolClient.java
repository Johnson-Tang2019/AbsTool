package com.abyssredemption.abstool.client;

import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.vault.storage.VaultStorage;
import java.nio.file.Path;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Client initialization; invoke only from a physical client entrypoint. */
public final class AbsToolClient {
    private static final SettingsShortcut SHORTCUT = new SettingsShortcut();
    private AbsToolClient() {
    }

    public static void init(Path configDirectory) {
        ConfigManager.init(configDirectory);
        VaultStorage.init(configDirectory);
        AbsTool.LOGGER.debug("Initializing Ab's Tool client");
    }

    public static void tickShortcut(Minecraft client) {
        com.abyssredemption.abstool.client.furnace.FurnaceTracker.INSTANCE.tick(client);
        com.abyssredemption.abstool.client.elytra.ElytraLaunch.guard(client);
        boolean focused = client.isWindowActive();
        boolean r = focused && InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_R);
        boolean b = focused && InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_B);
        if (SHORTCUT.update(r, b, client.player != null && client.level != null && client.gui.screen() == null)) {
            client.gui.setScreen(ClothConfigScreenFactory.create(null));
        }
    }
}
