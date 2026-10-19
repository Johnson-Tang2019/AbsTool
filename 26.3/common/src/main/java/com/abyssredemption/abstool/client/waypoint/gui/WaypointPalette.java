package com.abyssredemption.abstool.client.waypoint.gui;

import net.minecraft.network.chat.Component;

/** The sixteen legacy Xaero colors used by the shared waypoint protocol. */
public final class WaypointPalette {
    private static final String[] NAMES = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
    private static final int[] COLORS = {0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};
    private WaypointPalette() {}
    public static Component name(int color) { return Component.translatable("text.abstool.waypoint.color." + NAMES[color]); }
    public static int rgb(int color) { return COLORS[color]; }
}
