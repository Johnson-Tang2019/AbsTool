# Optional TweakerMore quick settings

Fabric registers the provider only when Fabric Loader reports `tweakermore`.
The shared UI and NeoForge contain no TweakerMore/MaLiLib class references.
Each screen creates its own entry list and pending-save session. Quick Settings
is created only if applicable entries exist; All shares the same entry instances.

The adapter reads the public TweakerMore `getAllOptions()` registry, keeps enabled
options whose config names contain `shulker`, and supports the boolean, integer
and double types found in the audited source. Restrictions are evaluated by
TweakerMore itself, so options requiring absent mods or a different Minecraft
version are omitted. General container settings are not treated as shulker-only
settings merely because their default list contains a shulker box.

Values/defaults/bounds and value-change callbacks come from the original MaLiLib
config objects. Known settings have concise bilingual labels; tooltips use the
original comments and future matching names fall back to TweakerMore labels.
Edits stay inside Cloth until Save. Modified values are written through the
original setters and `TweakerMoreConfigStorage.getInstance().save()` once per
screen save. No TweakerMore JSON is copied into AbsTool's configuration.
Missing/incompatible reflective API is logged and produces no quick-settings tab.

## Source and test dependency

[Upstream source](https://github.com/Fallen-Breath/tweakermore):
`TweakerMoreConfigs`, `TweakerMoreOption.isEnabled`, `TweakerMoreConfigStorage`,
and the config option classes. The integration does not copy upstream code.
Official Minecraft 26.2 Fabric test release: TweakerMore 3.33.2, Modrinth version
KIBGE0Vv, downloaded with its published SHA-512 verified; MaLiLib 0.29.6.
Neither dependency is bundled or required by AbsTool.

## Validation

The installed-mod client smoke test found nine applicable options plus a section
heading without Litematica installed. It exercised the real toggle widget,
discard/reopen behavior, upstream numeric bounds, non-finite input validation,
saving and disk reload through TweakerMore, then restored the test option.
It also verifies shared All/Quick editor instances.

Fabric without optional mods and NeoForge both passed startup/settings checks,
asserting exactly All/Tracking/Optimization and no Quick Settings category.
No existing vault/furnace gameplay or projection game tests were repeated.

```powershell
./tools/Prepare-CompatibilityTests.ps1
./gradlew.bat --offline -PclientSmoke -PwithTweakerMoreTest :fabric:runClient
./gradlew.bat --offline -PclientSmoke :fabric:runClient
./gradlew.bat --offline -PclientSmoke :neoforge:runClient
```

The installed branch uses `26.2Fabric/build/run/tweakerMoreQuick`, isolated from
normal play. Logs: `build/quick-present.log`, `build/quick-present-zh.log`,
`build/quick-absent-fabric.log`, `build/quick-absent-neoforge.log`.

The final Chinese screen was visually inspected with the Quick Settings tab
selected. Final logs include ABSTOOL_QUICK_SETTINGS_PASS and ABSTOOL_SETTINGS_SMOKE_PASS.

## 0.2.4 most-common-item icon

A separate, default-on AbsTool option replaces the mixed-box ellipsis with the
item type having the greatest total count. Slots and component variants of an
item are combined; equal totals use the first occupied slot. The displayed
representative is a copy of that first stack with count one. Contents are never
mutated. Empty boxes, non-box items and single-type hints retain upstream behavior.
TweakerMore's content-hint toggle and recognized custom-name overrides retain
precedence. Disable the AbsTool option to restore the ellipsis.

The optional renderer Mixins are separate from networking and schematic Mixins.
A discovery plugin checks the original SHA-256 fingerprints of TweakerMore's
`ShulkerBoxItemContentHintCommon` and `ShulkerBoxItemContentHintRenderer` before
activating them. The audited binary is TweakerMore 3.33.2 for Minecraft 26.2;
unknown renderer binaries keep upstream rendering and hide this extra switch,
while ordinary quick settings remain available through their existing API.

Only the first two reads of `Info.allItemSame` in the renderer are intercepted:
one decides whether to draw the icon, the other whether to draw the text. The
third read and the underlying flags remain unchanged so the mixed-box fill bar
still obeys its own TweakerMore setting. The selected icon is computed after the
original information preparation; existing scale and fill ratio are retained.
Work is bounded by the shulker box contents and no persistent contents cache is
introduced.

A real client game test creates an isolated world, builds actual container item
components and renders a comparison screen via the normal GUI item renderer.
Assertions cover total quantity across stacks, deterministic ties, component
variants, unchanged contents/fill ratio/mixed flags, empty/non-box items, upstream
disable and custom-name precedence. The screenshot visually confirms dots versus
stone for 32 diamonds + 20 stone + 20 stone, diamond on a 40/40 tie, unchanged apple
and empty-box behavior. Evidence: `build/shulker-hint-world.log` and
`build/shulker-evidence/majority-comparison.png`.

```powershell
./gradlew.bat --offline -PworldTests -PwithTweakerMoreTest -PshulkerHintTest :fabric:runClientGameTest
```

This profile replaces the game-test entrypoint list and does not execute the
existing tracker tests. Item construction is performed after world loading,
when Minecraft 26.2 item components have been bound.
