# Development status

Version 0.2.4 adds a configurable most-common-item icon for mixed TweakerMore
shulker hints. Actual GUI rendering in an isolated world confirmed the replacement,
ties, single-type and empty cases; source fingerprints gate the optional hooks.

Version 0.2.3 adds conditional TweakerMore shulker-box Quick Settings on Fabric.
The original mod owns values, validation limits and persistence; absent-mod
installations keep the original three categories. See docs/TWEAKERMORE_QUICK_SETTINGS.md.

Version 0.2.2 expands the audited version policy to 51 valid dependency combinations.
Five Litematica releases and five MaLiLib releases are accepted subject to upstream
minimums, paired with Iris/Sodium 1.11.1/0.9.0, 1.11.2/0.9.1 or 1.11.4/0.9.2.
The existing draw path and exact target-class fingerprint checks are retained.
See the 0.2.2 section of docs/COMPATIBILITY_MATRIX.md for boundary game evidence;
the matrix is not a claim that all 51 combinations were individually game-tested.

Version 0.2.1 additionally supports Iris 1.11.2 with Sodium 0.9.1 on Fabric.
The isolated projection game test passed; actual textured/translucent meshes,
occlusion, resource reload and a large schematic were visually inspected.
Litematica 0.28.8 and MaLiLib 0.29.6 remain required.

Version 0.2.0 adds All / Tracking / Optimization settings tabs and a Fabric-only
experimental Litematica/Iris adapter. The pinned combination passed actual
textured and translucent projection checks with Complementary Reimagined r5.9.1,
including model coverage, occlusion, rotation/mirror, layers, third person,
resource reload, resize, and a larger built projection. The user confirmed the
visible result. Existing feature tests were not repeated after the user asked
to stop those repetitions. See docs/COMPATIBILITY_MATRIX.md for exact coverage
and limitations; this is not universal shader-pack or NeoForge projection support.
