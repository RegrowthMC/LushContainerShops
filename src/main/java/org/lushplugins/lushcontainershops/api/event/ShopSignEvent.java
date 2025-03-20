package org.lushplugins.lushcontainershops.api.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.shop.ShopSign;

public abstract class ShopSignEvent extends Event {
    private final ShopSign sign;

    public ShopSignEvent(@NotNull ShopSign sign) {
        super();
        this.sign = sign;
    }

    public @NotNull ShopSign getShopSign() {
        return this.sign;
    }
}
