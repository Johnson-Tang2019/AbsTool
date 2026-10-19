package com.abyssredemption.abstool.client.compat.xaero;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.gui.SharedWaypointDetailsScreen;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.SupportMods;
import xaero.map.mods.gui.Waypoint;

/** Optional world-map actions. Never writes Xaero's local waypoint files. */
public final class XaeroWorldMapActions {
    private XaeroWorldMapActions() {}

    public static void append(Waypoint waypoint, IRightClickableElement target, ArrayList<RightClickOption> options) {
        if (!XaeroWaypointBridge.compatible() || !ConfigManager.get().serverWaypoints.enabled) return;
        Object original = waypoint.getOriginal();
        var shared = XaeroWaypointBridge.serverWaypoint(original);
        var state = SharedWaypointClient.state();
        boolean manage = state.supported() && state.synced() && state.canManage();
        if (shared == null && XaeroWaypointBridge.isLocalWaypoint(original)) {
            options.add(new Action("text.abstool.waypoint.publish", options.size(), target, screen -> {
                try {
                    var support = SupportMods.xaeroMinimap;
                    var field = support.getClass().getDeclaredField("rightClickWaypointWorld");
                    field.setAccessible(true);
                    var draft = XaeroWaypointBridge.draftInWorld(field.get(support), original);
                    if (draft == null || !SharedWaypointClient.create(draft)) failed();
                } catch (ReflectiveOperationException | RuntimeException failure) {
                    failed();
                }
            }).setActive(manage));
        } else if (shared != null) {
            options.add(new Action("text.abstool.waypoint.update", options.size(), target,
                    screen -> Minecraft.getInstance().gui.setScreen(new SharedWaypointDetailsScreen(screen, shared))).setActive(manage));
            options.add(new Action("text.abstool.waypoint.remove", options.size(), target, screen -> {
                Minecraft.getInstance().gui.setScreen(new ConfirmScreen(confirmed -> {
                    if (confirmed && !SharedWaypointClient.delete(shared)) failed();
                    Minecraft.getInstance().gui.setScreen(screen);
                }, Component.translatable("text.abstool.waypoint.confirm_remove"), Component.literal(shared.name())));
            }).setActive(manage));
        }
    }

    private static void failed() {
        var player = Minecraft.getInstance().player;
        if (player != null) player.sendSystemMessage(Component.translatable("text.abstool.waypoint.publish_failed"));
    }

    private static final class Action extends RightClickOption {
        private final Consumer<Screen> action;
        private Action(String key, int index, IRightClickableElement target, Consumer<Screen> action) {
            super(key, index, target);
            this.action = action;
        }
        @Override public void onAction(Screen screen) {
            try { screen.getClass().getMethod("closeRightClick").invoke(screen); }
            catch (ReflectiveOperationException ignored) { }
            action.accept(screen);
        }
    }
}
