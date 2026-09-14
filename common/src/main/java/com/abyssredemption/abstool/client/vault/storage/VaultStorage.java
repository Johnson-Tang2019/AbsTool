// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.vault.config.ModConfig;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class VaultStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path storagePath;
    // VaultKey already has efficient value-based equality. Keeping it as the map key avoids
    // rebuilding a concatenated scoped string during every lookup (including every render frame).
    private static final Map<VaultKey, VaultRecord> RECORDS = new HashMap<>();

    private VaultStorage() {
    }

    public static void init(Path configDirectory) {
        storagePath = configDirectory.resolve("abstool-vaults.json");
        load();
    }

    public static void exclude(VaultKey key) {
        if (isExcluded(key)) return;
        VaultRecord record = RECORDS.computeIfAbsent(key, VaultRecord::new);
        record.excludedAtMillis = System.currentTimeMillis();
        save();
    }

    public static boolean isExcluded(VaultKey key) {
        VaultRecord record = RECORDS.get(key);
        return record != null && record.excluded();
    }

    public static void applyRefresh(ModConfig config, String server, String dimension) {
        if (!config.refreshEnabled) return;

        applyRefresh(config, server, dimension, System.currentTimeMillis());
    }

    public static void applyRefresh(ModConfig config, String server, String dimension, long now) {
        if (!config.refreshEnabled) return;
        boolean changed = RECORDS.values().removeIf(record ->
                record.excluded()
                        && refreshApplies(config, record, server, dimension)
                        && isExpired(config, record.excludedAtMillis, now)
        );

        if (changed) save();
    }

    private static boolean refreshApplies(ModConfig config, VaultRecord record, String server, String dimension) {
        return switch (config.refreshScope) {
            case SERVER_DIMENSION -> record.server.equals(server) && record.dimension.equals(dimension);
            case SERVER_ALL_DIMENSIONS -> record.server.equals(server);
            case GLOBAL -> true;
        };
    }

    public static boolean isExpired(ModConfig config, long excludedAtMillis, long nowMillis) {
        if (config.refreshMode == ModConfig.RefreshMode.REAL_TIME_COOLDOWN) {
            long cooldownMillis = config.refreshCooldownMinutes * 60_000L;
            return nowMillis - excludedAtMillis >= cooldownMillis;
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone);
        LocalDateTime excluded = LocalDateTime.ofInstant(Instant.ofEpochMilli(excludedAtMillis), zone);
        LocalDateTime latestCutoff = now.withHour(config.dailyResetHour).withMinute(0).withSecond(0).withNano(0);
        if (now.isBefore(latestCutoff)) latestCutoff = latestCutoff.minusDays(1);
        return excluded.isBefore(latestCutoff);
    }

    private static void load() {
        RECORDS.clear();
        if (!Files.exists(storagePath)) return;

        try (Reader reader = Files.newBufferedReader(storagePath)) {
            StorageFile file = GSON.fromJson(reader, StorageFile.class);
            if (file != null && file.records != null) {
                for (VaultRecord record : file.records) {
                    if (record != null && record.server != null && record.dimension != null && record.excluded()) {
                        RECORDS.put(record.key(), record);
                    }
                }
            }
        } catch (Exception e) {
            AbsTool.LOGGER.warn("Failed to load vault storage", e);
        }
    }

    private static void save() {
        try {
            StorageFile file = new StorageFile();
            file.records = new ArrayList<>(RECORDS.values());
            JsonFiles.write(storagePath, GSON.toJson(file));
        } catch (IOException e) {
            AbsTool.LOGGER.warn("Failed to save vault storage", e);
        }
    }

    private static class StorageFile {
        Collection<VaultRecord> records = java.util.List.of();
    }
}
