package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.furnace.FurnaceTracker;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

public final class FurnaceWorldTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        if (!Boolean.getBoolean("abstool.furnaceTests")) return;
        try (var server = context.worldBuilder().createServer(); var connection = server.connect()) {
            server.runCommand("gamemode creative @a");
            server.runCommand("fill -5 99 -5 5 99 10 minecraft:stone");
            server.runCommand("fill -5 100 -5 5 105 10 minecraft:air");
            server.runCommand("tp @a 0.5 100 0.5 0 0");
            server.runCommand("setblock 0 100 4 minecraft:furnace{Items:[{Slot:0b,id:\"minecraft:dirt\",count:1}]}");
            server.runCommand("setblock 2 100 4 minecraft:blast_furnace{Items:[{Slot:0b,id:\"minecraft:potato\",count:1}]}");
            server.runCommand("setblock -2 100 4 minecraft:smoker{Items:[{Slot:0b,id:\"minecraft:raw_iron\",count:1}]}");
            server.runCommand("setblock 0 100 6 minecraft:furnace{Items:[{Slot:0b,id:\"minecraft:raw_iron\",count:1}]}");
            context.runOnClient(client -> {
                ConfigManager.get().enabled = false;
                var config = FurnaceTracker.config(); config.enabled = true; config.jadeFallback = false;
            });
            context.waitFor(client -> FurnaceTracker.INSTANCE.extract(client).boxes().targets().size() == 3, 400);
            context.getInput().lookAt(new net.minecraft.core.BlockPos(0, 100, 4));
            context.waitTicks(10);
            context.takeScreenshot("abstool-furnace-blocked");
            server.runCommand("data merge block 0 100 4 {Items:[]}");
            context.waitFor(client -> FurnaceTracker.INSTANCE.extract(client).boxes().targets().size() == 2, 200);
            server.runCommand("fill -3 100 3 3 102 3 minecraft:stone");
            context.waitTicks(10);
            context.takeScreenshot("abstool-furnace-through-wall");
            int before = FurnaceProtocolFixture.requests.get();
            context.waitTicks(20);
            if (FurnaceProtocolFixture.requests.get() - before > 22) throw new AssertionError("Request rate exceeded");
            FurnaceProtocolFixture.reply = false;
            context.waitFor(client -> FurnaceTracker.INSTANCE.extract(client).labels().stream().allMatch(l -> l.text().contains("stale")), 400);
            context.takeScreenshot("abstool-furnace-stale");
            context.runOnClient(client -> FurnaceTracker.config().enabled = false);
        } finally { FurnaceProtocolFixture.reply = true; }
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("jade")) testJade(context);
    }

    private void testJade(ClientGameTestContext context) {
        FurnaceProtocolFixture.reply = false;
        try (var server = context.worldBuilder().createServer(); var connection = server.connect()) {
            server.runCommand("gamemode creative @a");
            server.runCommand("fill -5 99 -5 5 99 8 minecraft:stone");
            server.runCommand("fill -5 100 -5 5 105 8 minecraft:air");
            server.runCommand("tp @a 0.5 100 0.5 0 0");
            server.runCommand("setblock 0 100 4 minecraft:furnace{Items:[{Slot:0b,id:\"minecraft:dirt\",count:1}]}");
            server.runCommand("setblock 40 100 4 minecraft:furnace{Items:[{Slot:0b,id:\"minecraft:dirt\",count:1}]}");
            context.runOnClient(client -> {
                FurnaceTracker.config().jadeFallback = true;
                FurnaceTracker.config().enabled = true;
            });
            context.waitFor(client -> FurnaceTracker.INSTANCE.extract(client).boxes().targets().size() == 1, 400);
            context.runOnClient(client -> {
                var target = FurnaceTracker.INSTANCE.extract(client).boxes().targets().getFirst();
                if (target.x() != 0) throw new AssertionError("Jade must honor its default range");
            });
            context.takeScreenshot("abstool-furnace-jade");
            server.runCommand("data merge block 0 100 4 {Items:[]}");
            context.waitFor(client -> FurnaceTracker.INSTANCE.extract(client).boxes().targets().isEmpty(), 200);
            context.runOnClient(client -> FurnaceTracker.config().enabled = false);
        } finally { FurnaceProtocolFixture.reply = true; }
    }
}
