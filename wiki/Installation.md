# Installation and versions

[简体中文](Installation.zh-CN.md) · [Home](Home.md)

## Choose the correct Minecraft line

Do not mix 26.2 and 26.3 JARs.

### Minecraft 26.2

Current stable line: Ab's Tool 0.4.12.

- Fabric and NeoForge builds are published separately.
- Java 25 is required by the development/build environment.
- Fabric Loader minimum: 0.19.3.
- Fabric API minimum: 0.159.0+26.2.
- Cloth Config: 26.2.155 or compatible.
- Mod Menu is optional on Fabric.

### Minecraft 26.3

Current beta line: Ab's Tool 0.5.0-beta.6.

- Fabric and NeoForge builds are published separately.
- Java 25.
- Fabric Loader 0.19.5.
- Fabric API 0.160.6+26.3.
- Cloth Config 26.3.158.
- NeoForge 26.3.0.23-beta is the current build baseline.
- 26.3 releases are prereleases while the NeoForge 26.3 toolchain remains beta.

## Optional integrations

Install only the integrations you use.

- Servux: preferred data source for the furnace tracker.
- Jade: optional furnace-data fallback.
- Litematica + MaLiLib + Iris + Sodium: optional Fabric schematic/shader compatibility.
- TweakerMore: optional shulker quick settings and mixed-shulker hint extension.
- Tweakeroo: optional Fabric elytra-equipment cooperation on supported versions.
- Xaero's Minimap / Xaero's World Map: optional server-shared waypoint display and management.
- AbsServerTool: optional server companion for statistics, advancements, performance profiling, and shared waypoints.

## Client/server placement

Ab's Tool is the client mod. Do not install it on the server just to get statistics or shared waypoints.

Use AbsServerTool on the server when you want the server-side features. Xaero is not required on a dedicated server.

## Local data separation

Minecraft 26.2 uses the original configuration files:

- config/abstool.json
- config/abstool-vaults.json

Minecraft 26.3 uses separate files:

- config/abstool-26.3.json
- config/abstool-26.3-vaults.json

This prevents the two game versions from overwriting each other's local state.
