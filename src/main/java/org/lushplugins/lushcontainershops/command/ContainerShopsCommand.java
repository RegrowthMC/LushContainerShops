package org.lushplugins.lushcontainershops.command;

import org.lushplugins.lushcontainershops.LushContainerShops;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@SuppressWarnings("unused")
@Command({"lushcontainershops", "containershops"})
public class ContainerShopsCommand {

    @Subcommand("reload")
    @CommandPermission("lushcontainershops.reload")
    public void reload(BukkitCommandActor actor) {
        LushContainerShops.getInstance().getConfigManager().reloadConfig();
        LushContainerShops.getInstance().getConfigManager().sendMessage(actor.sender(), "reload");
    }
}
