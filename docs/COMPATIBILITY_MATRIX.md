# Compatibility evidence — 0.2.2

The user confirmed the displayed in-game result. Support remains experimental
and limited to the audited Fabric/OpenGL combinations in SCHEMATIC_SHADER_COMPAT.md.

| Check | Result |
| --- | --- |
| Initial clean HEAD and dual-loader build | Passed at 9df1062; no reset |
| Existing vault game test and R+B | Passed initially; not repeated after user requested this |
| All / Tracking / Optimization actual settings screen | Captured in game; shared-editor assertions passed |
| Legacy configuration and unknown nested fields | Migration, backups, repeated saves and record preservation passed |
| Real generated brick projection, shaders off | Textured block visible |
| Same projection with shaders, adapter OFF | Selection visible; projected texture absent |
| Same projection with adapter AUTO | Texture and translucency visible; world shaders remain enabled |
| Bricks, glass, stained glass, pane, leaves, flowers/grass | Present in actual model-grid screenshot |
| Stairs, slab, door, trapdoor, fence, wall, static redstone | Present in model grid; all state variants are not certified |
| Upstream missing-block overlays / selection | Displayed with block meshes |
| Real stone wall in front of projection | Correctly occludes block meshes |
| Rotation + mirror, single layer, first/third person | Captured in game |
| Shader OFF/ON and adapter OFF/AUTO | Exercised in the same scene |
| Resource reload and window resize | Survived; screenshots captured with textures |
| Large 32 x 8 x 32 hollow-brick schematic | Visible; over 1,000 submitted indices required before sampling |
| Chest, sign and water | Included as exploratory fixtures; block-entity animation/text and fluid parity are not certified |
| Full dependencies with shaders disabled | Startup passed with SHADERS_OFF |
| No optional dependencies | Startup and settings passed |
| Sodium only / Litematica + MaLiLib / Iris + Sodium | Startup and settings passed; MISSING_DEPENDENCY reported |
| NeoForge settings | Startup passed; UNSUPPORTED_LOADER shown for projection adapter |
| NeoForge new projection feature | Unsupported by design; settings show that status |
| Vulkan, unpinned versions / altered dependency bytecode | Unsupported; version policy rejects unknown combinations |

The fixture captures actual Litematica schematics from a generated isolated
world and places them elsewhere. It never replaces a projection with a debug
box. Evidence is under `build/schematic-evidence/extended/`; the first three
images compare adapter OFF, adapter AUTO, and shaders OFF. Model, low-opacity,
overlay, wall, transformation, layer, third-person, reload, resize, settings, and
large-schematic images follow. The successful extended run is recorded in
`build/schematic-extended-world.log`.

## Measured frame intervals (original 0.2.0 pair)

Windows 11, Core i5-14600K, RTX 5070 Ti, driver 610.62, OpenGL,
Complementary Reimagined r5.9.1 default HIGH, view distance 5 chunks, cap 120 FPS.
Each sample spans 60 simulation ticks (about 3 seconds) after a settling period.
Intervals are measured between completed GameRenderer render calls, include
frame pacing, and are **not GPU execution times**. They cannot establish an FPS
improvement. The large case uses a different resolution and scene.

| Scene | Resolution | Frames | Median ms | p95 ms | Mean ms |
| --- | --- | ---: | ---: | ---: | ---: |
| No visible projection | 854x480 | 356 | 8.359 | 9.284 | 8.404 |
| One projected block | 854x480 | 348 | 8.364 | 9.253 | 8.480 |
| Model grid | 854x480 | 353 | 8.364 | 9.897 | 8.453 |
| Hollow-brick volume 8192 | 1024x640 | 352 | 8.380 | 9.272 | 8.407 |

## Limits not certified in this release

Other shader packs, every connecting/waterlogged model state, animated/modded
block entities, arbitrary mod renderers, large-coordinate and negative-origin
placement precision, view-bob/motion stress, subregion toggling, fullscreen,
dimension/rejoin stress, and long-duration memory/performance behavior remain
unverified. Physical dedicated-server startup was not rerun for this increment; physical-server/unknown-version gates
were verified in the isolated policy test. No combined tracking benchmark was run after the user asked to stop
retesting existing features. These are explicit coverage limits, not silently
hidden projection types. Existing Litematica features keep their upstream paths.

## Final packaging

`assemble :fabric:verifySchematic -PclientSmoke :neoforge:runClient` passed for
0.2.0. Both JARs were inspected for correct metadata, translation parity, the
retained MIT notice, absence of test classes and third-party mod classes, and
absence of the Fabric adapter in the NeoForge JAR. Exact checksums accompany the
GitHub Release as SHA256SUMS.txt.

## 0.2.1: Iris 1.11.2 / Sodium 0.9.1

The isolated client world test passed with the official Iris 1.11.2+mc26.2 and
Sodium 0.9.1+mc26.2 release JARs, Litematica 0.28.8, MaLiLib 0.29.6 and the same
Complementary Reimagined r5.9.1/OpenGL setup. Log:
`build/schematic-iris112-world.log`; 14 screenshots:
`build/schematic-evidence/iris112/`.

Visual inspection confirmed the OFF/AUTO comparison, actual textured translucent
block meshes, the model grid, wall occlusion, resource reload and the large
hollow-brick projection. The fixture also exercised shader toggling, opacity,
rotation/mirror, layers, third person and resize; final ACTIVE and submitted-index
assertions passed. This does not extend certification to other shader packs or
all model states. Existing tracking tests were not repeated.

Sodium 0.9.2 rejects Iris <=1.11.2 at Fabric resolution time; the supported old
pair therefore uses Sodium 0.9.1. Version-policy verification covers rejection
of that mismatched pair and an untested Iris version. The original pair remains
accepted. Both release loaders are assembled for 0.2.1.

## 0.2.2 version-family expansion

The policy enumerates 51 accepted combinations from five audited Litematica
releases, five MaLiLib releases with upstream minimums, and three fixed
Iris/Sodium pairs. The isolated policy check enumerates 420 combinations and
asserts both valid cases and rejected mismatches. Missing dependencies, physical
server, unknown versions, and unlisted Litematica 0.28.7 remain rejected.

Only boundary combinations receive new game sessions; byte-identical patched
classes and upstream metadata support the remaining combinations. **51 accepted
combinations does not mean 51 individually game-tested installations.**

The oldest boundary (Litematica 0.28.3 / MaLiLib 0.29.2 / Iris 1.11.1 / Sodium
0.9.0) passed the isolated real-world projection fixture. Actual textured
translucent projection, post-reload rendering and the large schematic were
visually inspected. Its earlier sampler implementation did not require a new
draw path. Evidence: `build/schematic-oldest-world.log` and
`build/schematic-evidence/oldest/`.

The hardware, OpenGL backend and Complementary Reimagined r5.9.1 shader pack
remain as recorded above. Existing vault/furnace feature tests were not rerun.
No claim is made for other Minecraft versions, Vulkan or every shader pack.

The early boundary (Litematica 0.28.4 / MaLiLib 0.29.3 / Iris 1.11.2 / Sodium
0.9.1) also passed. Textured translucent projection and the model scene after
resource reload were visually inspected. Evidence:
`build/schematic-early-world.log`, `build/schematic-evidence/early/`.

The middle boundary (Litematica 0.28.5 / MaLiLib 0.29.4 / Iris 1.11.4 / Sodium
0.9.2) passed with actual textured projection and post-reload model rendering
visually inspected. Evidence: `build/schematic-middle-world.log`,
`build/schematic-evidence/middle/`.

The recent boundary (Litematica 0.28.6 / MaLiLib 0.29.5 / Iris 1.11.4 / Sodium
0.9.2) passed; textured translucent projection and post-reload model rendering
were visually inspected. Evidence: `build/schematic-recent-world.log`,
`build/schematic-evidence/recent/`.

All four new boundary runs captured 14 screenshots each and completed their
shader/adapter switching, model grid, opacity, occlusion, transforms, layer,
third-person, resource reload, resize and large-schematic sequence. Final ACTIVE
state and submitted-index assertions passed. This coverage is specific to the
recorded shader pack and does not certify every model state or animation.
The original 0.28.8/0.29.6 combinations retain their previous release evidence.

Final 0.2.2 packaging: `assemble :fabric:verifySchematic` passed. JAR inspection
confirmed version metadata, matching translation keys, and no test classes or
bundled optional mod classes. The NeoForge JAR differs from 0.2.1 only in its
version metadata. SHA256SUMS-0.2.2.txt accompanies the release artifacts.
