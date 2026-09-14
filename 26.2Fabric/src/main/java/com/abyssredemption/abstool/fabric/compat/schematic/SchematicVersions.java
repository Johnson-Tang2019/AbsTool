package com.abyssredemption.abstool.fabric.compat.schematic;

import java.util.Map;

/** Version policy can be checked without linking any rendering classes. */
public final class SchematicVersions {
    public static final Map<String, String> PINNED = Map.of("minecraft", "26.2", "litematica", "0.28.8",
            "malilib", "0.29.6", "iris", "1.11.4+mc26.2", "sodium", "0.9.2+mc26.2");
    public static String rejection(Map<String, String> installed, boolean physicalClient) {
        if (!physicalClient) return "Physical server";
        for (String id : PINNED.keySet().stream().sorted().toList()) {
            String version = installed.get(id);
            if (version == null) return "Missing " + id;
            String expected = id.equals("sodium") && "1.11.2+mc26.2".equals(installed.get("iris"))
                    ? "0.9.1+mc26.2" : PINNED.get(id);
            boolean supported = id.equals("iris")
                    ? java.util.Set.of("1.11.2+mc26.2", "1.11.4+mc26.2").contains(version)
                    : expected.equals(version);
            if (!supported) return "Unsupported " + id + " " + version + " (expected " + expected + ")";
        }
        return "";
    }
}
