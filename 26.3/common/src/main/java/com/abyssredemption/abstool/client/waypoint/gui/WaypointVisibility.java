package com.abyssredemption.abstool.client.waypoint.gui;

import com.abyssredemption.abstool.client.vault.client.VaultTrackerController;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import net.minecraft.client.Minecraft;

/** Personal per-server visibility; never changes authoritative waypoint flags. */
public final class WaypointVisibility {
    private WaypointVisibility() {}
    public static int mask() {
        String key = VaultTrackerController.currentServerKey(Minecraft.getInstance());
        return ConfigManager.get().serverWaypoints.hiddenColors.getOrDefault(key, 0) & 0xFFFF;
    }
    public static boolean hidden(int color) { return color >= 0 && color < 16 && (mask() & (1 << color)) != 0; }
    public static void toggle(int color) {
        if (color < 0 || color > 15) return;
        String key = VaultTrackerController.currentServerKey(Minecraft.getInstance());
        int next = mask() ^ (1 << color);
        if (next == 0) ConfigManager.get().serverWaypoints.hiddenColors.remove(key);
        else ConfigManager.get().serverWaypoints.hiddenColors.put(key, next);
        ConfigManager.save();
    }
}
