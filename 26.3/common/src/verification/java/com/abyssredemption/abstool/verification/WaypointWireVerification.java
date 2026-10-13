package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.WaypointDraft;
import com.abyssredemption.abstool.client.waypoint.network.WaypointPayloads;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Independent fixtures matching AbsServerTool 0.3.6, not client-codec round trips. */
public final class WaypointWireVerification {
    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }
    public static void run() {
        UUID id=UUID.randomUUID(), author=UUID.randomUUID();
        var draft=new WaypointDraft("Home","minecraft:the_nether",-349,79,756,15,"H",true);
        check(WaypointPayloads.MutationRequest.TYPE.id().toString().equals("absservertool:shared_waypoint_mutation_request"));
        for (int operation=1; operation<=3; operation++) {
            var b=buffer();
            try {
                var body=operation==3?new WaypointDraft("","",0,0,0,0,"",false):draft;
                WaypointPayloads.MutationRequest.CODEC.encode(b,new WaypointPayloads.MutationRequest(2,41,operation,operation==1?null:id,operation==1?0:7,body));
                check(b.readVarInt()==2 && b.readVarLong()==41 && b.readVarInt()==operation);
                boolean present=b.readBoolean(); check(present==(operation!=1));
                if (present) check(b.readUUID().equals(id));
                check(b.readVarLong()==(operation==1?0:7));
                check(b.readUtf(64).equals(body.name()) && b.readUtf(128).equals(body.dimensionId()));
                check(b.readInt()==body.x() && b.readInt()==body.y() && b.readInt()==body.z() && b.readInt()==body.color());
                check(b.readUtf(8).equals(body.symbol()) && b.readBoolean()==body.enabled() && !b.isReadable());
            } finally { b.release(); }
        }
        var waypoint=new SharedWaypoint(id,"Home","minecraft:the_nether",-349,79,756,15,"H",true,author,1000,2000,7);
        var b=buffer();
        try {
            b.writeVarInt(2); b.writeVarLong(41); b.writeVarLong(9); b.writeVarInt(1); writeServerWaypoint(b,waypoint);
            var snapshot=WaypointPayloads.Snapshot.CODEC.decode(b);
            check(snapshot.waypoints().equals(java.util.List.of(waypoint)) && !b.isReadable());
        } finally { b.release(); }
        for (int operation=1; operation<=3; operation++) {
            b=buffer();
            try {
                b.writeVarInt(2); b.writeVarLong(10); b.writeVarInt(operation); b.writeUUID(id); b.writeVarLong(7);
                b.writeBoolean(operation!=3); if (operation!=3) writeServerWaypoint(b,waypoint);
                var delta=WaypointPayloads.Delta.CODEC.decode(b);
                check(delta.operation()==operation-1 && java.util.Objects.equals(delta.waypoint(),operation==3?null:waypoint) && !b.isReadable());
            } finally { b.release(); }
        }
        b=buffer();
        try {
            b.writeVarInt(2); b.writeVarLong(41); b.writeVarInt(0); b.writeBoolean(true); b.writeUUID(id); b.writeVarLong(7); b.writeVarLong(10);
            check(WaypointPayloads.MutationResult.CODEC.decode(b).datasetRevision()==10 && !b.isReadable());
        } finally { b.release(); }
        b=buffer();
        try {
            b.writeVarInt(2); b.writeVarLong(41); b.writeVarInt(3); b.writeUtf("Storage failed",128);
            check(WaypointPayloads.Error.CODEC.decode(b).message().equals("Storage failed") && !b.isReadable());
        } finally { b.release(); }
        System.out.println("AbsServerTool 0.3.6 waypoint wire fixtures passed.");
    }
    private static void writeServerWaypoint(RegistryFriendlyByteBuf b, SharedWaypoint w) {
        b.writeUUID(w.id()); b.writeUtf(w.name(),64); b.writeUtf(w.dimensionId(),128);
        b.writeInt(w.x()); b.writeInt(w.y()); b.writeInt(w.z()); b.writeInt(w.color());
        b.writeUtf(w.symbol(),8); b.writeBoolean(w.enabled()); b.writeUUID(w.createdBy());
        b.writeLong(w.createdAt()); b.writeLong(w.updatedAt()); b.writeVarLong(w.revision());
    }
    private static void check(boolean value) { if (!value) throw new AssertionError("Waypoint wire mismatch"); }
}
