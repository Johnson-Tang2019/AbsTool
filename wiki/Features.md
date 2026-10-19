# Features

Minecraft 26.3 beta.7 adds automatic screenshot image copying to the system clipboard on both loaders, plus a color-grouped, collapsible shared waypoint browser in settings. Viewers can inspect every point; administrators can edit coordinates, colors, and other fields. See [configuration](Configuration.md) and [shared waypoints](Shared-Waypoints.md).

[简体中文](Features.zh-CN.md) · [Home](Home.md)

## Ominous vault tracker

- Highlights loaded ominous vaults through blocks while ignoring normal vaults.
- A right-click can mark a vault as handled locally without consuming the normal interaction.
- Handled vaults can be hidden or shown in another color.
- Records are isolated by server/world and dimension.
- Render range, colors, tracers, item gating, and refresh rules are configurable.
- The tracker only inspects chunks already received by the client.

## Furnace input blockage tracker

- Discovers furnaces, blast furnaces, and smokers in loaded chunks.
- Marks a furnace blocked only when a non-empty input is not accepted by the synchronized recipe/input information.
- Missing fuel, a full output slot, or normal cooking are not treated as blockage.
- Confirmed blockage can render a box, tracer, and item name.
- Stale or timed-out results remain explicitly stale instead of being reported as current.
- Servux is preferred; Jade is an optional fallback.

## Server Stats

With a compatible AbsServerTool protocol-v2 server, the Server Stats tab can show:

- Overview statistics.
- Playtime.
- Block-placement statistics and ranges.
- Deaths.
- Daily/weekly trends.
- Completed visible-advancement counts when the server advertises that capability.

Unsupported or disconnected servers show explicit status instead of stale data.

## Server performance profiler client

When AbsServerTool advertises the profiler capability, Ab's Tool can request and display server-owned profiling sessions and reports. Sampling, permissions, and report authority stay on the server.

## Low-ceiling elytra quick launch

Experimental and off by default.

The feature can equip a usable glider from the normal inventory, issue a normal jump, wait for the real airborne state, and send one glide request. The server still decides whether fall-flying starts.

There is no automatic firework use, no extra propulsion, no forced camera movement, and no fabricated movement state.

## Litematica + Iris schematic compatibility

Fabric-only experimental rendering adapter.

It keeps the shader pack enabled while preserving Litematica's existing block meshes, textures, translucency, placement transforms, layer selection, and world depth. Unsupported dependency combinations are gated rather than guessed.

See [Compatibility](Compatibility.md) for version coverage.

## TweakerMore integration

On supported Fabric setups:

- Quick Settings exposes applicable TweakerMore shulker settings inside Ab's Tool.
- Mixed shulker boxes can show the most common item icon instead of the generic dots.
- The original TweakerMore config remains the source of truth.

## Server-shared Xaero waypoints

A compatible AbsServerTool server owns the shared waypoint dataset. Administrators can publish, update, and delete server waypoints; ordinary players receive read-only synchronized waypoints.

The 26.3 line supports both Fabric and NeoForge Xaero integration. The 26.2 stable line has more limited loader support. See [Shared Waypoints](Shared-Waypoints.md).
