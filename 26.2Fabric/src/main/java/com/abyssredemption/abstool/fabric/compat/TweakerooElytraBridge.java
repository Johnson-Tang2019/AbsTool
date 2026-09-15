package com.abyssredemption.abstool.fabric.compat;

import com.abyssredemption.abstool.client.elytra.ElytraLaunch;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

/** Optional access is deferred until the installed Tweakeroo has been identified. */
public final class TweakerooElytraBridge implements ElytraLaunch.EquipmentBridge {
    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("tweakeroo")) ElytraLaunch.external = new TweakerooElytraBridge();
    }
    public boolean enabled() {
        try {
            Class<?> type = Class.forName("fi.dy.masa.tweakeroo.config.FeatureToggle");
            Object toggle = type.getField("TWEAK_AUTO_SWITCH_ELYTRA").get(null);
            return (Boolean) type.getMethod("getBooleanValue").invoke(toggle);
        } catch (ReflectiveOperationException | LinkageError e) {
            // Unknown external state owns the transaction: do not risk a competing fallback swap.
            return true;
        }
    }
    public void equip(LocalPlayer player) throws ReflectiveOperationException {
        String version = FabricLoader.getInstance().getModContainer("tweakeroo").orElseThrow().getMetadata().getVersion().getFriendlyString();
        if (!version.equals("0.29.5")) throw new ReflectiveOperationException("Unverified Tweakeroo version " + version);
        // The upstream search includes the offhand slot. Do not let it select outside normal inventory.
        if (ElytraLaunch.usable(player.getOffhandItem())) throw new ReflectiveOperationException("Offhand glider requires manual equipment");
        Class.forName("fi.dy.masa.tweakeroo.util.InventoryUtils").getMethod("equipBestElytra", Player.class).invoke(null, player);
    }
}
