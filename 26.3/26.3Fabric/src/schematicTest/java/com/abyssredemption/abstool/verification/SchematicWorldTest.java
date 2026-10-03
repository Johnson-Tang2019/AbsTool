package com.abyssredemption.abstool.verification;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public final class SchematicWorldTest implements FabricClientGameTest {
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            com.abyssredemption.abstool.client.vault.config.ConfigManager.get().schematicShaderCompat.debug = true;
            com.abyssredemption.abstool.client.vault.config.ConfigManager.get().schematicShaderCompat.mode = com.abyssredemption.abstool.client.schematic.SchematicShaderConfig.Mode.OFF;
        });
        try (var world = context.worldBuilder().create()) {
            world.getServer().runCommand("gamemode creative @a");
            world.getServer().runCommand("fill -12 99 -12 12 99 18 stone");
            world.getServer().runCommand("fill -12 100 -12 12 108 18 air");
            world.getServer().runCommand("setblock 8 100 4 bricks");
            world.getServer().runCommand("tp @a 0.5 100 -3.5 0 0");
            context.waitFor(client -> client.level != null && client.level.getBlockState(new BlockPos(8, 100, 4)).is(Blocks.BRICKS));
            context.runOnClient(client -> {
                var area = new AreaSelection();
                area.setName("AbsTool isolated rendering fixture");
                area.addSubRegionBox(new Box(new BlockPos(8, 100, 4), new BlockPos(8, 100, 4), "block"), false);
                var schematic = LitematicaSchematic.createFromWorld(client.level, area,
                        new LitematicaSchematic.SchematicSaveInfo(false, true), "AbsTool test", message -> { throw new AssertionError(message); });
                if (schematic == null) throw new AssertionError("No real schematic created");
                DataManager.getSchematicPlacementManager().addSchematicPlacement(
                        SchematicPlacement.createFor(schematic, new BlockPos(0, 100, 4), "AbsTool rendering test", true, true), false);
                Configs.Visuals.ENABLE_RENDERING.setBooleanValue(true);
                Configs.Visuals.ENABLE_SCHEMATIC_RENDERING.setBooleanValue(true);
                Configs.Visuals.ENABLE_SCHEMATIC_BLOCKS.setBooleanValue(true);
                Configs.Visuals.ENABLE_SCHEMATIC_OVERLAY.setBooleanValue(false);
                Configs.Visuals.RENDER_BLOCKS_AS_TRANSLUCENT.setBooleanValue(true);
            });
            context.waitFor(client -> SchematicWorldHandler.getSchematicWorld() != null
                    && SchematicWorldHandler.getSchematicWorld().getBlockState(new BlockPos(0, 100, 4)).is(Blocks.BRICKS));
            context.getInput().lookAt(new BlockPos(0, 100, 4));
            context.waitTicks(160);
            context.takeScreenshot("schematic-baseline");
            context.runOnClient(client -> com.abyssredemption.abstool.client.vault.config.ConfigManager.get().schematicShaderCompat.mode = com.abyssredemption.abstool.client.schematic.SchematicShaderConfig.Mode.AUTO);
            context.waitTicks(160);
            context.takeScreenshot("schematic-compat");
            sample(context, "single-block");
            context.runOnClient(client -> Configs.Visuals.ENABLE_SCHEMATIC_RENDERING.setBooleanValue(false));
            context.waitTicks(20);
            sample(context, "no-visible-projection");
            context.runOnClient(client -> Configs.Visuals.ENABLE_SCHEMATIC_RENDERING.setBooleanValue(true));
            context.runOnClient(client -> net.irisshaders.iris.api.v0.IrisApi.getInstance().getConfig().setShadersEnabledAndApply(false));
            context.waitTicks(120);
            context.takeScreenshot("schematic-no-shaders");
            context.runOnClient(client -> net.irisshaders.iris.api.v0.IrisApi.getInstance().getConfig().setShadersEnabledAndApply(true));
            String[] models = {"bricks", "glass", "red_stained_glass", "glass_pane", "oak_leaves[persistent=true]",
                    "poppy", "short_grass", "oak_stairs[facing=east,half=bottom]", "stone_slab[type=top]", "oak_trapdoor[open=true,facing=west]",
                    "oak_fence", "cobblestone_wall", "repeater[facing=east,delay=3]", "comparator[facing=west,mode=subtract]", "redstone_wire",
                    "oak_door[half=lower,facing=south]", "chest[facing=south]", "oak_sign", "water", "stone_button[face=floor]"};
            world.getServer().runCommand("fill 20 99 20 32 99 32 stone");
            world.getServer().runCommand("fill 20 100 20 32 104 32 air");
            for (int i = 0; i < models.length; i++) {
                world.getServer().runCommand("setblock " + (20 + i % 5 * 2) + " 100 " + (20 + i / 5 * 3) + " " + models[i]);
            }
            world.getServer().runCommand("setblock 20 101 29 oak_door[half=upper,facing=south]");
            context.waitFor(client -> client.level.getBlockState(new BlockPos(20, 100, 20)).is(Blocks.BRICKS));
            var placement = context.computeOnClient(client -> {
                var area = new AreaSelection();
                area.setName("AbsTool model coverage");
                area.addSubRegionBox(new Box(new BlockPos(20, 100, 20), new BlockPos(28, 102, 29), "models"), false);
                var schematic = LitematicaSchematic.createFromWorld(client.level, area,
                        new LitematicaSchematic.SchematicSaveInfo(false, true), "AbsTool test", message -> { throw new AssertionError(message); });
                var result = SchematicPlacement.createFor(schematic, new BlockPos(-5, 100, 6), "Model coverage", true, true);
                DataManager.getSchematicPlacementManager().addSchematicPlacement(result, false);
                return result;
            });
            world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().forEach(player -> {
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
            }));
            world.getServer().runCommand("tp @a -0.5 105 0.5");
            context.waitTicks(5);
            context.getInput().lookAt(new BlockPos(-1, 100, 10));
            context.waitTicks(100);
            context.takeScreenshot("schematic-models");
            sample(context, "model-grid");
            context.runOnClient(client -> com.abyssredemption.abstool.client.vault.config.ConfigManager.get().schematicShaderCompat.opacityMultiplier = 0.2);
            context.waitTicks(20);
            context.takeScreenshot("schematic-low-opacity");
            context.runOnClient(client -> com.abyssredemption.abstool.client.vault.config.ConfigManager.get().schematicShaderCompat.opacityMultiplier = 1.0);
            context.runOnClient(client -> {
                var status = com.abyssredemption.abstool.client.schematic.SchematicShaderStatus.get();
                if (status.state() != com.abyssredemption.abstool.client.schematic.SchematicShaderStatus.State.ACTIVE || status.draws() == 0)
                    throw new AssertionError("No successful schematic submissions: " + status);
                Configs.Visuals.ENABLE_SCHEMATIC_OVERLAY.setBooleanValue(true);
            });
            context.waitTicks(40);
            context.takeScreenshot("schematic-overlays");
            context.runOnClient(client -> Configs.Visuals.ENABLE_SCHEMATIC_OVERLAY.setBooleanValue(false));
            world.getServer().runCommand("fill -6 100 3 4 103 3 stone");
            context.waitTicks(30);
            context.takeScreenshot("schematic-world-occlusion");
            world.getServer().runCommand("fill -6 100 3 4 103 3 air");
            context.runOnClient(client -> {
                placement.setRotation(net.minecraft.world.level.block.Rotation.CLOCKWISE_90, null);
                placement.setMirror(net.minecraft.world.level.block.Mirror.LEFT_RIGHT, null);
            });
            context.waitTicks(80);
            context.takeScreenshot("schematic-rotation-mirror");
            context.runOnClient(client -> {
                DataManager.getRenderLayerRange().setLayerMode(fi.dy.masa.malilib.util.LayerMode.SINGLE_LAYER, false);
                DataManager.getRenderLayerRange().setLayerSingle(101);
            });
            context.waitTicks(50);
            context.takeScreenshot("schematic-single-layer");
            context.runOnClient(client -> {
                DataManager.getRenderLayerRange().setLayerMode(fi.dy.masa.malilib.util.LayerMode.ALL, false);
                client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            });
            context.waitTicks(30);
            context.takeScreenshot("schematic-third-person");
            context.runOnClient(client -> client.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            var reload = context.computeOnClient(client -> client.reloadResourcePacks());
            context.waitFor(client -> reload.isDone(), 600);
            context.waitTicks(60);
            context.takeScreenshot("schematic-resource-reload");
            context.runOnClient(client -> client.getWindow().setWindowed(1024, 640));
            context.waitTicks(40);
            context.takeScreenshot("schematic-resized");
            context.runOnClient(client -> {
                var status = com.abyssredemption.abstool.client.schematic.SchematicShaderStatus.get();
                if (status.state() != com.abyssredemption.abstool.client.schematic.SchematicShaderStatus.State.ACTIVE)
                    throw new AssertionError("Compatibility failed after reload/resize: " + status);
            });
            context.runOnClient(client -> {
                placement.setEnabled(false);
                client.gui.setScreen(com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory.create(null));
            });
            context.waitTicks(10);
            context.takeScreenshot("schematic-settings-tabs");
            context.getInput().pressKey(256);
            world.getServer().runCommand("fill 20 110 20 51 117 51 bricks hollow");
            context.waitFor(client -> client.level.getBlockState(new BlockPos(20, 110, 20)).is(Blocks.BRICKS));
            var large = context.computeOnClient(client -> {
                var area = new AreaSelection();
                area.addSubRegionBox(new Box(new BlockPos(20, 110, 20), new BlockPos(51, 117, 51), "large"), false);
                var schematic = LitematicaSchematic.createFromWorld(client.level, area,
                        new LitematicaSchematic.SchematicSaveInfo(false, true), "AbsTool test", message -> { throw new AssertionError(message); });
                var result = SchematicPlacement.createFor(schematic, new BlockPos(-16, 105, 8), "8192-volume fixture", true, true);
                DataManager.getSchematicPlacementManager().addSchematicPlacement(result, false);
                return result;
            });
            world.getServer().runCommand("fill 20 110 20 51 117 51 air");
            context.getInput().lookAt(new BlockPos(0, 108, 8));
            context.waitTicks(160);
            context.takeScreenshot("schematic-large");
            context.waitFor(client -> com.abyssredemption.abstool.fabric.compat.schematic.SchematicAdapter.frameIndices > 1000, 600);
            sample(context, "large-8192-volume");
            context.runOnClient(client -> large.setEnabled(false));
        }
    }

    private static void sample(ClientGameTestContext context, String label) {
        context.runOnClient(client -> FrameSamples.begin(label));
        context.waitTicks(60);
        context.runOnClient(FrameSamples::end);
    }
}
