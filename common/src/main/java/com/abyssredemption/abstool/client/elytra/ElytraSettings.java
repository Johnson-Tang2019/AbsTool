package com.abyssredemption.abstool.client.elytra;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class ElytraSettings {
    public static void add(ConfigBuilder builder, ConfigCategory category) {
        var e = builder.entryBuilder();
        var c = ConfigManager.get().elytraAssist;
        category.addEntry(e.startTextDescription(text("category")).build());
        category.addEntry(e.startBooleanToggle(text("enabled"), c.enabled).setDefaultValue(false).setSaveConsumer(v -> c.enabled = v).build());
        category.addEntry(e.startEnumSelector(text("trigger"), ElytraConfig.Trigger.class, c.trigger)
                .setEnumNameProvider(v -> text("trigger." + v.name().toLowerCase(Locale.ROOT)))
                .setDefaultValue(ElytraConfig.Trigger.SNEAK_JUMP).setSaveConsumer(v -> c.trigger = v).build());
        category.addEntry(e.startKeyCodeField(text("key"), InputConstants.Type.KEYSYM.getOrCreate(c.keyCode))
                .setAllowMouse(false).setAllowModifiers(false).setDefaultValue(InputConstants.Type.KEYSYM.getOrCreate(71)).setKeySaveConsumer(v -> c.keyCode = v.getValue()).build());
        category.addEntry(e.startBooleanToggle(text("auto_equip"), c.autoEquip).setDefaultValue(true).setSaveConsumer(v -> c.autoEquip = v).build());
        category.addEntry(e.startIntField(text("swap_ticks"), c.swapTicks).setMin(2).setMax(40).setDefaultValue(10).setSaveConsumer(v -> c.swapTicks = v).build());
        category.addEntry(e.startIntField(text("request_ticks"), c.requestTicks).setMin(1).setMax(20).setDefaultValue(6).setSaveConsumer(v -> c.requestTicks = v).build());
    }
    private static Component text(String key) { return Component.translatable("text.abstool.elytra." + key); }
}
