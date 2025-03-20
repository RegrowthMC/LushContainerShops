package org.lushplugins.lushcontainershops.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.shop.ShopItem;
import org.lushplugins.lushlib.libraries.jackson.core.JsonProcessingException;
import org.lushplugins.lushlib.utils.DisplayItemStack;

public class ShopItemPersistentDataType implements PersistentDataType<String, ShopItem> {
    public static final ShopItemPersistentDataType INSTANCE = new ShopItemPersistentDataType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ShopItem> getComplexType() {
        return ShopItem.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull ShopItem complex, @NotNull PersistentDataAdapterContext context) {
        try {
            return LushContainerShops.getJacksonMapper().writeValueAsString(complex.getItem());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull ShopItem fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return new ShopItem(LushContainerShops.getJacksonMapper().readValue(primitive, DisplayItemStack.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
