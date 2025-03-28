package org.lushplugins.lushcontainershops.command;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushcontainershops.shop.ShopSign;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.Equipment;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.RayTrace;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

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
        Player player = actor.requirePlayer();
        if (block == null) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("not-shop");
        }

        ShopSign shop = ShopSign.from(block);
        if (shop == null) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("not-shop");
        }

        if (!shop.isOwner(player.getUniqueId())) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("cannot-edit");
        }

        shop.setProduct(ShopItem.from(heldItem));
        shop.updateTileState();

        LushContainerShops.getInstance().getPacketEventsHook().ifPresent(hook -> {
            hook.reloadVisualsInChunk(block.getChunk());
        });

        return LushContainerShops.getInstance().getConfigManager().getMessage("updated-shop");
    }

    @Subcommand("setcost")
    public String setCost(BukkitCommandActor actor, @RayTrace Block block, @Equipment(EquipmentSlot.HAND) ItemStack heldItem) {
        Player player = actor.requirePlayer();
        if (block == null) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("not-shop");
        }

        ShopSign shop = ShopSign.from(block);
        if (shop == null) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("not-shop");
        }

        if (!shop.isOwner(player.getUniqueId())) {
            return LushContainerShops.getInstance().getConfigManager().getMessage("cannot-edit");
        }

        shop.setCost(ShopItem.from(heldItem));
        shop.updateTileState();

        LushContainerShops.getInstance().getPacketEventsHook().ifPresent(hook -> {
            hook.reloadVisualsInChunk(block.getChunk());
        });

        return LushContainerShops.getInstance().getConfigManager().getMessage("updated-shop");
    }
}
