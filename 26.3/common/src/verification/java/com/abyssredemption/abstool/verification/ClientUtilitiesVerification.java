package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.screenshot.ClipboardImageFormats;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClientState;
import com.abyssredemption.abstool.client.waypoint.WaypointDraft;
import com.abyssredemption.abstool.client.waypoint.gui.WaypointBrowserModel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/** Independent image decoding, real screenshot bytecode, live groups, and permission loss. */
public final class ClientUtilitiesVerification {
    public static void main(String[] args) throws Exception {
        imageFormats(); screenshotHook(); groups(); permissions(); CategoryVerification.run();
        System.out.println("Client utilities passed: image pixels/orientation, screenshot hook, color grouping/folding, live refresh and viewer/admin write gates.");
    }
    private static SharedWaypoint waypoint(UUID id, String name, int color, long revision) {
        return new SharedWaypoint(id, name, "minecraft:overworld", -12345, 72, 9000, color, "P", false,
                new UUID(0, 1), 1, revision, revision);
    }
    private static void imageFormats() throws Exception {
        var image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        int[] pixels = {0xFF0000, 0x00FF00, 0x0000FF, 0x102030, 0xABCDEF, 0xFFFFFF};
        image.setRGB(0, 0, 3, 2, pixels, 0, 3);
        var file = Files.createTempFile("abstool-image-", ".png");
        try {
            ImageIO.write(image, "png", file.toFile());
            byte[] saved = Files.readAllBytes(file);
            var formats = ClipboardImageFormats.read(file);
            check(java.util.Arrays.equals(saved, formats.png()) && java.util.Arrays.equals(saved, Files.readAllBytes(file)), "Original PNG must remain intact");
            for (var bytes : List.of(formats.png(), formats.bmp())) {
                var decoded = ImageIO.read(new ByteArrayInputStream(bytes));
                check(decoded != null && decoded.getWidth() == 3 && decoded.getHeight() == 2, "Both clipboard formats must decode");
                for (int y = 0; y < 2; y++) for (int x = 0; x < 3; x++) {
                    check((decoded.getRGB(x, y) & 0xFFFFFF) == pixels[y * 3 + x], "Pixels and orientation must match the screenshot");
                }
            }
        } finally { Files.delete(file); }
    }
    private static ClassNode resource(String name) throws Exception {
        var node = new ClassNode();
        try (var input = ClientUtilitiesVerification.class.getClassLoader().getResourceAsStream(name)) {
            if (input == null) throw new AssertionError("Missing " + name);
            new ClassReader(input).accept(node, 0);
        }
        return node;
    }
    private static void screenshotHook() throws Exception {
        var screenshot = resource("net/minecraft/client/Screenshot.class");
        int targets = 0;
        for (var method : screenshot.methods) for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals("com/mojang/blaze3d/platform/NativeImage")
                    && call.name.equals("writeToFile") && call.desc.equals("(Ljava/io/File;)V")) targets++;
        }
        check(targets > 0, "The screenshot image-save hook must exist in this loader's Minecraft bytecode");
        var mixin = resource("com/abyssredemption/abstool/mixin/ScreenshotClipboardMixin.class");
        boolean original = false, after = false;
        for (var method : mixin.methods) for (var instruction : method.instructions) if (instruction instanceof MethodInsnNode call) {
            if (call.owner.equals("com/llamalad7/mixinextras/injector/wrapoperation/Operation") && call.name.equals("call")) original = true;
            if (call.owner.endsWith("/ScreenshotClipboard") && call.name.equals("saved")) after = original;
        }
        check(after, "Clipboard work must start after vanilla's successful file write");
    }
    private static void groups() {
        var state = new SharedWaypointClientState();
        var model = new WaypointBrowserModel();
        state.hello(true);
        var red = waypoint(UUID.randomUUID(), "Red", 12, 1);
        var white = waypoint(UUID.randomUUID(), "White", 15, 1);
        var second = waypoint(UUID.randomUUID(), "Another", 12, 1);
        state.snapshot(1, List.of(white, red, second)); model.refresh(state);
        check(model.groups().size() == 2 && model.groups().getFirst().color() == 12 && model.rows().size() == 5, "All points, including hidden points, must be grouped by color");
        model.toggle(12);
        check(model.rows().size() == 3 && state.waypoints().size() == 3, "Folding must preserve the authoritative dataset");
        var changed = waypoint(red.id(), red.name(), 1, 2);
        state.delta(2, 1, red.id(), 2, changed); model.refresh(state);
        check(model.groups().getFirst().color() == 1 && model.rows().stream().anyMatch(r -> changed.equals(r.waypoint())), "Changed colors must move to their new groups");
        model.foldAll(true); check(model.rows().size() == model.groups().size(), "Collapse all");
        model.foldAll(false); check(model.rows().size() == 6, "Expand all");
        long version = state.viewVersion(); state.permissions(true);
        check(state.viewVersion() > version, "Permission changes must invalidate the view");
        state.clear(); model.refresh(state); check(model.rows().isEmpty(), "Disconnect must remove stale points");
    }
    private static void permissions() {
        var sent = new java.util.ArrayList<net.minecraft.network.protocol.common.custom.CustomPacketPayload>();
        SharedWaypointClient.install(sent::add, ignored -> true);
        ConfigManager.get().serverWaypoints.enabled = true;
        var state = SharedWaypointClient.state(); state.hello(true);
        var point = waypoint(UUID.randomUUID(), "Point", 5, 1); state.snapshot(1, List.of(point));
        var draft = new WaypointDraft("Changed", "minecraft:the_nether", -5, 64, 100, 14, "C", true);
        check(!SharedWaypointClient.update(point, draft) && !SharedWaypointClient.create(draft) && !SharedWaypointClient.delete(point) && sent.isEmpty(), "Viewers must never send mutations");
        state.permissions(true); check(SharedWaypointClient.update(point, draft) && sent.size() == 1, "Administrators may submit edits");
        check(state.waypoints().get(point.id()).equals(point), "Wait for authoritative server updates");
        state.permissions(false); check(!SharedWaypointClient.update(point, draft) && sent.size() == 1, "Loss of permission must block an open editor");
        state.permissions(true); ConfigManager.get().serverWaypoints.enabled = false;
        check(!SharedWaypointClient.update(point, draft), "Disabled feature must not send edits");
        ConfigManager.get().serverWaypoints.enabled = true; state.clear(); SharedWaypointClient.install(null, null);
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
