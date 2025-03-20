package org.lushplugins.lushcontainershops.command;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.shop.ShopSign;
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
    public void setProduct(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        Block block = rayTraceBlock(player);
        if (block == null) {
            actor.reply("Couldn't find shop sign"); // TODO: Test (Try working out compat with ChatColorHandler)
            return;
        }

        ShopSign sign = ShopSign.from(block);
        if (sign == null) {
            actor.reply("Couldn't find shop sign"); // TODO: Test (Try working out compat with ChatColorHandler)
            return;
        }

        if (!player.getUniqueId().equals(sign.data().getOwner())) {
            actor.reply("You are not the owner of this shop"); // TODO: Test (Try working out compat with ChatColorHandler)
            return;
        }

        // TODO: ContextualParameter to get held ItemStack? (eg. @MainHand and @OffHand?)
        sign.data().setProduct(null); // TODO: Enter held item here
    }

    @Subcommand("setcost")
    public void setCost(BukkitCommandActor actor) {

    }

    // TODO: Migrate to Lamp ContextualParameter
    private @Nullable Block rayTraceBlock(LivingEntity entity) {
        RayTraceResult rayTrace = entity.rayTraceBlocks(5);
        return rayTrace != null ? rayTrace.getHitBlock() : null;
    }
}
