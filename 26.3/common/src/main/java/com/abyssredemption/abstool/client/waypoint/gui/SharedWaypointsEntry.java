package com.abyssredemption.abstool.client.waypoint.gui;

import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Live, color-grouped server waypoints inside the existing settings category. */
public final class SharedWaypointsEntry extends TextListEntry {
    private final WaypointBrowserModel model = new WaypointBrowserModel();
    private final List<Button> visible = new ArrayList<>();
    private final Map<Integer, Button> headers = new HashMap<>();
    private final Map<Integer, Button> categoryActions = new HashMap<>();
    private final Map<UUID, Button> actions = new HashMap<>();
    private final List<Button> toolbar;
    public SharedWaypointsEntry() {
        super(text("list"), Component.empty());
        toolbar = List.of(button("expand_all", () -> model.foldAll(false)), button("collapse_all", () -> model.foldAll(true)),
                button("refresh", SharedWaypointClient::sync));
    }
    private static Component text(String key, Object... args) { return Component.translatable("text.abstool.waypoint." + key, args); }
    private static Button button(String key, Runnable action) {
        return Button.builder(text(key), ignored -> action.run()).bounds(0, 0, 80, 20).build();
    }
    private void refresh() {
        if (model.refresh(SharedWaypointClient.state())) { headers.clear(); categoryActions.clear(); actions.clear(); }
    }
    @Override public int getItemHeight() {
        refresh();
        return 154 + (model.rows().isEmpty() ? 26 : model.rows().stream().mapToInt(r -> r.header() ? 26 : 38).sum());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int width, int height,
                                            int mouseX, int mouseY, boolean hovered, float delta) {
        refresh();
        visible.clear();
        var client = Minecraft.getInstance();
        var state = SharedWaypointClient.state();
        graphics.textRenderer().accept(x + 2, y + 4, text("xaero_status", text("xaero." + SharedWaypointClient.xaeroStatus())));
        graphics.textRenderer().accept(x + 2, y + 30, text("server_status", text(state.supported() ? "supported" : "unavailable")));
        graphics.textRenderer().accept(x + 2, y + 56, text("permission", text(state.canManage() ? "admin" : "viewer")));
        graphics.textRenderer().accept(x + 2, y + 82, text("count", state.waypointCount()));
        int column = Math.max(45, Math.min(100, (width - 10) / 3));
        for (int i = 0; i < toolbar.size(); i++) {
            var button = toolbar.get(i);
            button.setX(x + i * (column + 3)); button.setY(y + 104); button.setWidth(column);
            button.active = i != 2 || SharedWaypointClient.state().supported();
            draw(button, graphics, client, mouseX, mouseY, delta);
        }
        boolean canEdit = SharedWaypointClient.canManage();
        graphics.textRenderer().accept(x + 2, y + 131, text(canEdit ? "edit_hint" : "readonly_hint"));
        int rowY = y + 152;
        if (model.rows().isEmpty()) {
            graphics.textRenderer().accept(x + 2, rowY + 4, text(!state.supported() ? "unavailable" : !state.synced() ? "loading" : "empty"));
        }
        for (var row : model.rows()) {
            int rowHeight = row.header() ? 26 : 38;
            if (rowY + rowHeight >= 0 && rowY < client.gui.screen().height) {
                if (row.header()) {
                    var header = headers.computeIfAbsent(row.color(), color -> new CategoryButton(() -> model.toggle(color)));
                    header.setMessage(Component.literal(model.collapsed(row.color()) ? "▶ " : "▼ ")
                            .append(state.categoryName(row.color()).isEmpty() ? text("category_default") : Component.literal(state.categoryName(row.color())))
                            .append(" · ").append(WaypointPalette.name(row.color())).append(" (" + row.count() + ")"));
                    header.setX(x + 16); header.setY(rowY); header.setWidth(Math.max(50, width - (canEdit ? 88 : 20)));
                    graphics.fill(x + 2, rowY + 5, x + 13, rowY + 16, 0xFF000000 | WaypointPalette.rgb(row.color()));
                    graphics.outline(x + 1, rowY + 4, 13, 13, 0xFFAAAAAA);
                    draw(header, graphics, client, mouseX, mouseY, delta);
                    if (canEdit) {
                        var rename = categoryActions.computeIfAbsent(row.color(), color -> {
                            var b = new CategoryButton(() -> client.gui.setScreen(new WaypointCategoryScreen(client.gui.screen(),color)));
                            b.setMessage(text("category_rename")); return b;
                        });
                        rename.setX(x+width-67); rename.setY(rowY); rename.setWidth(64);
                        rename.active = SharedWaypointClient.canRenameCategories();
                        rename.setTooltip(Tooltip.create(text(rename.active ? "category_hint" : "category_upgrade")));
                        draw(rename,graphics,client,mouseX,mouseY,delta);
                    }
                } else {
                    var waypoint = row.waypoint();
                    var action = actions.computeIfAbsent(waypoint.id(), id -> button("details", () -> {
                        var current = SharedWaypointClient.state().waypoints().get(id);
                        if (current != null) client.gui.setScreen(new SharedWaypointDetailsScreen(client.gui.screen(), current));
                    }));
                    action.setMessage(text(canEdit ? "edit" : "details"));
                    action.setX(x + width - 65); action.setY(rowY + 5); action.setWidth(62);
                    String name = waypoint.name() + (waypoint.enabled() ? "" : " · " + text("hidden").getString());
                    String location = waypoint.x() + ", " + waypoint.y() + ", " + waypoint.z() + " · " + waypoint.dimensionId();
                    int textWidth = Math.max(40, width - 85);
                    graphics.textRenderer().accept(x + 12, rowY + 3, Component.literal(client.font.plainSubstrByWidth(name, textWidth)));
                    graphics.textRenderer().accept(x + 12, rowY + 17, Component.literal(client.font.plainSubstrByWidth(location, textWidth)));
                    action.setTooltip(Tooltip.create(Component.literal(waypoint.name() + "\n" + location)));
                    draw(action, graphics, client, mouseX, mouseY, delta);
                }
            }
            rowY += rowHeight;
        }
    }
    private void draw(Button button, GuiGraphicsExtractor graphics, Minecraft client, int mouseX, int mouseY, float delta) {
        if (button.getY() + button.getHeight() < 0 || button.getY() >= client.gui.screen().height) return;
        if (!graphics.containsPointInScissor(button.getX() + 4, button.getY() + 1)
                && !graphics.containsPointInScissor(button.getX() + 4, button.getY() + button.getHeight() - 1)) return;
        visible.add(button);
        button.extractRenderState(graphics, mouseX, mouseY, delta);
    }
    @Override public List<? extends GuiEventListener> children() { return visible; }
    @Override public List<? extends NarratableEntry> narratables() { return visible; }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (var button : List.copyOf(visible)) if (button.mouseClicked(event, doubleClick)) return true;
        return false;
    }
}
