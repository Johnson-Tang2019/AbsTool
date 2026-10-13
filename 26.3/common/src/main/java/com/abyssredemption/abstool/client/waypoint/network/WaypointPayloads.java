package com.abyssredemption.abstool.client.waypoint.network;

import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.WaypointDraft;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Wire contract for the optional AbsServerTool shared-waypoint capability. */
public final class WaypointPayloads {
    private WaypointPayloads() {}
    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("absservertool", path));
    }
    public record SyncRequest(int version, long requestId) implements CustomPacketPayload {
        public static final Type<SyncRequest> TYPE = payloadType("shared_waypoint_sync_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncRequest> CODEC = StreamCodec.of(
                (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.requestId); },
                b -> new SyncRequest(b.readVarInt(), b.readVarLong()));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    /** Server operations: 1 create, 2 update, 3 delete. Delete still carries an empty draft. */
    public record MutationRequest(int version, long requestId, int operation, UUID id, long expectedRevision, WaypointDraft draft) implements CustomPacketPayload {
        public static final Type<MutationRequest> TYPE = payloadType("shared_waypoint_mutation_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, MutationRequest> CODEC = StreamCodec.of(
                (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.requestId); b.writeVarInt(p.operation);
                    b.writeBoolean(p.id != null); if (p.id != null) b.writeUUID(p.id);
                    b.writeVarLong(p.expectedRevision); WaypointDraft.write(b,p.draft); },
                b -> { int v=b.readVarInt(); long request=b.readVarLong(); int op=b.readVarInt();
                    UUID id=b.readBoolean()?b.readUUID():null;
                    return new MutationRequest(v,request,op,id,b.readVarLong(),WaypointDraft.read(b)); });
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Snapshot(int version, long requestId, long datasetRevision, List<SharedWaypoint> waypoints) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE = payloadType("shared_waypoint_snapshot");
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC = StreamCodec.of(
                (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.requestId); b.writeVarLong(p.datasetRevision); b.writeVarInt(p.waypoints.size()); for (var w:p.waypoints) SharedWaypoint.write(b,w); },
                b -> { int version=b.readVarInt(); long id=b.readVarLong(); long rev=b.readVarLong(); int n=b.readVarInt();
                    if(n<0||n>4096) throw new IllegalArgumentException("Invalid waypoint count: "+n);
                    List<SharedWaypoint> list=new ArrayList<>(n); for(int i=0;i<n;i++) list.add(SharedWaypoint.read(b));
                    return new Snapshot(version,id,rev,List.copyOf(list)); });
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    /** operation: 0 add, 1 update, 2 delete. */
    public record Delta(int version, long datasetRevision, int operation, UUID id, long revision, SharedWaypoint waypoint) implements CustomPacketPayload {
        public static final Type<Delta> TYPE = payloadType("shared_waypoint_delta");
        public static final StreamCodec<RegistryFriendlyByteBuf, Delta> CODEC = StreamCodec.of(
                (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.datasetRevision); b.writeVarInt(p.operation+1); b.writeUUID(p.id); b.writeVarLong(p.revision);
                    b.writeBoolean(p.waypoint!=null); if(p.waypoint!=null) SharedWaypoint.write(b,p.waypoint); },
                b -> { int v=b.readVarInt(); long d=b.readVarLong(); int op=b.readVarInt()-1; UUID id=b.readUUID(); long r=b.readVarLong();
                    if(op<0||op>2) throw new IllegalArgumentException("Invalid delta operation: "+op);
                    return new Delta(v,d,op,id,r,b.readBoolean()?SharedWaypoint.read(b):null); });
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Permissions(int version, boolean canRead, boolean canManage) implements CustomPacketPayload {
        public static final Type<Permissions> TYPE = payloadType("shared_waypoint_permissions");
        public static final StreamCodec<RegistryFriendlyByteBuf, Permissions> CODEC = StreamCodec.of(
                (b,p) -> { b.writeVarInt(p.version); b.writeBoolean(p.canRead); b.writeBoolean(p.canManage); },
                b -> new Permissions(b.readVarInt(),b.readBoolean(),b.readBoolean()));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    /** status: 0 success, 1 permission denied, 2 not found, 3 revision conflict, 4 invalid, 5 limit, 6 rate limit. */
    public record MutationResult(int version, long requestId, int status, UUID waypointId, long revision, long datasetRevision) implements CustomPacketPayload {
        public static final Type<MutationResult> TYPE = payloadType("shared_waypoint_mutation_result");
        public static final StreamCodec<RegistryFriendlyByteBuf, MutationResult> CODEC = StreamCodec.of(
                (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.requestId); b.writeVarInt(p.status); b.writeBoolean(p.waypointId!=null); if(p.waypointId!=null)b.writeUUID(p.waypointId); b.writeVarLong(p.revision); b.writeVarLong(p.datasetRevision); },
                b -> { int v=b.readVarInt(); long r=b.readVarLong(); int s=b.readVarInt(); UUID id=b.readBoolean()?b.readUUID():null; return new MutationResult(v,r,s,id,b.readVarLong(),b.readVarLong()); });
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Error(int version, long requestId, int code, String message) implements CustomPacketPayload {
        public static final Type<Error> TYPE = payloadType("shared_waypoint_error");
        public static final StreamCodec<RegistryFriendlyByteBuf, Error> CODEC = StreamCodec.of(
                (b,p) -> { b.writeVarInt(p.version); b.writeVarLong(p.requestId); b.writeVarInt(p.code); b.writeUtf(p.message,128); },
                b -> new Error(b.readVarInt(),b.readVarLong(),b.readVarInt(),b.readUtf(128)));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
