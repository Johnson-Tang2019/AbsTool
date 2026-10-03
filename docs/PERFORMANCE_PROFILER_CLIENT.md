# Performance profiler client (Minecraft 26.2)

This document describes the client implementation in Ab's Tool. The profiler runs on the server; the client only requests sessions and displays server results. Both NeoForge and Fabric clients register the same optional `absservertool` play payloads. This repository does not implement the server sampler.

## Compatibility

- Keep protocol version `2`.
- Advertise `CAP_PERFORMANCE_PROFILER = 1 << 9` in Hello. Bit 7 already means advancements and bit 8 already means shared waypoints in the deployed protocol. The attached development draft's bit 7 would collide with advancements.
- Open the screen from the existing Server Stats category only when Hello advertises bit 9 **and** the status-request channel is available. Otherwise its button is disabled.
- All payload identifiers use `absservertool:performance_` followed by the request/response name defined in `PerformancePayloads`. The server implementation must use exactly those identifiers, field order, integer encodings, string limits and page bounds. In particular, the chunk-detail response is `version`, `requestId`, `reportId`, then the full chunk entry, entity type table, and block-entity type table.
- Errors use the existing `absservertool:stats_error` response with codes 20–30. The client routes an error to the profiler only if its request ID belongs to a pending profiler request.

## Screen behavior

- Opening requests status. While the screen is open and the server reports `running`, the client polls status no more than once every two seconds. Closing the screen sends no stop request.
- Duration presets fill an editable seconds field. The server-provided minimum and maximum control start eligibility; maximum `0` means no upper bound. The accepted server duration is shown after StartResponse.
- The Overview, Chunks, Players, and Slow ticks tabs use the server report. List pages are fetched only when needed. Selecting a chunk fetches its detail, including per-type timing and **Tick Calls**.
- All measured category values come from the server. They may overlap and do not add up to total tick time. Player associations are correlations, not proof of lag responsibility. Save-overlapping slow ticks are marked.
- A disconnected or changed connection clears cached reports and pending requests. Responses are accepted only for the matching pending request and current report.

## Server integration checklist

1. Add the bit-9 Hello capability only when all performance channels and permissions are implemented.
2. Match `PerformancePayloads` and `PerformanceModels` exactly; send bounded pages and tables.
3. Recheck permissions for each start, stop and report request on the server.
4. Manually verify reconnect, denied permission, normal completion, early stop, all list pages, chunk detail, and small GUI resolutions against the actual server implementation. A successful client build cannot establish these runtime behaviors.
