package com.abyssredemption.abstool.client;

import java.util.concurrent.atomic.AtomicBoolean;

/** Bridges the Cloth Config category selection to the next client tick. */
public final class ServerStatsNavigation {
    private static final AtomicBoolean REQUESTED = new AtomicBoolean();
    private static volatile Runnable opener;

    private ServerStatsNavigation() {}

    public static void requestOpen() {
        REQUESTED.set(true);
    }

    public static void install(Runnable action) {
        opener = action;
    }

    public static boolean isAvailable() {
        return opener != null;
    }

    public static boolean tick() {
        if (!REQUESTED.getAndSet(false)) return false;
        Runnable action = opener;
        if (action == null) return false;
        action.run();
        return true;
    }

    public static void resetForVerification() {
        REQUESTED.set(false);
        opener = null;
    }
}
