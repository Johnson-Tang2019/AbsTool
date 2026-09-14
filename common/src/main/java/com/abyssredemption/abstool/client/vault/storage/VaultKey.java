// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.storage;

import net.minecraft.core.BlockPos;

public record VaultKey(String server, String dimension, int x, int y, int z) {
    public static VaultKey of(String server, String dimension, BlockPos pos) {
        return new VaultKey(server, dimension, pos.getX(), pos.getY(), pos.getZ());
    }

    public String scopedKey() {
        return server + "|" + dimension + "|" + x + "|" + y + "|" + z;
    }

    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }
}
