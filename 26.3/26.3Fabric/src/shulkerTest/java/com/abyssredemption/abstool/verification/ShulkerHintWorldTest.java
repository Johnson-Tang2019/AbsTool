package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.fabric.compat.shulker.DominantItemHint;
import com.abyssredemption.abstool.fabric.compat.shulker.ShulkerMixinPlugin;
import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.impl.mc_tweaks.shulkerBoxItemContentHint.ShulkerBoxItemContentHintCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.List;

public final class ShulkerHintWorldTest implements net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest {
    private static ItemStack box(ItemStack... items) {
        ItemStack box = new ItemStack(Items.SHULKER_BOX);
        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(items)));
        return box;
    }
    public void runTest(net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            context.runOnClient(client -> {
            if (!ShulkerMixinPlugin.supported) throw new AssertionError("Renderer profile not enabled");
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT.setBooleanValue(true);
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT_SCALE.setDoubleValue(0.75);
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT_SHOW_BAR_ON_MIXED.setBooleanValue(false);
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT_CUSTOM_NAMES_OVERRIDE_ITEM.setBooleanValue(false);
            ItemStack mixed = box(new ItemStack(Items.DIAMOND, 32), new ItemStack(Items.STONE, 20), new ItemStack(Items.STONE, 20));
            ItemStack tie = box(new ItemStack(Items.DIAMOND, 40), new ItemStack(Items.STONE, 40));
            ItemStack single = box(new ItemStack(Items.APPLE, 16));
            ConfigManager.get().shulkerMostCommonItem = false;
            var off = ShulkerBoxItemContentHintCommon.prepareInformation(mixed);
            ConfigManager.get().shulkerMostCommonItem = true;
            var on = ShulkerBoxItemContentHintCommon.prepareInformation(mixed);
            if (!on.stack.is(Items.STONE) || on.allItemSame || on.allItemSameIgnoreNbt || on.fillRatio != off.fillRatio)
                throw new AssertionError("Total counts or mixed-box state changed incorrectly");
            if (!off.stack.is(Items.DIAMOND)) throw new AssertionError("OFF did not retain upstream behavior");
            if (!ShulkerBoxItemContentHintCommon.prepareInformation(tie).stack.is(Items.DIAMOND)) throw new AssertionError("Unstable tie");
            if (!ShulkerBoxItemContentHintCommon.prepareInformation(single).allItemSame) throw new AssertionError("Single-type behavior changed");
            if (ShulkerBoxItemContentHintCommon.prepareInformation(box()).enabled) throw new AssertionError("Empty box enabled");
            if (ShulkerBoxItemContentHintCommon.prepareInformation(new ItemStack(Items.STONE)).enabled) throw new AssertionError("Non-box enabled");
            ItemStack named = new ItemStack(Items.STONE, 25);
            named.set(DataComponents.CUSTOM_NAME, Component.literal("Named stone"));
            if (!DominantItemHint.select(List.of(new ItemStack(Items.DIAMOND, 40), named, new ItemStack(Items.STONE, 20))).is(Items.STONE))
                throw new AssertionError("Component variants must count together");
            if (mixed.get(DataComponents.CONTAINER).allItemsCopyStream().mapToInt(ItemStack::getCount).sum() != 72)
                throw new AssertionError("Input contents mutated");
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT.setBooleanValue(false);
            if (ShulkerBoxItemContentHintCommon.prepareInformation(mixed).enabled) throw new AssertionError("Upstream toggle bypassed");
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT.setBooleanValue(true);
            ItemStack renamed = mixed.copy();
            renamed.set(DataComponents.CUSTOM_NAME, Component.literal("minecraft:diamond"));
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT_CUSTOM_NAMES_OVERRIDE_ITEM.setBooleanValue(true);
            if (!ShulkerBoxItemContentHintCommon.prepareInformation(renamed).stack.is(Items.DIAMOND))
                throw new AssertionError("Explicit name override lost precedence");
            TweakerMoreConfigs.SHULKER_BOX_ITEM_CONTENT_HINT_CUSTOM_NAMES_OVERRIDE_ITEM.setBooleanValue(false);
            client.gui.setScreen(new Fixture(List.of(mixed, tie, single, box())));
            });
            context.waitTicks(60);
            context.takeScreenshot("shulker-majority");
            context.runOnClient(client -> client.gui.setScreen(null));
            org.slf4j.LoggerFactory.getLogger("abstool-smoke").info("ABSTOOL_SHULKER_HINT_PASS");
        }
    }
    private static final class Fixture extends Screen {
        private final List<ItemStack> boxes;
        Fixture(List<ItemStack> boxes) { super(Component.literal("Shulker hint comparison")); this.boxes = boxes; }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
            g.fill(0, 0, width, height, 0xff202028);
            g.text(font, "TweakerMore: dots / AbsTool: most common", 20, 15, 0xffffffff);
            String[] labels = {"32 diamond + 20 stone + 20 stone", "40 diamond + 40 stone (tie)", "16 apples (single type)", "Empty box"};
            boolean previous = ConfigManager.get().shulkerMostCommonItem;
            try {
                for (int i = 0; i < boxes.size(); i++) {
                    int y = 48 + i * 44;
                    g.text(font, labels[i], 20, y, 0xffffffff);
                    for (int column = 0; column < 2; column++) {
                        ConfigManager.get().shulkerMostCommonItem = column == 1;
                        g.pose().pushMatrix();
                        g.pose().translate(270 + column * 70, y - 7);
                        g.pose().scale(2, 2);
                        g.item(boxes.get(i), 0, 0);
                        g.itemDecorations(font, boxes.get(i), 0, 0);
                        g.pose().popMatrix();
                    }
                }
            } finally { ConfigManager.get().shulkerMostCommonItem = previous; }
        }
    }
}
