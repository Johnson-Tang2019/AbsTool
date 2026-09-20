# Changelog

## 0.4.0 — 2026-09-20

- Add a NeoForge client dashboard for compatible Ab's Server Tool protocol v2
  servers, including overview statistics, play-time, placement and death
  leaderboards, trends, pagination, refresh handling, and availability states.
- Keep the dashboard unavailable when the server component is missing or uses an
  incompatible protocol instead of presenting stale or unsupported data.
- 新增 NeoForge 服务器统计面板，支持总览、在线时间、方块放置、死亡排行与趋势；
  未安装兼容服务端或协议不匹配时会明确提示，不展示无效数据。

## 0.3.0 — 2026-09-15

- Add optional low-ceiling elytra launch on both loaders: equip from normal
  inventory, inject one normal jump edge and request glide once after vanilla
  airborne movement synchronization. Default off; Sneak + Jump or configurable key.
- Delegate equipment to Fabric Tweakeroo 0.29.5 when its auto-switch is enabled;
  otherwise use one bounded inventory swap. Preserve equipment and release
  control for upstream landing behavior. Cancel conflicting or timed-out actions.
- Add Elytra Assist to All / Optimization. No propulsion or automatic item use.
- 新增低顶空鞘翅快速起飞：支持物品栏换装、Tweakeroo 可选共存与独立快捷键。

## 0.2.4 — 2026-09-15

- Add an optional most-common-item icon for mixed shulker boxes in TweakerMore.
  Aggregate quantities across slots, keep deterministic ties, preserve upstream
  name overrides and mixed-box fill-bar behavior. Default on; configurable under
  Quick Settings while the audited renderer is present.
- 用总数量最多的物品图标替换混合潜影盒省略号，可在快捷设置关闭。


## 0.2.3 — 2026-09-15

- Add a conditional Quick Settings category for applicable TweakerMore shulker-box
  settings on Fabric, also shared with All. Hide it when TweakerMore is absent.
- Use original configuration values, defaults, bounds, callbacks and persistence;
  discard edits without changing the optional mod. Keep both loaders independent
  of a required TweakerMore/MaLiLib runtime dependency.
- 新增快捷设置：安装 TweakerMore 时显示其可用潜影盒相关配置，未安装时隐藏。


## 0.2.2 — 2026-09-15

- Expand Fabric schematic shader support to audited Litematica 0.28.3/4/5/6/8
  and MaLiLib 0.29.2–0.29.6, respecting each upstream minimum dependency.
- Add Iris 1.11.1 with Sodium 0.9.0; retain 1.11.2/0.9.1 and 1.11.4/0.9.2.
- Preserve release-class fingerprint checks, including the older Litematica
  sampler implementation; show the actual versions of all four dependencies.
- 新增多个投影／前置版本兼容，按依赖规则开放 51 个组合；不允许任意混搭。
  Iris 1.11.0、未核查版本和被修改的适配目标类仍不启用适配。


## 0.2.1 — 2026-09-14

- Add Fabric projection shader compatibility for Iris 1.11.2+mc26.2 paired with
  Sodium 0.9.1+mc26.2. Keep Iris 1.11.4 / Sodium 0.9.2 support.
- Validate the additional Iris release bytecode and show installed versions in diagnostics.
- 新增 Iris 1.11.2 + Sodium 0.9.1 的投影光影适配，保留原版本组合。
  Litematica 0.28.8 与 MaLiLib 0.29.6 版本要求不变。


## 0.2.0 — 2026-09-14

- Add All / Tracking / Optimization tabs to the existing shared settings screen.
- Add an experimental Fabric Litematica 0.28.8 / MaLiLib 0.29.6 / Iris 1.11.4 /
  Sodium 0.9.2 adapter for Minecraft 26.2 OpenGL. Preserve actual block meshes,
  textures, translucency, upstream placement/layer logic and world depth.
- Add AUTO/OFF, block-mesh opacity, rate-limited diagnostics and a local status command.
- Keep optional rendering Mixins separate from existing network Mixins; gate
  recognized versions and original dependency bytecode before applying hooks.
- Preserve unknown JSON fields and existing config backups without rewriting vault records.
- Add isolated real-world visual fixtures, frame-interval sampling, configuration
  migration checks, dependency-startup checks and dual-loader build CI.
- Verify the pinned combination with Complementary Reimagined r5.9.1 in game;
  retain explicit coverage limits. NeoForge does not enable the new projection adapter.

### 中文

- 统一设置新增“全部／追踪／优化”分类，继续使用 R+B、ModMenu 和 NeoForge 设置入口。
- 新增 Fabric 实验性投影光影适配，已实际检查贴图、透明度、复杂模型、遮挡、
  旋转镜像、切层、第三人称、资源重载、缩放和较大投影；用户已确认显示结果。
- 保留宝库、熔炉、旧配置与记录、网络 Mixin 和双加载器安装包。
- 仅支持文档列出的固定版本组合；NeoForge 暂不支持新增投影适配。
