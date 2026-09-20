# Server statistics 0.4.9 validation

Validated on 2026-09-20 with Java 25, Minecraft 26.2, Fabric Loader 0.19.5,
Fabric API 0.159.0+26.2, NeoForge 26.2.0.77 and Cloth Config 26.2.155.

## Regression and fix

0.4.8 installed a statistics opener only on NeoForge and hid its category when
that opener was absent. The Fabric JAR contained no statistics client. Older
versions depended on rendering an invisible entry to trigger a second screen.

Statistics state, codecs and the inline category entry now live in common.
Both loaders register the same protocol through their own transport adapters.
The category is always available and renders status/data directly; it does not
replace the settings screen or require another opening action.

## Executed checks

- `gradlew.bat :fabric:runClientGameTest -PworldTests -PstatsTests --offline`
  passed with `ABSTOOL_STATS_WORLD_PASS`. A real client clicks the category
  through screen mouse events, checks an unsupported integrated server, then
  exchanges hello/overview/leaderboard/trend packets with a test-only server
  fixture. It verifies switching away and back, layout coordinates and clearing
  statistics after disconnect.
- `gradlew.bat build :neoforge:runClient -PclientSmoke --offline` passed.
  Existing configuration/storage checks passed. The NeoForge client clicked
  the statistics tab, retained the settings screen, and emitted
  `ABSTOOL_SETTINGS_SMOKE_PASS`.
- Visually inspected Fabric overview and leaderboard screenshots and NeoForge
  disconnected-state screenshot. This caught and corrected the Cloth Config
  entry parameter order before release.
- Both release JARs contain `ServerStatsEntry` and statistics payload classes.
  Neither contains smoke-test or verification classes.

Local screenshots are under
`26.2Fabric/build/run/clientGameTest/screenshots/` and
`26.2NeoForge/run/screenshots/abstool-stats-smoke.png`.

## Scope

Fabric protocol validation uses a deterministic test fixture, not the user's
remote server. NeoForge coverage is the actual settings UI and build, not a
live remote statistics exchange. A server must provide a compatible
AbsServerTool protocol-v2 endpoint to supply statistics; otherwise the page
shows the unsupported-interface status. Test data is never packaged.
