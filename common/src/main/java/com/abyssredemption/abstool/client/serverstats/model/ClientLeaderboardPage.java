package com.abyssredemption.abstool.client.serverstats.model;
import com.abyssredemption.abstool.client.serverstats.network.LeaderboardType;
import java.util.List;
public record ClientLeaderboardPage(LeaderboardType type, int page, int totalPages, int totalEntries, long generatedAtEpochMillis, String contextLabel, List<ClientLeaderboardEntry> entries) {}
