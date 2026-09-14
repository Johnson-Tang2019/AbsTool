// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.abyssredemption.abstool.AbsTool;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import com.abyssredemption.abstool.client.vault.storage.JsonFiles;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static ModConfig config = new ModConfig();

    private ConfigManager() {
    }

    public static void init(Path configDirectory) {
        configPath = configDirectory.resolve("abstool.json");
        config = new ModConfig();
        load();
    }

    public static ModConfig get() {
        return config;
    }

    public static void load() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) config = loaded;
            } catch (Exception e) {
                AbsTool.LOGGER.warn("Failed to load config, using defaults", e);
            }
        }

        config.validate();
    }

    public static void save() {
        config.validate();
        try {
            JsonFiles.write(configPath, GSON.toJson(config));
        } catch (IOException e) {
            AbsTool.LOGGER.warn("Failed to save config", e);
        }
    }
}
