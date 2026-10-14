# 设置与按键

[English](Configuration.md) · [首页](Home.zh-CN.md)

## 打开设置

### Minecraft 26.2

游戏内同时按 R+B。Fabric 还可以通过 Mod Menu 打开，NeoForge 使用常规模组配置入口。

### Minecraft 26.3

Ab's Tool 按键会出现在“控制 -> 按键绑定 -> Ab's Tool”。

- 设置快捷键默认由 R+B 两个可重绑定按键组成。
- 解绑其中一个后，可以只按剩下的一个键打开设置。
- 两个都解绑即可完全关闭设置快捷键。
- 26.3 的独立鞘翅起飞键默认未绑定，并且只有在起飞功能选择“独立按键触发”时才生效。

## 设置页面结构

统一设置界面包含“全部 / 追踪 / 优化”等区域；服务器统计作为独立顶部页面显示。

可选联动只有在对应依赖存在且兼容性检查通过时才显示相关设置，不会强制把可选 Mod 变成必装前置。

## 本地配置文件

Minecraft 26.2：

- config/abstool.json
- config/abstool-vaults.json

Minecraft 26.3：

- config/abstool-26.3.json
- config/abstool-26.3-vaults.json

当前配置写入逻辑会保留未知 JSON 字段，并沿用项目现有的备份行为。

## 常用诊断命令

Fabric 投影光影适配：

~~~text
/abstool schematicshader status
~~~

它会报告当前兼容状态，而不是对未知版本悄悄强行启用。

## 由服务器控制的设置

部分能力不能只在客户端决定：

- 共享标点权限和数量限制由 AbsServerTool 管理。
- 性能分析权限与采样限制由 AbsServerTool 管理。
- 服务器统计项目取决于 AbsServerTool 对外声明的能力。
- 熔炉追踪使用 Servux/Jade 时，数据访问还取决于服务器端对应 Mod 的配置。
