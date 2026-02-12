package org.lushplugins.lushcontainershops.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.api.event.ShopSignPrepareEvent;
import org.lushplugins.lushcontainershops.shop.ShopContainer;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushcontainershops.shop.ShopSign;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.SuggestOnlinePlayers;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.Equipment;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.RayTrace;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.Stocker;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;

import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@Command({"lushcontainershops", "containershops", "chestshops", "chestshop"})
public class ContainerShopsCommand {

    @Subcommand("reload")
    @CommandPermission("lushcontainershops.reload")
    public void reload(BukkitCommandActor actor) {
        LushContainerShops.getInstance().getConfigManager().reloadConfig();
        LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "reload");
    }

    @Subcommand("addstocker")
    @CommandPermission("lushcontainershops.modifystockers")
    public void addStocker(BukkitCommandActor actor, @RayTrace Block block, @SuggestOnlinePlayers String stocker) {
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(stocker);
        if (player == null || !player.hasPlayedBefore()) {
            throw new InvalidPlayerException(stocker);
        }

        ShopContainer shop = ShopContainer.from(block);
        if (shop == null) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "not-shop-container");
            return;
        }

        if (!shop.isOwner(actor.uniqueId())) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "cannot-edit");
            return;
        }

        UUID uuid = player.getUniqueId();
        if (shop.isOwner(uuid) || shop.isStocker(uuid)) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "already-stocker", str -> str
                .replace("%player%", player.getName()));
            return;
        }

        if (shop.getStockers().size() >= 10) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "max-stockers");
            return;
        }

        shop.addStocker(player.getUniqueId());
        shop.updateContainerStatePDC();

        LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "added-stocker", str -> str
            .replace("%player%", player.getName()));
    }

    @Subcommand("removestocker")
    @CommandPermission("lushcontainershops.modifystockers")
    public void removeStocker(BukkitCommandActor actor, @RayTrace Block block, @Stocker String stocker) {
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(stocker);
        if (player == null) {
            throw new InvalidPlayerException(stocker);
        }

        ShopContainer shop = ShopContainer.from(block);
        if (shop == null) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "not-shop-container");
            return;
        }

        if (!shop.isOwner(actor.uniqueId())) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "cannot-edit");
            return;
        }

        UUID uuid = player.getUniqueId();
        if (shop.isOwner(uuid) || !shop.isStocker(uuid)) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "not-stocker", str -> str
                .replace("%player%", player.getName()));
            return;
        }

        shop.removeStocker(player.getUniqueId());
        shop.updateContainerStatePDC();

        LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "removed-stocker", str -> str
            .replace("%player%", player.getName()));
    }

    @Subcommand("setproduct")
    public String setProduct(BukkitCommandActor actor, @RayTrace Block block, @Equipment(EquipmentSlot.HAND) ItemStack heldItem) {
        return updateShop(actor, block, heldItem, (shop) -> {
            shop.setProduct(ShopItem.from(heldItem));
            LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.SET_PRODUCT));
        });
    }

    @Subcommand("setcost")
    public String setCost(BukkitCommandActor actor, @RayTrace Block block, @Equipment(EquipmentSlot.HAND) ItemStack heldItem) {
        return updateShop(actor, block, heldItem, (shop) -> {
            shop.setCost(ShopItem.from(heldItem));
            LushContainerShops.getInstance().callEvent(new ShopSignPrepareEvent(shop, ShopSignPrepareEvent.Step.SET_COST));
        });
    }

    private ShopSign getOwnedShopSign(BukkitCommandActor actor, Block block) {
        Player player = actor.requirePlayer();
        if (block == null) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "not-shop");
            return null;
        }

        ShopSign shop = ShopSign.from(block);
        if (shop == null) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "not-shop");
            return null;
        }

        if (shop.getContainerPosition() == null) {
            if (!shop.isOwner(player.getUniqueId())) {
                LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "no-container");
                return null;
            }

            Container container = shop.findPotentialContainer();
            if (container == null || !shop.linkContainer(container)) {
                LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "no-container");
                return null;
            }
        }

        if (!shop.isOwner(player.getUniqueId())) {
            LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "cannot-edit");
            return null;
        }

        return shop;
    }
    
    private String updateShop(BukkitCommandActor actor, Block block, ItemStack heldItem, Consumer<ShopSign> callback) {
        Player player = actor.requirePlayer();
        if (block == null) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("not-shop");
        }

        ShopSign shop = getOwnedShopSign(actor, block);
        if (shop == null) {
            return null;
        }

        if (heldItem.isEmpty()) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("no-item");
        }

        callback.accept(shop);
        shop.updateTileState();

        LushContainerShops.getInstance().getPacketEventsHook().ifPresent(hook -> {
            hook.reloadVisualsInChunk(block.getChunk());
        });

        return LushContainerShops.getInstance().getConfigManager().getMessage("updated-shop");
    }
}
