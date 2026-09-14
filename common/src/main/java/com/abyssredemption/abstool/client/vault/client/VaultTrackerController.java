// Adapted from Ominous Vault Track, Copyright (c) 2026 Damomo1 (MIT).
package com.abyssredemption.abstool.client.vault.client;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.vault.config.ModConfig;
import com.abyssredemption.abstool.client.vault.storage.VaultKey;
import com.abyssredemption.abstool.client.vault.storage.VaultStorage;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;

public class VaultTrackerController {
    private static final int RESCAN_INTERVAL_TICKS = 40;
    private static final int CHUNKS_SCANNED_PER_TICK = 8;

    private final Set<VaultKey> loadedOminousVaults = new HashSet<>();
    private final Map<Long, Set<VaultKey>> vaultsByChunk = new HashMap<>();
    private final ArrayDeque<Long> chunksToScan = new ArrayDeque<>();
    private int scanCooldownTicks;
    private int refreshCooldownTicks;
    private String lastServer = "";
    private String lastDimension = "";
    private int lastCenterChunkX = Integer.MIN_VALUE;
    private int lastCenterChunkZ = Integer.MIN_VALUE;
    private int lastChunkRadius = -1;
    private static String cachedTracerItemId = "";
    private static Item cachedTracerItem;

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            clearScanState();
            lastServer = "";
            lastDimension = "";
            refreshCooldownTicks = 0;
            return;
        }

        String server = currentServerKey(client);
        String dimension = currentDimensionKey(client.level);
        if (!server.equals(lastServer) || !dimension.equals(lastDimension)) {
            clearScanState();
            scanCooldownTicks = 0;
            refreshCooldownTicks = 0;
            lastServer = server;
            lastDimension = dimension;
        }

        if (--refreshCooldownTicks <= 0) {
            refreshCooldownTicks = 20 * 30;
            VaultStorage.applyRefresh(ConfigManager.get(), server, dimension);
        }

        ModConfig config = ConfigManager.get();
        if (!config.enabled) {
            clearScanState();
            return;
        }

        ClientLevel world = client.level;
        BlockPos playerPos = client.player.blockPosition();
        int centerChunkX = SectionPos.blockToSectionCoord(playerPos.getX());
        int centerChunkZ = SectionPos.blockToSectionCoord(playerPos.getZ());
        int chunkRadius = Math.min(config.renderRadius, client.options.getEffectiveRenderDistance());
        boolean scanAreaChanged = centerChunkX != lastCenterChunkX
                || centerChunkZ != lastCenterChunkZ
                || chunkRadius != lastChunkRadius;

        if (scanAreaChanged || (chunksToScan.isEmpty() && --scanCooldownTicks <= 0)) {
            scheduleScan(centerChunkX, centerChunkZ, chunkRadius);
            scanCooldownTicks = RESCAN_INTERVAL_TICKS;
        }
        scanQueuedChunks(world, server, dimension);
    }

    public InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (!ConfigManager.get().enabled || !world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!isOminousVault(state)) return InteractionResult.PASS;

        Minecraft client = Minecraft.getInstance();
        if (player != client.player) return InteractionResult.PASS;
        VaultKey key = VaultKey.of(currentServerKey(client), currentDimensionKey(world), pos);
        VaultStorage.exclude(key);
        loadedOminousVaults.add(key);

        return InteractionResult.PASS;
    }

    /** Capture world/config state before rendering; render callbacks consume immutable values only. */
    public VaultFrame extract(Minecraft client) {
        ModConfig config = ConfigManager.get();
        if (client.player == null || client.level == null || !config.enabled) return VaultFrame.EMPTY;
        String server = currentServerKey(client);
        String dimension = currentDimensionKey(client.level);
        BlockPos center = client.player.blockPosition();
        long radius = (long) config.renderRadius * 16;
        List<VaultFrame.Target> targets = new ArrayList<>();
        for (VaultKey key : loadedOminousVaults) {
            if (!key.server().equals(server) || !key.dimension().equals(dimension)) continue;
            long dx = (long) key.x() - center.getX();
            long dy = (long) key.y() - center.getY();
            long dz = (long) key.z() - center.getZ();
            if (dx * dx + dy * dy + dz * dz > radius * radius) continue;
            if (!client.level.hasChunk(key.x() >> 4, key.z() >> 4)
                    || !isOminousVault(client.level.getBlockState(key.toBlockPos()))) continue;
            boolean excluded = VaultStorage.isExcluded(key);
            if (excluded && config.excludedRenderMode == ModConfig.ExcludedRenderMode.HIDE) continue;
            targets.add(new VaultFrame.Target(key.x(), key.y(), key.z(),
                    0xFF000000 | (excluded ? config.excludedColor : config.highlightColor)));
        }
        return new VaultFrame(targets, shouldRenderTracer(client, config), 0xFF000000 | config.tracerColor);
    }

    public static boolean isOminousVault(BlockState state) {
        if (!state.is(Blocks.VAULT)) return false;
        return state.hasProperty(BlockStateProperties.OMINOUS) && state.getValue(BlockStateProperties.OMINOUS);
    }

    public static String currentServerKey(Minecraft client) {
        if (client == null) return "unknown";
        if (client.getSingleplayerServer() != null) {
            return "singleplayer:" + client.getSingleplayerServer().getWorldPath(LevelResource.ROOT)
                    .toAbsolutePath().normalize();
        }
        ServerData info = client.getCurrentServer();
        if (info != null && info.ip != null && !info.ip.isBlank()) return info.ip.trim().toLowerCase(Locale.ROOT);
        return "unknown";
    }

    public static String currentDimensionKey(Level world) {
        if (world == null) return "unknown";
        return world.dimension().identifier().toString();
    }

    private void scheduleScan(int centerChunkX, int centerChunkZ, int chunkRadius) {
        lastCenterChunkX = centerChunkX;
        lastCenterChunkZ = centerChunkZ;
        lastChunkRadius = chunkRadius;
        chunksToScan.clear();

        // Scan from the player outwards so nearby vaults appear first without a large one-tick spike.
        for (int ring = 0; ring <= chunkRadius; ring++) {
            for (int x = -ring; x <= ring; x++) {
                enqueueChunk(centerChunkX + x, centerChunkZ - ring);
                if (ring != 0) enqueueChunk(centerChunkX + x, centerChunkZ + ring);
            }
            for (int z = -ring + 1; z < ring; z++) {
                enqueueChunk(centerChunkX - ring, centerChunkZ + z);
                if (ring != 0) enqueueChunk(centerChunkX + ring, centerChunkZ + z);
            }
        }

        var iterator = vaultsByChunk.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int chunkX = (int) (long) entry.getKey();
            int chunkZ = (int) (entry.getKey() >> 32);
            if (Math.abs((long) chunkX - centerChunkX) > chunkRadius
                    || Math.abs((long) chunkZ - centerChunkZ) > chunkRadius) {
                loadedOminousVaults.removeAll(entry.getValue());
                iterator.remove();
            }
        }
    }

    private void enqueueChunk(int chunkX, int chunkZ) {
        long packed = packChunk(chunkX, chunkZ);
        chunksToScan.addLast(packed);
    }

    private void scanQueuedChunks(ClientLevel world, String server, String dimension) {
        for (int count = 0; count < CHUNKS_SCANNED_PER_TICK && !chunksToScan.isEmpty(); count++) {
            long packed = chunksToScan.removeFirst();
            int chunkX = (int) packed;
            int chunkZ = (int) (packed >> 32);
            LevelChunk chunk = world.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            Set<VaultKey> previous = vaultsByChunk.remove(packed);
            if (previous != null) loadedOminousVaults.removeAll(previous);
            if (chunk == null) continue;

            Set<VaultKey> found = scanChunk(chunk, server, dimension);
            if (!found.isEmpty()) {
                vaultsByChunk.put(packed, found);
                loadedOminousVaults.addAll(found);
            }
        }
    }

    private Set<VaultKey> scanChunk(LevelChunk chunk, String server, String dimension) {
        Set<VaultKey> found = new HashSet<>();
        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            BlockState state = chunk.getBlockState(pos);
            if (!isOminousVault(state)) continue;
            found.add(VaultKey.of(server, dimension, pos));
        }
        return found;
    }

    private void clearScanState() {
        loadedOminousVaults.clear();
        vaultsByChunk.clear();
        chunksToScan.clear();
        lastCenterChunkX = Integer.MIN_VALUE;
        lastCenterChunkZ = Integer.MIN_VALUE;
        lastChunkRadius = -1;
    }

    private static long packChunk(int chunkX, int chunkZ) {
        return Integer.toUnsignedLong(chunkX) | ((long) chunkZ << 32);
    }

    public static boolean shouldRenderTracer(Minecraft client, ModConfig config) {
        if (!config.renderTracers) return false;
        if (!config.tracerRequiresItem) return true;

        Identifier id = Identifier.tryParse(config.tracerItemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return false;
        if (!config.tracerItemId.equals(cachedTracerItemId)) {
            cachedTracerItemId = config.tracerItemId;
            cachedTracerItem = BuiltInRegistries.ITEM.getValue(id);
        }
        return client.player != null && cachedTracerItem != null
                && (client.player.getMainHandItem().is(cachedTracerItem) || client.player.getOffhandItem().is(cachedTracerItem));
    }
}
