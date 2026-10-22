package com.abyssredemption.abstool.client.mobmerge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.AABB;

/** Stable representatives, spatial buckets, and an explicit near-total box-overlap rule. */
public final class OverlapGroups {
    public record Candidate(int id, Object appearance, AABB box) {}
    public record Member(int representative, int count) {}
    private record Cell(int x, int y, int z, Object appearance) {}
    private static final class Group {
        final Candidate anchor;
        final List<Integer> ids = new ArrayList<>();
        Group(Candidate anchor) { this.anchor=anchor; ids.add(anchor.id()); }
    }
    private OverlapGroups() {}
    public static boolean overlaps(AABB a, AABB b) {
        double av=a.getXsize()*a.getYsize()*a.getZsize(), bv=b.getXsize()*b.getYsize()*b.getZsize();
        if (!(av>0 && bv>0) || !Double.isFinite(av+bv)) return false;
        if (Math.abs(a.getCenter().x-b.getCenter().x)>0.125 || Math.abs(a.getCenter().y-b.getCenter().y)>0.125
                || Math.abs(a.getCenter().z-b.getCenter().z)>0.125) return false;
        double intersection=Math.max(0,Math.min(a.maxX,b.maxX)-Math.max(a.minX,b.minX))
                *Math.max(0,Math.min(a.maxY,b.maxY)-Math.max(a.minY,b.minY))
                *Math.max(0,Math.min(a.maxZ,b.maxZ)-Math.max(a.minZ,b.minZ));
        return intersection/Math.max(av,bv)>=0.98;
    }
    public static Map<Integer,Member> build(List<Candidate> candidates) {
        var buckets=new HashMap<Cell,List<Group>>();
        var groups=new ArrayList<Group>();
        for(var candidate:candidates.stream().sorted(Comparator.comparingInt(Candidate::id)).toList()) {
            var center=candidate.box().getCenter();
            int x=(int)Math.floor(center.x),y=(int)Math.floor(center.y),z=(int)Math.floor(center.z);
            Group found=null;
            search: for(int dx=-1;dx<=1;dx++) for(int dy=-1;dy<=1;dy++) for(int dz=-1;dz<=1;dz++) {
                for(var group:buckets.getOrDefault(new Cell(x+dx,y+dy,z+dz,candidate.appearance()),List.of())) {
                    // Compare against the fixed anchor, never a transitive chain of neighbors.
                    if(overlaps(candidate.box(),group.anchor.box())) { found=group; break search; }
                }
            }
            if(found!=null) found.ids.add(candidate.id());
            else {
                var group=new Group(candidate); groups.add(group);
                buckets.computeIfAbsent(new Cell(x,y,z,candidate.appearance()),ignored->new ArrayList<>()).add(group);
            }
        }
        var result=new HashMap<Integer,Member>();
        for(var group:groups) if(group.ids.size()>1) for(int id:group.ids)
            result.put(id,new Member(group.anchor.id(),group.ids.size()));
        return Map.copyOf(result);
    }
}
