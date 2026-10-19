package com.abyssredemption.abstool.client.waypoint.gui;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Marks only our own settings screens, without retaining closed screens. */
public final class SettingsAppearance {
    private static final Set<Screen> SCREENS = Collections.newSetFromMap(new WeakHashMap<>());
    private SettingsAppearance() {}
    public static <T extends Screen> T register(T screen) { SCREENS.add(screen); return screen; }
    public static boolean translucent(Screen screen) { return Minecraft.getInstance().level != null && SCREENS.contains(screen); }
}
