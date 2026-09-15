# Ab's Tool

[中文说明](README.zh-CN.md)

[Project repository](https://github.com/Johnson-Tang2019/AbsTool)

Minecraft 26.2 utility mod for Fabric and NeoForge, following AbsMod's layout.
Mod ID: `abstool`. Base package: `com.abyssredemption.abstool`.

## Low ceiling elytra launch

Under R+B > Optimization > Elytra Assist, enable **Low ceiling quick launch**.
The default trigger is Sneak + Jump; a dedicated configurable key (default G) is
also available. The feature equips a usable glider from the normal inventory,
jumps, then sends one glide request after vanilla synchronizes the airborne
movement. The server decides whether flight starts. It provides no propulsion.
Fabric optionally delegates equipment to Tweakeroo 0.29.5 when its automatic
elytra switch is enabled; otherwise the shared Fabric/NeoForge fallback performs
one normal inventory swap. No additional dependency is required. Unknown enabled
Tweakeroo versions cancel safely. AbsTool does not restore armor after landing.
See [implementation and test notes](docs/ELYTRA_LAUNCH.md).

Experimental and off by default. Server-confirmed launch is tested; the separate
steered-exit test timed out, so reliably flying out of the space is not certified.

## Settings and schematic shaders

R+B opens the shared settings with **All / Tracking / Optimization** tabs.
ModMenu and the NeoForge settings button use the same screen. Existing settings,
unknown JSON fields, and vault records are retained.

The experimental **Fabric** Litematica/Iris adapter is under Optimization.
AUTO supports Minecraft 26.2 on OpenGL with these audited release combinations:

| Litematica | Supported MaLiLib |
| --- | --- |
| 0.28.3 / 0.28.4 | 0.29.2 through 0.29.6 |
| 0.28.5 | 0.29.4 through 0.29.6 |
| 0.28.6 / 0.28.8 | 0.29.5 / 0.29.6 |

Use one matching Iris/Sodium pair: **1.11.1 / 0.9.0**, **1.11.2 / 0.9.1**,
or **1.11.4 / 0.9.2**, all for Minecraft 26.2. Install dependencies separately.
Unknown releases and modified target bytecode remain disabled.
OFF restores upstream drawing. NeoForge does not enable this new adapter.
Textured and translucent projections were checked in game with Complementary
Reimagined r5.9.1; this is not a guarantee for other packs or versions.
Use `/abstool schematicshader status` for local diagnostics.

When TweakerMore is installed on Fabric, **Quick Settings** shows its applicable
shulker-box options, including content hints, scale, fill-level and tooltip hints.
The same editors appear under All. Values, defaults and limits come from
TweakerMore; saving uses its own configuration API. Discarded edits do not change
it. The category is hidden when TweakerMore is absent. No extra required dependency
is added to either loader.

Quick Settings also provides **Show most common item** (on by default): mixed
shulker boxes show the item with the largest total count instead of dots.
Counts combine slots and component variants of the same item; ties use the first
occupied slot. TweakerMore content hints must be enabled. Its explicit custom-name
override and mixed-box fill-bar setting keep their original behavior. Turn this
option off to restore dots. This renderer extension requires the audited
TweakerMore 3.33.2 renderer bytecode; regular quick settings remain independent.

[Implementation and limits](docs/SCHEMATIC_SHADER_COMPAT.md) ·
[Test evidence](docs/COMPATIBILITY_MATRIX.md) · [Test commands](docs/TESTING.md)

## Structure

- `common/src/main/java`: shared logic and a separate client initialization hook.
- `common/src/main/resources`: shared assets and translations.
- `26.2Fabric` (Gradle `:fabric`): Fabric common/client entrypoints and metadata.
- `26.2NeoForge` (Gradle `:neoforge`): NeoForge common/client entrypoints and metadata.

Both loaders compile the common sources directly into their own JAR. Common is
a source directory, not a Gradle subproject. No Architectury runtime is required.
## Ominous vault tracking

- Highlights loaded ominous vaults through blocks; normal vaults are ignored.
- Right-click a vault while tracking is enabled to mark it locally as handled.
  This does not consume the interaction or confirm that the server rewarded you.
- Hide handled vaults or show them in a different color. Records are separated
  by server address / singleplayer save path and dimension.
- Configure highlight and tracer colors, a 1–32 chunk range, tracer visibility,
  and an optional required item in either hand (default: ominous trial key).
- Optionally expire handled records after a real-time cooldown or at a local
  daily reset hour, scoped to a dimension, server/world, or all records.

Press **R+B together** in-game to open settings. The shortcut works while tracking
is disabled and does not activate over chat or another screen. On Fabric, optional
**Mod Menu → Ab's Tool → Configure** opens the same page. On NeoForge, use the
built-in **Mods → Ab's Tool → Config** entry. Enable highlighting and save to begin;
it is disabled by default.

Install **Cloth Config 26.2.155 or compatible** for your loader. Fabric also requires
Fabric API; Mod Menu 20.0.2 is optional. No server installation is required for vault tracking.
Scanning only inspects chunks already received by the client; it never loads distant chunks.
Refreshing local records does not reset Minecraft's vault rewards.

Settings are saved to `config/abstool.json`; handled records to
`config/abstool-vaults.json`. Writes retain the previous file as `.bak`.
Existing Ominous Vault Track files are not automatically imported. See
[third-party notices](THIRD_PARTY_NOTICES.md) for the source and MIT license.

## Furnace input tracking

Open **R+B**, Mod Menu (Fabric), or the NeoForge mod configuration screen and select
**Furnace tracker**. Enable it explicitly; vault and furnace tracking are independent.
The default radius is 256 blocks, with 128 blocks and all client-loaded chunks as alternatives.
It discovers ordinary furnaces, blast furnaces, and smokers by inspecting loaded chunk
block entities, eight chunks per tick. It never requests chunk loading.

Servux is preferred: the server must advertise `servux:entity_data`, enable its provider,
accept protocol version 2, and allow the player. No Ab's Tool server component is needed.
The implementation targets the Minecraft 26.2 protocol, including length-prefixed gzip NBT.
Channel observation preserves existing payload codecs and receivers, including MiniHUD's.
A failed handshake does not prove that the server is missing Servux; permissions and settings
can also prevent a response.

Jade is optional and requires compatible Jade 26.2 on both client and server. Its handshake
and provider mapping are reused. The default extra query range is 21 blocks plus player
interaction range; adjust it only to match an administrator's increased server rule.
Jade still performs its own distance, loaded-chunk, and provider checks. Replies to this
tracker do not replace Jade's crosshair-tooltip data.

Only a nonempty input absent from the server-synchronized input property set for that
furnace type is marked blocked. No fuel, a full output, and normal cooking are not blockage.
Missing/malformed inventory or missing recipe synchronization stays unknown. These property
sets describe accepted item types; component-sensitive custom recipe semantics are not
independently reconstructed on the client.

Requests default to 20 per second (configurable 1–40), with a small burst cap. New and blocked
entries receive priority; every third selection uses oldest-request order to keep ordinary
entries moving. Blocked entries are rechecked no sooner than 2 seconds, ordinary ones 5 seconds.
A request expires after 5 seconds. Three Servux timeouts trigger handshake recovery and Jade
fallback. Fresh valid data clears repaired markers. No response cannot clear or confirm one.

Red boxes, tracers, and item names indicate confirmed blockage. Entries outside the loaded
scan area, timed-out entries, and data older than 30 seconds (configurable) are gray and labeled
**last seen blocked (stale)**. Settings show discovered and unknown/expired counts. Up to 8192
entries are held in memory; disconnecting or changing dimension clears them to prevent world
mixups. Furnace records do not persist across sessions. To prevent overlapping text in dense arrays, the name nearest the crosshair is shown.
Fabric was visually checked with Iris 1.11.4, Sodium 0.9.2 and Complementary Reimagined r5.9.1,
including through-wall boxes. This is not a guarantee for every shader pack. Both loaders were
checked against the official Servux 0.11.5 server, and Fabric fallback against Jade 26.2.11.

## Build and run

Install JDK 25 and set JAVA_HOME to its installation directory. The wrapper uses
Gradle 9.5.1. Dependency versions are pinned in `gradle.properties` and `build.gradle`.

```powershell
.\gradlew.bat build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :neoforge:runClient
.\gradlew.bat :fabric:runServer
.\gradlew.bat :neoforge:runServer
```

For macOS/Linux, use `bash ./gradlew` instead of `.\gradlew.bat`.
Server runs require accepting Minecraft's EULA in the generated run directory.

Release JARs are written to `26.2Fabric/build/libs` and
`26.2NeoForge/build/libs`. Install only the JAR for your loader; the Fabric
build also requires Fabric API and Cloth Config. The NeoForge client requires
NeoForge Cloth Config. Files ending in `-sources.jar` are for development.

## Verification

`build` runs `:fabric:verifyVault` for shared configuration, shortcut, persistent
storage, scope isolation, and timed refresh behavior. Both loader JARs compile the
same shared feature code.

Opt-in development smoke mods open the configuration page, check its parent return
and render pipeline initialization, and automatically exit. They are separate
source sets and never enter release JARs:

```powershell
.\gradlew.bat -PclientSmoke -PwithModMenu :fabric:runClient
.\gradlew.bat -PclientSmoke :fabric:runClient
.\gradlew.bat -PclientSmoke :neoforge:runClient
.\gradlew.bat -PworldTests :fabric:runClientGameTest
```

The Fabric world test creates an isolated save and checks ominous-only scanning,
right-click handling, alternate colors, tracer gating and R+B, with screenshots.

Manual world check: enable tracking, place an ominous and a normal vault, verify
only the ominous vault is outlined; test right-click hiding, alternate colors,
tracer item gating, changing dimensions/worlds, and reconnecting. Check the actual
R+B keypress and the menu entry. Automated settings checks do not prove world visuals.

Keep gameplay code loader independent in common and registration adapters in
each loader. Reference client-only game classes only from client code.
Use English identifiers and comments, localize visible strings, and update both READMEs.
Metadata currently declares All Rights Reserved, matching the reference project.

### Optional furnace compatibility tests

Download pinned, checksum-verified test mods into the ignored build directory:

```powershell
./tools/Prepare-CompatibilityTests.ps1
./gradlew.bat -PworldTests -PfurnaceTests -PacceptMinecraftEula -PwithJadeTest -PwithIrisTest :fabric:runClientGameTest
```

Use `-PacceptMinecraftEula` only after accepting Minecraft's EULA. The furnace test creates
an isolated local server, uses a Servux v2 protocol fixture, and checks actual Jade fallback,
range changes, repair, stale state, request limits and screenshots with shaders enabled.
Omit `-PwithIrisTest` to check vanilla rendering. The test fixture is never shipped.

For the official Servux server test, prepare its separate local directory, launch the server,
then run either client smoke command in another terminal. Stop the test server afterwards:

```powershell
./tools/Prepare-CompatibilityTests.ps1 -PrepareServuxServer -AcceptMinecraftEula
./gradlew.bat -PwithRealServuxTest :fabric:runServuxTestServer
./gradlew.bat -PclientSmoke -PexternalServuxTests :fabric:runClient
./gradlew.bat -PclientSmoke -PexternalServuxTests :neoforge:runClient
```

Protocol references: [Servux 26.2 entity provider](https://github.com/sakura-ryoko/servux/blob/e40a7562f87b3ac0e557439728f8208aba9b66a6/src/main/java/fi/dy/masa/servux/dataproviders/EntitiesDataProvider.java)
and [Jade 26.2 furnace provider](https://github.com/Snownee/Jade/blob/747effeddcea3094b940772c7963c272bb2a07df/src/main/java/snownee/jade/addon/vanilla/FurnaceProvider.java).
