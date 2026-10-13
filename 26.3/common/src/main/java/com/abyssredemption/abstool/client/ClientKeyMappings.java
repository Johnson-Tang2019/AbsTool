package com.abyssredemption.abstool.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Vanilla stores these bindings in options.txt and exposes them in Controls. */
public final class ClientKeyMappings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse("abstool:main"));
    public static final KeyMapping SETTINGS_FIRST = new KeyMapping("key.abstool.settings_first", InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping SETTINGS_SECOND = new KeyMapping("key.abstool.settings_second", InputConstants.KEY_B, CATEGORY);
    public static final KeyMapping ELYTRA_LAUNCH = new KeyMapping("key.abstool.elytra_launch", InputConstants.UNKNOWN.getValue(), CATEGORY);
    private ClientKeyMappings() {}

    public static void register(Consumer<KeyMapping> register) {
        register.accept(SETTINGS_FIRST);
        register.accept(SETTINGS_SECOND);
        register.accept(ELYTRA_LAUNCH);
    }
    public static boolean settingsFirstDown() {
        return !bothSettingsUnbound() && (SETTINGS_FIRST.isUnbound() || SETTINGS_FIRST.isDown());
    }
    public static boolean settingsSecondDown() {
        return !bothSettingsUnbound() && (SETTINGS_SECOND.isUnbound() || SETTINGS_SECOND.isDown());
    }
    private static boolean bothSettingsUnbound() { return SETTINGS_FIRST.isUnbound() && SETTINGS_SECOND.isUnbound(); }
}
