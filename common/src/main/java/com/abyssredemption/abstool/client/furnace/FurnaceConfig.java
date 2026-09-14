package com.abyssredemption.abstool.client.furnace;

public final class FurnaceConfig {
    public boolean enabled = false;
    public Range range = Range.BLOCKS_256;
    public int requestsPerSecond = 20;
    public int staleSeconds = 30;
    public boolean jadeFallback = true;
    public int jadeExtraRange = 21;
    public boolean tracers = true;
    public boolean labels = true;
    public boolean showStale = true;
    public int color = 0xFF4040;
    public int tracerColor = 0xFFB040;

    public void validate() {
        if (range == null) range = Range.BLOCKS_256;
        requestsPerSecond = Math.clamp(requestsPerSecond, 1, 40);
        staleSeconds = Math.clamp(staleSeconds, 5, 300);
        jadeExtraRange = Math.clamp(jadeExtraRange, 0, 1000);
        color &= 0xFFFFFF;
        tracerColor &= 0xFFFFFF;
    }

    public enum Range {
        BLOCKS_128(128), BLOCKS_256(256), ALL_LOADED(0);
        public final int blocks;
        Range(int blocks) { this.blocks = blocks; }
    }
}
