# Projection dependency source audit — 2026-09-15

Scope: Minecraft 26.2 Fabric **stable releases currently listed by Modrinth**.
AbsTool HEAD: 443d9c5; clean tree at audit start. No compatibility gates changed.
This is source and release-bytecode evidence, not new game-test certification.
The existing 0.2.1 runtime coverage remains unchanged.

## Method and source anchors

Fetched the upstream history without changing the reference working trees.
Downloaded all 19 listed releases and verified each against its Modrinth SHA-512.
Read `fabric.mod.json` from the actual JAR, and compared SHA-256 hashes of the
classes used by the adapter. Full hashes and dependency clauses are in
[SCHEMATIC_VERSION_AUDIT.json](SCHEMATIC_VERSION_AUDIT.json).

Source anchors are version-bearing commits on the correct Minecraft branch,
not a claim that every version bump commit is the exact published build commit.
Release binaries are authoritative for the class comparisons. Iris merges 26.1
history into 26.2: using a version bump from the wrong parent produces a false
Minecraft API difference. This audit uses 26.2 first-parent anchors.

- Iris: [1.11.0 anchor 24711ad](https://github.com/IrisShaders/Iris/commit/24711ad),
  [1.11.1 anchor 52a6910](https://github.com/IrisShaders/Iris/commit/52a6910),
  [1.11.2 anchor 20e226b](https://github.com/IrisShaders/Iris/commit/20e226b),
  [1.11.4 reference f985448](https://github.com/IrisShaders/Iris/commit/f985448).
- Litematica: [0.28.3 anchor 4eb3f19](https://github.com/sakura-ryoko/litematica/commit/4eb3f19),
  [sampler change b42d48f](https://github.com/sakura-ryoko/litematica/commit/b42d48f),
  [0.28.8 anchor da80a4e](https://github.com/sakura-ryoko/litematica/commit/da80a4e).
- MaLiLib: [0.29.0 anchor 96ddb43](https://github.com/sakura-ryoko/malilib/commit/96ddb43),
  [0.29.6 anchor 5c028ed](https://github.com/sakura-ryoko/malilib/commit/5c028ed).
- Sodium: [0.9.1 anchor 2cfb93d](https://github.com/CaffeineMC/sodium/commit/2cfb93d),
  [0.9.2 anchor 6c26e7b](https://github.com/CaffeineMC/sodium/commit/6c26e7b).

The stable version list came from `/v2/project/{project}/version` filtered to
Minecraft 26.2 and Fabric, then `version_type == release`. A missing version in
this list is not evidence that it never existed on another distribution channel.

## Litematica

| Published version | Actual MaLiLib dependency | Adapter-target class group |
| --- | --- | --- |
| 0.28.3 | >=0.29.2- <0.30.0- | Older WorldRendererSchematic sampler handling |
| 0.28.4 | >=0.29.2- <0.30.0- | Same as 0.28.8 |
| 0.28.5 | >=0.29.4- <0.30.0- | Same as 0.28.8 |
| 0.28.6 | >=0.29.5- <0.30.0- | Same as 0.28.8 |
| 0.28.8 | >=0.29.5- <0.30.0- | Current tested version |

`ChunkRenderBatchDraw` and `BufferBuilderCache` are byte-identical across all five.
`WorldRendererSchematic` is byte-identical across 0.28.4/5/6/8. These are the three
classes pinned by AbsTool. The interface `IWorldSchematicRenderer` also matches
across those four versions.

The concrete 0.28.3 -> 0.28.4 change removes `setGpuSampler(GpuSampler)` from
`IWorldSchematicRenderer`. The old rendering code could retain a supplied vanilla
sampler; the new code always obtains its own sampler with `getGpuSampler()`.
This is a resource ownership/lifecycle difference, not a different block mesh
format. 0.28.3 needs separate reload/filtering validation before enabling it.

## MaLiLib

All seven versions 0.29.0 through 0.29.6 have identical `MaLiLibPipelines` and
`IrisCompat` class bytes. The pipeline class source also has no diff between the
0.29.0 and 0.29.6 anchors. This supports sharing the adapter's pipeline assumptions,
not treating the entire library as interchangeable.

Other subsystems changed: outline precision, UI components, inventory data, and
networking. In particular [bf3b880](https://github.com/sakura-ryoko/malilib/commit/bf3b880)
introduces explicit client protocol registration and compressed data packets with
an upstream-declared network compatibility break. Respect each Litematica release's
minimum MaLiLib version even though the rendering class hashes match.

Every inspected MaLiLib JAR declares `breaks.iris = <1.11.1-`. Consequently Iris
1.11.0 is not a valid full-stack candidate for this adapter.

## Iris and Sodium

| Iris stable release | Official Modrinth required Sodium release | Assessment |
| --- | --- | --- |
| 1.11.0 | 0.9.0 | Excluded by inspected MaLiLib versions |
| 1.11.1 | 0.9.0 | Candidate for additional runtime validation |
| 1.11.2 | 0.9.1 | Already tested in AbsTool 0.2.1 |
| 1.11.4 | 0.9.2 | Already tested in AbsTool 0.2.0/0.2.1 |

All versions here carry the Minecraft 26.2 suffix. The official pair mapping is
from the Modrinth release dependencies, not inferred from the loose `0.9.x`
dependency inside Iris. Sodium 0.9.0 excludes Iris <=1.10.8; 0.9.1 excludes
Iris <=1.11.1; 0.9.2 excludes Iris <=1.11.2. Passing a loader constraint alone
does not establish that another cross-pair works.

Concrete source and binary differences:

- Iris 1.11.0/1.11.1/1.11.2 have identical `IrisRenderingPipeline`,
  `MixinRenderPipeline`, `ImmediateState`, and `MixinBufferBuilder` release bytes.
  They do **not** have identical full renderers: 1.11.0 -> 1.11.1 changes reverse-Z
  and RenderType identity handling outside those target classes.
- Iris 1.11.1 -> 1.11.2 changes the GLYPH attribute order from Position/Color/UV0/UV2
  to Position/UV0/UV2/Color. Its Sodium uniform hook changes from `MappableRingBuffer`
  to `DynamicUniformStorage` plus `GpuBufferSlice`; it also adds a shadow hook for
  the `addMainPass` signature carrying a matrix. These are real integration changes
  despite identical adapter-target hashes.
- Iris 1.11.2 -> 1.11.4 extends glyph handling to `TEXT_SEE_THROUGH`, adds texture
  override data to compute transformation, removes the begin-level clip-control
  reset and changes the shadow depth clear value from 0 to 1. `beginLevelRendering`
  and `finalizeLevelRendering` remain available; the final composition hook used
  by AbsTool remains in place. `ImmediateState` and `MixinBufferBuilder` still match.
- Sodium 0.9.2 replaces terrain allocation with a multi-arena allocation system.
  Source changes in `RenderRegion`/`RenderRegionManager` and `UniformBufferManager`
  include section-time buffer resizing, copy-source usage, remapping and releasing
  the previous buffer. Iris integration therefore needs its corresponding version.

## Extension candidates, not enabled support

1. Highest-confidence expansion: listed Litematica 0.28.4/5/6/8 with compatible
   MaLiLib minimum versions and either already-tested Iris/Sodium pair. Their
   directly patched classes match existing bytecode. Test representative boundary
   combinations and preserve exact release fingerprints before enabling them.
2. Next: Iris 1.11.1 + Sodium 0.9.0. Direct hook classes match 1.11.2, but upstream
   uniforms, glyph format and world-render integration differ. A full projection
   scene with text/hand rendering and shader toggling is necessary.
3. Separate older sampler profile: Litematica 0.28.3, with resource reload,
   filtering and shader switching checked explicitly.
4. Do not enable Iris 1.11.0, arbitrary Iris/Sodium cross-pairs, future version
   strings, or other Minecraft versions based on this audit.

No existing tracking tests or new game sessions were run for this source audit.
No release is needed for an evidence-only document change.
