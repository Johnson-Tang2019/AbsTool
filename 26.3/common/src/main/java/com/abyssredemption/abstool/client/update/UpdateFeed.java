package com.abyssredemption.abstool.client.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

/** Only consider versioned assets from this project's matching release tag. */
final class UpdateFeed {
    static final String REPO = "https://github.com/Johnson-Tang2019/AbsTool";
    static final long MAX_JAR = 32L * 1024 * 1024;
    private static final Pattern VERSION = Pattern.compile(
            "(0|[1-9][0-9]{0,8})\\.(0|[1-9][0-9]{0,8})\\.(0|[1-9][0-9]{0,8})(?:-beta\\.([1-9][0-9]{0,8})(?:-dev\\.[0-9]{1,9})?)?");

    record Version(int major, int minor, int patch, int beta, boolean dev) implements Comparable<Version> {
        static Version parse(String value) {
            var m = VERSION.matcher(value.split("\\+", 2)[0]);
            if (!m.matches()) return null;
            return new Version(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)), m.group(4) == null ? Integer.MAX_VALUE : Integer.parseInt(m.group(4)),
                    m.group().contains("-dev."));
        }
        @Override public int compareTo(Version other) {
            int result = Integer.compare(major, other.major);
            if (result == 0) result = Integer.compare(minor, other.minor);
            if (result == 0) result = Integer.compare(patch, other.patch);
            if (result == 0) result = Integer.compare(beta, other.beta);
            return result == 0 ? Boolean.compare(other.dev, dev) : result;
        }
    }

    record Offer(String version, Version order, String tag, String file, URI uri, String hash, long size) {}

    static Offer select(Iterable<JsonElement> releases, Version installed, String game, String loader, boolean beta) {
        if (installed == null) return null;
        String suffix = "-mc" + game;
        Offer best = null;
        for (var entry : releases) {
            if (!(entry instanceof JsonObject release) || !release.has("draft") || release.get("draft").getAsBoolean()) continue;
            String tag = string(release, "tag_name");
            if (!tag.startsWith("v") || !tag.endsWith(suffix)) continue;
            String version = tag.substring(1, tag.length() - suffix.length());
            Version order = Version.parse(version);
            if (order == null || order.dev || version.contains("+") || order.compareTo(installed) <= 0
                    || (!beta && (order.beta != Integer.MAX_VALUE || release.get("prerelease").getAsBoolean()))
                    || (best != null && order.compareTo(best.order) <= 0)) continue;
            String name = "abstool-" + loader + "-" + version + "+" + game + ".jar";
            String url = REPO + "/releases/download/" + tag + "/" + name;
            if (!release.has("assets") || !release.get("assets").isJsonArray()) continue;
            for (var item : release.getAsJsonArray("assets")) {
                if (!(item instanceof JsonObject asset) || !name.equals(string(asset, "name"))) continue;
                String digest = string(asset, "digest").toLowerCase(Locale.ROOT);
                String actualUrl = string(asset, "browser_download_url").replace("%2B", "+").replace("%2b", "+");
                long size = asset.has("size") ? asset.get("size").getAsLong() : 0;
                if (!actualUrl.equals(url) || !digest.matches("sha256:[a-f0-9]{64}") || size <= 0 || size > MAX_JAR) continue;
                best = new Offer(version, order, tag, name, URI.create(url.replace("+", "%2B")), digest.substring(7), size);
                break;
            }
        }
        return best;
    }

    private static String string(JsonObject object, String key) {
        var value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }
}
