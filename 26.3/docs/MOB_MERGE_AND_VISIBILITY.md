# Local mob merging and waypoint visibility

This development build is local only: 0.5.0-beta.9-dev.1 for Minecraft 26.3.

## Controls

- Open settings with the configured R+B shortcut. In **Server Shared Waypoints**, each color header has **Hide color / Show color**. All players can use it. This hides that color from their own Xaero Minimap and World Map, while preserving the list, server data and other players' views. The preference is saved separately for each server or singleplayer world.
- In **Optimization**, enable **Merge nearly overlapping mobs**. It defaults to off. Matching mobs whose interpolated collision boxes overlap by at least 98% share one representative model, with a native name tag such as `Cow x 16`. Turning it off restores normal rendering on the next frame.

## Boundaries

This only changes client rendering. It does not remove entities, change collisions, AI, hit targets, server ticks, or multiplayer data. Different entity types, synchronized appearance states, equipment, poses, teams and hurt states stay separate. Players, invisible/glowing mobs, riders, vehicles, leashed mobs and dying mobs are excluded. Grouping is rebuilt during each world extraction, using the same per-entity partial tick as vanilla, including frozen entities. Fixed representatives prevent a chain of nearby mobs from merging a whole crowd.

The render submission stage only consumes extracted scalar flags. Models and count labels use Minecraft's existing entity and text rendering pipelines. No shaderpack or Iris version ceiling is added. Counts use native depth-tested labels within the normal local label distance; the optimization currently considers mobs within 128 blocks of the player.

## Local verification

The Fabric real-client fixture creates an isolated disposable local test server/world and captures actual GPU frames at 1600x900. It checks 16 cows, 10 pigs and 8 sheep, restoring a separated cow, disabling the feature, per-color Xaero storage removal, unchanged authoritative waypoint counts and preference reload. It also captures the real settings controls. The shader run uses installed Iris 1.11.7, Sodium 0.9.2 and Complementary Reimagined 5.6.1 on the local graphics card. This is a specific tested shader combination, not a claim about all shaderpacks. A separate local server avoids the Fabric test harness's singleplayer-disconnect synchronization stall.

Both loader builds run the overlap-rule verification and compare the shared production code and mixin registrations. NeoForge build parity is separate from the Fabric in-game screenshots.

```powershell
./gradlew.bat :fabric:runClientGameTest -PworldTests -PmobMergeTest
./gradlew.bat :fabric:runClientGameTest -PworldTests -PmobMergeTest -PwithIrisTest
./gradlew.bat build
```

The optional fixture dependencies live under the ignored `build/reference/testmods` directory and never ship in the mod JARs. Runtime evidence and unedited screenshots are preserved under `build/render-preview`.
