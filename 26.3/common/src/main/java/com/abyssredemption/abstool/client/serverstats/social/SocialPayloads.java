package com.abyssredemption.abstool.client.serverstats.social;

import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Optional protocol-v2 extension. Keep field order identical in the client implementation. */
public final class SocialPayloads {
    private SocialPayloads() {}
    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("absservertool",path));
    }
    public record Request(int protocolVersion,long requestId,int boardType,int page) implements CustomPacketPayload {
        public static final Type<Request> TYPE=SocialPayloads.type("social_leaderboard_request");
        public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.of(
                (b,v)->{b.writeVarInt(v.protocolVersion());b.writeVarLong(v.requestId());b.writeVarInt(v.boardType());b.writeVarInt(v.page());},
                b->new Request(b.readVarInt(),b.readVarLong(),b.readVarInt(),b.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Entry(int rank,UUID uuid,String playerName,int score,int subtype,long lastSeenAtEpochMillis,
            int daysSinceLastSeen,long windowPlayTimeTicks,long onFootDistanceCm,long observableActions,
            int activeDays,int shortVisits,boolean online) {
        public Entry {
            if(rank<1 || uuid==null || playerName==null || playerName.isBlank() || playerName.length()>64
                    || score<0 || score>100 || subtype<1 || subtype>3 || lastSeenAtEpochMillis<0 || daysSinceLastSeen< -1
                    || windowPlayTimeTicks<0 || onFootDistanceCm<0 || observableActions<0 || activeDays<0 || shortVisits<0)
                throw new IllegalArgumentException("Invalid social entry");
        }
    }
    public record Response(int protocolVersion,long requestId,int boardType,int page,int totalPages,int totalEntries,
            long generatedAtEpochMillis,long windowStartEpochMillis,long windowEndEpochMillis,boolean complete,List<Entry> entries) implements CustomPacketPayload {
        public Response {
            entries=List.copyOf(entries);
            if(boardType<0 || boardType>3 || page<1 || totalPages<page || totalEntries<0 || entries.size()>50
                    || generatedAtEpochMillis<0 || windowStartEpochMillis<0 || windowEndEpochMillis<windowStartEpochMillis
                    || entries.size()>totalEntries) throw new IllegalArgumentException("Invalid social page");
            for(var e:entries) if(e.rank()>totalEntries || boardType==1 && e.subtype()!=1 || boardType==2 && e.subtype()!=2
                    || boardType==3 && e.subtype()!=3 || boardType==0 && e.subtype()==3) throw new IllegalArgumentException("Mismatched social subtype");
        }
        public static final Type<Response> TYPE=SocialPayloads.type("social_leaderboard_response");
        public static final StreamCodec<RegistryFriendlyByteBuf,Response> CODEC=StreamCodec.of(SocialPayloads::write,SocialPayloads::read);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    private static void write(RegistryFriendlyByteBuf b,Response v) {
        b.writeVarInt(v.protocolVersion()); b.writeVarLong(v.requestId()); b.writeVarInt(v.boardType()); b.writeVarInt(v.page());
        b.writeVarInt(v.totalPages()); b.writeVarInt(v.totalEntries()); b.writeLong(v.generatedAtEpochMillis());
        b.writeLong(v.windowStartEpochMillis()); b.writeLong(v.windowEndEpochMillis()); b.writeBoolean(v.complete()); b.writeVarInt(v.entries().size());
        for(var e:v.entries()) {
            b.writeVarInt(e.rank()); b.writeUUID(e.uuid()); b.writeUtf(e.playerName(),64); b.writeVarInt(e.score()); b.writeVarInt(e.subtype());
            b.writeLong(e.lastSeenAtEpochMillis()); b.writeVarInt(e.daysSinceLastSeen()); b.writeVarLong(e.windowPlayTimeTicks());
            b.writeVarLong(e.onFootDistanceCm()); b.writeVarLong(e.observableActions()); b.writeVarInt(e.activeDays()); b.writeVarInt(e.shortVisits()); b.writeBoolean(e.online());
        }
    }
    private static Response read(RegistryFriendlyByteBuf b) {
        int version=b.readVarInt(); long id=b.readVarLong(); int board=b.readVarInt(),page=b.readVarInt(),pages=b.readVarInt(),total=b.readVarInt();
        long generated=b.readLong(),start=b.readLong(),end=b.readLong(); boolean complete=b.readBoolean(); int count=b.readVarInt();
        if(count<0 || count>50) throw new IllegalArgumentException("Social entry count outside 0..50");
        var entries=new ArrayList<Entry>(count);
        for(int i=0;i<count;i++) entries.add(new Entry(b.readVarInt(),b.readUUID(),readName(b),b.readVarInt(),b.readVarInt(),b.readLong(),b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarLong(),b.readVarInt(),b.readVarInt(),b.readBoolean()));
        return new Response(version,id,board,page,pages,total,generated,start,end,complete,entries);
    }
    private static String readName(RegistryFriendlyByteBuf b) {
        int length=b.readVarInt();
        if(length<0 || length>256 || length>b.readableBytes()) throw new IllegalArgumentException("Invalid social name length");
        byte[] bytes=new byte[length]; b.readBytes(bytes);
        try {
            String name=java.nio.charset.StandardCharsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            if(name.length()>64) throw new IllegalArgumentException("Social name too long"); return name;
        } catch(java.nio.charset.CharacterCodingException e) { throw new IllegalArgumentException("Malformed UTF-8 social name",e); }
    }
}
