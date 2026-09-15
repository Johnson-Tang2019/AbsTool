package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.vault.config.ModConfig;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SchematicVerification {
    public static void main(String[] args) throws Exception {
        legacyConfiguration();
        dependencyMatrix();
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
    private static void dependencyMatrix() {
        String[] litematica = {"0.28.3", "0.28.4", "0.28.5", "0.28.6", "0.28.8"};
        int[] minimum = {2, 2, 4, 5, 5};
        String[] iris = {"1.11.0+mc26.2", "1.11.1+mc26.2", "1.11.2+mc26.2", "1.11.4+mc26.2"};
        int accepted = 0;
        for (int l = 0; l < litematica.length; l++) {
            for (int m = 0; m <= 6; m++) {
                for (int i = 0; i < iris.length; i++) {
                    for (int sodium = 0; sodium <= 2; sodium++) {
                        var versions = java.util.Map.of("minecraft", "26.2", "litematica", litematica[l],
                                "malilib", "0.29." + m, "iris", iris[i], "sodium", "0.9." + sodium + "+mc26.2");
                        boolean expected = m >= minimum[l] && i > 0 && sodium == i - 1;
                        boolean actual = com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(versions, true).isEmpty();
                        require(expected == actual, "Dependency matrix mismatch: " + versions);
                        if (actual) accepted++;
                    }
                }
            }
        }
        require(accepted == 51, "Expected 51 audited combinations");
        var unknown = new java.util.HashMap<>(com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.PINNED);
        for (String version : new String[]{"0.28.7", "0.28.9", "0.28.8-custom"}) {
            unknown.put("litematica", version);
            require(!com.abyssredemption.abstool.fabric.compat.schematic.SchematicVersions.rejection(unknown, true).isEmpty(), "Unaudited release must remain gated");
        }
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
