package com.abyssredemption.abstool.client.serverstats.network;

import java.util.Arrays;
import java.util.Optional;

public enum LeaderboardType {
    PLAYTIME(0), DEATHS(1), BLOCKS_TOTAL(2), BLOCKS_WEEKLY(3);
    private final int id;
    LeaderboardType(int id) { this.id = id; }
    public int networkId() { return id; }
    public static Optional<LeaderboardType> fromNetworkId(int id) {
        return Arrays.stream(values()).filter(v -> v.id == id).findFirst();
    }
}
