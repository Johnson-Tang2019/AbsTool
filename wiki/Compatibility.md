# Compatibility matrix

[简体中文](Compatibility.zh-CN.md) · [Home](Home.md)

This page summarizes the current source-tree policy. It is not a promise that every theoretical combination has been manually played for hours.

## Core support

| Minecraft | Version | Fabric | NeoForge |
| --- | --- | --- | --- |
| 26.2 | 0.4.12 | Yes | Yes |
| 26.3 | 0.5.0-beta.6 | Yes | Yes, beta toolchain |

## Litematica / Iris compatibility

### Minecraft 26.2, Fabric

Audited Litematica / MaLiLib families:

| Litematica | MaLiLib |
| --- | --- |
| 0.28.3 / 0.28.4 | 0.29.2–0.29.6 |
| 0.28.5 | 0.29.4–0.29.6 |
| 0.28.6 / 0.28.8 | 0.29.5 / 0.29.6 |

Matching Iris / Sodium pairs:

- Iris 1.11.1 + Sodium 0.9.0
- Iris 1.11.2 + Sodium 0.9.1
- Iris 1.11.4 + Sodium 0.9.2

Complementary Reimagined r5.9.1 was used for the documented visual checks. This does not certify every shader pack.

NeoForge 26.2 does not enable this schematic shader adapter.

### Minecraft 26.3, Fabric

The 26.3 line uses interface/bytecode checks rather than only a static version list.

Current audited families include:

- Litematica 0.29.0–0.29.1.
- MaLiLib 0.30.0–0.30.2.
- Iris 1.11.5–1.11.7.

Later Iris releases can pass automatically if the required interfaces remain compatible. Incompatible targets disable the optional adapter.

## Xaero shared waypoints

See [Shared Waypoints](Shared-Waypoints.md) for the detailed version ranges.

At the latest 26.3 code:

- Fabric Minimap 26.5.1–26.6.0 and World Map 1.46.2–1.47.0 passed interface checks.
- NeoForge Minimap 26.5.2–26.6.0 and World Map 1.46.3–1.47.0 passed interface checks.
- There is no hard upper-version whitelist; the runtime contract decides whether the bridge activates.

## TweakerMore and Tweakeroo

The 26.2 source documents:

- TweakerMore 3.33.2 for the mixed-shulker renderer extension.
- Tweakeroo 0.29.5 for optional Fabric elytra equipment cooperation.

Both are optional. Missing optional mods must not prevent Ab's Tool from starting.

## AbsServerTool

Use the AbsServerTool JAR that matches the Minecraft version of the server.

The latest main branch is AbsServerTool 0.3.7 for Minecraft 26.3 and supports Fabric and NeoForge server builds. Minecraft 26.2 is maintained separately in its maintenance branch.
