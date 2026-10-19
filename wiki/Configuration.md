# Configuration and controls

[简体中文](Configuration.zh-CN.md) · [Home](Home.md)

## Opening settings

### Minecraft 26.2

Press R+B together in game. Fabric users may also use Mod Menu; NeoForge exposes its normal mod configuration entry.

### Minecraft 26.3

Ab's Tool key bindings are registered in vanilla Controls -> Key Binds -> Ab's Tool.

- The settings shortcut uses two configurable keys, R+B by default.
- Unbind either one to use the remaining key as a single-key shortcut.
- Unbind both to disable the shortcut.
- The dedicated elytra launch key is unbound by default in the 26.3 line and only matters when the feature is configured to use it.

## Settings layout

The shared settings screen contains All, Tracking, and Optimization areas. Server Stats is exposed as its own top-level page when applicable.

Optional integration categories appear only when their dependency and compatibility checks allow them.

## Screenshot image clipboard on 26.3

Starting with 0.5.0-beta.7, Fabric and NeoForge automatically copy the image itself to the system clipboard after a screenshot is saved, ready to paste into chats or image software. Vanilla PNG files remain in screenshots, and clipboard failures do not prevent file saves.

The feature defaults to enabled. Turn it off with Automatically copy screenshot images to the clipboard in Optimization. System clipboard operations use the game's SDL API; image conversion runs in the background.

## Local configuration files

Minecraft 26.2:

- config/abstool.json
- config/abstool-vaults.json

Minecraft 26.3:

- config/abstool-26.3.json
- config/abstool-26.3-vaults.json

The config writer preserves unknown JSON fields and keeps backup behavior used by the current project.

## Useful diagnostics

For the Fabric schematic/shader adapter:

~~~text
/abstool schematicshader status
~~~

The command reports the current compatibility state instead of silently enabling unknown combinations.

## Server-owned settings

Some features cannot be configured solely on the client:

- Shared waypoint permissions and limits are owned by AbsServerTool.
- Performance profiler permissions and sampling limits are owned by AbsServerTool.
- Server statistics depend on capabilities advertised by AbsServerTool.
- Furnace data access depends on the Servux/Jade server configuration when those providers are used.
