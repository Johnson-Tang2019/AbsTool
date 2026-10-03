package com.abyssredemption.abstool.fabric.compat.schematic;

import java.util.Map;

/** Exact third-party releases whose methods and bytecode were audited for 26.3. */
public final class SchematicVersions {
    public static final Map<String, String> PINNED = Map.of(
            "minecraft", "26.3",
            "litematica", "0.29.0",
            "malilib", "0.30.1",
            "iris", "1.11.7+26.3-fabric",
            "sodium", "mc26.3-0.9.2-fabric");

    private SchematicVersions() {}

    public static String rejection(Map<String, String> installed, boolean physicalClient) {
        if (!physicalClient) return "Physical server";
        for (String id : PINNED.keySet().stream().sorted().toList()) {
            String actual = installed.get(id);
            if (actual == null) return "Missing " + id;
            if (!PINNED.get(id).equals(actual)) return "Unsupported " + id + " " + actual;
        }
        return "";
    }
}
