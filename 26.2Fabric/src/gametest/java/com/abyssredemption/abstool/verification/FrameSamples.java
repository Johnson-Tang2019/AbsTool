package com.abyssredemption.abstool.verification;

/** Test-only CPU frame intervals, including pacing; these are not GPU timestamps. */
public final class FrameSamples {
    private static final double[] samples = new double[4096];
    private static int count;
    private static long previous;
    private static String label;
    public static void begin(String value) { label = value; count = 0; previous = 0; }
    public static void frame() {
        if (label == null) return;
        long now = System.nanoTime();
        if (previous != 0 && count < samples.length) samples[count++] = (now - previous) / 1_000_000.0;
        previous = now;
    }
    public static void end(net.minecraft.client.Minecraft client) {
        if (count < 20) throw new AssertionError("Not enough rendered frames for " + label);
        var data = java.util.Arrays.copyOf(samples, count);
        java.util.Arrays.sort(data);
        org.slf4j.LoggerFactory.getLogger("abstool-frame-test").info(
                "ABSTOOL_FRAME_SAMPLE phase={} frames={} medianMs={} p95Ms={} meanMs={} resolution={}x{} viewChunks={} cap={}",
                label, count, data[count / 2], data[(int)(count * 0.95)], java.util.Arrays.stream(data).average().orElseThrow(),
                client.getWindow().getWidth(), client.getWindow().getHeight(), client.options.renderDistance().get(), client.options.framerateLimit().get());
        label = null;
    }
}
