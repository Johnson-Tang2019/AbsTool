package com.abyssredemption.abstool.client.screenshot;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDL_ClipboardCleanupCallback;
import org.lwjgl.sdl.SDL_ClipboardDataCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;

/** SDL owns the clipboard lifetime; native buffers survive until its cleanup callback. */
public final class ImageClipboard {
    private static final AtomicLong IDS = new AtomicLong();
    private static final Map<Long, Payload> PAYLOADS = new ConcurrentHashMap<>();
    private static final SDL_ClipboardDataCallback DATA = SDL_ClipboardDataCallback.create((id, type, size) -> {
        var payload = PAYLOADS.get(id);
        var buffer = payload == null ? null : payload.buffers.get(MemoryUtil.memUTF8(type));
        MemoryUtil.memPutAddress(size, buffer == null ? 0 : buffer.remaining());
        return buffer == null ? MemoryUtil.NULL : MemoryUtil.memAddress(buffer);
    });
    private static final SDL_ClipboardCleanupCallback CLEANUP = SDL_ClipboardCleanupCallback.create(ImageClipboard::release);
    private ImageClipboard() {}
    /** Must run on Minecraft's main thread, as required by SDL. */
    public static boolean set(ClipboardImageFormats images) {
        long id = IDS.incrementAndGet();
        var payload = new Payload();
        boolean handedOff = false;
        try (var stack = MemoryStack.stackPush()) {
            payload.add("image/png", images.png());
            payload.add("image/bmp", images.bmp());
            PAYLOADS.put(id, payload);
            var types = stack.mallocPointer(2);
            // SDL's Windows backend publishes the first image format as native clipboard data.
            if (Platform.get() == Platform.WINDOWS) types.put(stack.UTF8("image/bmp")).put(stack.UTF8("image/png"));
            else types.put(stack.UTF8("image/png")).put(stack.UTF8("image/bmp"));
            types.flip();
            handedOff = SDLClipboard.SDL_SetClipboardData(DATA, CLEANUP, id, types);
            return handedOff;
        } finally {
            if (!handedOff) { PAYLOADS.remove(id); payload.close(); }
        }
    }
    private static void release(long id) {
        var payload = PAYLOADS.remove(id);
        if (payload != null) payload.close();
    }
    private static final class Payload implements AutoCloseable {
        private final Map<String, ByteBuffer> buffers = new HashMap<>();
        private boolean closed;
        void add(String type, byte[] bytes) {
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes).flip();
            buffers.put(type, buffer);
        }
        public synchronized void close() {
            if (closed) return;
            closed = true;
            buffers.values().forEach(MemoryUtil::memFree);
        }
    }
}
