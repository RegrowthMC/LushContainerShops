package org.lushplugins.lushcontainershops.command;

import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.api.event.ShopSignPrepareEvent;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushcontainershops.shop.ShopSign;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.Equipment;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.RayTrace;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

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
    
    private String updateShop(BukkitCommandActor actor, Block block, ItemStack heldItem, Consumer<ShopSign> callback) {
        Player player = actor.requirePlayer();
        if (block == null) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("not-shop");
        }

        ShopSign shop = ShopSign.from(block);
        if (shop == null) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("not-shop");
        }

        if (shop.getContainerPosition() == null) {
            if (!shop.isOwner(player.getUniqueId())) {
                return LushContainerShops.getInstance().getConfigManager().getMessage("no-container");
            }

            Container container = shop.findPotentialContainer();
            if (container == null || !shop.linkContainer(container)) {
                return LushContainerShops.getInstance().getConfigManager().getMessage("no-container");
            }
        }

        if (!shop.isOwner(player.getUniqueId())) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("cannot-edit");
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
