package com.abyssredemption.abstool.client.waypoint;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

/** Fields a client may submit; identity, author and timestamps remain server-owned. */
public record WaypointDraft(String name, String dimensionId, int x, int y, int z,
                            int color, String symbol, boolean enabled) {
    public boolean valid() {
        return name != null && !name.isBlank() && name.length() <= 64
                && dimensionId != null && dimensionId.length() <= 128
                && Identifier.tryParse(dimensionId) != null && color >= 0 && color <= 15
                && symbol != null && symbol.length() <= 16;
    }
    public static void write(RegistryFriendlyByteBuf b, WaypointDraft w) {
        b.writeUtf(w.name, 64); b.writeUtf(w.dimensionId, 128);
        b.writeInt(w.x); b.writeInt(w.y); b.writeInt(w.z);
        b.writeVarInt(w.color); b.writeUtf(w.symbol, 16); b.writeBoolean(w.enabled);
    }
    public static WaypointDraft read(RegistryFriendlyByteBuf b) {
        return new WaypointDraft(b.readUtf(64), b.readUtf(128), b.readInt(), b.readInt(),
                b.readInt(), b.readVarInt(), b.readUtf(16), b.readBoolean());
    }
}
