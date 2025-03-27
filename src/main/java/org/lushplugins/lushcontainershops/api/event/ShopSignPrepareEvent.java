package org.lushplugins.lushcontainershops.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.shop.ShopSign;

public class ShopSignPrepareEvent extends ShopSignEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Step step;
    private boolean cancel = false;

    public ShopSignPrepareEvent(@NotNull ShopSign sign, @NotNull Step step) {
        super(sign);
        this.step = step;
    }

    public @NotNull Step getStep() {
        return this.step;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
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

    public enum Step {
        SET_COST,
        SET_PRODUCT,
        ADD_COST
    }
}
