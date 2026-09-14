package com.abyssredemption.abstool.client.vault.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Keep the previous file as a recovery copy and replace only complete JSON writes. */
public final class JsonFiles {
    private JsonFiles() {
    }

    public static void write(Path path, String json) throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, json + "\n", StandardCharsets.UTF_8);
            if (Files.exists(path)) {
                Files.copy(path, path.resolveSibling(path.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
