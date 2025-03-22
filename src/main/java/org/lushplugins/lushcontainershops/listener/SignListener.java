package org.lushplugins.lushcontainershops.listener;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.api.event.ShopSignBreakEvent;
import org.lushplugins.lushcontainershops.api.event.ShopSignCreateEvent;
import org.lushplugins.lushcontainershops.api.event.ShopSignPrepareEvent;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushcontainershops.shop.ShopSign;
import org.lushplugins.lushcontainershops.shop.ShopData;

import java.util.UUID;

public class SignListener implements Listener {

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        ShopSign shopSign = ShopSign.from(block);
        if (shopSign == null) {
            return;
        }

        Player player = event.getPlayer();
        ShopData data = shopSign.data();
        if (!shopSign.isEstablished()) {
            event.setCancelled(true);

            if (!player.getUniqueId().equals(data.getOwner())) {
                return;
            }

            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType().isAir()) {
                // TODO: You must be holding an item message
                return;
            }

            if (data.getProduct() == null) {
                data.setProduct(ShopItem.from(heldItem));
                shopSign.updateSign();
                return;
            }

            data.setCost(ShopItem.from(heldItem));
            shopSign.updateSign();

            // TODO: Send success message
            return;
        }

        // TODO: Implement purchasing
        return;
    }

    @EventHandler
    public void onSignOpen(PlayerOpenSignEvent event) {
        Player player = event.getPlayer();
        Sign sign = event.getSign();

        ShopSign shopSign = ShopSign.from(sign);
        if (shopSign == null) {
            return;
        }

        ShopData data = shopSign.data();
        if (!shopSign.isEstablished()) {
            event.setCancelled(true);

            if (!player.getUniqueId().equals(data.getOwner())) {
                return;
            }

            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType().isAir()) {
                // TODO: You must be holding an item message
                return;
            }

            if (data.getProduct() == null) {
                data.setProduct(ShopItem.from(heldItem));
                shopSign.updateSign();
                return;
            }

            data.setCost(ShopItem.from(heldItem));
            shopSign.updateSign();

            // TODO: Send success message
            return;
        }

        // TODO: Implement purchasing
        return;
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

        ShopData shopData = new ShopData(owner, product, cost);
        ShopSign shopSign = new ShopSign(sign, shopData);

        Event shopSignEvent;
        if (product != null && cost != null) {
            shopSignEvent = new ShopSignCreateEvent(shopSign);
        } else {
            shopSignEvent = new ShopSignPrepareEvent(shopSign, ShopSignPrepareEvent.Step.PLACE);
        }

        if (!LushContainerShops.getInstance().callEvent(shopSignEvent)) {
            event.setCancelled(true);
            return;
        }

        shopSign.updateSign(event.lines());
    }

    private void onShopSignEdit(SignChangeEvent event, ShopSign shopSign) {
        shopSign.updateSign(event.lines());
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        Sign sign = getPossibleShopSign(event.getBlock());
        if (sign == null) {
            return;
        }

        ShopSign shopSign = ShopSign.from(sign);
        if (shopSign == null) {
            String topLine = event.getLine(0);
            if (topLine != null && topLine.equalsIgnoreCase("[Shop]")) {
                onShopSignCreate(event, sign);
            }

            return;
        }

        if (!event.getPlayer().getUniqueId().equals(shopSign.data().getOwner())) {
            event.setCancelled(true);
            return;
        }

        // We ignore the back of signs - players can do what they want with the back
        if (event.getSide() == Side.BACK) {
            return;
        }

        onShopSignEdit(event, shopSign);
    }

    @EventHandler
    public void onSignBreak(@NotNull BlockBreakEvent event) {
        ShopSign shopSign = ShopSign.from(event.getBlock());
        if (shopSign == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(shopSign.data().getOwner())) {
            event.setCancelled(true);
            return;
        }

        if (!LushContainerShops.getInstance().callEvent(new ShopSignBreakEvent(shopSign, player))) {
            event.setCancelled(true);
        }
    }

    public @Nullable Sign getPossibleShopSign(Block block) {
        if (!LushContainerShops.getInstance().getConfigManager().isWhitelistedSign(block.getType())) {
            return null;
        }

        if (!(block.getWorld().getBlockState(block.getLocation()) instanceof Sign sign)) {
            return null;
        }

        return sign;
    }
}
