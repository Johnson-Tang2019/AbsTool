# 兼容性矩阵

[English](https://github.com/Johnson-Tang2019/AbsTool/wiki/Compatibility) · [首页](https://github.com/Johnson-Tang2019/AbsTool/wiki/Home.zh-CN)

本页总结当前源码中的兼容策略，不代表每一种理论组合都经过了长时间人工游戏测试。

## 核心支持

| Minecraft | 版本 | Fabric | NeoForge |
| --- | --- | --- | --- |
| 26.2 | 0.4.12 | 支持 | 支持 |
| 26.3 | 0.5.0-beta.6 | 支持 | 支持，工具链仍为 beta |

## Litematica / Iris 兼容

### Minecraft 26.2，Fabric

已核查 Litematica / MaLiLib 组合：

| Litematica | MaLiLib |
| --- | --- |
| 0.28.3 / 0.28.4 | 0.29.2～0.29.6 |
| 0.28.5 | 0.29.4～0.29.6 |
| 0.28.6 / 0.28.8 | 0.29.5 / 0.29.6 |

Iris / Sodium 必须使用匹配组合：

- Iris 1.11.1 + Sodium 0.9.0
- Iris 1.11.2 + Sodium 0.9.1
- Iris 1.11.4 + Sodium 0.9.2

文档中的视觉检查使用 Complementary Reimagined r5.9.1，这不代表所有光影包都得到保证。

26.2 NeoForge 不启用这项投影光影适配。

### Minecraft 26.3，Fabric

26.3 不只依赖静态版本白名单，而是结合接口/字节码契约进行检查。

当前已核查：

- Litematica 0.29.0～0.29.1。
- MaLiLib 0.30.0～0.30.2。
- Iris 1.11.5～1.11.7。

后续 Iris 版本如果保持所需接口可以自动通过；接口不兼容时会停用可选适配。

## Xaero 共享标点

详细版本范围见[Xaero 服务器共享标点](https://github.com/Johnson-Tang2019/AbsTool/wiki/Shared-Waypoints.zh-CN)。

当前 26.3 最新代码已核查：

- Fabric：Minimap 26.5.1～26.6.0，World Map 1.46.2～1.47.0。
- NeoForge：Minimap 26.5.2～26.6.0，World Map 1.46.3～1.47.0。
- 不设置硬编码最高版本，是否启用由运行时接口契约判断。

## TweakerMore 与 Tweakeroo

26.2 源码明确记录：

- 混合潜影盒渲染扩展核查 TweakerMore 3.33.2。
- Fabric 鞘翅换装协作核查 Tweakeroo 0.29.5。

二者都是可选依赖。缺少可选 Mod 时不能影响 Ab's Tool 正常启动。

## AbsServerTool

服务器必须使用与 Minecraft 版本匹配的 AbsServerTool。

当前 main 最新为 Minecraft 26.3 的 AbsServerTool 0.3.7，同时构建 Fabric 与 NeoForge 服务端安装包；Minecraft 26.2 在独立维护分支继续维护。
