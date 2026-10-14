# Ab's Tool Wiki

[English](Home.md)

本 Wiki 根据 GitHub main 分支的最新代码整理。最后核对提交：cd0131663510be7aa72776ebde475b5222aa632a（2026-10-13）。

## 当前版本

| Minecraft | Ab's Tool | 加载器 | 状态 |
| --- | --- | --- | --- |
| 26.2 | 0.4.12 | Fabric、NeoForge | 稳定维护线 |
| 26.3 | 0.5.0-beta.6 | Fabric、NeoForge | Beta / 预发布 |

Minecraft 26.2 继续使用仓库根目录工程；Minecraft 26.3 位于独立的 26.3 工程中，因此两个游戏版本可以分别维护依赖、构建环境和本地配置。

## Ab's Tool 目前包含什么

Ab's Tool 定位为客户端工具与 QoL Mod。当前代码已经包含：

- 不祥宝库追踪与本地“已处理”记录。
- 熔炉输入堵塞追踪，优先使用 Servux，可选 Jade 回退。
- 与 AbsServerTool 协议 v2 对接的“服务器统计”页面。
- 与 AbsServerTool 对接的服务器性能分析客户端。
- 实验性的低顶空鞘翅快速起飞。
- Fabric 上实验性的 Litematica + Iris 投影光影兼容。
- 可选 TweakerMore 快捷设置与混合潜影盒“最多物品”提示。
- Xaero 服务器共享标点，其中 Minecraft 26.3 分支的集成最完整。

服务器统计、共享标点的权威存储与权限、性能采样等服务端逻辑由独立项目 AbsServerTool 提供。

## Wiki 导航

- [安装与版本](Installation.zh-CN.md)
- [功能说明](Features.zh-CN.md)
- [设置与按键](Configuration.zh-CN.md)
- [AbsServerTool 联动](Server-Integration.zh-CN.md)
- [Xaero 服务器共享标点](Shared-Waypoints.zh-CN.md)
- [兼容性矩阵](Compatibility.zh-CN.md)
- [开发与发布结构](Development.zh-CN.md)
- [故障排查](Troubleshooting.zh-CN.md)

## 项目链接

- 源码：https://github.com/Johnson-Tang2019/AbsTool
- Releases：https://github.com/Johnson-Tang2019/AbsTool/releases
- 服务端伴侣 Mod：https://github.com/Johnson-Tang2019/AbsServerTool
