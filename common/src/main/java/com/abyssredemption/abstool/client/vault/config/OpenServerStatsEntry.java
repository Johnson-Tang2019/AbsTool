package com.abyssredemption.abstool.client.vault.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Invisible category sentinel that schedules the NeoForge statistics screen for the next client tick. */
public final class OpenServerStatsEntry extends TextListEntry {
    private final AtomicBoolean opened = new AtomicBoolean();
    public OpenServerStatsEntry(Component label, Component value) {
        super(label, value);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                   int mouseX, int mouseY, int listWidth, boolean hovered, float delta) {
        super.extractRenderState(graphics, x, y, width, height, mouseX, mouseY, listWidth, hovered, delta);
        if (opened.compareAndSet(false, true)) {
            com.abyssredemption.abstool.client.ServerStatsNavigation.requestOpen();
        }
    }

    @Override
    public int getItemHeight() {
        return 0;
    }


    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
