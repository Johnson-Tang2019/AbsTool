# AbsServerTool 联动

[English](Server-Integration.md) · [首页](Home.zh-CN.md)

`abstoolmod`（Ab's Tool）保持客户端定位。需要服务器权威数据的功能由独立项目 AbsServerTool 提供。

## 协议模型

当前客户端与服务端使用协议 v2，通过 Hello 握手和 capability 位图声明能力。服务器只声明自己真正实现的功能。

当前能力家族包括：

- 总览/统计数据。
- 日、周趋势。
- 各类排行榜，包括兼容服务端提供的“已完成可显示进度”排行。
- 服务器共享标点。
- 服务器性能分析。

因此旧版服务器仍可继续提供它理解的旧功能，不需要假装支持新功能。

## 服务器统计

“服务器统计”页面在检测到兼容能力后读取 AbsServerTool 数据。切换连接时客户端会清空会话缓存，并对“不支持、未连接、协议不匹配、超时”等状态给出明确提示。

## 性能分析

详细使用说明见[性能分析 GUI](Performance-Profiler.zh-CN.md)。


性能分析由服务器掌控：

- AbsServerTool 负责权限检查。
- 只有显式启动后服务器才进行采样。
- Ab's Tool 负责请求会话、状态和报告并显示结果。
- 客户端不会把这些诊断统计夸大成精确 JVM CPU 火焰图。

## 共享标点

AbsServerTool 负责共享标点数据集、revision、写权限、全量快照和实时 delta。服务器不需要安装 Xaero。

Minecraft 26.3 当前 AbsServerTool 0.3.7 将数据保存在世界存档：

~~~text
data/absservertool_shared_waypoints.json
~~~

默认管理权限为 OP 等级 2，默认最多 256 个共享标点。

## 熔炉追踪不是 AbsServerTool 功能

熔炉追踪不要求 AbsServerTool。它优先使用 Servux，也可以回退到 Jade。不要把这两类数据提供协议与 AbsServerTool 的协议 v2 统计/共享标点通道混为一谈。

## 服务端伴侣项目

https://github.com/Johnson-Tang2019/AbsServerTool
