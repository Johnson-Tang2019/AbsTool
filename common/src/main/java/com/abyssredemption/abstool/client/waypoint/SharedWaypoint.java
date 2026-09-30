package com.abyssredemption.abstool.client.waypoint;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

/** Loader-neutral representation of a server-owned waypoint. */
public record SharedWaypoint(UUID id, String name, String dimensionId, int x, int y, int z,
                             int color, String symbol, boolean enabled, UUID createdBy,
                             long createdAt, long updatedAt, long revision) {
    public boolean valid() {
        return id != null && createdBy != null && name != null && !name.isBlank()
                && name.length() <= 64 && symbol != null && symbol.length() <= 16
                && dimensionId != null && dimensionId.length() <= 128
                && Identifier.tryParse(dimensionId) != null && color >= 0 && color <= 15
                && revision > 0 && createdAt >= 0 && updatedAt >= createdAt;
    }

    public static void write(RegistryFriendlyByteBuf b, SharedWaypoint w) {
        b.writeUUID(w.id); b.writeUtf(w.name, 64); b.writeUtf(w.dimensionId, 128);
        b.writeInt(w.x); b.writeInt(w.y); b.writeInt(w.z); b.writeVarInt(w.color);
        b.writeUtf(w.symbol, 16); b.writeBoolean(w.enabled); b.writeUUID(w.createdBy);
        b.writeLong(w.createdAt); b.writeLong(w.updatedAt); b.writeVarLong(w.revision);
    }

    public static SharedWaypoint read(RegistryFriendlyByteBuf b) {
        return new SharedWaypoint(b.readUUID(), b.readUtf(64), b.readUtf(128), b.readInt(),
                b.readInt(), b.readInt(), b.readVarInt(), b.readUtf(16), b.readBoolean(),
                b.readUUID(), b.readLong(), b.readLong(), b.readVarLong());
    }
}
