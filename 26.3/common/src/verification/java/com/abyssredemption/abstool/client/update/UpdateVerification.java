package com.abyssredemption.abstool.client.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public final class UpdateVerification {
    private static final String HASH = "1".repeat(64);

    public static void run() throws Exception {
        var list = new JsonArray();
        list.add(release("0.5.0-beta.9", "26.3", "fabric", false, HASH));
        list.add(release("0.5.0-beta.10", "26.3", "fabric", false, HASH));
        list.add(release("0.5.0-beta.11", "26.3", "neoforge", false, HASH));
        list.add(release("0.5.0-beta.12", "26.4", "fabric", false, HASH));
        list.add(release("0.5.0-beta.13", "26.3", "fabric", true, HASH));
        list.add(release("0.5.0-beta.14", "26.3", "fabric", false, ""));
        list.add(release("0.5.0-alpha.15", "26.3", "fabric", false, HASH));
        var installed = UpdateFeed.Version.parse("0.5.0-beta.8+26.3");
        check(UpdateFeed.select(list, installed, "26.3", "fabric", true).version().equals("0.5.0-beta.10"),
                "Expected numeric beta ordering and matching asset");
        check(UpdateFeed.select(list, installed, "26.3", "neoforge", true).version().equals("0.5.0-beta.11"),
                "NeoForge should select its own asset");
        check(UpdateFeed.select(list, installed, "26.3", "fabric", false) == null, "Stable channel excludes betas");
        check(UpdateFeed.select(list, UpdateFeed.Version.parse("0.5.0"), "26.3", "fabric", true) == null,
                "Stable version must not downgrade to a beta");

        Path game = Files.createTempDirectory("abstool-update-test").toRealPath();
        Process blocker = null, helper = null;
        try {
            var old = Files.createDirectory(game.resolve("mods")).resolve("abstool.jar");
            var job = Files.createDirectories(game.resolve(".abstool-updates/job-1-1"));
            var staged = job.resolve("update.jar");
            Files.writeString(old, "old");
            Files.writeString(staged, "new");
            String before = UpdateInstaller.sha256(old), after = UpdateInstaller.sha256(staged);
            try {
                UpdateInstaller.replace(old, staged, job.resolve("previous.jar"), before, HASH);
                throw new AssertionError("Expected corrupt update to be rejected");
            } catch (java.io.IOException expected) {
                check(Files.readString(old).equals("old"), "Rejected update changed the installed JAR");
            }
            var java = ProcessHandle.current().info().command().orElseThrow();
            var classpath = System.getProperty("java.class.path");
            blocker = new ProcessBuilder(java, "-cp", classpath, UpdateVerification.class.getName()).start();
            helper = new ProcessBuilder(java, "-cp", classpath, UpdateInstaller.class.getName(),
                    Long.toString(blocker.pid()), job.toString(), old.toString(), before, after).inheritIO().start();
            check(!helper.waitFor(350, TimeUnit.MILLISECONDS) && Files.readString(old).equals("old"),
                    "Installer ran before the game exited");
            blocker.destroy();
            check(blocker.waitFor(10, TimeUnit.SECONDS), "Fixture did not exit");
            check(helper.waitFor(15, TimeUnit.SECONDS) && helper.exitValue() == 0, "Installer process failed");
            check(Files.readString(old).equals("new") && Files.readString(job.resolve("previous.jar")).equals("old"),
                    "Expected atomic replacement and a backup");
            check(Files.readString(job.resolve("result.txt")).equals("installed"), "Missing installation receipt");
        } finally {
            for (var process : new Process[]{helper, blocker}) {
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                    process.waitFor(10, TimeUnit.SECONDS);
                }
            }
            try (var paths = Files.walk(game)) {
                for (var path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
        System.out.println("Update verification passed: channel/loader selection, integrity rejection, exit wait, backup and replacement.");
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }

    private static JsonObject release(String version, String game, String loader, boolean draft, String hash) {
        var release = new JsonObject();
        var tag = "v" + version + "-mc" + game;
        release.addProperty("tag_name", tag);
        release.addProperty("draft", draft);
        release.addProperty("prerelease", version.contains("-"));
        var assets = new JsonArray();
        var asset = new JsonObject();
        var name = "abstool-" + loader + "-" + version + "+" + game + ".jar";
        asset.addProperty("name", name);
        asset.addProperty("browser_download_url", UpdateFeed.REPO + "/releases/download/" + tag + "/" + name.replace("+", "%2B"));
        asset.addProperty("digest", "sha256:" + hash);
        asset.addProperty("size", 12345);
        assets.add(asset);
        release.add("assets", assets);
        return release;
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
