package com.abyssredemption.abstool.client.schematic;

public final class SchematicShaderConfig {
    public Mode mode = Mode.AUTO;
    public double opacityMultiplier = 1.0;
    public boolean debug = false;

    public void validate() {
        if (mode == null) mode = Mode.AUTO;
        opacityMultiplier = Double.isFinite(opacityMultiplier) ? Math.clamp(opacityMultiplier, 0.0, 1.0) : 1.0;
    }

    public enum Mode { AUTO, OFF }
}
