package com.abyssredemption.abstool.client.waypoint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Authoritative in-memory dataset for one server connection. */
public final class SharedWaypointClientState {
    public enum ApplyResult { APPLIED, IGNORED, RESYNC }
    private final Map<UUID, SharedWaypoint> waypoints = new LinkedHashMap<>();
    private boolean supported;
    private boolean canManage;
    private boolean synced;
    private long datasetRevision;
    private long viewVersion;
    private long connectionId;
    public long connectionId() { return connectionId; }
    private boolean categoriesSynced;
    private long categoryRevision;
    private Map<Integer, String> categoryNames = Map.of();
    public boolean categoriesSynced() { return categoriesSynced; }
    public long categoryRevision() { return categoryRevision; }
    public String categoryName(int color) { return categoryNames.getOrDefault(color, ""); }
    public boolean categories(long revision, Map<Integer, String> names) {
        if (!supported || revision < 0 || (categoriesSynced && revision < categoryRevision) || names.size() > 16
                || names.entrySet().stream().anyMatch(e -> e.getKey() < 0 || e.getKey() > 15
                    || !com.abyssredemption.abstool.client.waypoint.network.WaypointCategories.validName(e.getValue()))) return false;
        categoriesSynced = true; categoryRevision = revision; categoryNames = Map.copyOf(names); viewVersion++; return true;
    }
    public boolean supported() { return supported; }
    public boolean canManage() { return canManage; }
    public boolean synced() { return synced; }
    public long datasetRevision() { return datasetRevision; }
    public long viewVersion() { return viewVersion; }
    public int waypointCount() { return waypoints.size(); }
    public Map<UUID, SharedWaypoint> waypoints() { return Map.copyOf(waypoints); }
    public void clear() { connectionId++; categoryNames = Map.of(); categoryRevision = 0; categoriesSynced = false; waypoints.clear(); supported = false; canManage = false; synced = false; datasetRevision = 0; viewVersion++; }
    public void hello(boolean available) { clear(); supported = available; }
    public void permissions(boolean allowed) { if (supported && canManage != allowed) { canManage = allowed; viewVersion++; } }

    public ApplyResult snapshot(long revision, List<SharedWaypoint> values) {
        if (!supported || revision < 0 || (synced && revision < datasetRevision) || values.size() > 4096) return ApplyResult.IGNORED;
        Map<UUID, SharedWaypoint> replacement = new LinkedHashMap<>();
        for (SharedWaypoint waypoint : values) {
            if (!waypoint.valid() || replacement.putIfAbsent(waypoint.id(), waypoint) != null) return ApplyResult.RESYNC;
        }
        waypoints.clear(); waypoints.putAll(replacement); datasetRevision = revision; synced = true; viewVersion++;
        return ApplyResult.APPLIED;
    }

    public ApplyResult delta(long revision, int operation, UUID id, long itemRevision, SharedWaypoint value) {
        if (!supported) return ApplyResult.IGNORED;
        if (!synced) return ApplyResult.RESYNC;
        if (revision <= datasetRevision) return ApplyResult.IGNORED;
        if (revision != datasetRevision + 1 || id == null || itemRevision < 1) return ApplyResult.RESYNC;
        SharedWaypoint old = waypoints.get(id);
        if (operation == 0 && old == null && value != null && value.valid()
                && value.id().equals(id) && value.revision() == itemRevision) waypoints.put(id, value);
        else if (operation == 1 && old != null && itemRevision > old.revision() && value != null
                && value.valid() && value.id().equals(id) && value.revision() == itemRevision) waypoints.put(id, value);
        else if (operation == 2 && old != null && itemRevision > old.revision() && value == null) waypoints.remove(id);
        else return ApplyResult.RESYNC;
        datasetRevision = revision;
        viewVersion++;
        return ApplyResult.APPLIED;
    }
}
