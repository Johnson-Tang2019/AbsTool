# Server Shared Xaero Waypoints (client)

This document describes the AbsTool client implementation and the wire contract the companion AbsServerTool must implement. The server is the only authority. No waypoint is treated as shared when the Hello response lacks `CAP_SHARED_WAYPOINTS` (`1 << 8`). The existing Hello response binary layout and protocol version 2 remain unchanged.

## Wire contract

All payload identifiers use the `absservertool` namespace. Fields are encoded in the order shown by `WaypointPayloads` using Minecraft `RegistryFriendlyByteBuf`. All packets begin with protocol version 2. `shared_waypoint_sync_request` has a request ID. A matching `shared_waypoint_snapshot` has the request ID, dataset revision, and up to 4096 complete waypoints. `shared_waypoint_delta` has dataset revision, operation (0 add, 1 update, 2 delete), UUID, item revision, and a complete waypoint for add/update. Dataset revisions advance by one per mutation. Gaps or invalid deltas cause a new snapshot request.

The stable waypoint model contains UUID, name, registry dimension ID, XYZ, Xaero color index 0–15, symbol, enabled, creator UUID, creation/update epoch milliseconds, and item revision. Client create/update requests carry only the editable draft. Update/delete include expected item revision; the server must compare it and reject conflicts. The server assigns UUID, creator, timestamps, and new revisions. Mutation result status codes: 0 success, 1 permission denied, 2 not found, 3 revision conflict, 4 invalid data, 5 limit reached, 6 rate limited. The client never treats a request as committed before server confirmation and broadcast.

The server must send `shared_waypoint_permissions` with read/manage flags after Hello, enforce permission again for every write, persist the dataset with the world, send an authoritative snapshot on sync, and broadcast deltas only to clients accepting the payload. A server-side implementation is outside this client-only change; the capability must remain unset until that implementation exists.

## Xaero compatibility

Fabric supports the verified Xaero's Minimap 26.5.0 for Minecraft 26.2. The bridge uses its `ThirdPartyWaypointManager` and a dedicated `abstool:server_waypoints` origin; it does not modify local waypoint files. It reads the selected waypoint's `MinimapWorld.getDimId()` when publishing and refuses a missing dimension. It rebuilds the server collection from the authoritative client dataset after snapshots, deltas, dimension changes, and periodic reconciliation. Unknown Xaero versions are disabled. NeoForge registers the optional network protocol but currently has no Xaero display bridge.

## Limits

Only waypoints are synchronized; exploration data and map tiles are not. The client cannot provide cross-player behavior until AbsServerTool implements this matching capability. Xaero 26.5.0 GUI compatibility has been checked against the installed JAR's class signatures; in-game behavior still needs an integration check with the future server implementation.
