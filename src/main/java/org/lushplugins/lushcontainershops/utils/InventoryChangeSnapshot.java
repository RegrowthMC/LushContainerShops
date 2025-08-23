package org.lushplugins.lushcontainershops.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InventoryChangeSnapshot {
    private final Map<Integer, ItemStack> slotChanges = new HashMap<>();
    private final List<ItemStack> addedItems = new ArrayList<>();
    private final List<ItemStack> removedItems = new ArrayList<>();

    public void addChange(int slot, @Nullable ItemStack itemStack) {
        slotChanges.put(slot, itemStack);
    }

    public List<ItemStack> getRemovedItems() {
        return removedItems;
    }

    public void removeItem(int slot, ItemStack itemStack) {
        slotChanges.put(slot, null);
        removedItems.add(itemStack.clone());
    }

    public int countEmptiedSlots() {
        return (int) slotChanges.values().stream()
            .filter(Objects::isNull)
            .count();
    }

    public void applyTo(Inventory inventory) {
        slotChanges.forEach(inventory::setItem);
    }
}
