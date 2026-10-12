package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.fabric.compat.xaero.XaeroWaypointBridge;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipFile;

public final class XaeroCompatibilityVerification {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("Pass at least one Xaero JAR");
        for (String filename : args) {
            var nested = new ArrayList<Path>();
            try {
                var urls = new ArrayList<java.net.URL>();
                urls.add(Path.of(filename).toUri().toURL());
                try (var archive = new ZipFile(filename)) {
                    for (var entries = archive.entries(); entries.hasMoreElements();) {
                        var entry = entries.nextElement();
                        if (!entry.getName().endsWith(".jar")) continue;
                        Path extracted = Files.createTempFile("xaero-compat-", ".jar");
                        try (var input = archive.getInputStream(entry)) {
                            Files.copy(input, extracted, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                        nested.add(extracted);
                        urls.add(extracted.toUri().toURL());
                    }
                }
                try (var loader = new URLClassLoader(urls.toArray(java.net.URL[]::new),
                        XaeroCompatibilityVerification.class.getClassLoader())) {
                    if (!XaeroWaypointBridge.verifyContract(loader)) {
                        throw new AssertionError("Unsupported Xaero waypoint contract: " + filename);
                    }
                }
            } finally {
                for (Path extracted : nested) Files.deleteIfExists(extracted);
            }
        }
        System.out.println("Xaero waypoint contract passed for " + args.length + " JARs.");
    }
}
