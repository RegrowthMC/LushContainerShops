package org.lushplugins.lushcontainershops.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.shop.ShopSign;

public class ShopSignBreakEvent extends ShopSignEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private boolean cancel = false;

    public ShopSignBreakEvent(@NotNull ShopSign sign, @NotNull Player player) {
        super(sign);
        this.player = player;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
