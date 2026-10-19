# 服务器性能分析 GUI

[English](Performance-Profiler.md) · [首页](Home.zh-CN.md)

客户端 `abstoolmod`（Ab's Tool）只负责控制与显示。真正的采样、权限校验和报告都由 AbsServerTool 完成。

## 前提

服务器必须：

- 安装兼容的 AbsServerTool。
- 使用 protocol v2。
- 在 Hello capability 中提供性能分析 bit 9。
- 当前玩家具有服务端要求的管理员权限。

没有这项能力时，性能入口会保持不可用，不影响其他客户端功能。

## 启动分析

性能页面提供可编辑秒数输入框，默认值来自服务器，当前默认是 30 秒。

快捷值：

~~~text
10 / 30 / 60 / 120 / 300
~~~

快捷按钮只负责填写输入框。也可以直接输入例如 17、73、121 秒。最终允许范围由服务器 StatusResponse 给出并由服务端再次校验。

关闭 GUI **不会**停止服务器分析。

## 运行状态

分析运行中会显示 Session、目标时长和剩余时间。客户端最多约每 2 秒查询一次状态，本地根据 expected end time 绘制倒计时，不会每 Tick 发请求。

管理员可以点击“提前停止”生成当前报告。

## 报告页面

当前实现有四个 Tab：

1. 概览
2. 高负载区块
3. 玩家关联
4. 慢 Tick

概览包括平均 MSPT、P50/P95/P99、最大 Tick、阈值计数和各类插桩时间。

Chunk 页面支持分页并可点开详情，查看：

- Chunk / Entity / Block Entity / scheduled tick 时间。
- Tick Calls。
- Chunk Load / Generate。
- 玩家关联。
- Top Entity Types。
- Top Block Entity Types。

慢 Tick 页面会标记与 World Save 重叠的 Tick。

## 如何解释“玩家关联”

玩家关联是位置、视距/模拟范围与 Chunk 的**相关关系**，不是责任判定。

“关联 Chunk 测得耗时”不能写成“该玩家造成的耗时”。

## 如何解释“测得耗时”

AbsServerTool 只对指定 Minecraft Tick 路径做插桩。类别可能无法覆盖所有服务器工作，因此这些数据适合定位“哪里可疑”，不等价于 JVM CPU flamegraph。

需要继续定位到具体 Java 方法或 Mod 调用栈时，可配合 spark/JFR。
