package org.lushplugins.lushcontainershops.listener;

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
        ShopSign shop = getLinkedShopSignFromContainer(block);
        if (shop == null) {
            return;
        }

        HumanEntity player = event.getPlayer();
        if (!shop.isOwner(player.getUniqueId())) {
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

    private ShopSign getLinkedShopSignFromContainer(Block block) {
        ConfigManager configManager = LushContainerShops.getInstance().getConfigManager();
        if (!configManager.isWhitelistedContainer(block.getType())) {
            return null;
        }

        if (configManager.shouldAllowConnectedContainersOnly()) {
            for (Integer[] relativePosition : ShopSearchPath.ADJACENT_BLOCKS) {
                ShopSign shop = ShopSign.from(block.getRelative(
                    relativePosition[0],
                    relativePosition[1],
                    relativePosition[2]
                ));

                if (shop != null) {
                    return shop;
                }
            }

            return null;
        } else {
            // TODO: Check whether the container contains a shop container PDC
            return null;
        }
    }
}
