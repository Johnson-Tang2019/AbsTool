package com.abyssredemption.abstool.client.screenshot;

import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLError;

/** Preserve vanilla disk saves and copy the most recent successful capture asynchronously. */
public final class ScreenshotClipboard {
    private static final AtomicLong REQUESTS = new AtomicLong();
    private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1), task -> { var thread = new Thread(task, "abstool-screenshot-clipboard"); thread.setDaemon(true); return thread; },
            new ThreadPoolExecutor.DiscardOldestPolicy());
    private ScreenshotClipboard() {}
    public static void saved(File screenshot) {
        if (!ConfigManager.get().screenshotToClipboard) return;
        long request = REQUESTS.incrementAndGet();
        try {
            WORKER.execute(() -> {
                try {
                    var formats = ClipboardImageFormats.read(screenshot.toPath());
                    Minecraft.getInstance().execute(() -> offer(request, formats, 0));
                } catch (Exception | LinkageError failure) { failed(request, failure); }
            });
        } catch (RuntimeException failure) { failed(request, failure); }
    }
    private static void offer(long request, ClipboardImageFormats formats, int attempt) {
        if (request != REQUESTS.get() || !ConfigManager.get().screenshotToClipboard) return;
        try {
            if (ImageClipboard.set(formats)) { message("copied"); return; }
            if (attempt < 2) {
                CompletableFuture.delayedExecutor(75L * (attempt + 1), TimeUnit.MILLISECONDS).execute(() ->
                        Minecraft.getInstance().execute(() -> offer(request, formats, attempt + 1)));
            } else failed(request, new IllegalStateException(SDLError.SDL_GetError()));
        } catch (RuntimeException | LinkageError failure) { failed(request, failure); }
    }
    private static void failed(long request, Throwable failure) {
        AbsTool.LOGGER.warn("Screenshot was saved, but copying the image to the clipboard failed", failure);
        if (request == REQUESTS.get()) message("failed");
    }
    private static void message(String key) {
        var client = Minecraft.getInstance();
        client.execute(() -> client.showDebugChat(Component.translatable("text.abstool.screenshot." + key)));
    }
}
