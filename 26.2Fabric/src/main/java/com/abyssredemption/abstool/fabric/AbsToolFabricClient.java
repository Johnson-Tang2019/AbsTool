package com.abyssredemption.abstool.fabric;

import com.abyssredemption.abstool.client.AbsToolClient;
import com.abyssredemption.abstool.client.vault.client.VaultFrame;
import com.abyssredemption.abstool.client.vault.client.VaultRenderer;
import com.abyssredemption.abstool.client.vault.client.VaultTrackerController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;

public final class AbsToolFabricClient implements ClientModInitializer {
    private static final RenderStateDataKey<com.abyssredemption.abstool.client.furnace.FurnaceFrame> FURNACES = RenderStateDataKey.create(() -> "abstool:furnaces");
    private static final RenderStateDataKey<VaultFrame> VAULTS = RenderStateDataKey.create(() -> "abstool:vaults");

    @Override
    public void onInitializeClient() {
        com.abyssredemption.abstool.client.furnace.FurnaceNetwork.canSend = net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking::canSend;
        AbsToolClient.init(FabricLoader.getInstance().getConfigDir());
        com.abyssredemption.abstool.fabric.compat.schematic.SchematicBootstrap.init();
        if (FabricLoader.getInstance().isModLoaded("tweakermore")) {
            com.abyssredemption.abstool.client.vault.config.QuickSettings.provider =
                    com.abyssredemption.abstool.fabric.compat.TweakerMoreQuickSettings::create;
        }
        com.abyssredemption.abstool.fabric.compat.TweakerooElytraBridge.init();
        VaultTrackerController controller = new VaultTrackerController();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AbsToolClient.tickShortcut(client);
            controller.tick(client);
        });
        UseBlockCallback.EVENT.register(controller::onUseBlock);
        LevelExtractionEvents.END_EXTRACTION.register(context -> context.levelState().setData(FURNACES,
                com.abyssredemption.abstool.client.furnace.FurnaceTracker.INSTANCE.extract(Minecraft.getInstance())));
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> com.abyssredemption.abstool.client.furnace.FurnaceFrame.submit(
                context.levelState().getData(FURNACES), context.levelState().cameraRenderState,
                context.gameRenderer().gameRenderState().optionsRenderState, context.submitNodeCollector()));
        LevelExtractionEvents.END_EXTRACTION.register(context ->
                context.levelState().setData(VAULTS, controller.extract(Minecraft.getInstance())));
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> VaultRenderer.submit(
                context.levelState().getData(VAULTS), context.levelState().cameraRenderState,
                context.gameRenderer().gameRenderState().optionsRenderState, context.submitNodeCollector()));
    }
}
