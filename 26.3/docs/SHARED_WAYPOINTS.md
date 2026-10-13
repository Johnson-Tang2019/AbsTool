# Server-shared waypoints (26.3)

Fabric: with Xaero's Minimap 26.5.3 and World Map 1.46.4, right-click a local waypoint on the world map and choose **Publish to server**. A shared waypoint offers **Update server waypoint** and **Delete from server** (with confirmation). The existing Minimap waypoint list also retains these actions. Find that list through Minecraft Controls -> Xaero's Minimap -> Waypoints; bindings may be customized.

Enable server-shared waypoints in AbsTool settings. The server must advertise the capability and grant management permission (default OP level 2); a full snapshot must have arrived before writes are enabled. The right-click entries stay disabled for unsupported servers and read-only players. No minimap/map dependency is required on dedicated servers.

The 26.3 client follows AbsServerTool 0.3.6: combined `shared_waypoint_mutation_request` (1 create, 2 update, 3 delete), fixed-width integer colors, 8-character symbols, deltas with a nullable body, mutation dataset revision, and error message text. The internal client delta model remains 0/1/2 after decoding. The 26.2 code and protocol are unchanged.

Verification: `:fabric:verifyWaypoint` checks independent server-format wire fixtures. Run the actual optional-map injection and permissions smoke with `:fabric:runClient -PwaypointMenuSmoke -PxaeroReferenceDir=<26.3 Fabric mods directory>`. Only the two Xaero JARs are loaded; the smoke source set is excluded from production artifacts. NeoForge receives the protocol alignment but currently has no Xaero GUI bridge.
