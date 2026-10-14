# Troubleshooting

[简体中文](Troubleshooting.zh-CN.md) · [Home](Home.md)

## I cannot open Ab's Tool settings

- On 26.2, press R+B together.
- On 26.3, check Controls -> Key Binds -> Ab's Tool.
- If one of the two 26.3 settings keys is unbound, the other becomes a single-key shortcut.
- If both are unbound, the settings shortcut is disabled.
- Fabric users can also use Mod Menu when installed.

## Server Stats says unsupported

Server Stats requires a compatible AbsServerTool protocol-v2 server. A server can be reachable but still lack a newer capability such as advancements, shared waypoints, or profiling.

Use a server build matching the Minecraft version.

## Shared Xaero waypoints are unavailable

Check all of the following:

1. Xaero's Minimap is installed on the client.
2. The Xaero interface is supported by the current Ab's Tool line.
3. The server runs a matching AbsServerTool with shared-waypoint capability enabled.
4. The client received a full authoritative snapshot.
5. For publish/update/delete, the server granted management permission.

On Minecraft 26.2 NeoForge, the stable 0.4.12 line has the protocol but no Xaero display bridge. Use the 26.3 dual-loader line for the current NeoForge Xaero integration.

## Litematica projections disappear or corrupt with shaders

The adapter is intentionally gated.

- Confirm you are on Fabric.
- Confirm the Litematica/MaLiLib and Iris/Sodium versions match a supported family.
- Run:

~~~text
/abstool schematicshader status
~~~

Unknown or modified target bytecode can disable the adapter. This is safer than forcing an unverified render hook.

## Furnace tracker stays unknown or stale

Possible causes include:

- Servux provider unavailable or denied.
- Jade fallback unavailable.
- Request timeout.
- The furnace left the loaded scan area.
- Recipe/inventory data is missing or malformed.

A timeout cannot prove that a furnace is repaired.

## Low-ceiling elytra launch fails

The feature is experimental and server-validated.

- Ensure the feature is enabled.
- Ensure a usable glider exists in the normal inventory.
- Check for GUI or equipment conflicts.
- On supported Fabric Tweakeroo setups, unknown enabled Tweakeroo versions intentionally cancel instead of racing inventory actions.
- Successful fall-flying entry does not guarantee the player can steer through every narrow exit.

## 26.2 and 26.3 settings look different

This is intentional. The two Minecraft lines use separate config files and can have different key-binding/runtime APIs.
