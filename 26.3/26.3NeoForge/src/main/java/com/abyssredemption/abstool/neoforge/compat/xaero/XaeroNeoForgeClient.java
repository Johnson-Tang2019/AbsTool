package com.abyssredemption.abstool.neoforge.compat.xaero;

import com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointBridge;
import com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class XaeroNeoForgeClient {
    private static Screen waypointScreen;
    private static Runnable refresh;
    private XaeroNeoForgeClient() {}
    public static void init() {
        XaeroWaypointBridge.init(ModList.get().isLoaded("xaerominimap"));
        NeoForge.EVENT_BUS.addListener(XaeroNeoForgeClient::screenInitialized);
        NeoForge.EVENT_BUS.addListener(XaeroNeoForgeClient::tick);
    }
    private static void screenInitialized(ScreenEvent.Init.Post event) {
        refresh = XaeroWaypointScreen.attach(Minecraft.getInstance(), event.getScreen(), event::addListener);
        waypointScreen = refresh == null ? null : event.getScreen();
    }
    private static void tick(ClientTickEvent.Post event) {
        if (refresh == null) return;
        if (Minecraft.getInstance().gui.screen() == waypointScreen) refresh.run();
        else { refresh = null; waypointScreen = null; }
    }
}
