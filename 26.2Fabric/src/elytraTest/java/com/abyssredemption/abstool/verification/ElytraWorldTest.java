package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.elytra.ElytraLaunch;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class ElytraWorldTest implements FabricClientGameTest {
    private final AtomicBoolean glided = new AtomicBoolean();
    private boolean external;
    public void runTest(ClientGameTestContext context) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getPlayerList().getPlayers().stream().anyMatch(p -> p.isFallFlying())) glided.set(true);
        });
        external = FabricLoader.getInstance().isModLoaded("tweakeroo");
        if (Boolean.getBoolean("abstool.elytraExitOnly")) { exitTest(context); return; }
        try (var world = context.worldBuilder().create()) {
            configure(context);
            var server = world.getServer();
            for (String scenario : new String[]{"two-block", "open", "slab", "stairs", "full", "equipped", "held", "key"}) {
                fixture(context, server, scenario);
                glided.set(false);
                if (scenario.equals("held")) {
                    context.getInput().holdKey(options -> options.keyShift);
                    context.getInput().holdKey(options -> options.keyJump);
                } else if (scenario.equals("key")) {
                    context.runOnClient(client -> ConfigManager.get().elytraAssist.trigger = com.abyssredemption.abstool.client.elytra.ElytraConfig.Trigger.KEY);
                    context.getInput().holdKey(71);
                } else context.runOnClient(ElytraLaunch::begin);
                context.waitFor(client -> glided.get(), 100);
                if (scenario.equals("two-block")) context.takeScreenshot("elytra-two-block-launch");
                context.waitTicks(30);
                if (scenario.equals("held")) {
                    context.getInput().releaseKey(options -> options.keyJump);
                    context.getInput().releaseKey(options -> options.keyShift);
                }
                if (scenario.equals("key")) {
                    context.getInput().releaseKey(71);
                    context.runOnClient(client -> ConfigManager.get().elytraAssist.trigger = com.abyssredemption.abstool.client.elytra.ElytraConfig.Trigger.SNEAK_JUMP);
                }
                inventory(server);
                context.runOnClient(client -> {
                    if (ElytraLaunch.requests() != 1) throw new AssertionError("Unexpected request count " + scenario + ": " + ElytraLaunch.requests());
                });
                if (external) server.waitFor(s -> s.getPlayerList().getPlayers().getFirst().getItemBySlot(EquipmentSlot.CHEST).is(Items.NETHERITE_CHESTPLATE), 80);
                System.out.println("ABSTOOL_ELYTRA_PASS " + scenario + " external=" + external);
            }
            for (String item : new String[]{"air", "elytra[damage=431]"}) {
                fixture(context, server, "two-block");
                server.runCommand("item replace entity @a inventory.0 with " + item);
                context.waitTicks(10);
                context.runOnClient(client -> {
                    ElytraLaunch.begin(client);
                    if (ElytraLaunch.state() != ElytraLaunch.State.IDLE || ElytraLaunch.requests() != 0) throw new AssertionError("Invalid elytra accepted");
                });
            }
            if (external) {
                fixture(context, server, "two-block");
                server.runCommand("item replace entity @a inventory.0 with elytra[damage=427]");
                context.waitTicks(10);
                context.runOnClient(ElytraLaunch::begin);
                context.waitTicks(20);
                context.runOnClient(client -> {
                    if (ElytraLaunch.state() != ElytraLaunch.State.IDLE || ElytraLaunch.requests() != 0
                        || !client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.NETHERITE_CHESTPLATE))
                        throw new AssertionError("External failure fell back or did not time out");
                });
            }
            fixture(context, server, "two-block");
            context.runOnClient(client -> {
                ElytraLaunch.begin(client);
                client.gui.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(client.player));
            });
            context.waitTicks(2);
            context.runOnClient(client -> {
                if (ElytraLaunch.state() != ElytraLaunch.State.IDLE) throw new AssertionError("GUI retained state");
                client.gui.setScreen(null);
            });
            fixture(context, server, "two-block");
            server.runCommand("item replace entity @a hotbar.1 with diamond_chestplate");
            context.waitTicks(5);
            context.runOnClient(client -> {
                ElytraLaunch.begin(client);
                client.gameMode.handleContainerInput(client.player.inventoryMenu.containerId, 6, 1,
                    net.minecraft.world.inventory.ContainerInput.SWAP, client.player);
            });
            context.waitTicks(15);
            context.runOnClient(client -> {
                if (ElytraLaunch.state() != ElytraLaunch.State.IDLE || ElytraLaunch.requests() != 0
                    || !client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE))
                    throw new AssertionError("Manual equipment change was overridden");
            });
            fixture(context, server, "two-block");
            context.runOnClient(client -> {
                ElytraLaunch.begin(client);
                ConfigManager.get().elytraAssist.enabled = false;
            });
            context.waitTicks(2);
            context.runOnClient(client -> {
                if (ElytraLaunch.state() != ElytraLaunch.State.IDLE) throw new AssertionError("Disable retained state");
                ConfigManager.get().elytraAssist.enabled = true;
            });
            context.runOnClient(client -> {
                var screen = (me.shedaniel.clothconfig2.gui.ClothConfigScreen) com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory.create(null);
                screen.selectedCategoryIndex = 2;
                client.gui.setScreen(screen);
            });
            context.waitTicks(3);
            context.getInput().scroll(-30);
            context.waitTicks(3);
            context.takeScreenshot("elytra-settings");
            context.runOnClient(client -> client.gui.setScreen(null));
            fixture(context, server, "two-block");
            context.runOnClient(ElytraLaunch::begin);
            server.runCommand("execute in minecraft:the_nether run tp @a 0 100 0");
            context.waitFor(client -> client.level.dimension() == net.minecraft.world.level.Level.NETHER, 300);
            context.runOnClient(client -> {
                if (ElytraLaunch.state() != ElytraLaunch.State.IDLE) throw new AssertionError("Dimension change retained state");
            });
            server.runCommand("kill @a");
            context.waitTicks(5);
            context.runOnClient(client -> {
                if (ElytraLaunch.state() != ElytraLaunch.State.IDLE) throw new AssertionError("Death retained state");
            });
            System.out.println("ABSTOOL_ELYTRA_NEGATIVE_PASS");
        }
        context.runOnClient(client -> {
            if (ElytraLaunch.state() != ElytraLaunch.State.IDLE) throw new AssertionError("Disconnect retained state");
        });
        if (!Boolean.getBoolean("abstool.acceptMinecraftEula")) throw new AssertionError("Dedicated test requires explicit -PacceptMinecraftEula");
        try { java.nio.file.Files.writeString(java.nio.file.Path.of("eula.txt"), "eula=true\n"); }
        catch (java.io.IOException e) { throw new AssertionError(e); }
        try (var server = context.worldBuilder().createServer(); var connection = server.connect()) {
            configure(context);
            fixture(context, server, "two-block");
            glided.set(false);
            context.runOnClient(ElytraLaunch::begin);
            context.waitFor(client -> glided.get(), 100);
            context.waitTicks(25);
            inventory(server);
            System.out.println("ABSTOOL_ELYTRA_DEDICATED_PASS external=" + external);
            fixture(context, server, "two-block");
            context.runOnClient(client -> {
                try {
                    var field = net.minecraft.network.Connection.class.getDeclaredField("channel");
                    field.setAccessible(true);
                    var channel = (io.netty.channel.Channel) field.get(client.getConnection().getConnection());
                    channel.eventLoop().submit(() -> channel.pipeline().addBefore("packet_handler", "abstool_test_delay", new io.netty.channel.ChannelDuplexHandler() {
                        @Override public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
                            ctx.executor().schedule(() -> ctx.fireChannelRead(msg), 200, java.util.concurrent.TimeUnit.MILLISECONDS);
                        }
                        @Override public void write(io.netty.channel.ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) {
                            ctx.executor().schedule(() -> ctx.writeAndFlush(msg, promise), 200, java.util.concurrent.TimeUnit.MILLISECONDS);
                        }
                    })).syncUninterruptibly();
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            });
            glided.set(false);
            context.runOnClient(ElytraLaunch::begin);
            context.waitFor(client -> glided.get(), 100);
            context.waitTicks(30);
            inventory(server);
            System.out.println("ABSTOOL_ELYTRA_LATENCY_400MS_PASS external=" + external);
        }
    }
    private void exitTest(ClientGameTestContext context) {
        AtomicBoolean exited = new AtomicBoolean();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getPlayerList().getPlayers().stream().anyMatch(p -> p.isFallFlying() && p.getZ() > 1.4)) exited.set(true);
        });
        try (var world = context.worldBuilder().create()) {
            configure(context);
            fixture(context, world.getServer(), "two-block");
            world.getServer().runCommand("fill -5 102 1 5 102 5 air");
            world.getServer().runCommand("tp @a 0.5 100 0.8 0 0");
            context.waitTicks(10);
            context.getInput().holdKey(options -> options.keyUp);
            context.getInput().holdKey(options -> options.keySprint);
            context.runOnClient(ElytraLaunch::begin);
            context.waitFor(client -> exited.get(), 80);
            context.getInput().releaseKey(options -> options.keyUp);
            context.getInput().releaseKey(options -> options.keySprint);
            System.out.println("ABSTOOL_ELYTRA_PLAYER_STEERED_EXIT_PASS external=" + external);
        }
    }
    private void configure(ClientGameTestContext context) {
        context.runOnClient(client -> {
            ConfigManager.get().elytraAssist.enabled = true;
            if (external) {
                try {
                    Class<?> c = Class.forName("fi.dy.masa.tweakeroo.config.FeatureToggle");
                    c.getMethod("setBooleanValue", boolean.class).invoke(c.getField("TWEAK_AUTO_SWITCH_ELYTRA").get(null), true);
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            }
        });
    }
    private void fixture(ClientGameTestContext context, TestServerContext server, String scenario) {
        context.runOnClient(client -> ElytraLaunch.reset());
        server.runCommand("fill -5 99 -5 5 99 5 stone");
        server.runCommand("fill -5 100 -5 5 105 5 air");
        if (!scenario.equals("open")) server.runCommand("fill -5 102 -5 5 102 5 stone");
        if (scenario.equals("slab")) server.runCommand("setblock 0 102 0 stone_slab[type=top]");
        if (scenario.equals("stairs")) server.runCommand("setblock 0 102 0 stone_stairs[half=top]");
        server.runCommand("gamemode survival @a");
        server.runCommand("tp @a 0.5 100 0.5 0 0");
        server.runCommand("clear @a");
        server.runCommand("item replace entity @a armor.chest with netherite_chestplate");
        server.runCommand("item replace entity @a inventory.0 with elytra");
        if (scenario.equals("equipped")) {
            server.runCommand("item replace entity @a armor.chest with elytra");
            server.runCommand("item replace entity @a inventory.0 with netherite_chestplate");
        }
        if (scenario.equals("full")) {
            for (int i=1;i<27;i++) server.runCommand("item replace entity @a inventory." + i + " with stone 64");
            for (int i=0;i<9;i++) server.runCommand("item replace entity @a hotbar." + i + " with stone 64");
        }
        context.waitTicks(25);
    }
    private static void inventory(TestServerContext server) {
        server.runOnServer(s -> {
            var p = s.getPlayerList().getPlayers().getFirst();
            int wings = 0, armor = 0;
            for (var slot : p.inventoryMenu.slots) {
                if (slot.getItem().is(Items.ELYTRA)) wings += slot.getItem().getCount();
                if (slot.getItem().is(Items.NETHERITE_CHESTPLATE)) armor += slot.getItem().getCount();
            }
            if (wings != 1 || armor != 1) throw new AssertionError("Equipment lost or duplicated " + wings + "/" + armor);
        });
    }
}
