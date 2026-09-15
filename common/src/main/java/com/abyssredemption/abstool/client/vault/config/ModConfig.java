// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.config;

public class ModConfig {
    public boolean shulkerMostCommonItem = true;
    public com.abyssredemption.abstool.client.schematic.SchematicShaderConfig schematicShaderCompat = new com.abyssredemption.abstool.client.schematic.SchematicShaderConfig();
    public com.abyssredemption.abstool.client.furnace.FurnaceConfig furnace = new com.abyssredemption.abstool.client.furnace.FurnaceConfig();
    public boolean enabled = false;
    public int highlightColor = 0xFF3C3C;
    public ExcludedRenderMode excludedRenderMode = ExcludedRenderMode.HIDE;
    public int excludedColor = 0x40A0FF;
    public boolean renderTracers = true;
    public int tracerColor = 0x00FF80;
    public boolean tracerRequiresItem = false;
    public String tracerItemId = "minecraft:ominous_trial_key";
    /** Number of chunks to search around the player, bounded by the client's loaded range. */
    public int renderRadius = 8;
    public boolean refreshEnabled = false;
    public RefreshScope refreshScope = RefreshScope.SERVER_DIMENSION;
    public RefreshMode refreshMode = RefreshMode.REAL_TIME_COOLDOWN;
    public long refreshCooldownMinutes = 1440;
    public int dailyResetHour = 0;

    public com.abyssredemption.abstool.client.elytra.ElytraConfig elytraAssist = new com.abyssredemption.abstool.client.elytra.ElytraConfig();

    public void validate() {
        if (elytraAssist == null) elytraAssist = new com.abyssredemption.abstool.client.elytra.ElytraConfig();
        elytraAssist.validate();
        if (schematicShaderCompat == null) schematicShaderCompat = new com.abyssredemption.abstool.client.schematic.SchematicShaderConfig();
        schematicShaderCompat.validate();
        if (furnace == null) furnace = new com.abyssredemption.abstool.client.furnace.FurnaceConfig();
        furnace.validate();
        renderRadius = Math.max(1, Math.min(32, renderRadius));
        refreshCooldownMinutes = Math.max(1, Math.min(Long.MAX_VALUE / 60_000L, refreshCooldownMinutes));
        dailyResetHour = Math.max(0, Math.min(23, dailyResetHour));
        if (excludedRenderMode == null) excludedRenderMode = ExcludedRenderMode.HIDE;
        if (refreshScope == null) refreshScope = RefreshScope.SERVER_DIMENSION;
        if (refreshMode == null) refreshMode = RefreshMode.REAL_TIME_COOLDOWN;
        if (tracerItemId == null || tracerItemId.isBlank()) {
            tracerItemId = "minecraft:ominous_trial_key";
        }
        tracerItemId = tracerItemId.trim();
        highlightColor &= 0xFFFFFF;
        excludedColor &= 0xFFFFFF;
        tracerColor &= 0xFFFFFF;
    }

    public enum ExcludedRenderMode { HIDE, OTHER_COLOR }
    public enum RefreshScope { SERVER_DIMENSION, SERVER_ALL_DIMENSIONS, GLOBAL }
    public enum RefreshMode { REAL_TIME_COOLDOWN, DAILY_RESET }
}
