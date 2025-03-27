package org.lushplugins.lushcontainershops.utils;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushlib.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryUtils {

    public static void addOrDropItems(Inventory inventory, ItemStack[] items) {
        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(items);

        Location location = inventory.getLocation();
        if (location != null) {
            for (ItemStack additionalItem : additionalItems.values()) {
                location.getWorld().dropItem(location, additionalItem);
            }
        }
    }

    public static int countEmptySlots(Inventory inventory) {
        int count = 0;

        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null || item.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    /**
     * @param inventoryContents a list of item stacks and their related slots in the inventory
     * @param item item to compare contents to
     * @return a pair of taken items and a map of updates to make to the inventory or {@code null} if the required items are not present
     */
    public static @Nullable Pair<List<ItemStack>, Map<Integer, ItemStack>> prepareToTake(Map<Integer, ItemStack> inventoryContents, ShopItem item) {
        Map<Integer, ItemStack> slotsToUpdate = new HashMap<>();
        List<ItemStack> takenItems = new ArrayList<>();

        int remainingToTake = item.getAmount();
        for (Map.Entry<Integer, ItemStack> entry : inventoryContents.entrySet()) {
            int slot = entry.getKey();
            ItemStack itemStack = entry.getValue();
            int stackSize = itemStack.getAmount();

            if (remainingToTake >= stackSize) {
                takenItems.add(itemStack.clone());
                slotsToUpdate.put(slot, null);
                remainingToTake -= stackSize;
            } else {
                ItemStack inventoryItem = itemStack.clone();
                inventoryItem.setAmount(stackSize - remainingToTake);

                ItemStack takenItem = itemStack.clone();
                takenItem.setAmount(remainingToTake);

                takenItems.add(takenItem);
                slotsToUpdate.put(slot, inventoryItem);

                return new Pair<>(takenItems, slotsToUpdate);
            }

            if (remainingToTake <= 0) {
                return new Pair<>(takenItems, slotsToUpdate);
            }
        }

        return null;
    }

    public static @Nullable Pair<List<ItemStack>, Map<Integer, ItemStack>> prepareToTake(Inventory inventory, ShopItem item) {
        return prepareToTake(findSimilar(inventory, item), item);
    }

    public static boolean contains(Inventory inventory, ShopItem item) {
        return prepareToTake(inventory, item) != null;
    }

    public static @NotNull Map<Integer, ItemStack> findSimilar(Inventory inventory, ShopItem item) {
        Map<Integer, ItemStack> similarItems = new HashMap<>();

        ItemStack[] contents = inventory.getStorageContents();
        int remainingToFind = item.getAmount();
        for (int slot = 0; slot < contents.length; slot++) {
            if (remainingToFind <= 0) {
                break;
            }

            ItemStack contentItem = contents[slot];
            if (contentItem == null || !item.isSimilar(contentItem)) {
                continue;
            }

            similarItems.put(slot, contentItem);
            remainingToFind -= contentItem.getAmount();
        }

        return similarItems;
    }

    public static boolean containsSimilar(Inventory inventory, ShopItem item) {
        return !findSimilar(inventory, item).isEmpty();
    }
}
