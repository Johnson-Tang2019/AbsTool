package com.abyssredemption.abstool.client.waypoint.gui;

import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.WaypointDraft;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Viewers can select/copy field text; writes always recheck live server permissions. */
public final class SharedWaypointDetailsScreen extends Screen {
    private final Screen parent;
    private final SharedWaypoint original;
    private final List<EditBox> fields = new ArrayList<>();
    private CycleButton<Integer> colors;
    private Button enabledButton, saveButton;
    private boolean enabled;
    private int color, left, top, panelWidth;
    private Component error = Component.empty();
    public SharedWaypointDetailsScreen(Screen parent, SharedWaypoint original) {
        super(text("details"));
        this.parent = parent; this.original = original; this.enabled = original.enabled(); this.color = original.color();
    }
    private static Component text(String key, Object... args) { return Component.translatable("text.abstool.waypoint." + key, args); }
    @Override protected void init() {
        var previous = fields.stream().map(EditBox::getValue).toList();
        fields.clear();
        panelWidth = Math.min(440, width - 24); left = (width - panelWidth) / 2; top = Math.max(46, (height - 160) / 2);
        field(left + 64, top, panelWidth - 64, "field.name", original.name(), 64);
        field(left + 64, top + 26, panelWidth - 64, "field.dimension", original.dimensionId(), 128);
        int column = panelWidth / 3;
        field(left + 14, top + 52, column - 18, "field.x", Integer.toString(original.x()), 12);
        field(left + column + 14, top + 52, column - 18, "field.y", Integer.toString(original.y()), 12);
        field(left + column * 2 + 14, top + 52, panelWidth - column * 2 - 14, "field.z", Integer.toString(original.z()), 12);
        field(left + 64, top + 78, panelWidth / 2 - 68, "field.symbol", original.symbol(), 8);
        colors = addRenderableWidget(CycleButton.builder(WaypointPalette::name, color)
                .withValues(IntStream.range(0, 16).boxed().toList())
                .create(left + panelWidth / 2, top + 78, panelWidth - panelWidth / 2, 20, text("field.color"), (button, value) -> color = value));
        enabledButton = addRenderableWidget(Button.builder(text(enabled ? "enabled_yes" : "enabled_no"), b -> {
            enabled = !enabled; b.setMessage(text(enabled ? "enabled_yes" : "enabled_no"));
        }).bounds(left, top + 104, panelWidth, 20).build());
        saveButton = addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> save())
                .bounds(left, top + 132, panelWidth / 2 - 3, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose())
                .bounds(left + panelWidth / 2 + 3, top + 132, panelWidth - panelWidth / 2 - 3, 20).build());
        for (int i = 0; i < previous.size(); i++) fields.get(i).setValue(previous.get(i));
        permissions();
    }
    private void field(int x, int y, int width, String label, String value, int max) {
        EditBox box = new EditBox(font, x, y, width, 20, text(label));
        box.setMaxLength(max); box.setValue(value);
        fields.add(addRenderableWidget(box));
    }
    private boolean writable() {
        var current = SharedWaypointClient.state().waypoints().get(original.id());
        return SharedWaypointClient.canManage() && current != null && current.revision() == original.revision();
    }
    private void permissions() {
        boolean editable = writable();
        fields.forEach(field -> field.setEditable(editable));
        colors.active = enabledButton.active = saveButton.active = editable;
    }
    private void save() {
        if (!writable()) { error = text("readonly_hint"); return; }
        try {
            var draft = new WaypointDraft(fields.get(0).getValue(), fields.get(1).getValue(),
                    Integer.parseInt(fields.get(2).getValue()), Integer.parseInt(fields.get(3).getValue()),
                    Integer.parseInt(fields.get(4).getValue()), color, fields.get(5).getValue(), enabled);
            if (!draft.valid()) { error = text("invalid_fields"); return; }
            if (!SharedWaypointClient.update(original, draft)) { error = text("publish_failed"); return; }
            onClose();
        } catch (NumberFormatException failure) { error = text("invalid_fields"); }
    }
    @Override public void tick() { permissions(); }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        permissions();
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.textRenderer().accept(left, 12, title);
        var current = SharedWaypointClient.state().waypoints().get(original.id());
        graphics.textRenderer().accept(left, 28, current == null || current.revision() != original.revision() ? text("changed")
                : text(writable() ? "edit_hint" : "readonly_hint"));
        graphics.textRenderer().accept(left, top + 6, text("field.name"));
        graphics.textRenderer().accept(left, top + 32, text("field.dimension"));
        for (int i = 0; i < 3; i++) graphics.textRenderer().accept(left + i * (panelWidth / 3), top + 58, text("field." + "xyz".charAt(i)));
        graphics.textRenderer().accept(left, top + 84, text("field.symbol"));
        if (!error.getString().isEmpty()) graphics.textRenderer().accept(left, top + 156, error);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
