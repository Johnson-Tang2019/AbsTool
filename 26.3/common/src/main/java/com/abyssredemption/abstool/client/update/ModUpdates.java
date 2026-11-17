package com.abyssredemption.abstool.client.update;

import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public final class ModUpdates {
    private static final String API = "https://api.github.com/repos/Johnson-Tang2019/AbsTool/releases?per_page=100";
    private static final Set<String> HOSTS = Set.of("api.github.com", "github.com",
            "release-assets.githubusercontent.com", "objects.githubusercontent.com");
    private static final String INSTALLER = UpdateInstaller.class.getName();
    private static final String CLASS_FILE = INSTALLER.replace('.', '/') + ".class";
    private static final String GAME = "26.3";
    private static final java.util.concurrent.Executor WORKER = Executors.newSingleThreadExecutor(task -> {
        var thread = new Thread(task, "AbTool update");
        thread.setDaemon(true);
        return thread;
    });

    private static Path profile;
    private static Path jar;
    private static String loader;
    private static String version = "";
    private static long nextCheck;
    private static boolean checked;
    private static String lastNotified;
    private static boolean notify;
    private static boolean ready;
    private static boolean previousFailed;
    private static String status = "idle";
    private static UpdateFeed.Offer offer;

    private ModUpdates() {}

    public static void init(Path gameDir, Path modJar, String modLoader, String installed) {
        profile = gameDir;
        Path mods = gameDir.toAbsolutePath().normalize().resolve("mods");
        Path candidate = modJar == null ? null : modJar.toAbsolutePath().normalize();
        jar = candidate != null && mods.equals(candidate.getParent())
                && candidate.getFileName().toString().endsWith(".jar") ? candidate : null;
        loader = modLoader;
        version = installed;
    }

    public static void tick(Minecraft client) {
        if (!ready) {
            ready = true;
            if (profile != null) {
                Path root = profile.resolve(".abstool-updates");
                WORKER.execute(() -> {
                    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) return;
                    try (var jobs = Files.list(root)) {
                        var latest = jobs.filter(path -> path.getFileName().toString().matches("job-[0-9]+-[0-9]+"))
                                .max(java.util.Comparator.comparing(Path::toString));
                        if (latest.isPresent() && "install_failed".equals(Files.readString(latest.get().resolve("result.txt")))) {
                            client.execute(() -> previousFailed = true);
                        }
                    } catch (IOException ignored) {
                        // A job with no receipt may still be waiting for another client to close.
                    }
                });
            }
        }
        if (notify) {
            notify = false;
            if (offer != null && !offer.version().equals(lastNotified)) {
                lastNotified = offer.version();
                SystemToast.add(client.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                        text("title"), text("notification", offer.version()));
            }
        }
        if (ConfigManager.get().autoCheckUpdates && (!checked || System.nanoTime() - nextCheck >= 0) && !busy()) check(true);
    }

    static Component text(String key, Object... args) {
        return Component.translatable("text.abstool.updates." + key, args);
    }
    static Component statusText() {
        return offer == null ? text(status) : text(status, offer.version());
    }
    static boolean previousFailed() { return previousFailed; }
    static String installedVersion() { return version; }
    static UpdateFeed.Offer offer() { return offer; }
    static boolean busy() { return status.equals("checking") || status.equals("downloading") || status.equals("pending"); }
    static boolean canInstall() { return jar != null && offer != null && !busy(); }

    static void check(boolean automatic) {
        if (busy() || loader == null) return;
        checked = true;
        nextCheck = System.nanoTime() + java.time.Duration.ofHours(6).toNanos();
        var installed = UpdateFeed.Version.parse(version);
        if (installed == null) { status = "unsupported"; return; }
        var client = Minecraft.getInstance();
        boolean beta = ConfigManager.get().receiveBetaUpdates;
        status = "checking";
        offer = null;
        WORKER.execute(() -> {
            try (var stream = open(URI.create(API), "application/vnd.github+json")) {
                int limit = 8 * 1024 * 1024;
                byte[] data = stream.readNBytes(limit + 1);
                if (data.length > limit) throw new IOException("Release list too large");
                var found = UpdateFeed.select(JsonParser.parseString(new String(data, StandardCharsets.UTF_8))
                        .getAsJsonArray(), installed, GAME, loader, beta);
                client.execute(() -> {
                    offer = ConfigManager.get().receiveBetaUpdates == beta ? found : null;
                    status = offer == null ? "current" : jar == null ? "manual" : "available";
                    notify = automatic && offer != null && ConfigManager.get().autoCheckUpdates;
                });
            } catch (Exception failure) {
                AbsTool.LOGGER.warn("Could not check AbTool updates", failure);
                client.execute(() -> status = "check_failed");
            }
        });
    }

    static void betaChanged() {
        if (busy()) return;
        offer = null;
        status = "idle";
        check(false);
    }

    static void install(UpdateFeed.Offer selected) {
        if (!canInstall() || !offer.equals(selected)) return;
        var client = Minecraft.getInstance();
        status = "downloading";
        WORKER.execute(() -> {
            Path job = null;
            try {
                Path game = profile.toRealPath();
                Path mods = game.resolve("mods");
                Path target = jar.toAbsolutePath().normalize();
                if (!mods.equals(mods.toRealPath()) || !mods.equals(target.getParent())
                        || !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("Installed JAR must be directly inside the active profile's mods directory");
                }
                String originalHash = UpdateInstaller.sha256(target);
                Path root = game.resolve(".abstool-updates");
                Files.createDirectories(root);
                if (!root.equals(root.toRealPath())) throw new IOException("Unexpected updater directory");
                job = Files.createTempDirectory(root, "job-" + System.currentTimeMillis() + "-");
                Path staged = job.resolve("update.jar");
                try (var in = open(selected.uri(), "application/octet-stream");
                     var out = Files.newOutputStream(staged)) {
                    byte[] buffer = new byte[32768];
                    long remaining = selected.size();
                    while (remaining > 0) {
                        int count = in.read(buffer, 0, (int) Math.min(remaining, buffer.length));
                        if (count < 0) throw new IOException("Incomplete update download");
                        out.write(buffer, 0, count);
                        remaining -= count;
                    }
                    if (in.read() != -1) throw new IOException("Update exceeds the published size");
                }
                if (!UpdateInstaller.sha256(staged).equals(selected.hash())) throw new IOException("Update SHA-256 mismatch");
                Path helper = job.resolve(CLASS_FILE);
                Files.createDirectories(helper.getParent());
                try (var bytes = ModUpdates.class.getResourceAsStream("/" + CLASS_FILE)) {
                    if (bytes == null) throw new IOException("Missing installer helper");
                    Files.copy(bytes, helper);
                }
                boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");
                Path java = Path.of(System.getProperty("java.home"), "bin", windows ? "javaw.exe" : "java");
                var process = new ProcessBuilder(java.toString(), "-cp", job.toString(), INSTALLER,
                        Long.toString(ProcessHandle.current().pid()), job.toString(), target.toString(), originalHash, selected.hash())
                        .redirectErrorStream(true).redirectOutput(job.resolve("installer.log").toFile());
                for (String key : Set.of("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "CLASSPATH")) {
                    process.environment().remove(key);
                }
                var helperProcess = process.start();
                client.execute(() -> status = "pending");
                helperProcess.onExit().thenAccept(done -> client.execute(() -> {
                    if (done.exitValue() != 0 && status.equals("pending")) status = "install_failed";
                }));
            } catch (Exception failure) {
                if (job != null) {
                    try { Files.deleteIfExists(job.resolve("update.jar")); } catch (IOException ignored) {}
                }
                AbsTool.LOGGER.warn("Could not prepare AbTool update", failure);
                client.execute(() -> status = "install_failed");
            }
        });
    }

    private static InputStream open(URI url, String accept) throws IOException {
        try {
            return open(url, accept, null);
        } catch (IOException directFailure) {
            String configured = System.getenv("https_proxy");
            if (configured == null || configured.isBlank()) configured = System.getenv("HTTPS_PROXY");
            if (configured == null || configured.isBlank()) throw directFailure;
            URI proxy;
            try { proxy = URI.create(configured); }
            catch (IllegalArgumentException invalid) { throw directFailure; }
            if (!"http".equals(proxy.getScheme()) || proxy.getHost() == null || proxy.getUserInfo() != null) throw directFailure;
            return open(url, accept, new java.net.Proxy(java.net.Proxy.Type.HTTP,
                    new java.net.InetSocketAddress(proxy.getHost(), proxy.getPort() < 0 ? 80 : proxy.getPort())));
        }
    }

    private static InputStream open(URI url, String accept, java.net.Proxy proxy) throws IOException {
        for (int redirects = 0; redirects < 5; redirects++) {
            if (!"https".equals(url.getScheme()) || !HOSTS.contains(url.getHost())
                    || url.getUserInfo() != null || url.getPort() != -1) throw new IOException("Unexpected update host");
            var address = url.toURL();
            var connection = (HttpURLConnection) (proxy == null ? address.openConnection() : address.openConnection(proxy));
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "AbsTool-Update/" + version);
            connection.setRequestProperty("Accept", accept);
            try {
                int code = connection.getResponseCode();
                if (code == 200) {
                    var stream = connection.getInputStream();
                    return new java.io.FilterInputStream(stream) {
                        @Override public void close() throws IOException {
                            try { super.close(); } finally { connection.disconnect(); }
                        }
                    };
                }
                if (code < 300 || code >= 400 || connection.getHeaderField("Location") == null) {
                    throw new IOException("Update HTTP " + code);
                }
                url = url.resolve(connection.getHeaderField("Location"));
            } catch (IOException | RuntimeException failure) {
                connection.disconnect();
                throw failure;
            }
            connection.disconnect();
        }
        throw new IOException("Too many update redirects");
    }
}
