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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.api.event.ShopSignBreakEvent;
import org.lushplugins.lushcontainershops.api.event.ShopSignCreateEvent;
import org.lushplugins.lushcontainershops.api.event.ShopSignPrepareEvent;
import org.lushplugins.lushcontainershops.shop.ShopContainer;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushcontainershops.shop.ShopSign;
import org.lushplugins.lushcontainershops.utils.InventoryUtils;
import org.lushplugins.lushlib.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class SignListener implements Listener {

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        ShopSign shop = ShopSign.from(block);
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        // Handle shop creation if it is not established
        if (!shop.isEstablished()) {
            if (!event.getAction().isLeftClick()) {
                event.setCancelled(true);
            }

            if (shop.getContainerPosition() == null) {
                if (!shop.isOwner(player.getUniqueId())) {
                    LushContainerShops.getInstance().getConfigManager().sendMessage(player, "no-container");
                    return;
                }

                Container container = shop.findPotentialContainer();
                if (container == null || !shop.linkContainer(container)) {
                    LushContainerShops.getInstance().getConfigManager().sendMessage(player, "no-container");
                    return;
                }
            }

            if (!shop.isOwner(player.getUniqueId())) {
                LushContainerShops.getInstance().getConfigManager().sendMessage(player, "not-setup");
                return;
            }

            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.isEmpty()) {
                LushContainerShops.getInstance().getConfigManager().sendMessage(player, "no-item");
                return;
            }

            if (shop.getProduct() == null) {
                shop.setProduct(ShopItem.from(heldItem));
                LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.SET_PRODUCT));
                shop.updateTileState();
                LushContainerShops.getInstance().getConfigManager().sendMessage(player, "updated-shop");
                return;
            }

            shop.setCost(ShopItem.from(heldItem));
            LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.SET_COST));
            shop.updateTileState();

            LushContainerShops.getInstance().getPacketEventsHook().ifPresent(hook -> {
                hook.reloadVisualsInChunk(block.getChunk());
            });

            LushContainerShops.getInstance().getConfigManager().sendMessage(player, "updated-shop");
            return;
        }

        // Handle purchasing from shop
        ShopContainer shopContainer = shop.getShopContainer();
        if (shopContainer == null) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(player, "no-container");
            return;
        }

        ShopItem shopProduct = Objects.requireNonNull(shop.getProduct());
        ShopItem shopCost = Objects.requireNonNull(shop.getCost());

        // Collect the similar products and costs from their respective inventories
        Inventory shopInventory = shopContainer.container().getInventory();
        Pair<List<ItemStack>, Map<Integer, ItemStack>> productSnapshot = InventoryUtils.prepareToTake(shopInventory, shopProduct);
        if (productSnapshot == null) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(player, "missing-products");
            shop.updateTileState();
            return;
        }

        Inventory playerInventory = player.getInventory();
        Pair<List<ItemStack>, Map<Integer, ItemStack>> costSnapshot = InventoryUtils.prepareToTake(playerInventory, shopCost);
        if (costSnapshot == null) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(player, "missing-costs", (s) -> s.replace("%cost%", shopCost.asString()));
            return;
        }

        // Ensure that both the player and the container have enough empty slots for the transaction
        List<ItemStack> products = productSnapshot.first();
        int requiredPlayerSlots = products.size();
        int emptyShopSlots = InventoryUtils.countEmptySlots(shopInventory) + products.size();

        List<ItemStack> costs = costSnapshot.first();
        int requiredShopSlots = costs.size();
        int emptyPlayerSlots = InventoryUtils.countEmptySlots(playerInventory) + costs.size();

        if (requiredShopSlots > emptyShopSlots) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(player, "not-enough-container-slots");
            shop.updateTileState();
            return;
        }

        if (requiredPlayerSlots > emptyPlayerSlots) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(player, "not-enough-player-slots");
            return;
        }

        LushContainerShops.getInstance().log(Level.INFO, "Processing %s's purchase of %s (product) for %s (cost)"
            .formatted(player.getName(), shopProduct.asString(), shopCost.asString()));

        // Take products from container and costs from the player
        productSnapshot.second().forEach(shopInventory::setItem);
        costSnapshot.second().forEach(playerInventory::setItem);

        // Give products to player and place costs in container
        InventoryUtils.addOrDropItems(playerInventory, products.toArray(ItemStack[]::new));
        InventoryUtils.addOrDropItems(shopInventory, costs.toArray(ItemStack[]::new));

        shop.updateTileState();
    }

    @EventHandler
    public void onSignOpen(PlayerOpenSignEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ShopSign shop = ShopSign.from(event.getSign());
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!shop.isOwner(player.getUniqueId()) || !shop.isEstablished() || !player.isSneaking()) {
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
            shopSignEvent = new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.PREPARE);
        }

        if (!LushContainerShops.getInstance().callEvent(shopSignEvent)) {
            event.setCancelled(true);
            return;
        }

        shop.updateSignState(event.lines());

        LushContainerShops.getInstance().getPacketEventsHook().ifPresent(hook -> {
            hook.reloadVisualsInChunk(sign.getChunk());
        });
    }

    private void onShopSignEdit(SignChangeEvent event, ShopSign shop) {
        String rawProduct = event.getLine(1);
        if (rawProduct != null) {
            try {
                ShopItem product = ShopItem.parseString(rawProduct);
                if (LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.SET_PRODUCT))) {
                    shop.setProduct(product);
                }

                shop.setProduct(ShopItem.parseString(rawProduct));
            } catch (IllegalArgumentException ignored) {}
        }

        String rawCost = event.getLine(2);
        if (rawCost != null) {
            try {
                ShopItem cost = ShopItem.parseString(rawCost);
                if (LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.SET_COST))) {
                    shop.setCost(cost);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        shop.updateSignState(event.lines());

        LushContainerShops.getInstance().getPacketEventsHook().ifPresent(hook -> {
            hook.reloadVisualsInChunk(event.getBlock().getChunk());
        });
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

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
            if (topLine != null && topLine.equalsIgnoreCase(LushContainerShops.getInstance().getConfigManager().getMessage("header", "[Shop]"))) {
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
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        ShopSign shop = ShopSign.from(block);
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

        LushContainerShops.getInstance().getPacketEventsHook().ifPresent(hook -> {
            hook.reloadVisualsInChunk(block.getChunk());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onContainerDestroy(BlockDestroyEvent event) {
        ShopSign shop = ShopSign.from(event.getBlock());
        if (shop != null) {
            event.setCancelled(true);
        }
    }
}
