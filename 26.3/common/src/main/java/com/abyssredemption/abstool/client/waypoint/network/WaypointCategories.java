package com.abyssredemption.abstool.client.waypoint.network;

import java.util.Map;
import java.util.TreeMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Optional additive channel; the existing waypoint protocol remains unchanged. */
public final class WaypointCategories {
    private WaypointCategories() {}
    public static boolean validName(String name) {
        return name != null && name.length() <= 32 && name.chars().noneMatch(c -> Character.isISOControl(c) || c == 0xA7);
    }
    public record Request(int version, long requestId, boolean edit, int color, long expectedRevision, String name) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(Identifier.fromNamespaceAndPath("absservertool", "waypoint_categories_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.of(
            (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.requestId); b.writeBoolean(p.edit); b.writeVarInt(p.color); b.writeVarLong(p.expectedRevision); b.writeUtf(p.name,32); },
            b -> new Request(b.readVarInt(),b.readVarLong(),b.readBoolean(),b.readVarInt(),b.readVarLong(),b.readUtf(32)));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Snapshot(int version, long requestId, int status, long revision, Map<Integer,String> names) implements CustomPacketPayload {
        public Snapshot { names = Map.copyOf(names); }
        public static final Type<Snapshot> TYPE = new Type<>(Identifier.fromNamespaceAndPath("absservertool", "waypoint_categories_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC = StreamCodec.of(
            (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.requestId); b.writeVarInt(p.status); b.writeVarLong(p.revision); b.writeVarInt(p.names.size());
                p.names.forEach((c,n) -> { b.writeVarInt(c); b.writeUtf(n,32); }); },
            b -> { int v=b.readVarInt(); long id=b.readVarLong(); int status=b.readVarInt(); long rev=b.readVarLong(); int count=b.readVarInt();
                if (count<0 || count>16 || rev<0) throw new IllegalArgumentException("Invalid category snapshot");
                Map<Integer,String> names=new TreeMap<>();
                for(int i=0;i<count;i++) { int c=b.readVarInt(); String n=b.readUtf(32);
                    if(c<0 || c>15 || !validName(n) || names.putIfAbsent(c,n)!=null) throw new IllegalArgumentException("Invalid category"); }
                return new Snapshot(v,id,status,rev,names); });
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
