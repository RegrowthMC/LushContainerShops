package org.lushplugins.lushcontainershops.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.config.ConfigManager;
import org.lushplugins.lushcontainershops.shop.ShopContainer;
import org.lushplugins.lushcontainershops.shop.ShopSearchPath;
import org.lushplugins.lushcontainershops.shop.ShopSign;

public class ContainerListener implements Listener {

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent event) {
        Location location = event.getInventory().getLocation();
        if (location == null) {
            return;
        }

        Block block = location.getBlock();
        ShopContainer shopContainer = ShopContainer.from(block);
        if (shopContainer == null) {
            return;
        }

        HumanEntity player = event.getPlayer();
        if (!shopContainer.isOwner(player.getUniqueId())) {
            if (player.hasPermission("lushcontainershops.bypass")) {
                LushContainerShops.getInstance().getConfigManager().sendMessage(player, "bypassed-protection");
            } else {
                event.setCancelled(true);
                LushContainerShops.getInstance().getConfigManager().sendMessage(player, "no-access");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onContainerBreak(BlockBreakEvent event) {
        ShopContainer shopContainer = ShopContainer.from(event.getBlock());
        if (shopContainer == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!shopContainer.isOwner(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        for (ShopSign shop : shopContainer.getShopSigns()) {
            shop.unlinkContainer(shopContainer.container());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onContainerDestroy(BlockDestroyEvent event) {
        ShopContainer shopContainer = ShopContainer.from(event.getBlock());
        if (shopContainer != null) {
            event.setCancelled(true);
        }
    }
}
