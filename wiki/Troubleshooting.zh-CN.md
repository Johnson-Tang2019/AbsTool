# 故障排查

[English](https://github.com/Johnson-Tang2019/AbsTool/wiki/Troubleshooting) · [首页](https://github.com/Johnson-Tang2019/AbsTool/wiki/Home.zh-CN)

## 打不开 Ab's Tool 设置

- 26.2：同时按 R+B。
- 26.3：检查“控制 -> 按键绑定 -> Ab's Tool”。
- 26.3 中如果解绑设置组合键中的一个，剩下的键会变成单键快捷方式。
- 两个都解绑会关闭设置快捷键。
- Fabric 安装 Mod Menu 后也可以从 Mod Menu 打开。

## 服务器统计提示不支持

“服务器统计”需要兼容的 AbsServerTool 协议 v2 服务端。即使服务器能连接，也可能没有更晚加入的“进度、共享标点、性能分析”等 capability。

请确保 AbsServerTool 与服务器 Minecraft 版本匹配。

## Xaero 共享标点不可用

依次检查：

1. 客户端已安装 Xaero's Minimap。
2. 当前 Xaero 接口被所用 Ab's Tool 版本支持。
3. 服务器安装匹配的 AbsServerTool，并启用了共享标点能力。
4. 客户端已经收到完整权威 snapshot。
5. 发布/更新/删除还需要服务端授予管理权限。

Minecraft 26.2 NeoForge 的稳定 0.4.12 只有网络协议，没有 Xaero 显示桥接。当前 NeoForge Xaero 完整集成在 26.3 双加载器版本线。

## 开光影后 Litematica 投影消失或异常

适配器会主动做兼容门控：

- 确认使用 Fabric。
- 确认 Litematica/MaLiLib 与 Iris/Sodium 属于支持组合。
- 运行：

~~~text
/abstool schematicshader status
~~~

未知版本或被修改的目标字节码可能导致适配被禁用。这是为了避免对未验证渲染路径强行注入。

## 熔炉追踪一直 unknown 或 stale

常见原因：

- Servux provider 不可用或权限不允许。
- Jade 回退不可用。
- 请求超时。
- 熔炉离开客户端已加载扫描范围。
- 配方或库存数据缺失/格式异常。

“没有响应”不能直接证明熔炉已经修复。

## 低顶空鞘翅起飞失败

该功能仍是实验性并且最终由服务器判定：

- 确认功能已经开启。
- 普通物品栏里存在可用滑翔装备。
- 检查是否有 GUI 或手动换装冲突。
- Fabric 使用受支持的 Tweakeroo 时，未知且开启的 Tweakeroo 版本会主动取消，而不是抢装备槽。
- 成功进入 fall-flying 也不代表任何狭窄出口都能稳定钻出。

## 26.2 与 26.3 设置表现不同

这是有意设计。两个 Minecraft 版本使用独立配置文件，并且输入/渲染 API 也可能不同。
