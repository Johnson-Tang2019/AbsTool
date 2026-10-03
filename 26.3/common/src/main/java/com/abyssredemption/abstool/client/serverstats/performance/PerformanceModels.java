package com.abyssredemption.abstool.client.serverstats.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Bounded, loader-independent views of server profiler data. Durations are nanoseconds. */
public final class PerformanceModels {
    private PerformanceModels() {}
    public static final int MAX_PAGE_ENTRIES = 100;
    public static final int MAX_ASSOCIATED_PLAYERS = 16;
    public static final int MAX_TOP_CHUNKS = 20;
    public static final int MAX_TYPES = 20;

    private static int count(RegistryFriendlyByteBuf b, int max) {
        int n = b.readVarInt();
        if (n < 0 || n > max) throw new IllegalArgumentException("Invalid profiler entry count: " + n);
        return n;
    }

    public record Summary(long reportId, long sessionId, int requestedDurationSeconds,
                          long actualDurationMillis, boolean stoppedEarly, long startedAtEpochMillis,
                          long endedAtEpochMillis, int tickCount, long averageNs, long p50Ns,
                          long p95Ns, long p99Ns, long maxNs, int over50ms, int over100ms,
                          int over200ms, long chunkTickNs, long entityTickNs, long blockEntityTickNs,
                          long scheduledBlockNs, long scheduledFluidNs, long residualNs,
                          int saveOverlapSlowTicks, int chunkEntries, int playerEntries, int slowTickEntries) {
        public static Summary read(RegistryFriendlyByteBuf b) {
            return new Summary(b.readVarLong(), b.readVarLong(), b.readVarInt(), b.readVarLong(), b.readBoolean(),
                    b.readLong(), b.readLong(), b.readVarInt(), b.readVarLong(), b.readVarLong(),
                    b.readVarLong(), b.readVarLong(), b.readVarLong(), b.readVarInt(), b.readVarInt(),
                    b.readVarInt(), b.readVarLong(), b.readVarLong(), b.readVarLong(), b.readVarLong(),
                    b.readVarLong(), b.readVarLong(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt());
        }
        public static void write(RegistryFriendlyByteBuf b, Summary s) {
            b.writeVarLong(s.reportId); b.writeVarLong(s.sessionId); b.writeVarInt(s.requestedDurationSeconds);
            b.writeVarLong(s.actualDurationMillis); b.writeBoolean(s.stoppedEarly);
            b.writeLong(s.startedAtEpochMillis); b.writeLong(s.endedAtEpochMillis);
            b.writeVarInt(s.tickCount); b.writeVarLong(s.averageNs); b.writeVarLong(s.p50Ns);
            b.writeVarLong(s.p95Ns); b.writeVarLong(s.p99Ns); b.writeVarLong(s.maxNs);
            b.writeVarInt(s.over50ms); b.writeVarInt(s.over100ms); b.writeVarInt(s.over200ms);
            b.writeVarLong(s.chunkTickNs); b.writeVarLong(s.entityTickNs); b.writeVarLong(s.blockEntityTickNs);
            b.writeVarLong(s.scheduledBlockNs); b.writeVarLong(s.scheduledFluidNs); b.writeVarLong(s.residualNs);
            b.writeVarInt(s.saveOverlapSlowTicks); b.writeVarInt(s.chunkEntries);
            b.writeVarInt(s.playerEntries); b.writeVarInt(s.slowTickEntries);
        }
        public boolean valid() { return reportId > 0 && sessionId > 0 && requestedDurationSeconds > 0
                && actualDurationMillis >= 0 && tickCount >= 0 && averageNs >= 0 && p50Ns >= 0
                && p95Ns >= 0 && p99Ns >= 0 && maxNs >= 0 && over50ms >= 0 && over100ms >= 0
                && over200ms >= 0 && chunkEntries >= 0 && playerEntries >= 0 && slowTickEntries >= 0; }
    }

    public record Chunk(String dimension, int x, int z, long totalMeasuredNs, long chunkTickNs,
                        long entityTickNs, long blockEntityTickNs, long scheduledBlockNs,
                        long scheduledFluidNs, long entityTickCalls, long blockEntityTickCalls,
                        long loadCount, long generatedLoadCount, int loadAttribution,
                        List<String> associatedPlayers) {
        public static Chunk read(RegistryFriendlyByteBuf b) {
            String dimension=b.readUtf(128); int x=b.readVarInt(), z=b.readVarInt();
            long total=b.readVarLong(), chunk=b.readVarLong(), entity=b.readVarLong(), blockEntity=b.readVarLong();
            long block=b.readVarLong(), fluid=b.readVarLong(), entityCalls=b.readVarLong(), beCalls=b.readVarLong();
            long loads=b.readVarLong(), generated=b.readVarLong(); int attribution=b.readVarInt();
            int n=count(b,MAX_ASSOCIATED_PLAYERS); List<String> players=new ArrayList<>(n);
            for(int i=0;i<n;i++) players.add(b.readUtf(64));
            return new Chunk(dimension,x,z,total,chunk,entity,blockEntity,block,fluid,entityCalls,beCalls,
                    loads,generated,attribution,List.copyOf(players));
        }
        public static void write(RegistryFriendlyByteBuf b, Chunk c) {
            b.writeUtf(c.dimension,128); b.writeVarInt(c.x); b.writeVarInt(c.z);
            b.writeVarLong(c.totalMeasuredNs); b.writeVarLong(c.chunkTickNs); b.writeVarLong(c.entityTickNs);
            b.writeVarLong(c.blockEntityTickNs); b.writeVarLong(c.scheduledBlockNs); b.writeVarLong(c.scheduledFluidNs);
            b.writeVarLong(c.entityTickCalls); b.writeVarLong(c.blockEntityTickCalls); b.writeVarLong(c.loadCount);
            b.writeVarLong(c.generatedLoadCount); b.writeVarInt(c.loadAttribution);
            b.writeVarInt(c.associatedPlayers.size()); for(String player:c.associatedPlayers) b.writeUtf(player,64);
        }
        public boolean valid() { return dimension != null && !dimension.isBlank() && totalMeasuredNs >= 0
                && chunkTickNs >= 0 && entityTickNs >= 0 && blockEntityTickNs >= 0
                && scheduledBlockNs >= 0 && scheduledFluidNs >= 0 && entityTickCalls >= 0
                && blockEntityTickCalls >= 0 && loadCount >= 0 && generatedLoadCount >= 0
                && loadAttribution >= 0 && loadAttribution <= 3; }
    }

    public record Player(UUID uuid, String name, int uniqueLoadAssociations, int sharedLoadAssociations,
                         int generatedLoadAssociations, int associatedMeasuredChunks, long associatedChunkMeasuredNs) {
        public static Player read(RegistryFriendlyByteBuf b) {
            return new Player(b.readUUID(),b.readUtf(64),b.readVarInt(),b.readVarInt(),b.readVarInt(),
                    b.readVarInt(),b.readVarLong());
        }
        public static void write(RegistryFriendlyByteBuf b, Player p) {
            b.writeUUID(p.uuid); b.writeUtf(p.name,64); b.writeVarInt(p.uniqueLoadAssociations);
            b.writeVarInt(p.sharedLoadAssociations); b.writeVarInt(p.generatedLoadAssociations);
            b.writeVarInt(p.associatedMeasuredChunks); b.writeVarLong(p.associatedChunkMeasuredNs);
        }
        public boolean valid() { return uuid != null && name != null && uniqueLoadAssociations >= 0
                && sharedLoadAssociations >= 0 && generatedLoadAssociations >= 0
                && associatedMeasuredChunks >= 0 && associatedChunkMeasuredNs >= 0; }
    }

    public record SlowChunk(String dimension, int x, int z, long measuredNs) {
        public static SlowChunk read(RegistryFriendlyByteBuf b) { return new SlowChunk(b.readUtf(128),b.readVarInt(),b.readVarInt(),b.readVarLong()); }
        public static void write(RegistryFriendlyByteBuf b, SlowChunk c) { b.writeUtf(c.dimension,128);b.writeVarInt(c.x);b.writeVarInt(c.z);b.writeVarLong(c.measuredNs); }
    }
    public record SlowTick(long tickIndex, long timestamp, long durationNs, boolean saveActive, List<SlowChunk> topChunks) {
        public static SlowTick read(RegistryFriendlyByteBuf b) {
            long tick=b.readVarLong(), time=b.readLong(), ns=b.readVarLong(); boolean save=b.readBoolean();
            int n=count(b,MAX_TOP_CHUNKS); List<SlowChunk> chunks=new ArrayList<>(n);
            for(int i=0;i<n;i++) chunks.add(SlowChunk.read(b));
            return new SlowTick(tick,time,ns,save,List.copyOf(chunks));
        }
        public static void write(RegistryFriendlyByteBuf b, SlowTick t) {
            b.writeVarLong(t.tickIndex);b.writeLong(t.timestamp);b.writeVarLong(t.durationNs);b.writeBoolean(t.saveActive);
            b.writeVarInt(t.topChunks.size());for(SlowChunk c:t.topChunks)SlowChunk.write(b,c);
        }
    }
    public record TypeTiming(String typeId, long timeNs, long calls) {
        public static TypeTiming read(RegistryFriendlyByteBuf b) { return new TypeTiming(b.readUtf(128),b.readVarLong(),b.readVarLong()); }
        public static void write(RegistryFriendlyByteBuf b, TypeTiming t) { b.writeUtf(t.typeId,128);b.writeVarLong(t.timeNs);b.writeVarLong(t.calls); }
    }
    public record ChunkDetail(Chunk chunk, List<TypeTiming> entityTypes, List<TypeTiming> blockEntityTypes) {
        public static ChunkDetail read(RegistryFriendlyByteBuf b) {
            Chunk chunk=Chunk.read(b); int n=count(b,MAX_TYPES); List<TypeTiming> entities=new ArrayList<>(n);
            for(int i=0;i<n;i++) entities.add(TypeTiming.read(b));
            n=count(b,MAX_TYPES);List<TypeTiming> blockEntities=new ArrayList<>(n);
            for(int i=0;i<n;i++) blockEntities.add(TypeTiming.read(b));
            return new ChunkDetail(chunk,List.copyOf(entities),List.copyOf(blockEntities));
        }
        public static void write(RegistryFriendlyByteBuf b, ChunkDetail d) {
            Chunk.write(b,d.chunk);b.writeVarInt(d.entityTypes.size());for(TypeTiming t:d.entityTypes)TypeTiming.write(b,t);
            b.writeVarInt(d.blockEntityTypes.size());for(TypeTiming t:d.blockEntityTypes)TypeTiming.write(b,t);
        }
    }
    public record Page<T>(long reportId, int page, int totalPages, int totalEntries, List<T> entries) {}
}
