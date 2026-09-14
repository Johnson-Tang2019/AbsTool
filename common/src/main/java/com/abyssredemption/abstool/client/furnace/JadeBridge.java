package com.abyssredemption.abstool.client.furnace;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Optional Jade 26.2 adapter. Its own handshake supplies the provider ID mapping. */
public final class JadeBridge {
    private static boolean incompatible;
    private JadeBridge() {}

    public static boolean request(BlockPos pos, long token) {
        if (incompatible) return false;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) return false;
        double range = client.player.blockInteractionRange() + FurnaceTracker.config().jadeExtraRange;
        if (pos.distSqr(client.player.blockPosition()) > range * range) return false;
        try {
            Class<?> center = Class.forName("snownee.jade.impl.ObjectDataCenter");
            if (!center.getField("serverConnected").getBoolean(null)) return false;
            // Jade checks its actual server-side game rule and loaded chunks. Do not
            // invent a larger client allowance or spoof the player's position.
            Class<?> sync = Class.forName("snownee.jade.impl.BlockAccessorImpl$SyncData");
            CompoundTag extra = new CompoundTag();
            extra.putLong("abstool_request", token);
            Object data = sync.getConstructor(boolean.class, BlockHitResult.class, ItemStack.class, CompoundTag.class)
                    .newInstance(false, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false), ItemStack.EMPTY, extra);
            Object provider = Class.forName("snownee.jade.addon.vanilla.FurnaceProvider").getField("INSTANCE").get(null);
            CustomPacketPayload packet = (CustomPacketPayload) Class.forName("snownee.jade.network.RequestBlockPacket")
                    .getConstructor(sync, List.class).newInstance(data, List.of(provider));
            client.getConnection().send(new ServerboundCustomPayloadPacket(packet));
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            incompatible = true;
            return false;
        }
    }
}
