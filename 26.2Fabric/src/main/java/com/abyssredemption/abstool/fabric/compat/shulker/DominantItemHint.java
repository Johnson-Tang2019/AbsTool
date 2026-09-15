package com.abyssredemption.abstool.fabric.compat.shulker;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import me.fallenbreath.tweakermore.impl.mc_tweaks.shulkerBoxItemContentHint.ShulkerBoxItemContentHintCommon.Info;
import me.fallenbreath.tweakermore.util.InventoryUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.LinkedHashMap;

public final class DominantItemHint {
    public static boolean replacesEllipsis(Info info) {
        return ConfigManager.get().shulkerMostCommonItem && info.enabled && !info.allItemSameIgnoreNbt;
    }
    public static Info prepare(Info info, ItemStack box) {
        if (!replacesEllipsis(info)) return info;
        InventoryUtils.getStoredItems(box).ifPresent(items -> {
            ItemStack winner = select(items);
            if (!winner.isEmpty()) info.stack = winner;
        });
        return info;
    }
    /** Aggregate item counts, ignoring component differences; ties follow first occupied slot. */
    public static ItemStack select(Iterable<ItemStack> items) {
        var counts = new LinkedHashMap<Item, Long>();
        var representatives = new LinkedHashMap<Item, ItemStack>();
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            counts.merge(stack.getItem(), (long) stack.getCount(), Long::sum);
            representatives.putIfAbsent(stack.getItem(), stack);
        }
        ItemStack winner = ItemStack.EMPTY;
        long maximum = -1;
        for (var entry : counts.entrySet()) {
            if (entry.getValue() > maximum) {
                maximum = entry.getValue();
                winner = representatives.get(entry.getKey());
            }
        }
        return winner.isEmpty() ? ItemStack.EMPTY : winner.copyWithCount(1);
    }
}
