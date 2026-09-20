package com.abyssredemption.abstool.client.serverstats.gui;

import com.abyssredemption.abstool.client.serverstats.ServerStatsAvailability;
import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.network.LeaderboardType;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Live statistics inside Cloth Config, preserving the existing category navigation. */
public final class ServerStatsEntry extends TextListEntry {
    private final List<Button> buttons = new ArrayList<>();
    private int tab;
    private int period;
    private int page = 1;
    private int contentHeight = 220;
    private long lastRequest;

    public ServerStatsEntry() {
        super(Component.translatable("text.abstool.serverstats.category"), Component.empty());
        for (int i = 0; i < 5; i++) {
            int index = i;
            buttons.add(Button.builder(label("tab." + i), b -> { tab = index; page = 1; request(true); }).bounds(0, 0, 65, 20).build());
        }
        buttons.add(Button.builder(label("period.0"), b -> {
            period = (period + 1) % 3;
            b.setMessage(label("period." + period));
            page = 1;
            request(true);
        }).bounds(0, 0, 65, 20).build());
        buttons.add(Button.builder(label("previous"), b -> { if (page > 1) { page--; request(true); } }).bounds(0, 0, 65, 20).build());
        buttons.add(Button.builder(label("next"), b -> {
            var data = ServerStatsClient.state().get(type(), page);
            if (data != null && page < data.totalPages()) { page++; request(true); }
        }).bounds(0, 0, 65, 20).build());
        buttons.add(Button.builder(label("refresh"), b -> request(true)).bounds(0, 0, 65, 20).build());
    }

    private static Component label(String key, Object... args) {
        return Component.translatable("abstool.stats." + key, args);
    }

    private LeaderboardType type() {
        return switch (tab) {
            case 2 -> period == 0 ? LeaderboardType.TOTAL_PLACEMENTS : period == 1 ? LeaderboardType.TODAY_PLACEMENTS : LeaderboardType.WEEK_PLACEMENTS;
            case 3 -> period == 0 ? LeaderboardType.TOTAL_DEATHS : period == 1 ? LeaderboardType.TODAY_DEATHS : LeaderboardType.WEEK_DEATHS;
            default -> period == 0 ? LeaderboardType.TOTAL_PLAYTIME : period == 1 ? LeaderboardType.TODAY_PLAYTIME : LeaderboardType.WEEK_PLAYTIME;
        };
    }

    private void request(boolean force) {
        if (ServerStatsClient.state().availability() != ServerStatsAvailability.READY) return;
        long now = System.currentTimeMillis();
        if (!force && now - lastRequest < 10000) return;
        lastRequest = now;
        if (tab == 0) ServerStatsClient.requestOverview(force);
        else if (tab == 4) ServerStatsClient.requestTrend(period == 2 ? 1 : 0, period == 0 ? 7 : period == 1 ? 30 : 12, force);
        else ServerStatsClient.request(type(), page, force);
    }

    public List<Component> lines() {
        var state = ServerStatsClient.state();
        List<Component> lines = new ArrayList<>();
        lines.add(label("status." + state.availability().name().toLowerCase(java.util.Locale.ROOT)));
        if (state.availability() != ServerStatsAvailability.READY) return lines;
        if (tab == 0) {
            var o = state.overview();
            if (o == null) lines.add(label("loading"));
            else {
                lines.add(label("online", o.onlinePlayers(), o.knownPlayers()));
                lines.add(label("active", o.dau(), o.wau()));
                lines.add(label("playtime", o.todayPlayTimeTicks() / 1200, o.weekPlayTimeTicks() / 1200));
                lines.add(label("placements", o.todayPlacements(), o.weekPlacements()));
                lines.add(label("deaths", o.todayDeaths(), o.weekDeaths()));
                if (o.todayPartial() || o.weekPartial()) lines.add(label("partial"));
            }
        } else if (tab == 4) {
            var entries = state.trend(period == 2 ? 1 : 0);
            if (entries.isEmpty()) lines.add(label("empty"));
            for (var e : entries) lines.add(label("trend", e.key(), e.activePlayers(), e.playTimeTicks() / 1200, e.placements(), e.deaths()));
        } else if (!state.supports(type())) lines.add(label("unsupported_metric"));
        else {
            var data = state.get(type(), page);
            if (data == null) lines.add(label("loading"));
            else {
                if (data.entries().isEmpty()) lines.add(label("empty"));
                for (var e : data.entries()) lines.add(Component.literal(e.rank() + ". " + e.playerName() + "  ")
                        .append(tab == 1 ? label("minutes", e.value() / 1200) : Component.literal(Long.toString(e.value()))));
                lines.add(label("page", page, data.totalPages()));
            }
        }
        lines.add(label("metadata", state.timezone(), state.trackingStartedDate()));
        return lines;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int width,
                                   int height, int mouseX, int mouseY, boolean hovered, float delta) {
        request(false);
        int buttonWidth = Math.max(30, Math.min(90, (width - 20) / 5));
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            int column = i < 5 ? i : i - 5;
            button.setX(x + column * (buttonWidth + 3));
            button.setY(y + (i < 5 ? 0 : 24));
            button.setWidth(buttonWidth);
            button.active = i < 5 ? tab != i : ServerStatsClient.state().availability() == ServerStatsAvailability.READY;
            if (i == 5) button.setMessage(label((tab == 4 ? "trendperiod." : "period.") + period));
            if (i == 6) button.active &= tab > 0 && tab < 4 && page > 1;
            if (i == 7) {
                var data = ServerStatsClient.state().get(type(), page);
                button.active &= tab > 0 && tab < 4 && data != null && page < data.totalPages();
            }
            button.extractRenderState(graphics, mouseX, mouseY, delta);
        }
        int textY = y + 54;
        for (Component line : lines()) {
            for (var wrapped : Minecraft.getInstance().font.split(line, Math.max(40, width - 12))) {
                graphics.textRenderer().accept(x + 2, textY, wrapped);
                textY += 14;
            }
            textY += 4;
        }
        contentHeight = Math.max(150, textY - y + 8);
    }

    @Override public int getItemHeight() { return contentHeight; }
    @Override public List<? extends GuiEventListener> children() { return buttons; }
    @Override public List<? extends NarratableEntry> narratables() { return buttons; }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (Button button : buttons) if (button.mouseClicked(event, doubleClick)) return true;
        return false;
    }
}
