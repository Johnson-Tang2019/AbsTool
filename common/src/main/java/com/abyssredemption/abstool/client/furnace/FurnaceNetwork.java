package com.abyssredemption.abstool.client.furnace;

import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;

/** Observes existing channels without replacing another mod's codec or receiver. */
public final class FurnaceNetwork {
    public static final Identifier SERVUX = Identifier.parse("servux:entity_data");
    private static final Identifier JADE = Identifier.parse("jade:receive_data");
    public static Predicate<Identifier> canSend = id -> false;
    public static BooleanSupplier delegateServux = () -> true;
    private static final int MAX_BYTES = 1_048_576;

    private FurnaceNetwork() {}

    /** Keep our Jade replies out of Jade's crosshair-tooltip cache, including LAN/in-memory play. */
    public static boolean handleJadeReply(CustomPacketPayload payload) {
        if (!payload.type().id().equals(JADE)) return false;
        try {
            Object value = payload.getClass().getMethod("tag").invoke(payload);
            if (!(value instanceof CompoundTag tag) || !tag.contains("abstool_request")) return false;
            Minecraft client = Minecraft.getInstance();
            var connection = client.getConnection();
            var level = client.level;
            client.execute(() -> {
                if (connection != null && connection == client.getConnection() && level == client.level) {
                    FurnaceTracker.INSTANCE.receiveJade(tag);
                }
            });
            return true;
        } catch (ReflectiveOperationException e) { return false; }
    }

    public record Outgoing(Identifier id, byte[] bytes) implements CustomPacketPayload {
        @Override public Type<Outgoing> type() { return new Type<>(id); }
    }

    public static StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> outgoing(
            StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> delegate) {
        return new StreamCodec<>() {
            public ServerboundCustomPayloadPacket decode(FriendlyByteBuf buf) { return delegate.decode(buf); }
            public void encode(FriendlyByteBuf buf, ServerboundCustomPayloadPacket packet) {
                if (packet.payload() instanceof Outgoing raw) {
                    buf.writeIdentifier(raw.id());
                    buf.writeBytes(raw.bytes());
                } else delegate.encode(buf, packet);
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomPayloadPacket> incoming(
            StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomPayloadPacket> delegate) {
        return new StreamCodec<>() {
            public void encode(RegistryFriendlyByteBuf buf, ClientboundCustomPayloadPacket packet) { delegate.encode(buf, packet); }
            public ClientboundCustomPayloadPacket decode(RegistryFriendlyByteBuf buf) {
                int index = buf.readerIndex();
                Identifier id = buf.readIdentifier();
                byte[] copy = null;
                if ((id.equals(SERVUX) || id.equals(JADE)) && buf.readableBytes() <= MAX_BYTES) {
                    copy = new byte[buf.readableBytes()];
                    buf.getBytes(buf.readerIndex(), copy);
                }
                buf.readerIndex(index);
                ClientboundCustomPayloadPacket result;
                if (id.equals(SERVUX) && !delegateServux.getAsBoolean()) {
                    // No receiver owns this channel. Avoid NeoForge's per-packet
                    // missing-codec warning while preserving any registered codec.
                    buf.skipBytes(buf.readableBytes());
                    result = new ClientboundCustomPayloadPacket(new DiscardedPayload(id));
                } else result = delegate.decode(buf);
                if (copy != null) {
                    byte[] data = copy;
                    Minecraft client = Minecraft.getInstance();
                    var connection = client.getConnection();
                    var level = client.level;
                    client.execute(() -> {
                        if (connection != null && client.getConnection() == connection && client.level == level) {
                            receive(id, data);
                        }
                    });
                }
                return result;
            }
        };
    }

    public static void handshake() {
        send(Identifier.parse("minecraft:register"), SERVUX.toString().getBytes(StandardCharsets.UTF_8));
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            CompoundTag metadata = new CompoundTag();
            metadata.putInt("version", 2);
            metadata.putString("client", "abstool");
            buf.writeVarInt(2);
            buf.writeNbt(metadata);
            send(SERVUX, bytes(buf));
        } finally { buf.release(); }
    }

    public static void request(BlockPos pos) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeVarInt(3);
            buf.writeBlockPos(pos);
            send(SERVUX, bytes(buf));
        } finally { buf.release(); }
    }

    private static byte[] bytes(FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
    }

    private static void send(Identifier id, byte[] data) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) connection.send(new ServerboundCustomPayloadPacket(new Outgoing(id, data)));
    }

    private static void receive(Identifier id, byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        try {
            if (id.equals(JADE)) {
                CompoundTag tag = buf.readNbt();
                if (tag != null) FurnaceTracker.INSTANCE.receiveJade(tag);
                return;
            }
            int type = buf.readVarInt();
            if (type == 1) {
                CompoundTag tag = buf.readNbt();
                if (tag != null) FurnaceTracker.INSTANCE.servuxMetadata(tag.getIntOr("version", -1));
            } else if (type == 5) {
                BlockPos pos = buf.readBlockPos();
                FurnaceTracker.INSTANCE.receiveServux(pos, readData(buf));
            }
            // The 26.2 entity provider emits simple responses. Unsupported bulk/entity
            // messages remain available to the owning mod and cannot confirm a furnace.
        } catch (Exception ignored) {
            // A malformed or unsupported response expires as unknown, never blocked.
        } finally { buf.release(); }
    }

    /** Servux DataTag uses a big-endian length followed by named NBT, usually gzip. */
    public static CompoundTag readData(FriendlyByteBuf buf) throws java.io.IOException {
        int length = buf.readInt();
        if (length <= 0 || length > MAX_BYTES || length > buf.readableBytes()) throw new java.io.IOException("Invalid data length");
        byte[] data = new byte[length];
        buf.readBytes(data);
        var stream = new ByteArrayInputStream(data);
        var accounter = NbtAccounter.create(MAX_BYTES);
        if (length >= 2 && (data[0] & 255) == 0x1f && (data[1] & 255) == 0x8b) {
            return NbtIo.readCompressed(stream, accounter);
        }
        return NbtIo.read(new DataInputStream(stream), accounter);
    }
}
