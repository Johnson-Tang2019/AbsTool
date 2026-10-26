# Development and release structure

[简体中文](https://github.com/Johnson-Tang2019/AbsTool/wiki/Development.zh-CN) · [Home](https://github.com/Johnson-Tang2019/AbsTool/wiki/Home)

## Repository layout

Minecraft 26.2 is the root Gradle project:

- common/
- 26.2Fabric/
- 26.2NeoForge/
- root gradle.properties

Minecraft 26.3 is a separate project under:

- 26.3/common/
- 26.3/26.3Fabric/
- 26.3/26.3NeoForge/
- 26.3/gradle.properties

Changes for one Minecraft line must be made in that line's source tree.

The projects compile shared source directly into loader-specific JARs. No Architectury runtime is required.

## Build commands

Minecraft 26.2:

~~~text
./gradlew assemble
~~~

Minecraft 26.3:

~~~text
./26.3/gradlew -p 26.3 assemble
~~~

Java 25 is used.

## Current version baselines

Minecraft 26.2 root project:

- Ab's Tool 0.4.12
- Fabric Loader 0.19.3
- Fabric API 0.159.0+26.2
- Cloth Config 26.2.155
- NeoForge 26.2.0.77

Minecraft 26.3 project:

- Ab's Tool 0.5.0-beta.6
- Fabric Loader 0.19.5
- Fabric API 0.160.6+26.3
- Cloth Config 26.3.158
- NeoForge 26.3.0.23-beta
- Gradle 9.6.0

## Release tags

Minecraft 26.2 keeps the original tag pattern:

~~~text
v<version>
~~~

Minecraft 26.3 uses:

~~~text
v<version>-mc26.3
~~~

Example:

~~~text
v0.5.0-beta.6-mc26.3
~~~

The repository has separate release workflows so a 26.3 tag does not accidentally publish the 26.2 project.

## Publishing

Current release automation publishes loader JARs from version tags to GitHub Releases and the configured external distribution platforms. See docs/PUBLISHING.md for maintainer secrets and release steps.

## Licensing

Project metadata is All Rights Reserved. The ominous-vault feature contains code adapted from Ominous Vault Track under MIT; keep THIRD_PARTY_NOTICES.md and packaged license notices intact.

## Source-of-truth guidance

For current behavior, prefer:

1. Current source code.
2. Current README / README.zh-CN.
3. CHANGELOG.
4. Feature-specific docs.

Some historical STATUS sections may describe older releases and should not override the current source tree.
