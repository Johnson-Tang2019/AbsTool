package com.abyssredemption.abstool.client.vault.client;

import java.util.List;

/** Immutable data passed from world extraction to the rendering pipeline. */
public record VaultFrame(List<Target> targets, boolean tracers, int tracerColor) {
    public static final VaultFrame EMPTY = new VaultFrame(List.of(), false, 0);

    public VaultFrame {
        targets = List.copyOf(targets);
    }

    public record Target(int x, int y, int z, int color) {
    }
}
