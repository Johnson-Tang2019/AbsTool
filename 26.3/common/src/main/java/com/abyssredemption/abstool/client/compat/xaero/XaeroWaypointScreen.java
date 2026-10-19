package com.abyssredemption.abstool.client.compat.xaero;

import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.gui.SharedWaypointDetailsScreen;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Adds server actions to the verified Xaero waypoint list without a Xaero mixin. */
public final class XaeroWaypointScreen {
    private XaeroWaypointScreen() {}
    public static Runnable attach(Minecraft client, Screen screen, Consumer<Button> addWidget) {
            if(!XaeroWaypointBridge.isWaypointScreen(screen)) return null;
            int height = screen.height;
            Button publish=Button.builder(Component.translatable("text.abstool.waypoint.publish"),b -> XaeroWaypointBridge.publish(screen))
                    .bounds(8,height-82,112,20).build();
            Button update=Button.builder(Component.translatable("text.abstool.waypoint.update"),b -> {
                List<Object> chosen=XaeroWaypointBridge.selection(screen);
                if(chosen.size()==1) {
                    var waypoint=XaeroWaypointBridge.serverWaypoint(chosen.getFirst());
                    if(waypoint!=null) client.gui.setScreen(new SharedWaypointDetailsScreen(screen,waypoint));
                }
            })
                    .bounds(8,height-59,112,20).build();
            UUID[] armed={null};
            Button remove=Button.builder(Component.translatable("text.abstool.waypoint.remove"),b -> {
                List<Object> chosen=XaeroWaypointBridge.selection(screen);
                if(chosen.size()!=1) return;
                var waypoint=XaeroWaypointBridge.serverWaypoint(chosen.getFirst());
                if(waypoint==null) return;
                if(waypoint.id().equals(armed[0])) { XaeroWaypointBridge.remove(screen); armed[0]=null; }
                else armed[0]=waypoint.id();
            })
                    .bounds(8,height-36,112,20).build();
            Button readonly=Button.builder(Component.translatable("text.abstool.waypoint.managed"),b -> {})
                    .bounds(8,height-36,112,20).build();
            readonly.active=false;
            addWidget.accept(publish); addWidget.accept(update);
            addWidget.accept(remove); addWidget.accept(readonly);
            Runnable refresh = () -> {
                var state = SharedWaypointClient.state();
                boolean manage=com.abyssredemption.abstool.client.vault.config.ConfigManager.get().serverWaypoints.enabled
                        && XaeroWaypointBridge.compatible() && state.supported() && state.synced() && state.canManage();
                List<Object> selected=XaeroWaypointBridge.selection(screen);
                boolean one=selected.size()==1;
                boolean shared=one && XaeroWaypointBridge.serverWaypoint(selected.getFirst())!=null;
                UUID selectedId=shared?XaeroWaypointBridge.serverWaypoint(selected.getFirst()).id():null;
                if(!java.util.Objects.equals(armed[0],selectedId)) armed[0]=null;
                remove.setMessage(Component.translatable(armed[0]==null?"text.abstool.waypoint.remove":"text.abstool.waypoint.confirm_remove"));
                publish.active=manage && one && XaeroWaypointBridge.isLocalWaypoint(selected.getFirst());
                update.active=manage && shared;
                remove.active=manage && shared;
                publish.visible=manage; update.visible=manage; remove.visible=manage;
                readonly.visible=!manage && shared;
            };
            refresh.run();
            return refresh;
    }
}
