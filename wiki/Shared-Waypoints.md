# Server-shared Xaero waypoints

[简体中文](Shared-Waypoints.zh-CN.md) · [Home](Home.md)

Shared waypoints are server-owned. Ab's Tool integrates the synchronized dataset into Xaero on the client; AbsServerTool owns persistence, permissions, snapshots, revisions, and mutations.

Only waypoints are synchronized. Map tiles, explored regions, tracks, and other map data are not synchronized.

## Minecraft 26.2 stable line

Ab's Tool 0.4.12 contains the optional shared-waypoint protocol.

- Fabric has a verified Xaero's Minimap 26.5.0 display/management bridge.
- It uses Xaero's ThirdPartyWaypointManager and a dedicated abstool:server_waypoints origin.
- It does not edit the player's normal Xaero waypoint files.
- NeoForge 26.2 registers the optional protocol but does not provide the Xaero display bridge in this stable line.

A matching AbsServerTool server capability is required before the client enables shared-waypoint operations.

## Minecraft 26.3 beta line

Ab's Tool 0.5.0-beta.6 moves the Xaero implementation into shared dual-loader code.

Fabric and NeoForge support:

- Shared waypoint display.
- Waypoint-list management buttons.
- Server waypoint editing.
- Xaero World Map right-click publish/update/delete actions.
- The same server permission, capability, and snapshot gates on both loaders.

The integration no longer has a hard Xaero version ceiling. It checks the required interfaces before enabling itself.

Verified interface sets at the time of the latest code:

### Fabric

- Xaero's Minimap 26.5.1 through 26.6.0.
- Xaero's World Map 1.46.2 through 1.47.0.

### NeoForge

- Xaero's Minimap 26.5.2 through 26.6.0.
- Xaero's World Map 1.46.3 through 1.47.0.

Future versions may work when they preserve the required interfaces; incompatible interfaces disable the optional bridge instead of risking client startup.

## Administrator workflow

On a compatible server with management permission:

1. Select or right-click a local Xaero waypoint.
2. Choose Publish to server.
3. The server validates and creates the authoritative shared waypoint.
4. Connected clients receive the update.
5. Server-owned waypoints can later be updated or deleted through the provided Xaero actions.

Writes stay disabled until the server advertises the capability, grants permission, and an authoritative snapshot has arrived.

Ordinary players receive read-only shared waypoints.

## Server behavior

AbsServerTool 0.3.6 on Minecraft 26.3:

- Persists the shared dataset with the world.
- Uses server-authoritative revisions.
- Broadcasts live mutations.
- Defaults management permission to operator level 2.
- Defaults to 256 waypoints.
- Requires no Xaero installation on the dedicated server.

See the companion project for server configuration.
