package com.abyssredemption.abstool.client.serverstats.network;

import java.util.Arrays;
import java.util.Optional;

public enum LeaderboardType {
    TOTAL_PLAYTIME(0), TODAY_PLAYTIME(1), WEEK_PLAYTIME(2),
    TOTAL_DEATHS(10), TODAY_DEATHS(11), WEEK_DEATHS(12),
    TOTAL_PLACEMENTS(20), TODAY_PLACEMENTS(21), WEEK_PLACEMENTS(22);
    private final int id;
    LeaderboardType(int id) { this.id = id; }
    public int networkId() { return id; }
    public static Optional<LeaderboardType> fromNetworkId(int id) {
        return Arrays.stream(values()).filter(v -> v.id == id).findFirst();
    }
}
