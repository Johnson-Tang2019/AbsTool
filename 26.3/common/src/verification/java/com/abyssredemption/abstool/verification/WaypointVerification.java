package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClientState;
import java.util.List;
import java.util.UUID;

public final class WaypointVerification {
    private WaypointVerification() {}
    public static void main(String[] args) {
        var state=new SharedWaypointClientState();
        state.hello(true);
        var author=UUID.randomUUID();
        var first=new SharedWaypoint(UUID.randomUUID(),"Home","minecraft:overworld",1,64,2,3,"H",true,author,1,1,1);
        var sameName=new SharedWaypoint(UUID.randomUUID(),"Home","minecraft:the_nether",1,64,2,3,"H",true,author,1,1,1);
        check(state.snapshot(4,List.of(first,sameName))==SharedWaypointClientState.ApplyResult.APPLIED);
        check(state.waypoints().size()==2);
        check(state.delta(6,2,first.id(),2,null)==SharedWaypointClientState.ApplyResult.RESYNC);
        check(state.waypoints().size()==2 && state.datasetRevision()==4);
        var updated=new SharedWaypoint(first.id(),"New","minecraft:overworld",1,64,2,3,"N",true,author,1,2,2);
        check(state.delta(5,1,first.id(),2,updated)==SharedWaypointClientState.ApplyResult.APPLIED);
        check(state.delta(5,1,first.id(),2,updated)==SharedWaypointClientState.ApplyResult.IGNORED);
        check(state.delta(6,2,first.id(),3,null)==SharedWaypointClientState.ApplyResult.APPLIED);
        check(state.waypoints().size()==1 && state.waypoints().containsKey(sameName.id()));
        state.clear();
        check(!state.supported() && state.waypoints().isEmpty() && state.datasetRevision()==0);
        System.out.println("Shared waypoint revision and session verification passed.");
    }
    private static void check(boolean condition) { if(!condition) throw new AssertionError(); }
}
