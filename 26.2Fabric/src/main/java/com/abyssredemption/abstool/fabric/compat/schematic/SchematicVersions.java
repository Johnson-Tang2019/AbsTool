package com.abyssredemption.abstool.fabric.compat.schematic;

import java.util.Map;
import java.util.Set;

/** Audited releases and their upstream dependency constraints, without rendering linkage. */
public final class SchematicVersions {
    public static final Map<String, String> PINNED = Map.of("minecraft", "26.2", "litematica", "0.28.8",
            "malilib", "0.29.6", "iris", "1.11.4+mc26.2", "sodium", "0.9.2+mc26.2");
    private static final Map<String, Integer> MIN_MALILIB = Map.of(
            "0.28.3", 2, "0.28.4", 2, "0.28.5", 4, "0.28.6", 5, "0.28.8", 5);
    private static final Map<String, String> IRIS_SODIUM = Map.of(
            "1.11.1+mc26.2", "0.9.0+mc26.2", "1.11.2+mc26.2", "0.9.1+mc26.2",
            "1.11.4+mc26.2", "0.9.2+mc26.2");
    private static final Set<String> MALILIB = Set.of("0.29.2", "0.29.3", "0.29.4", "0.29.5", "0.29.6");

    public static String rejection(Map<String, String> installed, boolean physicalClient) {
        if (!physicalClient) return "Physical server";
        for (String id : PINNED.keySet().stream().sorted().toList()) {
            if (installed.get(id) == null) return "Missing " + id;
        }
        if (!"26.2".equals(installed.get("minecraft"))) return "Unsupported minecraft " + installed.get("minecraft");
        String iris = installed.get("iris");
        if (!IRIS_SODIUM.containsKey(iris)) return "Unsupported iris " + iris;
        String sodium = installed.get("sodium");
        if (!IRIS_SODIUM.get(iris).equals(sodium))
            return "Unsupported sodium " + sodium + " (Iris " + iris + " requires " + IRIS_SODIUM.get(iris) + ")";
        String litematica = installed.get("litematica");
        if (!MIN_MALILIB.containsKey(litematica)) return "Unsupported litematica " + litematica;
        String malilib = installed.get("malilib");
        if (!MALILIB.contains(malilib)) return "Unsupported malilib " + malilib;
        if (Integer.parseInt(malilib.substring("0.29.".length())) < MIN_MALILIB.get(litematica))
            return "Unsupported malilib " + malilib + " (Litematica " + litematica
                    + " requires >=0.29." + MIN_MALILIB.get(litematica) + ")";
        return "";
    }
}
