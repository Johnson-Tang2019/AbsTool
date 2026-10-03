package com.abyssredemption.abstool.client.serverstats.performance;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read-only report browser with explicit server-controlled start and stop actions. */
public final class PerformanceProfilerScreen extends Screen {
    private final Screen parent;
    private EditBox seconds;
    private Button start, stop, previous, next, scrollUp, scrollDown;
    private Button detailBack;
    private final List<Button> chunkButtons = new ArrayList<>();
    private int tab;
    private int page = 1;
    private int offset;
    private boolean detail;
    private boolean durationSet;

    public PerformanceProfilerScreen(Screen parent) {
        super(Component.translatable("abstool.performance.title"));
        this.parent = parent;
    }
    private static Component label(String key, Object... args) {
        return Component.translatable("abstool.performance." + key, args);
    }
    @Override protected void init() {
        int left = Math.max(8, (width - Math.min(500, width - 16)) / 2);
        int area = Math.min(500, width - 16);
        int column = Math.max(55, (area - 9) / 4);
        for (int i = 0; i < 4; i++) {
            int selected = i;
            addRenderableWidget(Button.builder(label("tab." + i), b -> {
                tab = selected; page = 1; offset = 0; detail = false; PerformanceClient.requestPage(tab, page);
            }).bounds(left + i * (column + 3), 34, column, 20).build());
        }
        seconds = addRenderableWidget(new EditBox(font, left, 61, 65, 20, label("duration")));
        seconds.setMaxLength(8);
        seconds.setValue("30");
        int x = left + 70;
        for (int preset : new int[]{10, 30, 60, 120, 300}) {
            addRenderableWidget(Button.builder(Component.literal(Integer.toString(preset)), b -> seconds.setValue(Integer.toString(preset)))
                    .bounds(x, 61, Math.min(44, Math.max(32, (area - 75) / 5 - 3)), 20).build());
            x += Math.max(35, (area - 75) / 5);
        }
        start = addRenderableWidget(Button.builder(label("start"), b -> {
            try { PerformanceClient.start(Integer.parseInt(seconds.getValue())); }
            catch (NumberFormatException e) { seconds.setValue("30"); }
        }).bounds(left, 87, Math.min(110, area / 3), 20).build());
        stop = addRenderableWidget(Button.builder(label("stop"), b -> PerformanceClient.stop())
                .bounds(left + Math.min(115, area / 3 + 5), 87, Math.min(110, area / 3), 20).build());
        addRenderableWidget(Button.builder(label("back"), b -> onClose())
                .bounds(left + area - 75, 87, 75, 20).build());
        detailBack = addRenderableWidget(Button.builder(label("detail_back"), b -> { detail = false; offset = 0; })
                .bounds(left + area - 160, 87, 80, 20).build());
        int bottom = height - 28;
        previous = addRenderableWidget(Button.builder(label("previous"), b -> {
            if (page > 1) { page--; offset = 0; PerformanceClient.requestPage(tab, page); }
        }).bounds(left, bottom, 80, 20).build());
        next = addRenderableWidget(Button.builder(label("next"), b -> {
            if (page < totalPages()) { page++; offset = 0; PerformanceClient.requestPage(tab, page); }
        }).bounds(left + 84, bottom, 80, 20).build());
        scrollUp = addRenderableWidget(Button.builder(Component.literal("▲"), b -> offset = Math.max(0, offset - 1))
                .bounds(left + area - 50, bottom, 22, 20).build());
        scrollDown = addRenderableWidget(Button.builder(Component.literal("▼"), b -> offset++)
                .bounds(left + area - 25, bottom, 22, 20).build());
        for (int i = 0; i < 12; i++) {
            int row = i;
            chunkButtons.add(addRenderableWidget(Button.builder(Component.empty(), b -> {
                var data = PerformanceClient.chunks(page);
                if (data == null || offset + row >= data.entries().size()) return;
                PerformanceClient.requestDetail(data.entries().get(offset + row));
                detail = true;
                offset = 0;
            }).bounds(left, 200 + i * 14, area - 55, 14).build()));
        }
        PerformanceClient.opened();
    }
    private int totalPages() {
        return switch (tab) {
            case 1 -> PerformanceClient.chunks(page) == null ? 1 : PerformanceClient.chunks(page).totalPages();
            case 2 -> PerformanceClient.players(page) == null ? 1 : PerformanceClient.players(page).totalPages();
            case 3 -> PerformanceClient.slowTicks(page) == null ? 1 : PerformanceClient.slowTicks(page).totalPages();
            default -> 1;
        };
    }
    @Override public void onClose() { Minecraft.getInstance().gui.setScreen(parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        var status = PerformanceClient.status();
        var summary = PerformanceClient.summary();
        if (status != null && !durationSet) {
            seconds.setValue(Integer.toString(status.defaultDurationSeconds())); durationSet = true;
        }
        start.active = PerformanceClient.supported() && status != null && status.state() == 0
                && !PerformanceClient.pending("start") && !PerformanceClient.pending("stop");
        stop.active = PerformanceClient.supported() && status != null && status.state() == 1
                && !PerformanceClient.pending("stop") && !PerformanceClient.pending("start");
        previous.visible = next.visible = tab > 0 && !detail;
        scrollUp.visible = scrollDown.visible = tab > 0;
        previous.active = page > 1;
        next.active = page < totalPages();
        List<Component> lines = new ArrayList<>();
        if (!PerformanceClient.supported()) lines.add(label("unsupported"));
        else if (status == null) lines.add(label("loading"));
        else {
            lines.add(label("state." + status.state()));
            lines.add(label("limits", status.minimumDurationSeconds(), status.maximumDurationSeconds() == 0 ? "∞" : status.maximumDurationSeconds()));
            if (status.state() == 1) {
                long remaining = Math.max(0, (status.expectedEndEpochMillis() - System.currentTimeMillis() + 999) / 1000);
                lines.add(label("running", status.requestedDurationSeconds(), remaining));
            }
        }
        if (PerformanceClient.error() != null) {
            String error = PerformanceClient.error();
            lines.add(label(error.startsWith("server:") ? "error." + error.substring(7) : "error." + error));
        }
        if (summary != null) {
            if (detail && PerformanceClient.detail() != null) detailLines(lines, PerformanceClient.detail());
            else switch (tab) {
                case 0 -> overviewLines(lines, summary);
                case 1 -> chunkLines(lines);
                case 2 -> playerLines(lines);
                case 3 -> slowLines(lines);
                default -> { }
            }
        } else if (status != null && status.latestReportId() == 0) lines.add(label("no_report"));
        int maxRows = Math.max(0, Math.min(12, (height - 232) / 14));
        scrollDown.active = detail ? offset + Math.max(1, (height - 145) / 14) < Math.max(0, lines.size() - 2)
                : offset + maxRows < listSize();
        scrollUp.active = offset > 0;
        detailBack.visible = detail;
        int left = Math.max(8, (width - Math.min(500, width - 16)) / 2);
        for (int i = 0; i < chunkButtons.size(); i++) {
            Button button = chunkButtons.get(i);
            var data = PerformanceClient.chunks(page);
            boolean visible = tab == 1 && !detail && data != null && i < maxRows && offset + i < data.entries().size();
            button.visible = visible;
            if (visible) {
                var c = data.entries().get(offset + i);
                button.setMessage(Component.literal(c.dimension() + " [" + c.x() + ", " + c.z() + "]  " + ms(c.totalMeasuredNs()) + " ms"));
            }
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int y = 113;
        List<Component> visibleLines = lines;
        if (detail && lines.size() > 2) {
            visibleLines = new ArrayList<>(lines.subList(0, 2));
            visibleLines.addAll(lines.subList(Math.min(lines.size(), 2 + offset), lines.size()));
        }
        for (Component line : visibleLines) {
            for (var wrapped : font.split(line, Math.max(60, Math.min(500, width - 24)))) {
                if (y >= height - 32) break;
                graphics.textRenderer().accept(left, y, wrapped);
                y += 12;
            }
            y += 2;
        }
    }
    private int listSize() {
        return switch (tab) {
            case 1 -> PerformanceClient.chunks(page) == null ? 0 : PerformanceClient.chunks(page).entries().size();
            case 2 -> PerformanceClient.players(page) == null ? 0 : PerformanceClient.players(page).entries().size();
            case 3 -> PerformanceClient.slowTicks(page) == null ? 0 : PerformanceClient.slowTicks(page).entries().size();
            default -> 0;
        };
    }
    private static String ms(long ns) { return String.format(java.util.Locale.ROOT, "%.2f", ns / 1_000_000.0); }
    private static String time(long epochMillis) {
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochMilli(epochMillis));
    }
    private void overviewLines(List<Component> out, PerformanceModels.Summary s) {
        out.add(label("report", s.reportId(), s.tickCount(), s.actualDurationMillis()));
        out.add(label("report_period", time(s.startedAtEpochMillis()), time(s.endedAtEpochMillis()),
                s.stoppedEarly() ? label("stopped_early") : label("completed")));
        out.add(label("tick_times", ms(s.averageNs()), ms(s.p50Ns()), ms(s.p95Ns()), ms(s.p99Ns()), ms(s.maxNs())));
        out.add(label("thresholds", s.over50ms(), s.over100ms(), s.over200ms()));
        out.add(label("categories", ms(s.chunkTickNs()), ms(s.entityTickNs()), ms(s.blockEntityTickNs())));
        out.add(label("scheduled", ms(s.scheduledBlockNs()), ms(s.scheduledFluidNs()), ms(s.residualNs())));
        out.add(label("counts", s.chunkEntries(), s.playerEntries(), s.slowTickEntries()));
        if (s.saveOverlapSlowTicks() > 0) out.add(label("save_warning", s.saveOverlapSlowTicks()));
        out.add(label("measured_note"));
    }
    private void chunkLines(List<Component> out) {
        out.add(label("chunk_note"));
        var p = PerformanceClient.chunks(page);
        if (p == null) { PerformanceClient.requestPage(tab, page); out.add(label("loading")); }
        else { out.add(label("page", page, p.totalPages())); out.add(label("measured_note")); }
    }
    private void playerLines(List<Component> out) {
        out.add(label("player_note"));
        var p = PerformanceClient.players(page);
        if (p == null) { PerformanceClient.requestPage(tab, page); out.add(label("loading")); }
        else {
            out.add(label("page", page, p.totalPages()));
            for (int i = offset; i < Math.min(p.entries().size(), offset + Math.max(1, (height - 192) / 14)); i++) {
                var e = p.entries().get(i);
                out.add(label("player", e.name(), e.uniqueLoadAssociations(), e.sharedLoadAssociations(),
                        e.generatedLoadAssociations(), e.associatedMeasuredChunks(), ms(e.associatedChunkMeasuredNs())));
            }
        }
    }
    private void slowLines(List<Component> out) {
        var p = PerformanceClient.slowTicks(page);
        if (p == null) { PerformanceClient.requestPage(tab, page); out.add(label("loading")); }
        else {
            out.add(label("page", page, p.totalPages()));
            for (int i = offset; i < Math.min(p.entries().size(), offset + Math.max(1, (height - 192) / 14)); i++) {
                var e = p.entries().get(i);
                String top = e.topChunks().isEmpty() ? "" : e.topChunks().getFirst().dimension() + " ["
                        + e.topChunks().getFirst().x() + ", " + e.topChunks().getFirst().z() + "]";
                out.add(label("slow", e.tickIndex(), ms(e.durationNs()), e.saveActive() ? "⚠ save" : "", top));
            }
        }
    }
    private void detailLines(List<Component> out, PerformanceModels.ChunkDetail d) {
        out.add(label("detail", d.chunk().dimension(), d.chunk().x(), d.chunk().z(), ms(d.chunk().totalMeasuredNs())));
        out.add(label("chunk_note"));
        out.add(label("tick_calls", d.chunk().entityTickCalls(), d.chunk().blockEntityTickCalls()));
        out.add(label("chunk_categories", ms(d.chunk().chunkTickNs()), ms(d.chunk().entityTickNs()),
                ms(d.chunk().blockEntityTickNs()), ms(d.chunk().scheduledBlockNs()), ms(d.chunk().scheduledFluidNs())));
        out.add(label("loads", d.chunk().loadCount(), d.chunk().generatedLoadCount(), d.chunk().loadAttribution()));
        out.add(label("associated", String.join(", ", d.chunk().associatedPlayers())));
        out.add(label("entity_types"));
        for (var e : d.entityTypes()) out.add(label("type", e.typeId(), ms(e.timeNs()), e.calls()));
        out.add(label("block_entity_types"));
        for (var e : d.blockEntityTypes()) out.add(label("type", e.typeId(), ms(e.timeNs()), e.calls()));
    }
}
