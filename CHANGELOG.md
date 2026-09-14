# Changelog

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
