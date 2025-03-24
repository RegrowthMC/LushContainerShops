package org.lushplugins.lushcontainershops.command;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.shop.ShopData;
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
            return "Couldn't find shop sign";
        }

        ShopSign sign = ShopSign.from(block);
        if (sign == null) {
            return "Couldn't find shop sign";
        }

        ShopData data = sign.data();
        if (!data.isOwner(player.getUniqueId())) {
            return "You are not the owner of this shop";
        }

        data.setProduct(ShopItem.from(heldItem));
        sign.updateSign();
        return "Successfully updated product!";
    }

    @Subcommand("setcost")
    public String setCost(BukkitCommandActor actor, @RayTrace Block block, @Equipment(EquipmentSlot.HAND) ItemStack heldItem) {
        Player player = actor.requirePlayer();
        if (block == null) {
            return "Couldn't find shop sign";
        }

        ShopSign sign = ShopSign.from(block);
        if (sign == null) {
            return "Couldn't find shop sign";
        }

        ShopData data = sign.data();
        if (!data.isOwner(player.getUniqueId())) {
            return "You are not the owner of this shop";
        }

        data.setCost(ShopItem.from(heldItem));
        sign.updateSign();
        return "Successfully updated cost!";
    }
}
