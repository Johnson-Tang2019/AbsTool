package com.abyssredemption.abstool.client.serverstats.network;

public final class ProtocolConstants {
    public static final int VERSION = 1;
    public static final int CAP_PLAYTIME = 1;
    public static final int CAP_DEATHS = 1 << 1;
    public static final int CAP_BLOCKS_TOTAL = 1 << 2;
    public static final int CAP_BLOCKS_WEEKLY = 1 << 3;
    private ProtocolConstants() {}
}
