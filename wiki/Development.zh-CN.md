# 开发与发布结构

[English](https://github.com/Johnson-Tang2019/AbsTool/wiki/Development) · [首页](https://github.com/Johnson-Tang2019/AbsTool/wiki/Home.zh-CN)

## 仓库结构

Minecraft 26.2 使用仓库根目录 Gradle 工程：

- common/
- 26.2Fabric/
- 26.2NeoForge/
- 根目录 gradle.properties

Minecraft 26.3 使用 26.3 下的独立工程：

- 26.3/common/
- 26.3/26.3Fabric/
- 26.3/26.3NeoForge/
- 26.3/gradle.properties

修改某个 Minecraft 版本时，应在对应版本的源码树中完成，不要让另一条版本线被顺手改坏。

两个工程都会把共享源码直接编进各自加载器 JAR，不需要 Architectury 运行时。

## 构建命令

Minecraft 26.2：

~~~text
./gradlew assemble
~~~

Minecraft 26.3：

~~~text
./26.3/gradlew -p 26.3 assemble
~~~

使用 Java 25。

## 当前版本基线

Minecraft 26.2 根工程：

- Ab's Tool 0.4.12
- Fabric Loader 0.19.3
- Fabric API 0.159.0+26.2
- Cloth Config 26.2.155
- NeoForge 26.2.0.77

Minecraft 26.3 工程：

- Ab's Tool 0.5.0-beta.6
- Fabric Loader 0.19.5
- Fabric API 0.160.6+26.3
- Cloth Config 26.3.158
- NeoForge 26.3.0.23-beta
- Gradle 9.6.0

## 发布标签

Minecraft 26.2 保留原标签格式：

~~~text
v<version>
~~~

Minecraft 26.3 使用：

~~~text
v<version>-mc26.3
~~~

例如：

~~~text
v0.5.0-beta.6-mc26.3
~~~

仓库使用独立发布工作流，避免 26.3 标签误发布 26.2 工程。

## 自动发布

当前发布流程可以根据版本标签构建加载器 JAR，并发布到 GitHub Releases 及已经配置的外部分发平台。维护者密钥和发布步骤见 docs/PUBLISHING.md。

## 许可证

项目元数据为 All Rights Reserved。不祥宝库功能包含从 Ominous Vault Track 适配的 MIT 代码，必须继续保留 THIRD_PARTY_NOTICES.md 与打包进 JAR 的许可证声明。

## 哪些文件优先作为事实来源

判断当前行为时建议按以下顺序：

1. 当前源码。
2. 当前 README / README.zh-CN。
3. CHANGELOG。
4. 各功能独立文档。

部分历史 STATUS 内容只描述旧版本，不应覆盖当前源码事实。
