package com.abyssredemption.abstool.smoke;

import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.slf4j.LoggerFactory;

/** Opt-in development mod; never packaged in release artifacts. */
public final class SettingsSmoke {
    private final FurnaceServerSmoke furnace = new FurnaceServerSmoke();
    private int ticks;
    private Screen settings;
    private Screen parent;

    public void tick(Minecraft client, Function<Screen, Screen> factory) {
        if (Boolean.getBoolean("abstool.externalServuxTests")) { furnace.tick(client); return; }
        if (!client.isGameLoadFinished() || client.gui.overlay() != null) return;
        ticks++;
        if (ticks == 20) {
            parent = client.gui.screen();
            settings = factory.apply(parent);
            if (settings == null) throw new AssertionError("Settings factory returned null");
            client.gui.setScreen(settings);
            try {
                Class.forName("com.abyssredemption.abstool.client.vault.client.VaultRenderTypes");
            } catch (ReflectiveOperationException error) {
                throw new AssertionError("Render pipeline initialization failed", error);
            }
        }
        if (ticks == 40) {
            var config = (me.shedaniel.clothconfig2.gui.AbstractConfigScreen) settings;
            config.getCategorizedEntries().values().forEach(entries -> entries.forEach(entry -> {
                if (entry instanceof me.shedaniel.clothconfig2.gui.entries.BooleanListEntry
                        || entry instanceof me.shedaniel.clothconfig2.gui.entries.EnumListEntry) {
                    for (var child : entry.children()) {
                        if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
                            if (widget.getY() > 0 && widget.getMessage().getString().isBlank()) {
                                throw new AssertionError("A laid-out configuration button has no label");
                            }
                        }
                    }
                }
            }));
            Screenshot.grab(client.gameDirectory, "abstool-settings-smoke.png",
                    client.gameRenderer.mainRenderTarget(), 1,
                    message -> LoggerFactory.getLogger("abstool-smoke").info(message.getString()));
        }
        if (ticks == 60) {
            if (client.gui.screen() != settings || settings.children().isEmpty()) {
                throw new AssertionError("Settings screen was not initialized and displayed");
            }
            settings.keyPressed(new KeyEvent(256, 0, 0));
            if (client.gui.screen() != parent) throw new AssertionError("Settings did not return to parent");
            LoggerFactory.getLogger("abstool-smoke").info("ABSTOOL_SETTINGS_SMOKE_PASS");
            client.stop();
        }
        if (ticks > 200) throw new AssertionError("Client smoke test timed out");
    }
}
