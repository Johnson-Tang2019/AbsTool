package com.abyssredemption.abstool.client.serverstats.gui;

import com.abyssredemption.abstool.client.serverstats.ServerStatsAvailability;
import com.abyssredemption.abstool.client.serverstats.ServerStatsClient;
import com.abyssredemption.abstool.client.serverstats.model.*;
import com.abyssredemption.abstool.client.serverstats.network.LeaderboardType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class ServerStatsScreen extends Screen {
    @FunctionalInterface private interface TextSink { void accept(int x, int y, Component value); }
    private final Screen parent;
    private int tab;
    private int page = 1;
    private int trendPeriod;
    private int trendCount = 7;
    private LeaderboardType type = LeaderboardType.TOTAL_PLAYTIME;

    public ServerStatsScreen(Screen parent) { super(Component.translatable("abstool.serverstats.title")); this.parent = parent; }

    @Override protected void init() {
        super.init();
        String[] tabs = {"概览", "在线时间", "方块放置", "死亡", "趋势"};
        int left = width / 2 - 250;
        for (int i = 0; i < tabs.length; i++) {
            int selected = i;
            addRenderableWidget(Button.builder(Component.literal(tabs[i]), b -> { tab = selected; page = 1; load(); })
                    .bounds(left + i * 100, 58, 94, 20).build());
        }
        addMetricButtons();
        addRenderableWidget(Button.builder(Component.literal("上一页"), b -> { if (page > 1) { page--; load(); } })
                .bounds(width / 2 - 100, height - 35, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("下一页"), b -> { ClientLeaderboardPage d = ServerStatsClient.state().get(type, page); if (d != null && page < d.totalPages()) { page++; load(); } })
                .bounds(width / 2 + 30, height - 35, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("刷新"), b -> refresh()).bounds(width - 155, height - 35, 65, 20).build());
        addRenderableWidget(Button.builder(Component.literal("关闭"), b -> onClose()).bounds(width - 85, height - 35, 65, 20).build());
        load();
    }

    private void addMetricButtons() {
        String[] labels = {"累计", "今日", "本周"};
        for (int i = 0; i < labels.length; i++) {
            int selected = i;
            addRenderableWidget(Button.builder(Component.literal(labels[i]), b -> selectMetric(selected))
                    .bounds(width / 2 - 150 + i * 100, 82, 90, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("7天"), b -> selectTrend(0, 7)).bounds(width / 2 + 160, 82, 55, 20).build());
        addRenderableWidget(Button.builder(Component.literal("30天"), b -> selectTrend(0, 30)).bounds(width / 2 + 220, 82, 55, 20).build());
        addRenderableWidget(Button.builder(Component.literal("12周"), b -> selectTrend(1, 12)).bounds(width / 2 + 280, 82, 55, 20).build());
    }

    private void selectMetric(int selected) {
        type = switch (tab) {
            case 1 -> selected == 0 ? LeaderboardType.TOTAL_PLAYTIME : selected == 1 ? LeaderboardType.TODAY_PLAYTIME : LeaderboardType.WEEK_PLAYTIME;
            case 2 -> selected == 0 ? LeaderboardType.TOTAL_PLACEMENTS : selected == 1 ? LeaderboardType.TODAY_PLACEMENTS : LeaderboardType.WEEK_PLACEMENTS;
            case 3 -> selected == 0 ? LeaderboardType.TOTAL_DEATHS : selected == 1 ? LeaderboardType.TODAY_DEATHS : LeaderboardType.WEEK_DEATHS;
            default -> type;
        };
        page = 1; load();
    }

    private void selectTrend(int period, int count) { if (tab == 4) { trendPeriod = period; trendCount = count; ServerStatsClient.requestTrend(period, count, true); } }
    private void load() { if (tab == 0) ServerStatsClient.requestOverview(false); else if (tab == 4) ServerStatsClient.requestTrend(trendPeriod, trendCount, false); else ServerStatsClient.request(type, page, false); }
    private void refresh() { if (tab == 0) ServerStatsClient.requestOverview(true); else if (tab == 4) ServerStatsClient.requestTrend(trendPeriod, trendCount, true); else ServerStatsClient.request(type, page, true); }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        TextSink text = (x, y, value) -> g.textRenderer().accept(x, y, value);
        text.accept(width / 2 - 45, 25, title);
        var state = ServerStatsClient.state();
        String status = switch (state.availability()) {
            case READY -> "已连接";
            case CHECKING -> "正在检查服务器统计接口";
            case UNSUPPORTED -> "当前服务器未提供 AbsServerTool 统计接口；其他功能不受影响";
            case PROTOCOL_MISMATCH -> "服务器统计协议不兼容，客户端协议：2";
            case ERROR -> "服务器返回的统计数据无效";
            default -> "未连接";
        };
        text.accept(Math.max(8, width / 2 - 300), 42, Component.literal(status));
        if (tab == 0) drawOverview(text, state.overview());
        else if (tab == 4) drawTrend(text, state.trend(trendPeriod));
        else drawLeaderboard(text, state.get(type, page));
        text.accept(width / 2 - 180, height - 55, Component.literal("服务器时区：" + valueOrUnknown(state.timezone()) + "  记录起始：" + valueOrUnknown(state.trackingStartedDate())));
    }

    private void drawOverview(TextSink text, ServerOverview o) {
        if (o == null) { text.accept(width / 2 - 45, 120, Component.literal("正在加载概览...")); return; }
        String[] values = {"今日活跃：" + mark(o.dau(), o.todayPartial()), "本周活跃：" + mark(o.wau(), o.weekPartial()), "当前在线：" + o.onlinePlayers(), "历史玩家：" + o.knownPlayers(), "今日累计时长：" + duration(o.todayPlayTimeTicks()), "本周累计时长：" + duration(o.weekPlayTimeTicks()), "今日放置：" + number(o.todayPlacements()), "本周放置：" + number(o.weekPlacements())};
        for (int i = 0; i < values.length; i++) text.accept(width / 2 - 250 + (i % 2) * 250, 100 + (i / 2) * 32, Component.literal(values[i]));
        text.accept(width / 2 - 250, 245, Component.literal("今日死亡：" + number(o.todayDeaths()) + "  本周死亡：" + number(o.weekDeaths())));
    }

    private void drawTrend(TextSink text, List<TrendEntry> entries) {
        if (entries.isEmpty()) { text.accept(width / 2 - 45, 120, Component.literal("正在加载趋势...")); return; }
        int y = 105;
        for (TrendEntry e : entries) { text.accept(width / 2 - 250, y, Component.literal(e.key() + "  活跃 " + e.activePlayers() + "  时长 " + duration(e.playTimeTicks()) + "  放置 " + number(e.placements()) + "  死亡 " + number(e.deaths()) + (e.complete() ? "" : "  不完整"))); y += 20; }
    }

    private void drawLeaderboard(TextSink text, ClientLeaderboardPage d) {
        if (d == null) { text.accept(width / 2 - 45, 120, Component.literal("正在加载排行榜...")); return; }
        int y = 105;
        for (ClientLeaderboardEntry e : d.entries()) { text.accept(width / 2 - 220, y, Component.literal(e.rank() + "  " + e.playerName() + "  " + number(e.value()) + (e.online() ? "  ●" : ""))); y += 20; }
        text.accept(width / 2 - 45, height - 55, Component.literal("第 " + page + " / " + d.totalPages() + " 页"));
    }

    private static String valueOrUnknown(String value) { return value.isBlank() ? "未知" : value; }
    private static String mark(int value, boolean partial) { return value + (partial ? "*" : ""); }
    private static String number(long value) { return NumberFormat.getIntegerInstance(Locale.US).format(value); }
    private static String duration(long ticks) { long seconds = Math.max(0, ticks) / 20; long days = seconds / 86400; seconds %= 86400; long hours = seconds / 3600; seconds %= 3600; long minutes = seconds / 60; seconds %= 60; if (days > 0) return days + "天" + hours + "小时"; if (hours > 0) return hours + "小时" + minutes + "分"; if (minutes > 0) return minutes + "分" + seconds + "秒"; return seconds + "秒"; }
    @Override public void onClose() { minecraft.setScreenAndShow(parent); }
}
