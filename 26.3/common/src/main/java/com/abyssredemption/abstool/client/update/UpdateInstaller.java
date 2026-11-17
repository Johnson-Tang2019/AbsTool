package com.abyssredemption.abstool.client.update;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

/** Standalone helper: this class must run with only the JDK on its classpath. */
public final class UpdateInstaller {
    private UpdateInstaller() {}

    public static void main(String[] args) throws Exception {
        Path job = Path.of(args[1]).toRealPath();
        Path root = job.getParent();
        Path target = Path.of(args[2]);
        String result = "install_failed";
        try {
            var parent = ProcessHandle.of(Long.parseLong(args[0]));
            if (parent.isPresent()) parent.get().onExit().get(7, TimeUnit.DAYS);
            Path mods = root.getParent().resolve("mods");
            if (!target.getParent().equals(mods) || !mods.toRealPath().equals(mods)
                    || !target.getFileName().toString().endsWith(".jar")) throw new IOException("Unexpected install path");
            try (var lockFile = FileChannel.open(root.resolve("install.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 var lock = lockFile.lock()) {
                replace(target, job.resolve("update.jar"), job.resolve("previous.jar"), args[3], args[4]);
            }
            result = "installed";
        } finally {
            Files.deleteIfExists(job.resolve("update.jar"));
            Files.writeString(job.resolve("result.txt"), result);
        }
    }

    static void replace(Path target, Path staged, Path backup, String oldHash, String newHash) throws Exception {
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) || !Files.isRegularFile(staged, LinkOption.NOFOLLOW_LINKS)
                || !sha256(target).equals(oldHash) || !sha256(staged).equals(newHash)) {
            throw new IOException("Installed file changed or download checksum failed");
        }
        Files.copy(target, backup);
        if (!sha256(backup).equals(oldHash) || !sha256(target).equals(oldHash)) throw new IOException("Backup verification failed");
        // An unsupported or failed atomic move leaves the installed JAR untouched.
        Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    static String sha256(Path file) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        try (var stream = Files.newInputStream(file)) {
            var buffer = new byte[32768];
            for (int read; (read = stream.read(buffer)) != -1;) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
