package com.abyssredemption.abstool.smoke;

import com.abyssredemption.abstool.client.furnace.FurnaceTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.slf4j.LoggerFactory;

/** Connects only to the isolated local Servux test server prepared by the developer. */
public final class FurnaceServerSmoke {
    private int ticks, stage;
    public void tick(Minecraft client) {
        if (!client.isGameLoadFinished() || client.gui.overlay() != null) return;
        ticks++;
        if (stage == 0 && ticks > 20) {
            stage = 1;
            String address = "127.0.0.1:25576";
            ConnectScreen.startConnecting(client.gui.screen(), client, ServerAddress.parseString(address),
                    new ServerData("Ab's Tool isolated Servux test", address, ServerData.Type.OTHER), false, null);
        }
        if (stage == 1 && client.level != null && client.player != null && client.gui.screen() == null) {
            stage = 2;
            client.getConnection().sendCommand("gamemode creative");
            client.getConnection().sendCommand("fill -5 99 -5 5 99 8 stone");
            client.getConnection().sendCommand("fill -5 100 -5 5 105 8 air");
            client.getConnection().sendCommand("tp @s 0.5 100 0.5 0 0");
            client.getConnection().sendCommand("setblock 0 100 4 furnace{Items:[{Slot:0b,id:\"minecraft:dirt\",count:1}]}");
            client.getConnection().sendCommand("setblock 2 100 4 blast_furnace{Items:[{Slot:0b,id:\"minecraft:potato\",count:1}]}");
            client.getConnection().sendCommand("setblock -2 100 4 smoker{Items:[{Slot:0b,id:\"minecraft:raw_iron\",count:1}]}");
            client.getConnection().sendCommand("setblock 0 100 6 furnace{Items:[{Slot:0b,id:\"minecraft:raw_iron\",count:1}]}");
            FurnaceTracker.config().enabled = true;
            FurnaceTracker.config().jadeFallback = false;
        }
        if (stage == 2 && client.level != null && FurnaceTracker.INSTANCE.extract(client).boxes().targets().size() == 3) {
            stage = 3;
            Screenshot.grab(client.gameDirectory, "abstool-real-servux.png", client.gameRenderer.mainRenderTarget(), 1, message -> {});
            client.getConnection().sendCommand("data merge block 0 100 4 {Items:[]}");
        }
        if (stage == 3 && FurnaceTracker.INSTANCE.extract(client).boxes().targets().size() == 2) {
            LoggerFactory.getLogger("abstool-smoke").info("ABSTOOL_REAL_SERVUX_PASS: three furnace types, valid input excluded, repaired input cleared");
            client.stop();
            stage = 4;
        }
        if (ticks > 1400) throw new AssertionError("Real Servux test timed out: " + FurnaceTracker.INSTANCE.status().getString());
    }
}
