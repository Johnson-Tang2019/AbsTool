# Client updates

Open **Ab's Tool Settings → All → Manage Ab's Tool Updates**. Automatic checks and Beta updates are enabled by default. Checks run in the background on startup and every six hours while the client is open. Turn off automatic checks to use **Check Now** only, or turn off Beta updates to see stable releases only.

Updates come from public [GitHub Releases](https://github.com/Johnson-Tang2019/AbsTool/releases). The checker requires a newer version, an exact Minecraft version and loader match, and a published SHA-256 for the JAR. Alpha builds, drafts, and downgrades are skipped. The updater installs Ab's Tool only; any changed requirements for other mods still need to be handled separately.

Select **Install Update** and confirm. Wait for the ready message, quit Minecraft, and then relaunch. The updater verifies the download, waits for the client to exit, keeps a backup, and atomically replaces the installed file. It keeps the existing filename to avoid duplicate JARs. The version displayed in the game comes from the JAR's metadata.

For an installation outside the active profile's `mods` directory, use the GitHub release and install manually. Each successful installation keeps `previous.jar` in its job folder under the profile's `.abstool-updates` directory. To roll back, quit Minecraft and copy that backup over the installed Ab's Tool JAR. Keep backups outside `mods`.
