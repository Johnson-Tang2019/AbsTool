# Server-shared Xaero waypoints

[简体中文](https://github.com/Johnson-Tang2019/AbsTool/wiki/Shared-Waypoints.zh-CN) · [Home](https://github.com/Johnson-Tang2019/AbsTool/wiki/Home)

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

Beta.8 adds shared names to color categories. Administrators can use **Rename** beside a header; viewers can read the names. Names support up to 32 characters, with blank restoring the default. AbsServerTool 0.3.8 validates changes, persists them across restarts and synchronizes them to connected players. Older servers retain waypoint support with naming disabled. Category surfaces and the in-game settings backdrop are lighter and translucent. Shared Xaero display names receive a small server-rack icon; local waypoints and original server names remain unchanged.

Ab's Tool 0.5.0-beta.6 moves the Xaero implementation into shared dual-loader code.

Fabric and NeoForge support:

- Shared waypoint display.
- Waypoint-list management buttons.
- Server waypoint editing.
- Xaero World Map right-click publish/update/delete actions.
- The same server permission, capability, and snapshot gates on both loaders.

The integration no longer has a hard Xaero version ceiling. It checks the required interfaces before enabling itself.

Starting with 0.5.0-beta.7, the Shared Waypoints settings page lists all synchronized points directly below its status, with names, dimensions, and coordinates. Color groups can be folded individually or expanded/collapsed together, and the list has a refresh action. Confirmed color edits move into their new groups automatically.

Open Details or Edit to inspect the name, dimension ID, X/Y/Z, symbol, color, and visibility. Ordinary players receive read-only fields; administrators may save changes. Losing permission, disconnecting, or another server-side revision disables saving. Browsing this list does not require Xaero; displaying points on the map still uses the optional Xaero integration.

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

AbsServerTool 0.3.7 on Minecraft 26.3:

- Persists the shared dataset with the world.
- Uses server-authoritative revisions.
- Broadcasts live mutations.
- Defaults management permission to operator level 2.
- Defaults to 256 waypoints.
- Requires no Xaero installation on the dedicated server.

See the companion project for server configuration.
