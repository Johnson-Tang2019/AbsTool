package com.abyssredemption.abstool.client.schematic;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class SchematicShaderSettings {
    public static void add(ConfigBuilder builder) {
        var category = builder.getOrCreateCategory(text("category"));
        var entries = builder.entryBuilder();
        var status = SchematicShaderStatus.get();
        category.addEntry(entries.startTextDescription(text("experimental")).build());
        category.addEntry(entries.startTextDescription(text("state." + status.state().name().toLowerCase(Locale.ROOT))).build());
        category.addEntry(entries.startTextDescription(Component.literal(status.detail() + "; draws=" + status.draws())).build());
        if (status.state() == SchematicShaderStatus.State.UNSUPPORTED_LOADER) return;
        var config = ConfigManager.get().schematicShaderCompat;
        category.addEntry(entries.startEnumSelector(text("mode"), SchematicShaderConfig.Mode.class, config.mode)
                .setEnumNameProvider(value -> text("mode." + value.name().toLowerCase(Locale.ROOT)))
                .setDefaultValue(SchematicShaderConfig.Mode.AUTO).setSaveConsumer(v -> config.mode = v).build());
        category.addEntry(entries.startDoubleField(text("opacity"), config.opacityMultiplier)
                .setMin(0.0).setMax(1.0).setDefaultValue(1.0).setSaveConsumer(v -> config.opacityMultiplier = v).build());
        category.addEntry(entries.startBooleanToggle(text("debug"), config.debug)
                .setDefaultValue(false).setSaveConsumer(v -> config.debug = v).build());
    }
    private static Component text(String key) { return Component.translatable("text.abstool.schematic." + key); }
}
