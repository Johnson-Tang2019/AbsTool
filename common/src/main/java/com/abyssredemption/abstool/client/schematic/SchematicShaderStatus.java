package com.abyssredemption.abstool.client.schematic;

import java.util.function.Supplier;

/** Safe to read on either loader without linking optional client mods. */
public final class SchematicShaderStatus {
    public enum State { OFF, MISSING_DEPENDENCY, SHADERS_OFF, UNSUPPORTED_LOADER, UNSUPPORTED_VERSION, UNSUPPORTED_BACKEND, ACTIVE, ERROR }
    public record Snapshot(State state, String detail, long draws) {}
    private static Supplier<Snapshot> provider = () -> new Snapshot(State.UNSUPPORTED_LOADER, "Fabric only; NeoForge tracking remains available", 0);
    public static void register(Supplier<Snapshot> value) { provider = value; }
    public static Snapshot get() { return provider.get(); }
}
