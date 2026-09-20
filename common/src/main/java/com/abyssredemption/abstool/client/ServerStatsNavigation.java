package com.abyssredemption.abstool.client;

import java.util.concurrent.atomic.AtomicBoolean;

/** Bridges the Cloth Config category selection to the next client tick. */
public final class ServerStatsNavigation {
    private static final AtomicBoolean REQUESTED = new AtomicBoolean();

    private ServerStatsNavigation() {}

    public static void requestOpen() {
        REQUESTED.set(true);
    }

    public static boolean consumeOpenRequest() {
        return REQUESTED.getAndSet(false);
    }
}
