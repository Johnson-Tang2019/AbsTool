// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private static JsonObject original = new JsonObject();

    private ConfigManager() {
    }

    public static void init(Path configDirectory) {
        configPath = configDirectory.resolve("abstool.json");
        config = new ModConfig();
        original = new JsonObject();
        load();
    }

    public static ModConfig get() {
        return config;
    }

    public static void load() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                var json = JsonParser.parseReader(reader);
                ModConfig loaded = GSON.fromJson(json, ModConfig.class);
                if (json.isJsonObject()) original = json.getAsJsonObject().deepCopy();
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
            JsonObject merged = original.deepCopy();
            merge(merged, GSON.toJsonTree(config).getAsJsonObject());
            JsonFiles.write(configPath, GSON.toJson(merged));
            original = merged;
        } catch (IOException e) {
            AbsTool.LOGGER.warn("Failed to save config", e);
        }
    }

    private static void merge(JsonObject destination, JsonObject known) {
        known.entrySet().forEach(entry -> {
            var old = destination.get(entry.getKey());
            if (old != null && old.isJsonObject() && entry.getValue().isJsonObject()) {
                merge(old.getAsJsonObject(), entry.getValue().getAsJsonObject());
            } else {
                destination.add(entry.getKey(), entry.getValue().deepCopy());
            }
        });
    }
}
