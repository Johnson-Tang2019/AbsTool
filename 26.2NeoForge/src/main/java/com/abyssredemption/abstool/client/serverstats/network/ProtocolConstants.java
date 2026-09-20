package com.abyssredemption.abstool.client.serverstats.network;

public final class ProtocolConstants {
    public static final int VERSION = 2;
    public static final int CAP_OVERVIEW = 1;
    public static final int CAP_ACTIVITY = 1 << 1;
    public static final int CAP_PLAYTIME = 1 << 2;
    public static final int CAP_DEATHS = 1 << 3;
    public static final int CAP_PLACEMENTS = 1 << 4;
    public static final int CAP_DAILY_TREND = 1 << 5;
    public static final int CAP_WEEKLY_TREND = 1 << 6;
    public static final int MAX_ENTRIES = 100;
    public static final int MAX_DAILY_TREND = 90;
    public static final int MAX_WEEKLY_TREND = 26;
    private ProtocolConstants() {}
}
