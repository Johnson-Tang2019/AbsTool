package com.abyssredemption.abstool.client.waypoint.gui;

import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClientState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/** A live grouped view; folding never changes the server-owned dataset. */
public final class WaypointBrowserModel {
    public record Group(int color, List<SharedWaypoint> waypoints) {}
    public record Row(int color, SharedWaypoint waypoint, int count) { public boolean header() { return waypoint == null; } }
    private long version = Long.MIN_VALUE;
    private List<Group> groups = List.of();
    private List<Row> rows = List.of();
    private final Set<Integer> collapsed = new HashSet<>();
    public boolean refresh(SharedWaypointClientState state) {
        if (version == state.viewVersion()) return false;
        version = state.viewVersion();
        var colors = new TreeMap<Integer, List<SharedWaypoint>>();
        state.waypoints().values().forEach(w -> colors.computeIfAbsent(w.color(), ignored -> new ArrayList<>()).add(w));
        var order = Comparator.comparing(SharedWaypoint::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SharedWaypoint::dimensionId).thenComparing(w -> w.id().toString());
        groups = colors.entrySet().stream().map(entry -> {
            entry.getValue().sort(order);
            return new Group(entry.getKey(), List.copyOf(entry.getValue()));
        }).toList();
        rebuild();
        return true;
    }
    public List<Group> groups() { return groups; }
    public List<Row> rows() { return rows; }
    public boolean collapsed(int color) { return collapsed.contains(color); }
    public void toggle(int color) { if (!collapsed.remove(color)) collapsed.add(color); rebuild(); }
    public void foldAll(boolean fold) { collapsed.clear(); if (fold) groups.forEach(g -> collapsed.add(g.color())); rebuild(); }
    private void rebuild() {
        var result = new ArrayList<Row>();
        for (var group : groups) {
            result.add(new Row(group.color(), null, group.waypoints().size()));
            if (!collapsed(group.color())) group.waypoints().forEach(w -> result.add(new Row(group.color(), w, 0)));
        }
        rows = List.copyOf(result);
    }
}
