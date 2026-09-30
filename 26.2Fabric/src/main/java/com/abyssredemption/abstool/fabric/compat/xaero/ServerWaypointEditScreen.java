package com.abyssredemption.abstool.fabric.compat.xaero;

import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.WaypointDraft;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Edits a copy; Xaero's live waypoint changes only after the server broadcasts an update. */
public final class ServerWaypointEditScreen extends Screen {
    private final Screen parent;
    private final SharedWaypoint original;
    private final List<EditBox> fields = new ArrayList<>();
    private boolean enabled;
    public ServerWaypointEditScreen(Screen parent, SharedWaypoint original) {
        super(Component.translatable("text.abstool.waypoint.update"));
        this.parent=parent; this.original=original; this.enabled=original.enabled();
    }
    @Override protected void init() {
        fields.clear();
        int x=width/2-100; int y=height/2-95;
        addField(x,y,"Name",original.name(),64);
        addField(x,y+24,"X",Integer.toString(original.x()),12);
        addField(x,y+48,"Y",Integer.toString(original.y()),12);
        addField(x,y+72,"Z",Integer.toString(original.z()),12);
        addField(x,y+96,"Symbol",original.symbol(),16);
        addField(x,y+120,"Color (0-15)",Integer.toString(original.color()),2);
        addRenderableWidget(Button.builder(Component.translatable(enabled?"text.abstool.waypoint.enabled_yes":"text.abstool.waypoint.enabled_no"),b -> {
            enabled=!enabled; b.setMessage(Component.translatable(enabled?"text.abstool.waypoint.enabled_yes":"text.abstool.waypoint.enabled_no"));
        }).bounds(x,y+145,97,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b -> save()).bounds(x+103,y+145,97,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),b -> onClose()).bounds(x,y+169,200,20).build());
    }
    private void addField(int x,int y,String hint,String value,int max) {
        EditBox box=new EditBox(font,x,y,200,20,Component.literal(hint));
        box.setMaxLength(max); box.setValue(value); box.setHint(Component.literal(hint));
        fields.add(addRenderableWidget(box));
    }
    private void save() {
        try {
            WaypointDraft draft=new WaypointDraft(fields.get(0).getValue(),original.dimensionId(),
                    Integer.parseInt(fields.get(1).getValue()),Integer.parseInt(fields.get(2).getValue()),
                    Integer.parseInt(fields.get(3).getValue()),Integer.parseInt(fields.get(5).getValue()),
                    fields.get(4).getValue(),enabled);
            if(!draft.valid() || !SharedWaypointClient.update(original,draft)) throw new IllegalArgumentException();
            minecraft.gui.setScreen(parent);
        } catch(IllegalArgumentException e) {
            if(minecraft.player!=null) minecraft.player.sendSystemMessage(Component.translatable("text.abstool.waypoint.error.4"));
        }
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
