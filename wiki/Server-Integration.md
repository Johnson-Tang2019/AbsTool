# AbsServerTool integration

[简体中文](https://github.com/Johnson-Tang2019/AbsTool/wiki/Server-Integration.zh-CN) · [Home](https://github.com/Johnson-Tang2019/AbsTool/wiki/Home)

Ab's Tool stays client-side. Server-owned features are provided by the separate AbsServerTool project.

## Protocol model

Current client/server integration uses protocol version 2 with a Hello handshake and capability bits. The server advertises only the features it actually supports.

Current capability families include:

- Overview/statistics data.
- Daily and weekly trends.
- Leaderboards, including completed visible advancements on supported servers.
- Server-shared waypoints.
- Server performance profiler.

An older server can therefore keep serving the features it understands without pretending to support newer ones.

## Statistics

The Server Stats page uses AbsServerTool data when the compatible capability is present. The client clears session state when the connection changes and shows explicit unsupported, disconnected, mismatch, and timeout states.

## Performance profiler

The profiler is server-authoritative:

- AbsServerTool owns permission checks.
- Sampling occurs on the server only when explicitly started.
- Ab's Tool requests sessions/status/reports and renders the results.
- The client does not turn profiler output into a claim of exact JVM CPU attribution.

## Shared waypoints

AbsServerTool owns the waypoint dataset, revisions, mutation permissions, snapshots, and live deltas. Xaero is not needed on the server.

For Minecraft 26.3, current AbsServerTool 0.3.7 stores the dataset in the world save at:

~~~text
data/absservertool_shared_waypoints.json
~~~

Default management permission is operator level 2 and the default dataset limit is 256 waypoints.

## Furnace tracker is separate

The furnace tracker does not require AbsServerTool. It uses Servux when available and may fall back to Jade. Do not confuse those provider protocols with the AbsServerTool protocol-v2 statistics/waypoint channel.

## Companion project

https://github.com/Johnson-Tang2019/AbsServerTool
