# Server-shared waypoints (26.3)

Fabric and NeoForge: with compatible Xaero Minimap and World Map interfaces, right-click a local waypoint on the world map and choose **Publish to server**. A shared waypoint offers **Update server waypoint** and **Delete from server** (with confirmation). The existing Minimap waypoint list also retains these actions. Find that list through Minecraft Controls -> Xaero's Minimap -> Waypoints; bindings may be customized.

There is no Xaero version whitelist or upper limit. The current Minecraft 26.3 Fabric Minimap releases 26.5.1–26.6.0 and World Map releases 1.46.2–1.47.0, and NeoForge Minimap releases 26.5.2–26.6.0 and World Map releases 1.46.3–1.47.0, pass the required interface checks. Future releases are inspected before applying the optional integration; changed required interfaces disable it with a log message.

Ab's Tool bindings are listed under Controls -> Key Binds -> Ab's Tool on both loaders. Hold the two settings keys together (R+B by default). Unbind either one to use the other as a single key; unbind both to disable the settings shortcut. The dedicated elytra launch key defaults to unbound and takes effect when the feature is enabled with the key trigger selected. Vanilla persists all of these bindings in `options.txt`.

Enable server-shared waypoints in AbsTool settings. The server must advertise the capability and grant management permission (default OP level 2); a full snapshot must have arrived before writes are enabled. The right-click entries stay disabled for unsupported servers and read-only players. No minimap/map dependency is required on dedicated servers.

The 26.3 client follows AbsServerTool 0.3.6: combined `shared_waypoint_mutation_request` (1 create, 2 update, 3 delete), fixed-width integer colors, 8-character symbols, deltas with a nullable body, mutation dataset revision, and error message text. The internal client delta model remains 0/1/2 after decoding. The 26.2 code and protocol are unchanged.

Verification: `:fabric:verifyWaypoint` and `:neoforge:verifyWaypoint` check independent server-format wire fixtures. `verifyXaeroParity` compares the shared Xaero bytecode in both release JARs and validates their loader-specific mixin configuration; it is part of the root `build` and the release workflow. `:fabric:verifyXaero` and `:neoforge:verifyXaero` inspect real release JARs supplied through `-PxaeroReferenceDir=<reference directory>`.

The optional Fabric menu smoke remains available through `:fabric:runClient -PwaypointMenuSmoke -PxaeroReferenceDir=<26.3 Fabric mods directory>`. Only the two Xaero JARs are loaded; smoke fixtures are excluded from production artifacts. Builds and interface checks do not prove multiplayer mutation persistence or visual behavior on a live NeoForge client.
