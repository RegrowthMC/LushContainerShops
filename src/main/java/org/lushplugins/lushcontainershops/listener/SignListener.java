package org.lushplugins.lushcontainershops.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.api.event.ShopSignBreakEvent;
import org.lushplugins.lushcontainershops.api.event.ShopSignCreateEvent;
import org.lushplugins.lushcontainershops.api.event.ShopSignPrepareEvent;
import org.lushplugins.lushcontainershops.shop.ShopContainer;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushcontainershops.shop.ShopSign;

import java.util.UUID;

public class SignListener implements Listener {

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        ShopSign shop = ShopSign.from(block);
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!shop.isEstablished()) {
            if (!event.getAction().isLeftClick()) {
                event.setCancelled(true);
            }

            if (!shop.isOwner(player.getUniqueId())) {
                return;
            }

            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType().isAir()) {
                LushContainerShops.getInstance().getConfigManager().sendMessage(player, "no-item");
                return;
            }

            if (shop.getProduct() == null) {
                shop.setProduct(ShopItem.from(heldItem));
                LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.ADD_PRODUCT));
                shop.updateSignState();
                return;
            }

            shop.setCost(ShopItem.from(heldItem));
            LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.ADD_COST));
            shop.updateSignState();
            return;
        }

        // TODO: Implement purchasing
    }

    @EventHandler
    public void onSignOpen(PlayerOpenSignEvent event) {
        ShopSign shop = ShopSign.from(event.getSign());
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!shop.isOwner(player.getUniqueId()) || !shop.isEstablished()) {
            event.setCancelled(true);
        }
    }

    private void onShopSignCreate(SignChangeEvent event, Sign sign) {
        UUID owner = event.getPlayer().getUniqueId();

        String rawProduct = event.getLine(1);
        ShopItem product = null;
        if (rawProduct != null) {
            try {
                product = ShopItem.parseString(rawProduct);
            } catch (IllegalArgumentException ignored) {}
        }

        String rawCost = event.getLine(2);
        ShopItem cost = null;
        if (rawCost != null) {
            try {
                cost = ShopItem.parseString(rawCost);
            } catch (IllegalArgumentException ignored) {}
        }

        ShopSign shop = new ShopSign(sign, owner, product, cost, null);
        Container container = shop.findPotentialContainer();
        if (container != null) {
            if (!shop.linkContainer(container)) {
                LushContainerShops.getInstance().getConfigManager().sendMessage(event.getPlayer(), "no-access");
                event.setCancelled(true);
                return;
            }
        }

        Event shopSignEvent;
        if (product != null && cost != null) {
            shopSignEvent = new ShopSignCreateEvent(shop);
        } else {
            shopSignEvent = new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.PLACE);
        }

        if (!LushContainerShops.getInstance().callEvent(shopSignEvent)) {
            event.setCancelled(true);
            return;
        }

        shop.updateSignState(event.lines());
    }

    private void onShopSignEdit(SignChangeEvent event, ShopSign shopSign) {
        shopSign.updateSignState(event.lines());
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        Block block = event.getBlock();
        if (!LushContainerShops.getInstance().getConfigManager().isWhitelistedSign(block.getType())) {
            return;
        }

        if (!(block.getWorld().getBlockState(block.getLocation()) instanceof Sign sign)) {
            return;
        }

        ShopSign shop = ShopSign.from(sign);
        if (shop == null) {
            String topLine = event.getLine(0);
            if (topLine != null && topLine.equalsIgnoreCase("[Shop]")) {
                if (event.getSide() == Side.FRONT) {
                    onShopSignCreate(event, sign);
                } else {
                    LushContainerShops.getInstance().getConfigManager().sendMessage(event.getPlayer(), "wrong-side");
                }
            }

            return;
        }

        if (!shop.isOwner(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // We ignore the back of signs - players can do what they want with the back
        if (event.getSide() == Side.BACK) {
            return;
        }

        onShopSignEdit(event, shop);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignBreak(@NotNull BlockBreakEvent event) {
        ShopSign shop = ShopSign.from(event.getBlock());
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!shop.isOwner(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (!LushContainerShops.getInstance().callEvent(new ShopSignBreakEvent(shop, player))) {
            event.setCancelled(true);
        }

        shop.unlinkContainer();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onContainerDestroy(BlockDestroyEvent event) {
        ShopSign shop = ShopSign.from(event.getBlock());
        if (shop != null) {
            event.setCancelled(true);
        }
    }
}
