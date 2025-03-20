package org.lushplugins.lushcontainershops.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.shop.ShopSign;

public class ShopSignEditEvent extends ShopSignEvent {
    private static final HandlerList handlers = new HandlerList();

    public ShopSignEditEvent(@NotNull ShopSign sign) {
        super(sign);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
