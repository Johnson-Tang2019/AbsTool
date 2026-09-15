# Low ceiling elytra launch

## Source audit

Target: Minecraft 26.2. Optional Fabric integration: official Tweakeroo 0.29.5,
Modrinth version `tzGdG1Ri`, source commit `e6f0c04819f60d361d94c24599a0974d631b2459`.

- [Tweakeroo launch hook](https://github.com/sakura-ryoko/tweakeroo/blob/e6f0c04819f60d361d94c24599a0974d631b2459/src/main/java/fi/dy/masa/tweakeroo/mixin/entity/MixinLocalPlayer_elytraSwap.java)
  only calls its equipment routine on an airborne vanilla glide-check edge.
  Waiting on the ground alone cannot activate it. Its stop-flying hook restores
  the cached chestplate, or runs its normal default chestplate selection when
  the cache is empty.
- [Equipment implementation](https://github.com/sakura-ryoko/tweakeroo/blob/e6f0c04819f60d361d94c24599a0974d631b2459/src/main/java/fi/dy/masa/tweakeroo/util/InventoryUtils.java)
  exposes `equipBestElytra(Player)`, a utility rather than a stable compatibility
  API. It selects vanilla elytra with more than ten durability remaining, then
  uses three normal SWAP interactions via a temporary hotbar slot. This preserves
  occupied slots without requiring empty inventory space. AbsTool invokes this
  equipment-only routine once and does not invoke the combined input hook.
- Vanilla `LivingEntity.canGlideUsing` checks GLIDER/EQUIPPABLE and remaining
  durability. The shared selector also explicitly requires a chest-slot
  equippable item allowed for players and a non-broken stack, including on
  NeoForge where gliding attributes extend the vanilla rules.
- Vanilla `LocalPlayer.tick` sends movement after `aiStep`. The launch request
  is issued at tick return, after the real airborne movement packet. No position,
  onGround flag, collision dimensions or client flying flag is fabricated.

Release bytecode was inspected alongside this source. SHA-256:

- InventoryUtils: `47d8b4cc74938247e0a019021da6f9a4bae150f4d833719b42821d62439297ab`
- Elytra swap Mixin: `e8bd0261353c57deba428a211843bf68b28aa17e0361bae191820580f00b3980`

The upstream search can include the offhand. When a usable glider is held there,
the external integration cancels and requires manual equipment rather than
letting the utility take a glider outside the requested inventory range.

## Behavior

Off by default. Sneak + Jump is the default edge trigger. A keyboard binding
(default G, configurable or unbound) is available in the existing settings.
The same editor instances appear under All and Optimization.

One transaction has one owner: NONE (already equipped), TWEAKEROO, or ABS_TOOL.
Tweakeroo access is deferred and Fabric-only; missing optional classes never
load on NeoForge. Unknown enabled Tweakeroo versions cancel rather than race its
inventory behavior. Its settings are only read. The supported utility does not
populate its original-armor cache, so its own normal uncached landing selection
applies; AbsTool never enforces a particular replacement chestplate.

The fallback uses three normal SWAP clicks once. It checks the player inventory,
empty cursor and chest-slot pickup permission first. It never takes items from
containers, nested shulker contents, or ender storage, and never drops equipment.
Subsequent equipment changes cancel the launch. It does not restore armor.

Swap wait defaults to 10 ticks (2–40). The initial airborne window defaults to
6 ticks (1–20). Only one glide request is sent, with a bounded 40-tick confirmation
wait and a 10-tick cooldown. Held triggers cannot create repeated transactions.
GUI conflicts, disable, invalid player/world, death and dimension changes clear
state. No extra propulsion, item-use, view steering or movement packets are added.

## Reproducing the new-feature tests

Prepare official optional jars with `tools/Prepare-CompatibilityTests.ps1`.
Use JDK 25. Pass `-PacceptMinecraftEula` only after accepting the Minecraft EULA;
it writes acceptance only in the isolated game-test working directory.

```powershell
./gradlew.bat --offline -PworldTests -PelytraTests -PacceptMinecraftEula :fabric:runClientGameTest
./gradlew.bat --offline -PworldTests -PelytraTests -PacceptMinecraftEula -PwithTweakerooTest :fabric:runClientGameTest
./gradlew.bat --offline assemble
```

The dedicated-server latency fixture delays inbound and outbound packets by
200 ms each using a test-only Netty handler. Test fixtures issue server commands
to build arenas and supply equipment; none of that code is packaged in the mod.
Old vault and furnace gameplay tests are excluded by the elytra test entrypoint.

## Validation status

Validated on 2026-09-15:

- Fabric without Tweakeroo and with Tweakeroo 0.29.5: server-confirmed two-block
  launch, open space, slab/stair ceilings, full inventory, equipped glider,
  held Sneak + Jump, and dedicated key. Equipment counts are preserved.
- Tweakeroo landing restoration and bounded failure when its durability threshold
  rejects the available elytra passed. Manual equipment changes, GUI conflicts,
  disable and dimension changes cancel the transaction; death/disconnect cleanup
  was checked.
- Both profiles passed local dedicated-server launch and injected 400 ms round-trip
  latency checks. Logs: `build/elytra-matrix-fallback.log` and
  `build/elytra-matrix-tweakeroo.log`.
- NeoForge startup and settings smoke passed (`build/elytra-neoforge-smoke.log`).
  The full in-world matrix was run on Fabric, not NeoForge.

Known limitation: the additional player-steered exit fixture timed out while
waiting for the server to observe gliding beyond the exit (`build/elytra-exit.log`).
Therefore actual low-ceiling launch is verified, but reliably flying out of the
space is not certified. The feature remains experimental and off by default.
Compilation does not replace these runtime checks.
