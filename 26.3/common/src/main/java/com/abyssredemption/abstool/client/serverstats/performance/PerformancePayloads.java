package com.abyssredemption.abstool.client.serverstats.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Optional protocol-v2 performance payloads; packet IDs and field order match the profiler contract. */
public final class PerformancePayloads {
    private PerformancePayloads() {}
    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String name) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("absservertool", "performance_" + name));
    }
    private static void header(RegistryFriendlyByteBuf b, int version, long requestId) { b.writeVarInt(version);b.writeVarLong(requestId); }
    private static int count(RegistryFriendlyByteBuf b) {
        int n=b.readVarInt(); if(n<0 || n>PerformanceModels.MAX_PAGE_ENTRIES) throw new IllegalArgumentException("Invalid profiler page size: "+n); return n;
    }
    private static <T> List<T> entries(RegistryFriendlyByteBuf b, Function<RegistryFriendlyByteBuf,T> read) {
        int n=count(b);List<T> result=new ArrayList<>(n);for(int i=0;i<n;i++)result.add(read.apply(b));return List.copyOf(result);
    }
    private static <T> void entries(RegistryFriendlyByteBuf b, List<T> list, BiConsumer<RegistryFriendlyByteBuf,T> write) {
        b.writeVarInt(list.size());for(T item:list)write.accept(b,item);
    }
    public record StartRequest(int version,long requestId,int durationSeconds) implements CustomPacketPayload {
        public static final Type<StartRequest> TYPE=payloadType("start_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,StartRequest> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarInt(p.durationSeconds);},
                b->new StartRequest(b.readVarInt(),b.readVarLong(),b.readVarInt()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record StartResponse(int version,long requestId,long sessionId,int acceptedDurationSeconds,
                                long startedAtEpochMillis,long expectedEndEpochMillis) implements CustomPacketPayload {
        public static final Type<StartResponse> TYPE=payloadType("start_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,StartResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.sessionId);b.writeVarInt(p.acceptedDurationSeconds);b.writeLong(p.startedAtEpochMillis);b.writeLong(p.expectedEndEpochMillis);},
                b->new StartResponse(b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarInt(),b.readLong(),b.readLong()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record StatusRequest(int version,long requestId) implements CustomPacketPayload {
        public static final Type<StatusRequest> TYPE=payloadType("status_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,StatusRequest> CODEC=StreamCodec.of(
                (b,p)->header(b,p.version,p.requestId),b->new StatusRequest(b.readVarInt(),b.readVarLong()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    /** Server state: 0 idle, 1 running, 2 finalizing. */
    public record StatusResponse(int version,long requestId,int state,long sessionId,int defaultDurationSeconds,
                                 int minimumDurationSeconds,int maximumDurationSeconds,int requestedDurationSeconds,
                                 long startedAtEpochMillis,long expectedEndEpochMillis,long elapsedMillis,
                                 long latestReportId) implements CustomPacketPayload {
        public static final Type<StatusResponse> TYPE=payloadType("status_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,StatusResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarInt(p.state);b.writeVarLong(p.sessionId);
                    b.writeVarInt(p.defaultDurationSeconds);b.writeVarInt(p.minimumDurationSeconds);b.writeVarInt(p.maximumDurationSeconds);
                    b.writeVarInt(p.requestedDurationSeconds);b.writeLong(p.startedAtEpochMillis);b.writeLong(p.expectedEndEpochMillis);
                    b.writeVarLong(p.elapsedMillis);b.writeVarLong(p.latestReportId);},
                b->new StatusResponse(b.readVarInt(),b.readVarLong(),b.readVarInt(),b.readVarLong(),b.readVarInt(),
                        b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readLong(),b.readLong(),b.readVarLong(),b.readVarLong()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record StopRequest(int version,long requestId,long sessionId) implements CustomPacketPayload {
        public static final Type<StopRequest> TYPE=payloadType("stop_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,StopRequest> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.sessionId);},
                b->new StopRequest(b.readVarInt(),b.readVarLong(),b.readVarLong()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record StopResponse(int version,long requestId,long sessionId,long reportId,
                               long actualDurationMillis,boolean stoppedEarly) implements CustomPacketPayload {
        public static final Type<StopResponse> TYPE=payloadType("stop_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,StopResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.sessionId);b.writeVarLong(p.reportId);b.writeVarLong(p.actualDurationMillis);b.writeBoolean(p.stoppedEarly);},
                b->new StopResponse(b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarLong(),b.readVarLong(),b.readBoolean()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record SummaryRequest(int version,long requestId,long reportId) implements CustomPacketPayload {
        public static final Type<SummaryRequest> TYPE=payloadType("report_summary_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,SummaryRequest> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.reportId);},
                b->new SummaryRequest(b.readVarInt(),b.readVarLong(),b.readVarLong()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record SummaryResponse(int version,long requestId,PerformanceModels.Summary summary) implements CustomPacketPayload {
        public static final Type<SummaryResponse> TYPE=payloadType("report_summary_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,SummaryResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);PerformanceModels.Summary.write(b,p.summary);},
                b->new SummaryResponse(b.readVarInt(),b.readVarLong(),PerformanceModels.Summary.read(b)));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record ChunkPageRequest(int version,long requestId,long reportId,int page) implements CustomPacketPayload {
        public static final Type<ChunkPageRequest> TYPE=payloadType("chunk_page_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,ChunkPageRequest> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.reportId);b.writeVarInt(p.page);},
                b->new ChunkPageRequest(b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarInt()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record ChunkPageResponse(int version,long requestId,PerformanceModels.Page<PerformanceModels.Chunk> page) implements CustomPacketPayload {
        public static final Type<ChunkPageResponse> TYPE=payloadType("chunk_page_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,ChunkPageResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);writePage(b,p.page,PerformanceModels.Chunk::write);},
                b->new ChunkPageResponse(b.readVarInt(),b.readVarLong(),readPage(b,PerformanceModels.Chunk::read)));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record PlayerPageRequest(int version,long requestId,long reportId,int page) implements CustomPacketPayload {
        public static final Type<PlayerPageRequest> TYPE=payloadType("player_page_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,PlayerPageRequest> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.reportId);b.writeVarInt(p.page);},
                b->new PlayerPageRequest(b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarInt()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record PlayerPageResponse(int version,long requestId,PerformanceModels.Page<PerformanceModels.Player> page) implements CustomPacketPayload {
        public static final Type<PlayerPageResponse> TYPE=payloadType("player_page_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,PlayerPageResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);writePage(b,p.page,PerformanceModels.Player::write);},
                b->new PlayerPageResponse(b.readVarInt(),b.readVarLong(),readPage(b,PerformanceModels.Player::read)));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record SlowTickPageRequest(int version,long requestId,long reportId,int page) implements CustomPacketPayload {
        public static final Type<SlowTickPageRequest> TYPE=payloadType("slow_tick_page_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,SlowTickPageRequest> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.reportId);b.writeVarInt(p.page);},
                b->new SlowTickPageRequest(b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarInt()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record SlowTickPageResponse(int version,long requestId,PerformanceModels.Page<PerformanceModels.SlowTick> page) implements CustomPacketPayload {
        public static final Type<SlowTickPageResponse> TYPE=payloadType("slow_tick_page_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,SlowTickPageResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);writePage(b,p.page,PerformanceModels.SlowTick::write);},
                b->new SlowTickPageResponse(b.readVarInt(),b.readVarLong(),readPage(b,PerformanceModels.SlowTick::read)));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record ChunkDetailRequest(int version,long requestId,long reportId,String dimension,int chunkX,int chunkZ) implements CustomPacketPayload {
        public static final Type<ChunkDetailRequest> TYPE=payloadType("chunk_detail_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,ChunkDetailRequest> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.reportId);b.writeUtf(p.dimension,128);b.writeVarInt(p.chunkX);b.writeVarInt(p.chunkZ);},
                b->new ChunkDetailRequest(b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readUtf(128),b.readVarInt(),b.readVarInt()));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record ChunkDetailResponse(int version,long requestId,long reportId,PerformanceModels.ChunkDetail detail) implements CustomPacketPayload {
        public static final Type<ChunkDetailResponse> TYPE=payloadType("chunk_detail_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,ChunkDetailResponse> CODEC=StreamCodec.of(
                (b,p)->{header(b,p.version,p.requestId);b.writeVarLong(p.reportId);PerformanceModels.ChunkDetail.write(b,p.detail);},
                b->new ChunkDetailResponse(b.readVarInt(),b.readVarLong(),b.readVarLong(),PerformanceModels.ChunkDetail.read(b)));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    private static <T> void writePage(RegistryFriendlyByteBuf b, PerformanceModels.Page<T> p,
                                      BiConsumer<RegistryFriendlyByteBuf,T> write) {
        b.writeVarLong(p.reportId());b.writeVarInt(p.page());b.writeVarInt(p.totalPages());
        b.writeVarInt(p.totalEntries());entries(b,p.entries(),write);
    }
    private static <T> PerformanceModels.Page<T> readPage(RegistryFriendlyByteBuf b,Function<RegistryFriendlyByteBuf,T> read) {
        return new PerformanceModels.Page<>(b.readVarLong(),b.readVarInt(),b.readVarInt(),b.readVarInt(),entries(b,read));
    }
}
