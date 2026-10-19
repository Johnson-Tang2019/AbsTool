# Xaero 服务器共享标点

[English](Shared-Waypoints.md) · [首页](Home.zh-CN.md)

共享标点由服务器持有权威数据。Ab's Tool 在客户端把同步数据接入 Xaero；AbsServerTool 负责持久化、权限、快照、revision 和增删改。

只同步标点，不同步地图瓦片、已探索区域、轨迹或其他地图数据。

## Minecraft 26.2 稳定线

Ab's Tool 0.4.12 已包含可选共享标点协议。

- Fabric 已验证 Xaero's Minimap 26.5.0 的显示与管理桥接。
- 使用 Xaero 的 ThirdPartyWaypointManager，并使用独立的 abstool:server_waypoints 来源。
- 不直接修改玩家自己的 Xaero 普通标点文件。
- 26.2 NeoForge 会注册可选网络协议，但该稳定线没有 Xaero 显示桥接。

客户端只有在服务器提供匹配的 AbsServerTool 共享标点 capability 后才会启用共享操作。

## Minecraft 26.3 测试线

Ab's Tool 0.5.0-beta.6 已把 Xaero 核心实现迁到 Fabric/NeoForge 共享代码。

Fabric 与 NeoForge 都支持：

- 共享标点显示。
- 标点列表中的管理按钮。
- 服务器标点编辑界面。
- Xaero World Map 右键发布/更新/删除。
- 两端使用相同的服务器权限、capability 和 snapshot 门控。

26.3 不再写死 Xaero 最高版本号，而是在启用前检查需要的接口。

从 0.5.0-beta.7 开始，“服务器共享标点”设置页会在状态下方直接列出所有已同步标点，包括名称、维度和坐标。标点按颜色分组，每组可独立折叠，也可全部展开／折叠和手动刷新。修改颜色后，服务器确认的数据会自动移动到对应组。

点击“查看详情”或“编辑”可查看名称、维度 ID、X/Y/Z、符号、颜色与显示状态。普通玩家的字段只读；管理员可以保存修改。编辑期间失去权限、断开连接或该标点被其他人修改时，保存会停用。查看此列表无需安装 Xaero，地图中的显示仍需要可选 Xaero 集成。

当前最新代码核对通过的接口版本：

### Fabric

- Xaero's Minimap 26.5.1～26.6.0。
- Xaero's World Map 1.46.2～1.47.0。

### NeoForge

- Xaero's Minimap 26.5.2～26.6.0。
- Xaero's World Map 1.46.3～1.47.0。

后续版本如果保持所需接口也可能自动通过；接口发生变化时会安全停用可选桥接，而不是冒险让客户端崩溃。

## 管理员使用流程

在兼容服务器且拥有管理权限时：

1. 选择或右键一个本地 Xaero 标点。
2. 点击“发布到服务器”。
3. 服务器校验并创建权威共享标点。
4. 在线客户端收到更新。
5. 之后可通过 Xaero 提供的入口更新或从服务器删除。

只有在服务器声明能力、授予管理权限并且客户端已经收到完整权威快照后，写操作才会启用。

普通玩家接收只读共享标点。

## 服务端行为

Minecraft 26.3 的 AbsServerTool 0.3.7：

- 共享数据随世界存档持久化。
- 使用服务器权威 revision。
- 实时广播成功的修改。
- 默认管理权限为 OP 等级 2。
- 默认最多 256 个共享标点。
- 专用服务器无需安装 Xaero。

服务端配置请参考 AbsServerTool 项目。
