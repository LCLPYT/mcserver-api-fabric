/*
 * Copyright (c) 2022 LCLP.
 *
 * Licensed under the MIT License. For more information, consider the LICENSE file in the project's root directory.
 */

package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.lclpnetwork.facade.MCStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class StatsManager {

    private final WeakHashMap<Inventory, StatsInventory> statsInventories = new WeakHashMap<>();

    public void markAsStats(Inventory inv, StatsInventory statsInventory) {
        statsInventories.put(inv, statsInventory);
    }

    @Nullable
    public StatsInventory getStatsInventory(Inventory inv) {
        return statsInventories.get(inv);
    }

    public static class StatsInventory {

        private final int page;
        private final Text title;
        private final MCStats.Entry mainEntry;
        private final List<MCStats.Entry> items;
        private final Map<ItemStack, MCStats.Entry> groups = new HashMap<>();
        private ItemStack prevPageItem = null;
        private ItemStack nextPageItem = null;
        private ItemStack backItem = null;
        private StatsInventory parent = null;

        public StatsInventory(int page, Text title, MCStats.Entry mainEntry, List<MCStats.Entry> items) {
            this.page = page;
            this.title = title;
            this.mainEntry = mainEntry;
            this.items = items;
        }

        public int getPage() {
            return page;
        }

        public Text getTitle() {
            return title;
        }

        public MCStats.Entry getMainEntry() {
            return mainEntry;
        }

        public List<MCStats.Entry> getItems() {
            return items;
        }

        public void setItemStackGroup(ItemStack stack, MCStats.Entry groupEntry) {
            groups.put(stack, groupEntry);
        }

        public MCStats.Entry getItemStackGroup(ItemStack stack) {
            return groups.get(stack);
        }

        public ItemStack getPrevPageItem() {
            return prevPageItem;
        }

        public void setPrevPageItem(ItemStack prevPageItem) {
            this.prevPageItem = prevPageItem;
        }

        public ItemStack getNextPageItem() {
            return nextPageItem;
        }

        public void setNextPageItem(ItemStack nextPageItem) {
            this.nextPageItem = nextPageItem;
        }

        public void setBackItem(ItemStack backItem) {
            this.backItem = backItem;
        }

        public ItemStack getBackItem() {
            return backItem;
        }

        public void setParent(StatsInventory parent) {
            this.parent = parent;
        }

        public StatsInventory getParent() {
            return parent;
        }

    }
}
