// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.storage;

public class VaultRecord {
    public String server = "";
    public String dimension = "";
    public int x;
    public int y;
    public int z;
    public long excludedAtMillis;

    public VaultRecord() {
    }

    public VaultRecord(VaultKey key) {
        this.server = key.server();
        this.dimension = key.dimension();
        this.x = key.x();
        this.y = key.y();
        this.z = key.z();
    }

    public VaultKey key() {
        return new VaultKey(server, dimension, x, y, z);
    }

    public boolean excluded() {
        return excludedAtMillis > 0;
    }
}
