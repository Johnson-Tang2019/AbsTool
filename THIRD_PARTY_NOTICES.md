# Third-party notices

The ominous vault feature is adapted from [Ominous Vault Track](https://github.com/Johnson-Tang2019/ominous-vault-track),
commit `64fcd0677483c7b52b23e6e69c6fc42bd2711f68`.
Copyright (c) 2026 Damomo1. Licensed under MIT.
The full license is included in both distribution JARs at
`META-INF/licenses/ominous-vault-track-LICENSE.txt`.

Changes include Minecraft 26.2 and NeoForge support, shared render snapshots,
Ab's Tool settings integration, the R+B shortcut, world isolation, and storage validation.

## Optional schematic compatibility dependencies

Litematica 0.28.8, MaLiLib 0.29.6, and Iris 1.11.4 are separately installed
LGPL-3.0 dependencies. The adapter calls their runtime APIs and supplies original
integration hooks. Their source files, shaders, assets, and complete JARs are not
copied into AbsTool. Sodium and Complementary Reimagined are also separate test
dependencies, not redistributed. Source references and artifact pins are listed
in `docs/SCHEMATIC_SHADER_COMPAT.md`.

AbsTool's All Rights Reserved declaration does not relicense these dependencies.
No general exemption from dependency license obligations is asserted for Mixin
integration. Redistributors must review applicable dependency terms before
combining or bundling artifacts; this project supplies no such bundle.
