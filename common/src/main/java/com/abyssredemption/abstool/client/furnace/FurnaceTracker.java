package com.abyssredemption.abstool.client.furnace;

import com.abyssredemption.abstool.client.vault.client.VaultFrame;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import io.netty.buffer.Unpooled;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Client-thread discovery and rate-limited, conservative furnace classification. */
public final class FurnaceTracker {
    public static final FurnaceTracker INSTANCE = new FurnaceTracker();
    private static final long TIMEOUT = 5_000;
    private static final int MAX_TARGETS = 8192;
    private final Map<BlockPos, Entry> targets = new LinkedHashMap<>();
    private final ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
    private Object connection;
    private ClientLevel level;
    private Object recipes;
    private long lastTick, nextScan, handshakeAt, nextHandshake, quarantineUntil;
    private long token;
    private double budget;
    private int sequence;
    private boolean servux;
    private int servuxTimeouts;
    private int centerX, centerZ, scanRadius = -1;

    private FurnaceTracker() {}
    public static FurnaceConfig config() { return ConfigManager.get().furnace; }
    public int targetCount() { return targets.size(); }
    public Component status() {
        long now = System.nanoTime() / 1_000_000;
        long unknown = targets.values().stream().filter(e -> e.validAt == 0 || e.timedOut || now - e.validAt > config().staleSeconds * 1000L).count();
        return Component.translatable("text.abstool.furnace.status", servux ? "Servux" : "—", targets.size(), unknown);
    }

    private static final class Entry {
        final BlockPos pos;
        ResourceKey<RecipePropertySet> kind;
        long lastRequest, validAt, pendingAt, token;
        boolean blocked, jade, timedOut;
        ItemStack input = ItemStack.EMPTY;
        Entry(BlockPos pos, ResourceKey<RecipePropertySet> kind) { this.pos = pos; this.kind = kind; }
    }

    public void tick(Minecraft client) {
        long now = System.nanoTime() / 1_000_000;
        if (client.getConnection() != connection) {
            targets.clear(); chunks.clear(); servux = false; servuxTimeouts = 0; handshakeAt = 0; nextHandshake = 0;
            connection = client.getConnection(); level = null; recipes = null; budget = 0;
        }
        if (client.level == null || client.player == null || connection == null) { lastTick = now; return; }
        if (level != client.level) {
            boolean changedDimension = level != null;
            targets.clear(); chunks.clear(); nextScan = 0; scanRadius = -1;
            level = client.level;
            // Servux has no dimension or transaction ID in a response. Drain old
            // in-flight responses before issuing requests in a different dimension.
            quarantineUntil = changedDimension ? now + TIMEOUT + 1000 : now;
        }
        if (recipes != client.getConnection().recipes()) {
            recipes = client.getConnection().recipes();
            for (Entry e : targets.values()) { e.validAt = 0; e.pendingAt = 0; }
        }
        if (!config().enabled) {
            for (Entry e : targets.values()) { e.pendingAt = 0; e.timedOut = true; }
            budget = 0; lastTick = now; return;
        }
        if (!servux && now >= nextHandshake && FurnaceNetwork.canSend.test(FurnaceNetwork.SERVUX)) {
            FurnaceNetwork.handshake(); handshakeAt = now; nextHandshake = now + 30_000;
        }
        discover(client, now);
        for (Entry e : targets.values()) {
            if (e.pendingAt != 0 && now - e.pendingAt > TIMEOUT) {
                e.pendingAt = 0; e.timedOut = true;
                if (!e.jade && ++servuxTimeouts >= 3) servux = false;
            }
        }
        double elapsed = lastTick == 0 ? 0 : Math.clamp((now - lastTick) / 1000.0, 0, 0.25);
        lastTick = now;
        budget = Math.min(2, budget + elapsed * config().requestsPerSecond);
        if (now < quarantineUntil || (!servux && handshakeAt != 0 && now - handshakeAt < TIMEOUT)) return;
        while (budget >= 1) {
            budget -= 1;
            Entry entry = choose(client, now, sequence++ % 3 != 2);
            if (entry == null) break;
            entry.lastRequest = now;
            entry.pendingAt = now;
            entry.token = ++token;
            entry.jade = !servux || (entry.timedOut && !entry.jade);
            if (!entry.jade) FurnaceNetwork.request(entry.pos);
            else if (!config().jadeFallback || !JadeBridge.request(entry.pos, entry.token)) entry.pendingAt = 0;
        }
    }

    private Entry choose(Minecraft client, long now, boolean priority) {
        Comparator<Entry> order = Comparator.comparingLong(e -> e.lastRequest);
        if (priority) order = Comparator.<Entry>comparingInt(e -> e.lastRequest == 0 ? 0 : e.blocked ? 1 : 2).thenComparing(order);
        return targets.values().stream()
                .filter(e -> e.pendingAt == 0 && now - e.lastRequest >= (e.blocked ? 2000 : 5000) && active(client, e))
                .min(order).orElse(null);
    }

    private void discover(Minecraft client, long now) {
        int x = client.player.blockPosition().getX() >> 4, z = client.player.blockPosition().getZ() >> 4;
        int radius = config().range.blocks == 0 ? client.options.getEffectiveRenderDistance() + 3
                : Math.min(client.options.getEffectiveRenderDistance() + 3, config().range.blocks / 16 + 1);
        if (radius != scanRadius || x != centerX || z != centerZ || (chunks.isEmpty() && now >= nextScan)) {
            chunks.clear(); centerX = x; centerZ = z; scanRadius = radius; nextScan = now + 2000;
            for (int ring = 0; ring <= radius; ring++) {
                for (int dx = -ring; dx <= ring; dx++) for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) chunks.add(new ChunkPos(x + dx, z + dz));
                }
            }
        }
        for (int i = 0; i < 8 && !chunks.isEmpty(); i++) {
            ChunkPos pos = chunks.removeFirst();
            var chunk = level.getChunkSource().getChunk(pos.x(), pos.z(), ChunkStatus.FULL, false);
            if (chunk == null) continue;
            for (var be : chunk.getBlockEntities().values()) {
                if (!(be instanceof AbstractFurnaceBlockEntity)) continue;
                var kind = kind(be.getBlockState());
                if (kind == null) continue;
                BlockPos block = be.getBlockPos().immutable();
                if (!inRange(client, block)) continue;
                Entry previous = targets.get(block);
                if (previous != null && previous.kind != kind) targets.remove(block);
                if (targets.size() < MAX_TARGETS) targets.computeIfAbsent(block, p -> new Entry(p, kind));
            }
        }
        targets.values().removeIf(e -> level.hasChunk(e.pos.getX() >> 4, e.pos.getZ() >> 4)
                ? kind(level.getBlockState(e.pos)) != e.kind : !e.blocked && e.pendingAt == 0);
    }

    private static ResourceKey<RecipePropertySet> kind(BlockState state) {
        if (state.is(Blocks.FURNACE)) return RecipePropertySet.FURNACE_INPUT;
        if (state.is(Blocks.BLAST_FURNACE)) return RecipePropertySet.BLAST_FURNACE_INPUT;
        if (state.is(Blocks.SMOKER)) return RecipePropertySet.SMOKER_INPUT;
        return null;
    }

    private static boolean inRange(Minecraft client, BlockPos pos) {
        int radius = config().range.blocks;
        return radius == 0 || pos.distSqr(client.player.blockPosition()) <= (double) radius * radius;
    }

    private boolean active(Minecraft client, Entry e) {
        return inRange(client, e.pos) && level.hasChunk(e.pos.getX() >> 4, e.pos.getZ() >> 4)
                && kind(level.getBlockState(e.pos)) == e.kind;
    }

    public void servuxMetadata(int version) {
        if (handshakeAt != 0 && version == 2) { servux = true; servuxTimeouts = 0; }
    }

    public void receiveServux(BlockPos pos, CompoundTag tag) {
        Entry e = pending(pos, false);
        if (e == null || !tag.getStringOr("id", "").equals(expectedId(e))) return;
        var inventory = tag.getList("Items");
        if (inventory.isEmpty()) return;
        ItemStack input = ItemStack.EMPTY;
        boolean found = false;
        for (var value : inventory.get()) {
            if (!(value instanceof CompoundTag stack)) return;
            if (stack.getByteOr("Slot", (byte) -1) != 0) continue;
            if (found) return;
            found = true;
            var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var parsed = ItemStack.CODEC.parse(ops, stack).result();
            if (parsed.isEmpty()) return;
            input = parsed.get();
        }
        accept(e, input);
    }

    public void receiveJade(CompoundTag tag) {
        if (!tag.contains("x") || !tag.contains("y") || !tag.contains("z")) return;
        Entry e = pending(new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0)), true);
        if (e == null || tag.getLongOr("abstool_request", -1) != e.token || !tag.getStringOr("BlockId", "").equals(expectedId(e))) return;
        if (!(tag.get("minecraft:furnace") instanceof ByteArrayTag data)) return;
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data.getAsByteArray()), level.registryAccess());
        try {
            buf.readVarInt(); buf.readVarInt();
            List<ItemStack> inventory = ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(buf);
            if (inventory.size() == 3 && !buf.isReadable()) accept(e, inventory.getFirst());
        } finally { buf.release(); }
    }

    private String expectedId(Entry e) { return BuiltInRegistries.BLOCK.getKey(level.getBlockState(e.pos).getBlock()).toString(); }

    private Entry pending(BlockPos pos, boolean jade) {
        Minecraft client = Minecraft.getInstance();
        Entry e = targets.get(pos);
        long now = System.nanoTime() / 1_000_000;
        return config().enabled && client.level == level && client.player != null && e != null
                && e.pendingAt != 0 && now - e.pendingAt <= TIMEOUT && e.jade == jade && active(client, e) ? e : null;
    }

    private void accept(Entry e, ItemStack input) {
        var propertySet = Minecraft.getInstance().getConnection().recipes().propertySet(e.kind);
        // EMPTY is also the sentinel for a missing sync. Never infer blockage from it.
        if (!input.isEmpty() && propertySet == RecipePropertySet.EMPTY) return;
        e.input = input.copy();
        e.blocked = !input.isEmpty() && !propertySet.test(input);
        e.validAt = System.nanoTime() / 1_000_000;
        e.pendingAt = 0; e.timedOut = false;
        if (!e.jade) servuxTimeouts = 0;
    }

    public FurnaceFrame extract(Minecraft client) {
        if (!config().enabled || client.level != level || client.player == null) return FurnaceFrame.EMPTY;
        List<VaultFrame.Target> boxes = new ArrayList<>();
        List<FurnaceFrame.Label> labels = new ArrayList<>();
        FurnaceFrame.Label selectedLabel = null;
        double bestAlignment = 0;
        long now = System.nanoTime() / 1_000_000;
        for (Entry e : targets.values()) {
            if (!e.blocked) continue;
            boolean stale = e.timedOut || e.validAt == 0 || now - e.validAt > config().staleSeconds * 1000L || !active(client, e);
            if (stale && !config().showStale) continue;
            int color = 0xFF000000 | (stale ? 0x999999 : config().color);
            boxes.add(new VaultFrame.Target(e.pos.getX(), e.pos.getY(), e.pos.getZ(), color));
            if (config().labels) {
                double alignment = net.minecraft.world.phys.Vec3.atCenterOf(e.pos).subtract(client.player.getEyePosition())
                        .normalize().dot(client.player.getLookAngle());
                if (alignment > bestAlignment) {
                    bestAlignment = alignment;
                    selectedLabel = new FurnaceFrame.Label(e.pos.getX(), e.pos.getY(), e.pos.getZ(),
                            Component.translatable(stale ? "text.abstool.furnace.stale" : "text.abstool.furnace.blocked", e.input.getHoverName()).getString(), color);
                }
            }
        }
        if (selectedLabel != null) labels.add(selectedLabel);
        return new FurnaceFrame(new VaultFrame(boxes, config().tracers, 0xFF000000 | config().tracerColor), labels);
    }
}
