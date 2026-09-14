# Reproducible verification

Use JDK 25 and the checked-in Gradle wrapper. All fixtures use project build/run
directories. Never copy test worlds or dependencies into an ordinary game profile.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-25'
./tools/Prepare-CompatibilityTests.ps1
./gradlew.bat assemble :fabric:verifySchematic
./gradlew.bat -PworldTests -PschematicTests -PwithIrisTest :fabric:runClientGameTest
./gradlew.bat -PclientSmoke :fabric:runClient
./gradlew.bat -PclientSmoke :neoforge:runClient
./gradlew.bat -PworldTests -PfurnaceTests -PacceptMinecraftEula -PwithJadeTest -PwithIrisTest :fabric:runClientGameTest
```

Optional-dependency startup checks use separate directories and require no
world creation:

```powershell
./gradlew.bat -PclientSmoke -PschematicStartup=none :fabric:runClient
./gradlew.bat -PclientSmoke -PschematicStartup=sodium :fabric:runClient
./gradlew.bat -PclientSmoke -PschematicStartup=litematica :fabric:runClient
./gradlew.bat -PclientSmoke -PschematicStartup=iris :fabric:runClient
./gradlew.bat -PclientSmoke -PschematicStartup=full :fabric:runClient
```

CI builds both loader artifacts and runs the new deterministic configuration and
version-policy checks. It does not launch a GPU game or claim visual acceptance.

Only use the EULA flag after accepting Minecraft's EULA (authorized for this
task's local tests). The schematic test does not require a dedicated server.
The existing furnace protocol fixture is synthetic Servux-wire coverage, not
proof of an actual Servux installation. Existing external Servux tests remain
available separately.

`verifySchematic` includes legacy JSON migration, unknown-field preservation, backup,
repeat-save stability, finite opacity, and record non-modification checks in
isolation from the original tracking/protocol tests. Per the user's instruction,
existing feature tests are not repeated after the initial baseline. With
`schematicTests`, only the new schematic entrypoint runs; without it the original
entrypoint list is restored by resource processing.

Screenshot acceptance must inspect actual textures and real-world shading.
Passing client assertions means only that the exercised assertions passed.

### Iris 1.11.2 / Sodium 0.9.1

After preparing optional test dependencies, run the same isolated projection
fixture with the additional pair (no existing tracking tests):

```powershell
./gradlew.bat --offline -PworldTests -PschematicTests -PwithIrisTest -Piris112Test :fabric:runClientGameTest
```

The flag selects both release JARs together. Omitting it retains the original
Iris 1.11.4 / Sodium 0.9.2 fixture. Do not combine Iris 1.11.2 with Sodium 0.9.2.
