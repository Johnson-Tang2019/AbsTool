package com.abyssredemption.abstool.client.screenshot;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** PNG for image-aware clients and BMP for the native Windows bitmap clipboard. */
public record ClipboardImageFormats(byte[] png, byte[] bmp) {
    public static ClipboardImageFormats read(Path file) throws IOException {
        byte[] png = Files.readAllBytes(file);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        if (image == null) throw new IOException("Screenshot is not a readable image");
        var rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        var graphics = rgb.createGraphics();
        try { graphics.drawImage(image, 0, 0, null); } finally { graphics.dispose(); }
        var encoded = new ByteArrayOutputStream();
        if (!ImageIO.write(rgb, "bmp", encoded)) throw new IOException("BMP image writer is unavailable");
        return new ClipboardImageFormats(png, encoded.toByteArray());
    }
}
