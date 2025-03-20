package org.lushplugins.lushcontainershops.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.lushplugins.lushcontainershops.LushContainerShops;

public class ContainerListener implements Listener {

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent event) {
        Location location = event.getInventory().getLocation();
        if (location == null) {
            return;
        }

        Block block = location.getBlock();
        if (!LushContainerShops.getInstance().getConfigManager().isWhitelistedContainer(block.getType())) {
            return;
        }


    }
}
