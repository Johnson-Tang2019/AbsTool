// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClothConfigScreenFactory {
    private ClothConfigScreenFactory() {
    }

    public static Screen create(Screen parent) {
        ModConfig config = ConfigManager.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("text.abstool.vault.title"))
                .setSavingRunnable(ConfigManager::save);
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory all = builder.getOrCreateCategory(Component.translatable("text.abstool.category.all"));
        ConfigCategory tracking = builder.getOrCreateCategory(Component.translatable("text.abstool.category.tracking"));
        ConfigCategory optimization = builder.getOrCreateCategory(Component.translatable("text.abstool.category.optimization"));
        ConfigCategory serverStats = builder.getOrCreateCategory(Component.translatable("text.abstool.serverstats.category"));

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("text.abstool.vault.category.general"));
        general.addEntry(entries.startBooleanToggle(Component.translatable("text.abstool.vault.option.enabled"), config.enabled)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.enabled = value)
                .build());
        general.addEntry(entries.startColorField(Component.translatable("text.abstool.vault.option.highlight_color"), config.highlightColor)
                .setDefaultValue(0xFF3C3C)
                .setSaveConsumer(value -> config.highlightColor = value)
                .build());
        general.addEntry(entries.startEnumSelector(Component.translatable("text.abstool.vault.option.excluded_mode"), ModConfig.ExcludedRenderMode.class, config.excludedRenderMode)
                .setEnumNameProvider(value -> Component.translatable("text.abstool.vault.value." + value.name().toLowerCase(Locale.ROOT)))
                .setDefaultValue(ModConfig.ExcludedRenderMode.HIDE)
                .setSaveConsumer(value -> config.excludedRenderMode = value)
                .build());
        general.addEntry(entries.startColorField(Component.translatable("text.abstool.vault.option.excluded_color"), config.excludedColor)
                .setDefaultValue(0x40A0FF)
                .setSaveConsumer(value -> config.excludedColor = value)
                .build());
        general.addEntry(entries.startIntField(Component.translatable("text.abstool.vault.option.render_chunks"), config.renderRadius)
                .setDefaultValue(8)
                .setMin(1)
                .setMax(32)
                .setSaveConsumer(value -> config.renderRadius = value)
                .build());

        ConfigCategory keybinds = builder.getOrCreateCategory(Component.translatable("text.abstool.vault.category.keybinds"));
        keybinds.addEntry(entries.startTextDescription(Component.translatable("text.abstool.vault.hotkey_hint")).build());
        serverStats.addEntry(new com.abyssredemption.abstool.client.serverstats.gui.ServerStatsEntry());

        ConfigCategory tracer = builder.getOrCreateCategory(Component.translatable("text.abstool.vault.category.tracer"));
        tracer.addEntry(entries.startBooleanToggle(Component.translatable("text.abstool.vault.option.render_tracers"), config.renderTracers)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.renderTracers = value)
                .build());
        tracer.addEntry(entries.startColorField(Component.translatable("text.abstool.vault.option.tracer_color"), config.tracerColor)
                .setDefaultValue(0x00FF80)
                .setSaveConsumer(value -> config.tracerColor = value)
                .build());
        tracer.addEntry(entries.startBooleanToggle(Component.translatable("text.abstool.vault.option.tracer_requires_item"), config.tracerRequiresItem)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.tracerRequiresItem = value)
                .build());
        tracer.addEntry(entries.startStrField(Component.translatable("text.abstool.vault.option.tracer_item"), config.tracerItemId)
                .setDefaultValue("minecraft:ominous_trial_key")
                .setErrorSupplier(value -> {
                    Identifier id = Identifier.tryParse(value.trim());
                    return id != null && BuiltInRegistries.ITEM.containsKey(id)
                            ? Optional.empty() : Optional.of(Component.translatable("text.abstool.vault.invalid_item"));
                })
                .setSaveConsumer(value -> config.tracerItemId = value.trim())
                .build());

        ConfigCategory refresh = builder.getOrCreateCategory(Component.translatable("text.abstool.vault.category.refresh"));
        refresh.addEntry(entries.startBooleanToggle(Component.translatable("text.abstool.vault.option.refresh_enabled"), config.refreshEnabled)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.refreshEnabled = value)
                .build());
        refresh.addEntry(entries.startEnumSelector(Component.translatable("text.abstool.vault.option.refresh_scope"), ModConfig.RefreshScope.class, config.refreshScope)
                .setEnumNameProvider(value -> Component.translatable("text.abstool.vault.value." + value.name().toLowerCase(Locale.ROOT)))
                .setDefaultValue(ModConfig.RefreshScope.SERVER_DIMENSION)
                .setSaveConsumer(value -> config.refreshScope = value)
                .build());
        refresh.addEntry(entries.startEnumSelector(Component.translatable("text.abstool.vault.option.refresh_mode"), ModConfig.RefreshMode.class, config.refreshMode)
                .setEnumNameProvider(value -> Component.translatable("text.abstool.vault.value." + value.name().toLowerCase(Locale.ROOT)))
                .setDefaultValue(ModConfig.RefreshMode.REAL_TIME_COOLDOWN)
                .setSaveConsumer(value -> config.refreshMode = value)
                .build());
        refresh.addEntry(entries.startLongField(Component.translatable("text.abstool.vault.option.refresh_minutes"), config.refreshCooldownMinutes)
                .setDefaultValue(1440L)
                .setMin(1L)
                .setMax(Long.MAX_VALUE / 60_000L)
                .setSaveConsumer(value -> config.refreshCooldownMinutes = value)
                .build());
        refresh.addEntry(entries.startIntSlider(Component.translatable("text.abstool.vault.option.daily_hour"), config.dailyResetHour, 0, 23)
                .setDefaultValue(0)
                .setSaveConsumer(value -> config.dailyResetHour = value)
                .build());

        com.abyssredemption.abstool.client.furnace.FurnaceSettings.add(builder);
        com.abyssredemption.abstool.client.schematic.SchematicShaderSettings.add(builder);
        for (ConfigCategory category : java.util.List.of(keybinds, general, tracer, refresh,
                builder.getOrCreateCategory(Component.translatable("text.abstool.furnace.category")))) {
            tracking.addEntry(entries.startTextDescription(category.getCategoryKey()).build());
            tracking.getEntries().addAll(category.getEntries());
            builder.removeCategory(category.getCategoryKey());
        }
        ConfigCategory schematic = builder.getOrCreateCategory(Component.translatable("text.abstool.schematic.category"));
        optimization.addEntry(entries.startTextDescription(schematic.getCategoryKey()).build());
        optimization.getEntries().addAll(schematic.getEntries());
        builder.removeCategory(schematic.getCategoryKey());
        com.abyssredemption.abstool.client.elytra.ElytraSettings.add(builder, optimization);
        // Share the same editor instances: switching tabs cannot resurrect stale values on save.
        all.getEntries().addAll(tracking.getEntries());
        all.getEntries().addAll(optimization.getEntries());
        QuickSettings.Session quick = QuickSettings.provider.apply(entries);
        if (!quick.entries().isEmpty()) {
            ConfigCategory shortcuts = builder.getOrCreateCategory(Component.translatable("text.abstool.category.quick"));
            shortcuts.getEntries().addAll(quick.entries());
            all.getEntries().addAll(quick.entries());
        }
        builder.setSavingRunnable(() -> { ConfigManager.save(); quick.save().run(); });
        builder.setFallbackCategory(all);
        return builder.build();
    }

}
