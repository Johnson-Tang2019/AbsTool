package com.abyssredemption.abstool.fabric.compat.xaero;

import com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointBridge;
import com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;

public final class XaeroFabricClient {
    private XaeroFabricClient() {}
    public static void init() {
        XaeroWaypointBridge.init(FabricLoader.getInstance().isModLoaded("xaerominimap"));
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            Runnable refresh = XaeroWaypointScreen.attach(client, screen, Screens.getWidgets(screen)::add);
            if (refresh != null) ScreenEvents.afterTick(screen).register(ignored -> refresh.run());
        });
    }
}
