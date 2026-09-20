package com.abyssredemption.abstool.client.vault.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** A clickable settings row that opens the NeoForge-only server statistics screen. */
public final class OpenServerStatsEntry extends TextListEntry {
    private final AtomicBoolean opened = new AtomicBoolean();
    public OpenServerStatsEntry(Component label, Component value) {
        super(label, value);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            net.minecraft.client.Minecraft.getInstance().execute(() -> {
                try {
                    Class.forName("com.abyssredemption.abstool.client.serverstats.ServerStatsClient")
                            .getMethod("open").invoke(null);
                } catch (ReflectiveOperationException ignored) {
                    // The server statistics client exists only on the NeoForge client distribution.
                }
            });
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                   int mouseX, int mouseY, int listWidth, boolean hovered, float delta) {
        super.extractRenderState(graphics, x, y, width, height, mouseX, mouseY, listWidth, hovered, delta);
        if (opened.compareAndSet(false, true)) {
            net.minecraft.client.Minecraft.getInstance().execute(this::open);
        }
    }

    private void open() {
        try {
            Class.forName("com.abyssredemption.abstool.client.serverstats.ServerStatsClient")
                    .getMethod("open").invoke(null);
        } catch (ReflectiveOperationException ignored) {
            // The server statistics client exists only on the NeoForge client distribution.
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
