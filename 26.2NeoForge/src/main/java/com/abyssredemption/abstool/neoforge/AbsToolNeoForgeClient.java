package com.abyssredemption.abstool.neoforge;

import com.abyssredemption.abstool.AbsTool;
import com.abyssredemption.abstool.client.AbsToolClient;
import com.abyssredemption.abstool.client.vault.client.VaultFrame;
import com.abyssredemption.abstool.client.vault.client.VaultRenderer;
import com.abyssredemption.abstool.client.vault.client.VaultTrackerController;
import com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(value = AbsTool.MOD_ID, dist = Dist.CLIENT)
public final class AbsToolNeoForgeClient {
    private static final ContextKey<com.abyssredemption.abstool.client.furnace.FurnaceFrame> FURNACES = new ContextKey<>(Identifier.parse("abstool:furnaces"));
    private static final ContextKey<VaultFrame> VAULTS = new ContextKey<>(
            Identifier.fromNamespaceAndPath(AbsTool.MOD_ID, "vaults"));
    private final VaultTrackerController controller = new VaultTrackerController();

    public AbsToolNeoForgeClient(ModContainer container) {
        com.abyssredemption.abstool.client.furnace.FurnaceNetwork.delegateServux = () ->
                com.abyssredemption.abstool.neoforge.mixin.NetworkRegistryAccessor.abstool$registrations()
                        .getOrDefault(net.minecraft.network.ConnectionProtocol.PLAY, java.util.Map.of())
                        .containsKey(com.abyssredemption.abstool.client.furnace.FurnaceNetwork.SERVUX);
        com.abyssredemption.abstool.client.furnace.FurnaceNetwork.canSend = id -> Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().getConnection().hasChannel(id);
        AbsToolClient.init(FMLPaths.CONFIGDIR.get());
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> ClothConfigScreenFactory.create(parent));
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::extract);
        NeoForge.EVENT_BUS.addListener(this::submit);
        NeoForge.EVENT_BUS.addListener(this::useBlock);
    }

    private void tick(ClientTickEvent.Post event) {
        AbsToolClient.tickShortcut(Minecraft.getInstance());
        controller.tick(Minecraft.getInstance());
    }

    private void extract(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(FURNACES, com.abyssredemption.abstool.client.furnace.FurnaceTracker.INSTANCE.extract(Minecraft.getInstance()));
        event.getRenderState().setRenderData(VAULTS, controller.extract(Minecraft.getInstance()));
    }

    private void submit(SubmitCustomGeometryEvent event) {
        com.abyssredemption.abstool.client.furnace.FurnaceFrame.submit(event.getLevelRenderState().getRenderData(FURNACES),
                event.getLevelRenderState().cameraRenderState,
                Minecraft.getInstance().gameRenderer.gameRenderState().optionsRenderState, event.getSubmitNodeCollector());
        VaultRenderer.submit(event.getLevelRenderState().getRenderData(VAULTS),
                event.getLevelRenderState().cameraRenderState,
                Minecraft.getInstance().gameRenderer.gameRenderState().optionsRenderState,
                event.getSubmitNodeCollector());
    }

    private void useBlock(PlayerInteractEvent.RightClickBlock event) {
        controller.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
    }
}
