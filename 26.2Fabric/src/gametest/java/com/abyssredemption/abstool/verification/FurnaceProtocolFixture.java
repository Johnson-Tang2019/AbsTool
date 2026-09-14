package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.furnace.FurnaceNetwork;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Test-only Servux v2 wire fixture; uses real server block entity serialization. */
public final class FurnaceProtocolFixture implements ModInitializer {
    public static volatile boolean reply = true;
    public static final AtomicInteger requests = new AtomicInteger();
    private static final Set<UUID> registered = ConcurrentHashMap.newKeySet();
    private static final CustomPacketPayload.Type<FurnaceNetwork.Outgoing> TYPE = new CustomPacketPayload.Type<>(FurnaceNetwork.SERVUX);
    private static final StreamCodec<FriendlyByteBuf, FurnaceNetwork.Outgoing> CODEC = new StreamCodec<>() {
        public FurnaceNetwork.Outgoing decode(FriendlyByteBuf buf) {
            byte[] data = new byte[buf.readableBytes()]; buf.readBytes(data);
            return new FurnaceNetwork.Outgoing(FurnaceNetwork.SERVUX, data);
        }
        public void encode(FriendlyByteBuf buf, FurnaceNetwork.Outgoing data) { buf.writeBytes(data.bytes()); }
    };
    public void onInitialize() {
        if (!Boolean.getBoolean("abstool.furnaceTests")) return;
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {});
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            FriendlyByteBuf in = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.bytes()));
            FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
            try {
                int type = in.readVarInt();
                if (!reply) return;
                if (type == 2) {
                    var metadata = in.readNbt();
                    if (metadata == null || metadata.getIntOr("version", -1) != 2) throw new AssertionError("Bad handshake");
                    registered.add(context.player().getUUID());
                    out.writeVarInt(1); out.writeNbt(metadata);
                } else if (type == 3 && registered.contains(context.player().getUUID())) {
                    requests.incrementAndGet();
                    var pos = in.readBlockPos();
                    if (!context.player().level().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) throw new AssertionError("Unloaded query");
                    var entity = context.player().level().getBlockEntity(pos);
                    if (entity == null) return;
                    CompoundTag nbt = entity.saveWithFullMetadata(context.player().registryAccess());
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    NbtIo.writeCompressed(nbt, bytes);
                    out.writeVarInt(5); out.writeBlockPos(pos); out.writeInt(bytes.size()); out.writeBytes(bytes.toByteArray());
                } else return;
                byte[] data = new byte[out.readableBytes()]; out.readBytes(data);
                ServerPlayNetworking.send(context.player(), new FurnaceNetwork.Outgoing(FurnaceNetwork.SERVUX, data));
            } catch (java.io.IOException e) { throw new RuntimeException(e); }
            finally { in.release(); out.release(); }
        });
    }
}
