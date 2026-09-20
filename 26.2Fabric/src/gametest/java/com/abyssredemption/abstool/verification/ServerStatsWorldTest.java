package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.serverstats.ServerStatsAvailability;
import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.gui.ServerStatsEntry;
import com.abyssredemption.abstool.client.serverstats.model.TrendEntry;
import com.abyssredemption.abstool.client.serverstats.network.LeaderboardType;
import com.abyssredemption.abstool.client.serverstats.network.payload.*;
import com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory;
import java.util.List;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

/** Exercises real category clicks and packets in an isolated integrated server. */
public final class ServerStatsWorldTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            client.options.guiScale().set(1);
            client.resizeGui();
        });
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> ServerStatsClient.state().availability() == ServerStatsAvailability.UNSUPPORTED);
            openStatistics(context);
            context.runOnClient(client -> {
                if (!entry((ClothConfigScreen) client.gui.screen()).lines().getFirst().getString().contains("does not provide")) {
                    throw new AssertionError("Unsupported server must display an explicit status");
                }
            });
            context.takeScreenshot("abstool-stats-unsupported");
            click(context, "text.abstool.category.tracking");
            click(context, "text.abstool.serverstats.category");
            context.runOnClient(client -> {
                if (!(client.gui.screen() instanceof ClothConfigScreen)) throw new AssertionError("Category click replaced the settings screen");
            });
        }
        registerFixture();
        try (var world = context.worldBuilder().create()) {
            context.waitFor(client -> ServerStatsClient.state().overview() != null);
            openStatistics(context);
            context.runOnClient(client -> {
                var lines = entry((ClothConfigScreen) client.gui.screen()).lines();
                if (lines.stream().noneMatch(line -> line.getString().contains("12345"))) throw new AssertionError("Overview response not shown");
            });
            context.takeScreenshot("abstool-stats-overview");
            click(context, "abstool.stats.tab.2");
            context.waitFor(client -> ServerStatsClient.state().get(LeaderboardType.TOTAL_PLACEMENTS, 1) != null);
            context.runOnClient(client -> {
                if (entry((ClothConfigScreen) client.gui.screen()).lines().stream().noneMatch(line -> line.getString().contains("FixturePlayer"))) {
                    throw new AssertionError("Leaderboard response not shown");
                }
            });
            context.takeScreenshot("abstool-stats-leaderboard");
            click(context, "abstool.stats.tab.4");
            context.waitFor(client -> !ServerStatsClient.state().trend(0).isEmpty());
            context.takeScreenshot("abstool-stats-trend");
            click(context, "text.abstool.category.tracking");
            click(context, "text.abstool.serverstats.category");
            context.runOnClient(client -> {
                var screen = (ClothConfigScreen) client.gui.screen();
                if (!screen.getSelectedCategory().equals(Component.translatable("text.abstool.serverstats.category"))) {
                    throw new AssertionError("Cannot return to statistics using top navigation");
                }
            });
        }
        context.runOnClient(client -> {
            if (ServerStatsClient.state().overview() != null) throw new AssertionError("Statistics leaked after disconnect");
        });
        org.slf4j.LoggerFactory.getLogger("abstool-tests").info("ABSTOOL_STATS_WORLD_PASS: tab clicks, unsupported server, overview, leaderboard, trends, disconnect");
    }

    private static void openStatistics(ClientGameTestContext context) {
        context.setScreen(() -> ClothConfigScreenFactory.create(null));
        context.waitTicks(5);
        click(context, "text.abstool.serverstats.category");
        context.waitTicks(5);
        context.runOnClient(client -> {
            var screen = (ClothConfigScreen) client.gui.screen();
            if (!screen.getSelectedCategory().equals(Component.translatable("text.abstool.serverstats.category"))) throw new AssertionError("Top tab did not select statistics");
            if (entry(screen).getItemHeight() < 100) throw new AssertionError("Stats panel has no visible height");
            var first = (net.minecraft.client.gui.components.AbstractWidget) entry(screen).children().getFirst();
            if (first.getX() < 15 || first.getWidth() < 50) throw new AssertionError("Statistics layout uses incorrect entry coordinates");
        });
    }

    private static ServerStatsEntry entry(ClothConfigScreen screen) {
        return (ServerStatsEntry) screen.getCategorizedEntries().get(Component.translatable("text.abstool.serverstats.category")).getFirst();
    }

    private static void click(ClientGameTestContext context, String key) {
        context.runOnClient(client -> {
            var screen = (ClothConfigScreen) client.gui.screen();
            var candidates = new java.util.ArrayList<net.minecraft.client.gui.components.events.GuiEventListener>(screen.children());
            candidates.addAll(entry(screen).children());
            for (var candidate : candidates) {
                if (candidate instanceof net.minecraft.client.gui.components.AbstractWidget widget
                        && widget.getMessage().equals(Component.translatable(key))) {
                    var event = new net.minecraft.client.input.MouseButtonEvent(widget.getX() + widget.getWidth() / 2.0,
                            widget.getY() + widget.getHeight() / 2.0, new net.minecraft.client.input.MouseButtonInfo(0, 0));
                    if (!screen.mouseClicked(event, false)) throw new AssertionError("Screen did not handle click: " + key);
                    screen.mouseReleased(event);
                    return;
                }
            }
            throw new AssertionError("Missing button: " + key);
        });
        context.waitTicks(3);
    }

    private static void registerFixture() {
        ServerPlayNetworking.registerGlobalReceiver(HelloRequestPayload.TYPE, (p, c) ->
                ServerPlayNetworking.send(c.player(), new HelloResponsePayload(2, "fixture", 127, 10, "2026-09-20", "Asia/Shanghai", false)));
        ServerPlayNetworking.registerGlobalReceiver(OverviewRequestPayload.TYPE, (p, c) ->
                ServerPlayNetworking.send(c.player(), new OverviewResponsePayload(2, p.requestId(), "2026-09-20", "2026-W38", 1, 42, 5, 12, 12000, 72000, 12345, 54321, 2, 8, false, false, System.currentTimeMillis())));
        ServerPlayNetworking.registerGlobalReceiver(LeaderboardRequestPayload.TYPE, (p, c) ->
                ServerPlayNetworking.send(c.player(), new LeaderboardResponsePayload(2, p.requestId(), p.leaderboardType(), p.page(), 1, 1, System.currentTimeMillis(), "All time", List.of(new LeaderboardResponsePayload.Entry(1, c.player().getUUID(), "FixturePlayer", 6789, true)))));
        ServerPlayNetworking.registerGlobalReceiver(TrendRequestPayload.TYPE, (p, c) ->
                ServerPlayNetworking.send(c.player(), new TrendResponsePayload(2, p.requestId(), p.periodType(), List.of(new TrendEntry("2026-09-20", 5, 12000, 12345, 2, true)))));
    }
}
