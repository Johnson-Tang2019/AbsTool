package com.abyssredemption.abstool.client.update;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import java.util.List;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class UpdateEntry extends TextListEntry {
    private final Button open = Button.builder(ModUpdates.text("open"), button -> {
        var client = Minecraft.getInstance();
        client.gui.setScreen(new UpdateScreen(client.gui.screen()));
    }).bounds(0, 0, 140, 20).build();

    public UpdateEntry() { super(ModUpdates.text("title"), Component.empty()); }
    @Override public int getItemHeight() { return 26; }
    @Override public List<? extends GuiEventListener> children() { return List.of(open); }
    @Override public List<? extends NarratableEntry> narratables() { return List.of(open); }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int width,
                                             int height, int mouseX, int mouseY, boolean hovered, float delta) {
        open.setX(x);
        open.setY(y + 2);
        open.setWidth(Math.min(width, 160));
        open.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private static final class UpdateScreen extends Screen {
        private final Screen parent;
        private Button auto, beta, check, install;
        private int left, top;
        UpdateScreen(Screen parent) {
            super(ModUpdates.text("title"));
            this.parent = parent;
        }

        @Override protected void init() {
            int w = Math.min(300, width - 20);
            left = (width - w) / 2;
            top = Math.max(4, (height - 181) / 2);
            auto = addButton("auto", 66, w, () -> {
                var config = ConfigManager.get();
                config.autoCheckUpdates = !config.autoCheckUpdates;
                ConfigManager.save();
            });
            beta = addButton("beta", 89, w, () -> {
                var config = ConfigManager.get();
                config.receiveBetaUpdates = !config.receiveBetaUpdates;
                ConfigManager.save();
                ModUpdates.betaChanged();
            });
            check = addButton("check", 112, w, () -> ModUpdates.check(false));
            install = addButton("install", 135, w, () -> {
                var selected = ModUpdates.offer();
                if (selected == null) return;
                minecraft.gui.setScreen(new ConfirmScreen(confirmed -> {
                    minecraft.gui.setScreen(this);
                    if (confirmed) ModUpdates.install(selected);
                }, ModUpdates.text("available", selected.version()), ModUpdates.text("confirm")));
            });
            addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose())
                    .bounds(left, top + 158, w, 20).build());
            tick();
        }

        private Button addButton(String key, int offset, int w, Runnable action) {
            return addRenderableWidget(Button.builder(ModUpdates.text(key), b -> action.run())
                    .bounds(left, top + offset, w, 20).build());
        }

        @Override public void tick() {
            var config = ConfigManager.get();
            auto.setMessage(ModUpdates.text("auto", ModUpdates.text(config.autoCheckUpdates ? "on" : "off")));
            beta.setMessage(ModUpdates.text("beta", ModUpdates.text(config.receiveBetaUpdates ? "on" : "off")));
            beta.active = check.active = !ModUpdates.busy();
            install.active = ModUpdates.canInstall();
        }

        @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            super.extractRenderState(graphics, mouseX, mouseY, delta);
            graphics.textRenderer().accept(left, top + 2, title);
            graphics.textRenderer().accept(left, top + 16, ModUpdates.text("version", ModUpdates.installedVersion()));
            graphics.textRenderer().accept(left, top + 30, ModUpdates.statusText());
            if (ModUpdates.previousFailed()) graphics.textRenderer().accept(left, top + 44, ModUpdates.text("previous_failed"));
        }
        @Override public void onClose() { minecraft.gui.setScreen(parent); }
    }
}
