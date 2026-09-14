package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.vault.config.ModConfig;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SchematicVerification {
    public static void main(String[] args) throws Exception {
        legacyConfiguration();
        var versions = com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.PINNED;
        require(com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(versions, true).isEmpty(), "Pinned combination must pass");
        require(!com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(versions, false).isEmpty(), "Physical server must not load rendering hooks");
        var iris112 = new java.util.HashMap<>(versions);
        iris112.put("iris", "1.11.2+mc26.2");
        require(!com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(iris112, true).isEmpty(), "Incompatible Iris/Sodium pair must remain gated");
        iris112.put("sodium", "0.9.1+mc26.2");
        require(com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(iris112, true).isEmpty(), "Iris 1.11.2 must be supported");
        iris112.put("iris", "1.11.3+mc26.2");
        require(!com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(iris112, true).isEmpty(), "Untested Iris versions must remain gated");
        for (String id : versions.keySet()) {
            var changed = new java.util.HashMap<>(versions);
            changed.remove(id);
            require(com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(changed, true).startsWith("Missing"), "Missing dependency must not activate: " + id);
            changed.put(id, "unrecognized-build");
            require(com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(changed, true).startsWith("Unsupported"), "Unknown version must not activate: " + id);
        }
        System.out.println("Schematic configuration verification passed.");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void legacyConfiguration() throws Exception {
        Path dir = Files.createTempDirectory(Path.of("."), "legacy-config-");
        String old = """
                {"enabled":true,"highlightColor":1193046,"tracerItemId":"minecraft:trial_key",
                 "refreshEnabled":true,"refreshScope":"GLOBAL","refreshCooldownMinutes":37,
                 "furnace":{"enabled":true,"range":"ALL_LOADED","requestsPerSecond":13,"future":{"keep":[1,2,3]}},
                 "futureSetting":{"text":"keep me","flag":false}}
                """;
        Files.writeString(dir.resolve("abstool.json"), old);
        byte[] records = "{\"legacy-record\":\"leave untouched\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(dir.resolve("abstool-vaults.json"), records);
        ConfigManager.init(dir);
        var config = ConfigManager.get();
        require(config.enabled && config.highlightColor == 1193046 && config.furnace.requestsPerSecond == 13
                && config.refreshScope == ModConfig.RefreshScope.GLOBAL, "Legacy choices must survive adding a module");
        config.schematicShaderCompat.opacityMultiplier = Double.NaN;
        config.schematicShaderCompat.mode = null;
        ConfigManager.save();
        require(config.schematicShaderCompat.opacityMultiplier == 1.0 && config.schematicShaderCompat.mode != null,
                "Nonfinite opacity and unknown mode must normalize");
        var saved = com.google.gson.JsonParser.parseString(Files.readString(dir.resolve("abstool.json"))).getAsJsonObject();
        var legacy = com.google.gson.JsonParser.parseString(old).getAsJsonObject();
        for (var entry : legacy.entrySet()) {
            if (!entry.getKey().equals("furnace")) require(entry.getValue().equals(saved.get(entry.getKey())), "Legacy field changed: " + entry.getKey());
        }
        require(saved.getAsJsonObject("furnace").get("future").equals(legacy.getAsJsonObject("furnace").get("future")), "Nested unknown fields must survive");
        require(java.util.Arrays.equals(records, Files.readAllBytes(dir.resolve("abstool-vaults.json"))), "Config migration must not rewrite records");
        require(Files.readString(dir.resolve("abstool.json.bak")).equals(old), "Migration must retain original backup");
        ConfigManager.init(dir);
        ConfigManager.save();
        require(com.google.gson.JsonParser.parseString(Files.readString(dir.resolve("abstool.json"))).equals(saved), "Repeated save must be stable");
    }

}
