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
