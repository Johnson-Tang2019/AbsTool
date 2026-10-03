package com.abyssredemption.abstool.fabric.compat;

import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.vault.config.QuickSettings;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

/** Invoked only when TweakerMore is installed. Its public config API owns all mutations. */
public final class TweakerMoreQuickSettings {
    public static QuickSettings.Session create(ConfigEntryBuilder builder) {
        try {
            Class<?> configs = Class.forName("me.fallenbreath.tweakermore.config.TweakerMoreConfigs");
            Class<?> storageClass = Class.forName("me.fallenbreath.tweakermore.config.TweakerMoreConfigStorage");
            Object storage = storageClass.getMethod("getInstance").invoke(null);
            var save = storageClass.getMethod("save");
            var entries = new ArrayList<AbstractConfigListEntry<?>>();
            boolean[] dirty = {false};
            for (Object option : (Iterable<?>) configs.getMethod("getAllOptions").invoke(null)) {
                if (!(boolean) option.getClass().getMethod("isEnabled").invoke(option)) continue;
                IConfigBase config = (IConfigBase) option.getClass().getMethod("getConfig").invoke(option);
                if (!config.getName().toLowerCase(Locale.ROOT).contains("shulker")) continue;
                String labelKey = "text.abstool.quick.option." + config.getName();
                Component name = net.minecraft.locale.Language.getInstance().has(labelKey)
                        ? Component.translatable(labelKey) : Component.translatable("tweakermore.config." + config.getName());
                Component tooltip = Component.literal(config.getComment());
                if (config instanceof ConfigBoolean value) {
                    entries.add(builder.startBooleanToggle(name, value.getBooleanValue())
                            .setDefaultValue(value.getDefaultBooleanValue()).setTooltip(tooltip)
                            .setSaveConsumer(next -> { if (next != value.getBooleanValue()) {
                                value.setBooleanValue(next); dirty[0] = true;
                            }}).build());
                } else if (config instanceof ConfigInteger value) {
                    entries.add(builder.startIntField(name, value.getIntegerValue())
                            .setDefaultValue(value.getDefaultIntegerValue()).setTooltip(tooltip)
                            .setMin(value.getMinIntegerValue()).setMax(value.getMaxIntegerValue())
                            .setSaveConsumer(next -> { if (next != value.getIntegerValue()) {
                                value.setIntegerValue(next); dirty[0] = true;
                            }}).build());
                } else if (config instanceof ConfigDouble value) {
                    entries.add(builder.startDoubleField(name, value.getDoubleValue())
                            .setDefaultValue(value.getDefaultDoubleValue()).setTooltip(tooltip)
                            .setMin(value.getMinDoubleValue()).setMax(value.getMaxDoubleValue())
                            .setErrorSupplier(next -> Double.isFinite(next) ? Optional.empty()
                                    : Optional.of(Component.translatable("text.abstool.quick.finite")))
                            .setSaveConsumer(next -> { if (next != value.getDoubleValue()) {
                                value.setDoubleValue(next); dirty[0] = true;
                            }}).build());
                }
            }
            if (com.abyssredemption.abstool.fabric.compat.shulker.ShulkerMixinPlugin.supported) {
                var config = com.abyssredemption.abstool.client.vault.config.ConfigManager.get();
                entries.addFirst(builder.startBooleanToggle(Component.translatable("text.abstool.quick.dominant"), config.shulkerMostCommonItem)
                        .setDefaultValue(true).setTooltip(Component.translatable("text.abstool.quick.dominant.tooltip"))
                        .setSaveConsumer(value -> config.shulkerMostCommonItem = value).build());
            }
            if (entries.isEmpty()) return QuickSettings.EMPTY;
            entries.addFirst(builder.startTextDescription(Component.translatable("text.abstool.quick.tweakermore")).build());
            return new QuickSettings.Session(java.util.List.copyOf(entries), () -> {
                if (!dirty[0]) return;
                try { save.invoke(storage); dirty[0] = false; }
                catch (ReflectiveOperationException failure) { throw new IllegalStateException("Cannot save TweakerMore settings", failure); }
            });
        } catch (ReflectiveOperationException | LinkageError failure) {
            AbsTool.LOGGER.warn("TweakerMore quick settings API is unavailable", failure);
            return QuickSettings.EMPTY;
        }
    }
}
