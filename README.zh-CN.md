# Ab's Tool

[English](README.md)

[项目仓库](https://github.com/Johnson-Tang2019/AbsTool)

仿照 AbsMod 结构建立的 Minecraft 26.2 Fabric／NeoForge 双加载器工具模组。
Mod ID：`abstool`。基础包名：`com.abyssredemption.abstool`。

## 设置分类与投影光影兼容

R+B 打开统一设置页，分类为 **全部／追踪／优化**。
ModMenu 和 NeoForge 设置按钮使用同一界面；保留已有设置、未知 JSON 字段和宝库记录。

“优化”中新增实验性的 **Fabric** Litematica／Iris 适配。AUTO 仅适用于
Minecraft 26.2、Litematica 0.28.8、MaLiLib 0.29.6、Iris 1.11.4+mc26.2、
Sodium 0.9.2+mc26.2 和 OpenGL，需要分别安装依赖。OFF 恢复上游绘制。
NeoForge 暂不启用这项新适配。
已在 Complementary Reimagined r5.9.1 下实际检查带贴图、半透明投影，
不代表所有光影包或其他版本均已兼容。客户端诊断命令：`/abstool schematicshader status`。

[实现与限制](docs/SCHEMATIC_SHADER_COMPAT.md) ·
[验证记录](docs/COMPATIBILITY_MATRIX.md) · [测试命令](docs/TESTING.md)

## 目录结构

- `common/src/main/java`：共享逻辑及独立的客户端初始化入口。
- `common/src/main/resources`：共享资源和语言文件。
- `26.2Fabric`（Gradle `:fabric`）：Fabric 通用／客户端入口及元数据。
- `26.2NeoForge`（Gradle `:neoforge`）：NeoForge 通用／客户端入口及元数据。

共享源码直接编译进各加载器的 JAR，common 是源码目录，并非 Gradle 子项目。
无需 Architectury 运行时。

## 不祥宝库追踪

- 穿过方块显示已加载的不祥宝库轮廓，忽略普通宝库。
- 开启追踪时右键宝库，将其在本地标记为已处理；不拦截正常交互，
  也不代表服务器已发放奖励。
- 已处理宝库可隐藏或改用另一种颜色；记录按服务器地址／单人存档路径和维度隔离。
- 可设置高亮与追踪线颜色、1–32 区块范围、追踪线开关，以及是否需要任一手持有指定物品
  （默认物品为不祥试炼钥匙）。
- 可按现实时间冷却或本地每日指定时刻清除已处理标记；范围可选当前维度、
  当前服务器／存档全部维度或所有记录。

在游戏中**同时按 R+B** 打开设置。追踪功能关闭时快捷键仍有效，聊天或其他界面中
不会触发。Fabric 安装可选的 **Mod Menu → Ab's Tool → 配置** 后可打开相同页面；
NeoForge 使用自带的 **模组 → Ab's Tool → 配置** 入口。
功能默认关闭，请在设置页开启高亮并保存。

客户端需要安装对应加载器的 **Cloth Config 26.2.155 或兼容版本**。
Fabric 还需 Fabric API，Mod Menu 20.0.2 为可选项。追踪不需要服务端安装此模组。
扫描只检查客户端已收到的区块，不主动加载远处区块。
刷新本地标记不会重置 Minecraft 宝库的奖励领取状态。

设置保存在 `config/abstool.json`，已处理记录保存在 `config/abstool-vaults.json`。
保存时会将上一版文件保留为 `.bak`。不会自动导入原 Ominous Vault Track 的文件。
原项目来源和 MIT 许可见[第三方声明](THIRD_PARTY_NOTICES.md)。

## 构建与运行

安装 JDK 25，将 JAVA_HOME 指向安装目录。Wrapper 使用 Gradle 9.5.1。
依赖版本固定在 `gradle.properties` 和 `build.gradle` 中。

```powershell
.\gradlew.bat build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :neoforge:runClient
.\gradlew.bat :fabric:runServer
.\gradlew.bat :neoforge:runServer
```

macOS／Linux 使用 `bash ./gradlew` 替换 `.\gradlew.bat`。
启动服务端需要在生成的运行目录中接受 Minecraft EULA。

发布 JAR 位于 `26.2Fabric/build/libs` 与 `26.2NeoForge/build/libs`。
只安装对应加载器的 JAR；Fabric 版还需 Fabric API 和 Cloth Config，
NeoForge 客户端还需 NeoForge 版 Cloth Config。
以 `-sources.jar` 结尾的文件供开发使用。

## 验证

`build` 会运行 `:fabric:verifyVault`，检查共享配置、组合键、记录持久化、
作用域隔离和定时刷新。两个加载器的 JAR 都编译同一套共享功能源码。

以下可选开发测试会打开设置页、验证返回父界面与渲染管线初始化，然后自动退出。
测试模组使用独立源码集，不进入发布 JAR：

```powershell
.\gradlew.bat -PclientSmoke -PwithModMenu :fabric:runClient
.\gradlew.bat -PclientSmoke :fabric:runClient
.\gradlew.bat -PclientSmoke :neoforge:runClient
.\gradlew.bat -PworldTests :fabric:runClientGameTest
```

Fabric 世界测试会创建独立存档，检查不祥宝库筛选、右键标记、已处理颜色、
追踪线持物条件与 R+B，并输出截图。

游戏内人工检查：开启追踪，放置不祥与普通宝库，确认仅不祥宝库显示轮廓；
检查右键隐藏、已处理颜色、追踪线持物条件、切换维度／存档及重连记录。
同时实测 R+B 按键和模组菜单入口。设置页自动检查不等同于游戏内视觉验收。

游戏逻辑放在 common，加载器注册适配放在各加载器目录；
客户端游戏类仅从客户端代码引用。
标识符和注释使用英文，界面文字使用语言文件，并同步维护两版 README。
元数据沿用参考项目的 All Rights Reserved 声明。

## 熔炉输入堵塞追踪

通过 **R+B**、Fabric 的 Mod Menu 或 NeoForge 模组设置入口，进入“熔炉堵塞追踪”分类并启用。
熔炉和宝库追踪各自独立。默认扫描半径 256 格，可选 128 格或全部客户端已加载区块。
每 tick 最多检查 8 个区块的方块实体，只发现普通熔炉、高炉和烟熏炉，不主动加载区块。

优先使用 Servux：服务器需要公布 `servux:entity_data` 通道、启用该功能、接受协议版本 2，
并允许当前玩家查询。服务器无需安装 Ab's Tool。当前实现针对 26.2 的带长度前缀 gzip NBT 协议，
保留其他模组（如 MiniHUD）已有的通道编解码器和接收器。没有握手响应也可能是权限或设置问题。

Jade 是可选备用，需要客户端和服务器都安装兼容的 Jade 26.2。复用 Jade 的握手和提供器映射，
默认额外查询距离为 21 格，加上玩家交互距离；管理员提高服务器规则后，可相应调整客户端值。
Jade 仍执行自己的距离、区块加载和提供器检查。追踪器的响应不会替换 Jade 准星提示的数据。

只标记“非空输入不属于服务器同步的对应炉型可烧炼物品集合”的情况。缺燃料、输出满、正常烧炼均不算堵塞。
库存缺失、格式无效或缺少配方同步时保持未知。这些同步集合按物品类型判断，不在客户端重建依赖物品组件的自定义配方逻辑。

默认每秒 20 次请求，可调 1–40 次，并限制突发请求。新发现和堵塞目标优先，每三次调度至少有一次按最久未检查排序，
保证普通目标继续巡检。堵塞目标最快每 2 秒复查，普通目标每 5 秒；单次请求 5 秒超时。
Servux 连续三次超时后会重新握手并尝试 Jade。只有新的有效数据能确认堵塞已解除。

确认堵塞时显示红框、追踪线和物品名称。超时、超出已加载扫描区域或数据超过默认 30 秒（可调）时，
改为灰色并显示“上次发现堵塞（已过期）”。设置页显示已发现和未知／过期数量。
内存中最多保留 8192 条；断线或切换维度时清空，防止不同世界串记录，不跨会话保存。
名称仅显示最接近准星的目标，避免密集阵列文字重叠。
Fabric 已目视验证 Iris 1.11.4、Sodium 0.9.2 和 Complementary Reimagined r5.9.1 的穿墙框线，
不代表所有光影包均已验证。两种加载器均已连接官方 Servux 0.11.5 服务器测试，Fabric 的备用通道已测试 Jade 26.2.11。

### 可选熔炉兼容性测试

下面的脚本仅向忽略的 build 目录下载固定版本测试模组，并检查官方 SHA-512 校验值：

```powershell
./tools/Prepare-CompatibilityTests.ps1
./gradlew.bat -PworldTests -PfurnaceTests -PacceptMinecraftEula -PwithJadeTest -PwithIrisTest :fabric:runClientGameTest
```

仅在接受 Minecraft EULA 后使用 `-PacceptMinecraftEula`。测试创建隔离本地服务器，使用 Servux v2 协议测试夹具，
并测试真实 Jade 备用通道、范围切换、修复、过期、请求限速和启用光影后的截图。
去掉 `-PwithIrisTest` 可测试原版渲染。测试夹具不进入正式 JAR。

官方 Servux 服务器测试使用单独的本地目录。先启动服务器，再在另一个终端运行其中一个客户端测试，结束后停止测试服务器：

```powershell
./tools/Prepare-CompatibilityTests.ps1 -PrepareServuxServer -AcceptMinecraftEula
./gradlew.bat -PwithRealServuxTest :fabric:runServuxTestServer
./gradlew.bat -PclientSmoke -PexternalServuxTests :fabric:runClient
./gradlew.bat -PclientSmoke -PexternalServuxTests :neoforge:runClient
```

协议参考：[Servux 26.2 entity provider](https://github.com/sakura-ryoko/servux/blob/e40a7562f87b3ac0e557439728f8208aba9b66a6/src/main/java/fi/dy/masa/servux/dataproviders/EntitiesDataProvider.java)、
[Jade 26.2 furnace provider](https://github.com/Snownee/Jade/blob/747effeddcea3094b940772c7963c272bb2a07df/src/main/java/snownee/jade/addon/vanilla/FurnaceProvider.java)。
