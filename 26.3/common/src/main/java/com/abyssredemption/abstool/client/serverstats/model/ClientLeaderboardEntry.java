package com.abyssredemption.abstool.client.serverstats.model;
import java.util.UUID;
public record ClientLeaderboardEntry(int rank, UUID uuid, String playerName, long value, boolean online) {}
