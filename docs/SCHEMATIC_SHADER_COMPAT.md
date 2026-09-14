# Schematic shader compatibility (experimental)

This is an incremental Fabric-only adapter for Minecraft 26.2. NeoForge retains
vault/furnace tracking and settings; it does not activate this adapter.

The audit started from `9df10623c73013d03352b8c5ffab5a8db0b8230e` with a clean tree.
`7c77246` is historical context, not a reset target. Existing network Mixins and
tracking render pipelines are independent of the optional compatibility Mixins.

## Fixed research and test combination

| Component | Version | Reference |
| --- | --- | --- |
| Minecraft | 26.2 | Existing project |
| Litematica | 0.28.8 | Modrinth CuniXtbo; source 5bd793d44ad5f047cad3f4b389e8dd0368b6e5a8 |
| MaLiLib | 0.29.6 | Modrinth KvjmGjAV; source 5c028ed94037a73c07118cdc8170f89dc4a6e516 |
| Iris | 1.11.4+mc26.2 / 1.11.2+mc26.2 | Modrinth gxZWWnKH / oaD6KQls |
| Sodium | 0.9.2+mc26.2 / 0.9.1+mc26.2 | Modrinth xJZxADzI / 2Yom1N68 |
| Complementary Reimagined | r5.9.1 | Modrinth ErCjThzb, default HIGH profile |

The test preparation script downloads official artifacts and verifies their
SHA-512 values. Dependencies and shader packs are not bundled in AbsTool.
The Iris research checkout is `f9854483542fa2c002b0f8016a227345c8674494` on the
26.2 branch (reports 1.11.4); release bytecode remains the authority for hooks.

## Evidence and render path

[Litematica issue 1140](https://github.com/maruohon/litematica/issues/1140#issuecomment-5095683393)
was closed because shader use is unsupported for 26.2+, not because a fix shipped.
[Iris issue 2974](https://github.com/IrisShaders/Iris/issues/2974) is historical
CustomUBO/atlas context, not proof of the present root cause.

Litematica builds model meshes through its existing schematic world and chunk
dispatcher, prepares camera-relative DynamicTransforms and ChunkFix, and submits
ChunkRenderBatchDraw for opaque/translucent groups. MaLiLib assigns its legacy
pipelines to Iris terrain programs; Iris can extend BLOCK layouts while rendering
the world. The experiment preserves upstream meshes, placement transforms,
visibility selection, and rebuild scheduling. It isolates buffer construction
and defers the two frame groups until after Iris world composition and before
the level matrix is popped. Iris uses the main target's depth attachment in this
version; no depth clear or raw OpenGL call is added by AbsTool.

The fixed combination passed actual textured-projection, occlusion, and selected
lifecycle checks. The user also confirmed the displayed result in game. It
remains experimental outside the documented coverage. A draw counter or
successful build alone is not visual acceptance. See COMPATIBILITY_MATRIX.md.

## Configuration and isolation

R+B, ModMenu, and the NeoForge settings entry use the same screen. Tabs are All,
Tracking, and Optimization. All shares editor instances with its filtered tabs
so pending values cannot overwrite each other on save.

`schematicShaderCompat` is added to the existing abstool.json. Existing fields,
including unknown nested JSON fields, are merged on save. Existing backup and
atomic file writing remain in use. Vault records are not migrated or rewritten.

AUTO activates only the pinned combination with shaders and the supported
backend; OFF leaves upstream rendering in control. The opacity multiplier is
bounded to 0..1; nonfinite values normalize to 1. Debug logging is rate-limited.
`/abstool schematicshader status` is a local client command.

The independent Mixin plugin checks dependency versions and target bytecode
before applying hooks without loading optional target classes. Seven original
dependency class resources are SHA-256 checked, including rendering methods,
buffer constructors and Iris state/layout code. Unrecognized versions or altered
binaries remain unsupported. Shared code does not reference optional APIs.

The final submission is injected at RETURN of IrisRenderingPipeline's
finalizeLevelRendering, rather than relying on relative injection priority at a
shared vanilla node. That ordering difference was verified with before/after
screenshots: submitting at the shared node left the texture invisible. The
adapter temporarily scopes Iris's layout-extension/override state only around
its own draw and restores it in finally; the world's shader pack stays enabled.
Upstream meshes, atlas, samplers, transform slices, and layer selection are reused.
Frame records are cleared at frame start, after submission, and on world exit.
No persistent GPU cache is added. A draw failure disables this adapter and is
reported; OFF clears the error so AUTO can be retried.

Opacity affects block meshes. Block entities keep upstream rendering behavior;
their animations and opacity are not covered by that multiplier. Mod custom
renderers and all shader-pack-specific depth/composition effects are not certified.

## Test-only upstream workaround

MaLiLib enables vanilla IDE validation in development. Iris 1.11.4 returns a
one-slot binding array, while vanilla validates sixteen slots, causing an
ArrayIndexOutOfBoundsException even before a projection exists. The opt-in test
Mixin pads only this validation result with null slots. Validation remains
enabled. This fixture is not packaged in either release JAR and is not counted
as a production compatibility fix.

## Additional 0.2.1 dependency pair

Iris 1.11.2 requires the supported Sodium 0.9.1 pair; Sodium 0.9.2 explicitly
breaks Iris <=1.11.2 in its Fabric metadata. The original Iris 1.11.4 / Sodium
0.9.2 pair remains supported. Litematica and MaLiLib pins are unchanged.
Iris 1.11.2 has separate hashes for IrisRenderingPipeline and MixinRenderPipeline;
ImmediateState is byte-identical. Unknown binaries and cross-pairs remain gated.
