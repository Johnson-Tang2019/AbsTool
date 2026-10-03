package com.abyssredemption.abstool.smoke;

import java.util.function.Function;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/** Real optional-mod persistence checks in an isolated development run directory. */
public final class QuickSettingsSmoke {
    private static final Component QUICK = Component.translatable("text.abstool.category.quick");
    private static BooleanListEntry toggle(AbstractConfigScreen screen) {
        return (BooleanListEntry) screen.getCategorizedEntries().get(QUICK).stream()
                .filter(e -> e.getFieldName().equals(Component.translatable("text.abstool.quick.option.shulkerBoxItemContentHint")))
                .findFirst().orElseThrow();
    }
    private static void click(BooleanListEntry entry) {
        var button = (AbstractWidget) entry.children().getFirst();
        button.setFocused(true);
        if (!button.keyPressed(new KeyEvent(32, 0, 0))) throw new AssertionError("Toggle button rejected space");
    }
    public static Screen check(Minecraft client, Function<Screen, Screen> factory, Screen parent, AbstractConfigScreen screen) {
        try {
            Class<?> configs = Class.forName("me.fallenbreath.tweakermore.config.TweakerMoreConfigs");
            Object option = configs.getField("SHULKER_BOX_ITEM_CONTENT_HINT").get(null);
            var getter = option.getClass().getMethod("getBooleanValue");
            var setter = option.getClass().getMethod("setBooleanValue", boolean.class);
            boolean original = (boolean) getter.invoke(option);
            if (!screen.getCategorizedEntries().get(Component.translatable("text.abstool.category.all"))
                    .containsAll(screen.getCategorizedEntries().get(QUICK))) throw new AssertionError("Quick editors must be shared with All");
            click(toggle(screen));
            if ((boolean) getter.invoke(option) != original) throw new AssertionError("Unsaved edit changed TweakerMore");
            // Discard this screen; only the new screen is subsequently saved.
            screen = (AbstractConfigScreen) factory.apply(parent);
            client.gui.setScreen(screen);
            if (toggle(screen).getValue() != original) throw new AssertionError("Discard did not restore live value");
            click(toggle(screen));
            var scale = (DoubleListEntry) screen.getCategorizedEntries().get(QUICK).stream()
                    .filter(e -> e.getFieldName().equals(Component.translatable("text.abstool.quick.option.shulkerBoxItemContentHintScale")))
                    .findFirst().orElseThrow();
            var text = (EditBox) scale.children().stream().filter(EditBox.class::isInstance).findFirst().orElseThrow();
            String previous = text.getValue();
            text.setValue("NaN");
            if ("NaN".equals(text.getValue()) && scale.getConfigError().isEmpty()) throw new AssertionError("NaN must be rejected");
            text.setValue("2");
            if (scale.getConfigError().isEmpty()) throw new AssertionError("Upstream maximum must be respected");
            text.setValue(previous);
            screen.saveAll(false);
            if ((boolean) getter.invoke(option) == original) throw new AssertionError("Save did not update TweakerMore");
            Class<?> storageClass = Class.forName("me.fallenbreath.tweakermore.config.TweakerMoreConfigStorage");
            Object storage = storageClass.getMethod("getInstance").invoke(null);
            setter.invoke(option, original);
            storageClass.getMethod("load").invoke(storage);
            if ((boolean) getter.invoke(option) == original) throw new AssertionError("Saved value did not survive disk reload");
            setter.invoke(option, original);
            storageClass.getMethod("save").invoke(storage);
            screen = (AbstractConfigScreen) factory.apply(parent);
            screen.selectedCategoryIndex = 3;
            client.gui.setScreen(screen);
            org.slf4j.LoggerFactory.getLogger("abstool-smoke").info("ABSTOOL_QUICK_SETTINGS_PASS entries={}", screen.getCategorizedEntries().get(QUICK).size());
            return screen;
        } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
    }
}
