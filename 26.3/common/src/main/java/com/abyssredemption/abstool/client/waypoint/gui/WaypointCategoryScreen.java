package com.abyssredemption.abstool.client.waypoint.gui;

import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Server-authoritative category naming with conflict and permission-loss guards. */
public final class WaypointCategoryScreen extends Screen {
    private final Screen parent;
    private final int color;
    private final long revision;
    private final long connectionId;
    private EditBox name;
    private Button save;
    private int left,top;
    public WaypointCategoryScreen(Screen parent,int color) {
        super(text("category_name")); this.parent=parent; this.color=color;
        revision=SharedWaypointClient.state().categoryRevision();
        connectionId=SharedWaypointClient.state().connectionId();
    }
    private static Component text(String key) { return Component.translatable("text.abstool.waypoint."+key); }
    private boolean writable() { return SharedWaypointClient.canRenameCategories()
        && connectionId==SharedWaypointClient.state().connectionId() && revision==SharedWaypointClient.state().categoryRevision(); }
    @Override protected void init() {
        String previous = name==null ? SharedWaypointClient.state().categoryName(color) : name.getValue();
        int w=Math.min(340,width-24); left=(width-w)/2; top=height/2-20;
        name=addRenderableWidget(new EditBox(font,left,top,w,20,title)); name.setMaxLength(32); name.setValue(previous);
        save=addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> {
            if(writable() && SharedWaypointClient.renameCategory(color,revision,name.getValue())) onClose();
        }).bounds(left,top+32,w/2-3,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose()).bounds(left+w/2+3,top+32,w/2-3,20).build());
        setInitialFocus(name); tick();
    }
    @Override public void tick() { boolean allowed=writable(); name.setEditable(allowed); save.active=allowed
        && com.abyssredemption.abstool.client.waypoint.network.WaypointCategories.validName(name.getValue()); }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta) {
        tick(); super.extractRenderState(graphics,mouseX,mouseY,delta);
        graphics.textRenderer().accept(left,top-40,title.copy().append(" · ").append(WaypointPalette.name(color)));
        graphics.textRenderer().accept(left,top-22,text(writable()?"category_hint":"changed"));
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
