# 安装与版本

[English](Installation.md) · [首页](Home.zh-CN.md)

## 先选择正确的 Minecraft 版本线

不要混用 26.2 与 26.3 的 JAR。

### Minecraft 26.2

当前稳定线：Ab's Tool 0.4.12。

- Fabric 与 NeoForge 分别发布独立安装包。
- 开发/构建环境使用 Java 25。
- Fabric Loader 最低 0.19.3。
- Fabric API 最低 0.159.0+26.2。
- Cloth Config：26.2.155 或兼容版本。
- Fabric 可选安装 Mod Menu。

### Minecraft 26.3

当前测试线：Ab's Tool 0.5.0-beta.6。

- Fabric 与 NeoForge 分别发布独立安装包。
- Java 25。
- Fabric Loader 0.19.5。
- Fabric API 0.160.6+26.3。
- Cloth Config 26.3.158。
- 当前 NeoForge 构建基线为 26.3.0.23-beta。
- 由于 NeoForge 26.3 工具链仍处于 beta，26.3 版本按预发布版本发布。

## 可选联动

只安装你实际使用的联动 Mod。

- Servux：熔炉追踪优先使用的数据源。
- Jade：熔炉数据的可选回退。
- Litematica + MaLiLib + Iris + Sodium：Fabric 上可选的投影光影兼容。
- TweakerMore：可选潜影盒快捷设置与混合潜影盒提示增强。
- Tweakeroo：支持版本下可与 Fabric 鞘翅换装逻辑协作。
- Xaero's Minimap / Xaero's World Map：可选服务器共享标点显示与管理。
- AbsServerTool：服务器统计、进度、性能分析与共享标点所需的可选服务端伴侣。

## 客户端与服务端怎么装

Ab's Tool 是客户端 Mod。不要为了服务器统计或共享标点把 Ab's Tool 装进服务端。

需要服务端功能时，在服务器安装 AbsServerTool。专用服务器不需要安装 Xaero。

## 本地数据隔离

Minecraft 26.2 使用原有文件：

- config/abstool.json
- config/abstool-vaults.json

Minecraft 26.3 使用独立文件：

- config/abstool-26.3.json
- config/abstool-26.3-vaults.json

这样两个游戏版本不会互相覆盖本地配置和宝库记录。
