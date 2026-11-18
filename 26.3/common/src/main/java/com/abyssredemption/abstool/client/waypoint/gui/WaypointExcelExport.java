package com.abyssredemption.abstool.client.waypoint.gui;

import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.export.ExcelWorkbook;
import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/** Exports a snapshot of the current server's synchronized waypoints. */
final class WaypointExcelExport {
    private static boolean writing;
    private static Path lastFile;
    private static long lastConnection;

    private WaypointExcelExport() {}

    static boolean canExport() {
        var state = SharedWaypointClient.state();
        return !writing && state.supported() && state.synced();
    }

    static Path lastFile() {
        return lastConnection == SharedWaypointClient.state().connectionId() ? lastFile : null;
    }

    static void start() {
        if (!canExport()) return;
        var client = Minecraft.getInstance();
        var state = SharedWaypointClient.state();
        long connectionId = state.connectionId();
        var points = state.waypoints().values().stream()
                .sorted(Comparator.comparingInt(SharedWaypoint::color)
                        .thenComparing(SharedWaypoint::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(SharedWaypoint::id))
                .toList();
        List<List<?>> rows = new ArrayList<>(points.size());
        for (var point : points) {
            String category = state.categoryName(point.color());
            if (category.isEmpty()) category = Component.translatable("text.abstool.waypoint.category_default").getString();
            rows.add(List.of(point.name(), point.dimensionId(), point.x(), point.y(), point.z(), category,
                    WaypointPalette.name(point.color()).getString(), String.format(Locale.ROOT, "#%06X", WaypointPalette.rgb(point.color())),
                    point.symbol(), point.enabled()));
        }
        List<String> headers = List.of("name", "dimension", "x", "y", "z", "category", "color", "rgb", "symbol", "enabled")
                .stream().map(key -> text(key).getString()).toList();
        var sheet = new ExcelWorkbook.Table(text("sheet").getString(), headers, List.copyOf(rows));
        var directory = client.gameDirectory.toPath().resolve("abstool-exports");
        writing = true;
        CompletableFuture.supplyAsync(() -> ExcelWorkbook.save(directory, "server-waypoints", List.of(sheet)))
                .whenComplete((path, error) -> client.execute(() -> {
                    writing = false;
                    Component message;
                    if (error == null) {
                        lastFile = path;
                        lastConnection = connectionId;
                        message = Component.translatable("abstool.stats.export.saved", path.getFileName().toString());
                    } else {
                        AbsTool.LOGGER.warn("Could not export shared waypoints", error);
                        message = Component.translatable("abstool.stats.export.write_failed");
                    }
                    SystemToast.add(client.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                            text("button"), message);
                }));
    }

    static Component text(String key) {
        return Component.translatable("text.abstool.waypoint.export." + key);
    }
}
