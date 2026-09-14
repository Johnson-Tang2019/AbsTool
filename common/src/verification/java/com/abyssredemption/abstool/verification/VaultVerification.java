package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.SettingsShortcut;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.vault.config.ModConfig;
import com.abyssredemption.abstool.client.vault.storage.VaultKey;
import com.abyssredemption.abstool.client.vault.storage.VaultStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

/** Checks externally observable behavior using isolated files under the build directory. */
public final class VaultVerification {
    public static void main(String[] args) throws Exception {
        shortcut();
        configuration();
        storage();
        dailyReset();
        furnaceProtocol();
        System.out.println("Vault verification passed: shortcut, config, persistence, isolation, refresh and corrupt input.");
    }

    private static void shortcut() {
        SettingsShortcut chord = new SettingsShortcut();
        require(!chord.update(true, false, true), "R alone must not open settings");
        require(!chord.update(false, true, true), "B alone must not open settings");
        require(chord.update(true, true, true), "B then R must open settings");
        require(!chord.update(true, true, true), "Holding the chord must not reopen settings");
        chord.update(true, false, true);
        require(chord.update(true, true, true), "R then B must open settings");
        chord.update(false, false, true);
        require(!chord.update(true, true, false), "Typing in a screen must not open settings");
        require(!chord.update(true, true, true), "Closing a screen while held must not reopen it");
        chord.update(false, false, true);
        require(chord.update(true, true, true), "Releasing and pressing must rearm the shortcut");
    }

    private static void configuration() throws Exception {
        Path dir = Files.createTempDirectory(Path.of("."), "config-");
        ConfigManager.init(dir);
        require(!ConfigManager.get().enabled, "Tracking must default to disabled");
        ModConfig config = ConfigManager.get();
        config.enabled = true;
        config.furnace.enabled = true;
        config.furnace.requestsPerSecond = 200;
        config.furnace.range = com.abyssredemption.abstool.client.furnace.FurnaceConfig.Range.ALL_LOADED;
        config.renderRadius = Integer.MAX_VALUE;
        config.refreshCooldownMinutes = Long.MAX_VALUE;
        config.dailyResetHour = -1;
        config.refreshMode = null;
        config.excludedRenderMode = null;
        config.tracerItemId = " minecraft:trial_key ";
        ConfigManager.save();
        ConfigManager.init(dir);
        config = ConfigManager.get();
        require(config.enabled && config.renderRadius == 32, "Settings must persist with bounded range");
        require(config.furnace.enabled && config.furnace.requestsPerSecond == 40
                && config.furnace.range == com.abyssredemption.abstool.client.furnace.FurnaceConfig.Range.ALL_LOADED,
                "Furnace settings must persist with bounded request rate");
        require(config.refreshCooldownMinutes <= Long.MAX_VALUE / 60_000L, "Cooldown multiplication must not overflow");
        require(config.dailyResetHour == 0 && config.refreshMode != null && config.excludedRenderMode != null,
                "Invalid enum/hour values must normalize");
        require(config.tracerItemId.equals("minecraft:trial_key"), "Explicit item choice must be preserved");
        Files.writeString(dir.resolve("abstool.json"), "{broken");
        ConfigManager.init(dir);
        require(!ConfigManager.get().enabled, "Corrupt config must use defaults");
        require(Files.readString(dir.resolve("abstool.json")).equals("{broken"), "Loading must preserve corrupt input");
        ConfigManager.save();
        require(Files.readString(dir.resolve("abstool.json.bak")).equals("{broken"), "Saving must retain a recovery copy");
    }

    private static void furnaceProtocol() throws Exception {
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("id", "minecraft:furnace");
        var bytes = new java.io.ByteArrayOutputStream();
        net.minecraft.nbt.NbtIo.writeCompressed(tag, bytes);
        var buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            buf.writeInt(bytes.size()); buf.writeBytes(bytes.toByteArray());
            require(com.abyssredemption.abstool.client.furnace.FurnaceNetwork.readData(buf).equals(tag), "Servux gzip NBT must round-trip");
            buf.clear(); buf.writeInt(Integer.MAX_VALUE);
            try {
                com.abyssredemption.abstool.client.furnace.FurnaceNetwork.readData(buf);
                throw new AssertionError("Oversized packet length must be rejected before allocation");
            } catch (java.io.IOException expected) { }
            buf.clear(); buf.writeInt(100); buf.writeByte(0);
            try {
                com.abyssredemption.abstool.client.furnace.FurnaceNetwork.readData(buf);
                throw new AssertionError("Truncated payload must be rejected");
            } catch (java.io.IOException expected) { }
        } finally { buf.release(); }
    }

    private static void storage() throws Exception {
        Path dir = Files.createTempDirectory(Path.of("."), "records-");
        VaultKey first = new VaultKey("server-a", "minecraft:overworld", -32, -10, 20);
        VaultKey dimension = new VaultKey("server-a", "minecraft:the_nether", -32, -10, 20);
        VaultKey other = new VaultKey("server-b", "minecraft:overworld", -32, -10, 20);
        VaultKey localA = new VaultKey("singleplayer:/saves/a", "minecraft:overworld", 0, 0, 0);
        VaultKey localB = new VaultKey("singleplayer:/saves/b", "minecraft:overworld", 0, 0, 0);
        VaultStorage.init(dir);
        VaultStorage.exclude(first);
        require(!VaultStorage.isExcluded(dimension) && !VaultStorage.isExcluded(other), "Records must be scoped");
        VaultStorage.exclude(dimension);
        VaultStorage.exclude(other);
        VaultStorage.exclude(localA);
        require(!VaultStorage.isExcluded(localB), "Singleplayer worlds must remain separate");
        VaultStorage.init(dir);
        require(VaultStorage.isExcluded(first) && VaultStorage.isExcluded(localA), "Exclusions must survive reload");
        String saved = Files.readString(dir.resolve("abstool-vaults.json"));
        VaultStorage.exclude(first);
        require(saved.equals(Files.readString(dir.resolve("abstool-vaults.json"))), "Repeated interaction must not rewrite storage");
        ModConfig config = new ModConfig();
        config.refreshCooldownMinutes = 1;
        long future = System.currentTimeMillis() + 120_000;
        VaultStorage.applyRefresh(config, first.server(), first.dimension(), future);
        require(VaultStorage.isExcluded(first), "Refresh must remain off by default");
        config.refreshEnabled = true;
        VaultStorage.applyRefresh(config, first.server(), first.dimension(), future);
        require(!VaultStorage.isExcluded(first) && VaultStorage.isExcluded(dimension) && VaultStorage.isExcluded(other),
                "Dimension refresh must only remove its own expired exclusions");
        config.refreshScope = ModConfig.RefreshScope.SERVER_ALL_DIMENSIONS;
        VaultStorage.applyRefresh(config, first.server(), first.dimension(), future);
        require(!VaultStorage.isExcluded(dimension) && VaultStorage.isExcluded(other), "Server refresh must stay scoped");
        config.refreshScope = ModConfig.RefreshScope.GLOBAL;
        VaultStorage.applyRefresh(config, first.server(), first.dimension(), future);
        require(!VaultStorage.isExcluded(other) && !VaultStorage.isExcluded(localA), "Global refresh must expire every scope");
        Files.writeString(dir.resolve("abstool-vaults.json"), "{\"records\":[null,{}, {\"server\":null}]}");
        VaultStorage.init(dir);
        require(!VaultStorage.isExcluded(first), "Malformed entries must be ignored");
    }

    private static void dailyReset() {
        ModConfig config = new ModConfig();
        config.refreshMode = ModConfig.RefreshMode.DAILY_RESET;
        config.dailyResetHour = 6;
        long cutoff = LocalDate.of(2026, 9, 14).atTime(6, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        require(!VaultStorage.isExpired(config, cutoff - 1000, cutoff - 1), "Daily reset must not occur early");
        require(VaultStorage.isExpired(config, cutoff - 1000, cutoff), "Daily reset must apply at cutoff");
        require(!VaultStorage.isExpired(config, cutoff, cutoff + 1000), "Exclusion made at cutoff belongs to the new day");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
