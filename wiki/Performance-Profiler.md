# Server Performance Profiler GUI

[简体中文](Performance-Profiler.zh-CN.md) · [Home](Home.md)

The client `abstoolmod` (Ab's Tool) only controls and displays profiler sessions. Sampling, permissions and report authority remain on AbsServerTool.

## Requirements

The server must expose protocol v2 performance capability bit 9 and the player must pass the server-side administrator permission check.

## Starting a session

The editable duration field defaults to the value returned by the server, currently 30 seconds by default. Presets 10/30/60/120/300 only fill the field; arbitrary allowed integer durations such as 17 or 73 seconds are supported.

Closing the screen does not stop the server session.

## Report tabs

The current client provides:

1. Overview
2. Chunks
3. Player associations
4. Slow ticks

Chunk rows can open detail with instrumented category timings, tick-call counts, load/generation activity and top entity/block-entity types.

Slow ticks mark overlap with world saves.

## Interpretation

Player association is correlation with player load/simulation areas, not proof that a player caused lag.

Instrumented time is diagnostic coverage of selected Minecraft tick paths, not a complete JVM CPU flamegraph. Use spark/JFR when call-stack-level diagnosis is required.
