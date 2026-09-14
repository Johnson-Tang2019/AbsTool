package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.vault.client.VaultTrackerController;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.vault.config.ModConfig;
import com.abyssredemption.abstool.client.vault.storage.VaultKey;
import com.abyssredemption.abstool.client.vault.storage.VaultStorage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class VaultWorldTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getServer().runCommand("gamemode creative @a");
            world.getServer().runCommand("fill -5 99 -5 5 99 8 minecraft:stone");
            world.getServer().runCommand("fill -5 100 -5 5 105 8 minecraft:air");
            world.getServer().runCommand("tp @a 0.5 100 0.5 0 0");
            BlockPos ominous = new BlockPos(0, 100, 4);
            BlockPos normal = new BlockPos(2, 100, 4);
            world.getServer().runOnServer(server -> {
                server.overworld().setBlockAndUpdate(ominous,
                        Blocks.VAULT.defaultBlockState().setValue(BlockStateProperties.OMINOUS, true));
                server.overworld().setBlockAndUpdate(normal, Blocks.VAULT.defaultBlockState());
            });
            context.waitFor(client -> client.level != null && client.player != null
                    && client.level.getBlockState(ominous).is(Blocks.VAULT));
            context.runOnClient(client -> {
                ConfigManager.get().enabled = true;
                ConfigManager.get().renderRadius = 2;
            });
            context.waitTicks(60);
            context.getInput().lookAt(ominous);
            VaultTrackerController controller = new VaultTrackerController();
            context.runOnClient(client -> {
                controller.tick(client);
                var frame = controller.extract(client);
                if (frame.targets().size() != 1) throw new AssertionError("Expected only the ominous vault: " + frame);
            });
            context.waitTicks(10);
            context.takeScreenshot("abstool-ominous-vault");
            context.getInput().lookAt(normal);
            context.waitTicks(10);
            context.runOnClient(client -> {
                var state = client.gameRenderer.gameRenderState();
                var origin = com.abyssredemption.abstool.client.vault.client.VaultRenderer.getCrosshairOrigin(
                        state.levelRenderState.cameraRenderState, state.optionsRenderState.bobView,
                        state.optionsRenderState.damageTiltStrength);
                if (!origin.isFinite() || origin.length() > 1.0F) {
                    throw new AssertionError("Reverse-Z tracer origin must remain near the camera: " + origin);
                }
            });
            context.takeScreenshot("abstool-tracer");
            world.getServer().runCommand("fill -1 100 3 1 102 3 minecraft:stone");
            context.getInput().lookAt(normal);
            context.waitTicks(10);
            context.takeScreenshot("abstool-vault-through-wall");
            world.getServer().runCommand("fill -1 100 3 1 102 3 minecraft:air");
            context.getInput().lookAt(ominous);
            context.waitTicks(5);
            context.getInput().pressMouse(1);
            context.waitTicks(5);
            context.runOnClient(client -> {
                VaultKey key = VaultKey.of(VaultTrackerController.currentServerKey(client),
                        VaultTrackerController.currentDimensionKey(client.level), ominous);
                if (!VaultStorage.isExcluded(key)) throw new AssertionError("Right-click callback did not mark vault");
                if (!controller.extract(client).targets().isEmpty()) throw new AssertionError("Handled vault was not hidden");
                ConfigManager.get().excludedRenderMode = ModConfig.ExcludedRenderMode.OTHER_COLOR;
                var targets = controller.extract(client).targets();
                if (targets.size() != 1 || targets.getFirst().color() != (0xFF000000 | ConfigManager.get().excludedColor)) {
                    throw new AssertionError("Handled vault did not use the configured alternate color");
                }
            });
            context.waitTicks(10);
            context.takeScreenshot("abstool-handled-vault");
            context.runOnClient(client -> {
                ConfigManager.get().tracerRequiresItem = true;
                if (VaultTrackerController.shouldRenderTracer(client, ConfigManager.get())) {
                    throw new AssertionError("Empty hands must not satisfy the required tracer item");
                }
            });
            context.getInput().holdKey(82);
            context.getInput().holdKey(66);
            context.waitFor(client -> client.gui.screen() instanceof me.shedaniel.clothconfig2.gui.AbstractConfigScreen);
            context.getInput().releaseKey(82);
            context.getInput().releaseKey(66);
            context.takeScreenshot("abstool-rb-settings");
            context.getInput().pressKey(256);
            context.runOnClient(client -> ConfigManager.get().enabled = false);
        }
    }
}
