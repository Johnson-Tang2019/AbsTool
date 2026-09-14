package com.abyssredemption.abstool.client.furnace;

import java.util.Locale;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.network.chat.Component;

public final class FurnaceSettings {
    private FurnaceSettings() {}
    public static void add(ConfigBuilder builder) {
        FurnaceConfig config = FurnaceTracker.config();
        var category = builder.getOrCreateCategory(text("category"));
        var entries = builder.entryBuilder();
        category.addEntry(entries.startTextDescription(text("hint")).build());
        category.addEntry(entries.startTextDescription(FurnaceTracker.INSTANCE.status()).build());
        category.addEntry(entries.startBooleanToggle(text("enabled"), config.enabled).setDefaultValue(false).setSaveConsumer(v -> config.enabled = v).build());
        category.addEntry(entries.startEnumSelector(text("range"), FurnaceConfig.Range.class, config.range)
                .setEnumNameProvider(v -> text(v.name().toLowerCase(Locale.ROOT))).setDefaultValue(FurnaceConfig.Range.BLOCKS_256).setSaveConsumer(v -> config.range = v).build());
        category.addEntry(entries.startIntSlider(text("rate"), config.requestsPerSecond, 1, 40).setDefaultValue(20).setSaveConsumer(v -> config.requestsPerSecond = v).build());
        category.addEntry(entries.startIntField(text("expiry"), config.staleSeconds).setMin(5).setMax(300).setDefaultValue(30).setSaveConsumer(v -> config.staleSeconds = v).build());
        category.addEntry(entries.startBooleanToggle(text("jade"), config.jadeFallback).setDefaultValue(true).setSaveConsumer(v -> config.jadeFallback = v).build());
        category.addEntry(entries.startIntField(text("jade_range"), config.jadeExtraRange).setMin(0).setMax(1000).setDefaultValue(21)
                .setTooltip(text("jade_range_hint")).setSaveConsumer(v -> config.jadeExtraRange = v).build());
        category.addEntry(entries.startBooleanToggle(text("tracers"), config.tracers).setDefaultValue(true).setSaveConsumer(v -> config.tracers = v).build());
        category.addEntry(entries.startBooleanToggle(text("labels"), config.labels).setDefaultValue(true).setSaveConsumer(v -> config.labels = v).build());
        category.addEntry(entries.startBooleanToggle(text("show_stale"), config.showStale).setDefaultValue(true).setSaveConsumer(v -> config.showStale = v).build());
        category.addEntry(entries.startColorField(text("color"), config.color).setDefaultValue(0xFF4040).setSaveConsumer(v -> config.color = v).build());
        category.addEntry(entries.startColorField(text("tracer_color"), config.tracerColor).setDefaultValue(0xFFB040).setSaveConsumer(v -> config.tracerColor = v).build());
    }
    private static Component text(String key) { return Component.translatable("text.abstool.furnace." + key); }
}
