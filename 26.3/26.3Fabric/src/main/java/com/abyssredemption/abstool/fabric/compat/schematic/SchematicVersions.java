package com.abyssredemption.abstool.fabric.compat.schematic;

import java.util.Map;
import java.util.Set;

/** Version gate for Minecraft 26.3; optional renderer bytecode is checked separately. */
public final class SchematicVersions {
    public static final Set<String> REQUIRED = Set.of("minecraft", "litematica", "malilib", "iris", "sodium");
    public static final Map<String, String> BASELINE = Map.of(
            "minecraft", "26.3",
            "litematica", "0.29.0",
            "malilib", "0.30.1",
            "iris", "1.11.7+mc26.3",
            "sodium", "0.9.2+mc26.3");

    private SchematicVersions() {}

    public static String rejection(Map<String, String> installed, boolean physicalClient) {
        if (!physicalClient) return "Physical server";
        for (String id : REQUIRED.stream().sorted().toList()) {
            if (installed.get(id) == null) return "Missing " + id;
        }
        if (!"26.3".equals(installed.get("minecraft"))) return "Unsupported minecraft " + installed.get("minecraft");
        for (String id : Set.of("iris", "sodium")) {
            if (!installed.get(id).endsWith("+mc26.3")) return "Unsupported " + id + " " + installed.get(id);
        }
        return "";
    }
}
