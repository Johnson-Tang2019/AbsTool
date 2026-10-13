package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointBridge;
import com.abyssredemption.abstool.client.compat.xaero.XaeroWorldMapContract;
import com.abyssredemption.abstool.client.compat.xaero.XaeroMapMixinSupport;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipFile;

public final class XaeroCompatibilityVerification {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("Pass at least one Xaero JAR");
        if (plugin(null).shouldApplyMixin("xaero.map.mods.gui.WaypointReader", "WaypointReaderMixin")) {
            throw new AssertionError("Optional map mixin must stay disabled without dependencies");
        }
        int minimaps = 0, maps = 0;
        for (String filename : args) {
            if (Path.of(filename).getFileName().toString().startsWith("xaeroworldmap-")) {
                try (var archive = new ZipFile(filename)) {
                    XaeroWorldMapContract.ResourceReader resources = path -> {
                        var entry = archive.getEntry(path);
                        if (entry == null) throw new java.io.IOException("Missing " + path);
                        try (var input = archive.getInputStream(entry)) { return input.readAllBytes(); }
                    };
                    String rejection = XaeroWorldMapContract.rejection(resources);
                    if (!rejection.isEmpty()) throw new AssertionError(filename + ": " + rejection);
                    if (!plugin(resources).shouldApplyMixin("xaero.map.mods.gui.WaypointReader", "WaypointReaderMixin")) {
                        throw new AssertionError("Compatible map must enable its shared mixin: " + filename);
                    }
                    String menu = "xaero/map/mods/gui/WaypointReader.class";
                    var changed = new org.objectweb.asm.tree.ClassNode();
                    new org.objectweb.asm.ClassReader(resources.read(menu)).accept(changed, 0);
                    changed.methods.removeIf(m -> m.name.equals("getRightClickOptions"));
                    var writer = new org.objectweb.asm.ClassWriter(0);
                    changed.accept(writer);
                    if (XaeroWorldMapContract.rejection(path -> path.equals(menu) ? writer.toByteArray() : resources.read(path)).isEmpty()) {
                        throw new AssertionError("Changed menu interface must be rejected");
                    }
                    if (plugin(path -> path.equals(menu) ? writer.toByteArray() : resources.read(path))
                            .shouldApplyMixin("xaero.map.mods.gui.WaypointReader", "WaypointReaderMixin")) {
                        throw new AssertionError("Changed map must leave the shared mixin disabled");
                    }
                }
                maps++;
                continue;
            }
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
                    minimaps++;
                }
            } finally {
                for (Path extracted : nested) Files.deleteIfExists(extracted);
            }
        }
        System.out.println("Xaero contracts passed: " + minimaps + " Minimap and " + maps + " World Map JARs; changed menu interfaces rejected.");
    }
    private static XaeroMapMixinSupport plugin(XaeroWorldMapContract.ResourceReader resources) {
        var plugin = new XaeroMapMixinSupport() {
            protected java.util.Optional<XaeroWorldMapContract.ResourceReader> mapResources() {
                return java.util.Optional.ofNullable(resources);
            }
        };
        plugin.onLoad("com.abyssredemption.abstool.client.compat.xaero.mixin");
        return plugin;
    }
}
