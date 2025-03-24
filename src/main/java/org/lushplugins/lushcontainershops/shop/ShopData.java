package org.lushplugins.lushcontainershops.shop;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.persistence.ShopItemPersistentDataType;
import org.lushplugins.lushcontainershops.persistence.UUIDPersistentDataType;

import java.util.UUID;

public class ShopData {
    private final UUID owner;
    // TODO: If the sign is broken then the data should be removed from the container
    // If the container is broken then the position should be removed from the sign
    // If a sign has no saved position then it will search for a new container on right-click
    private Vector3i containerPosition;
    private ShopItem product;
    private ShopItem cost;

    public ShopData(@NotNull UUID owner, @Nullable ShopItem product, @Nullable ShopItem cost) {
        this.owner = owner;
        this.product = product;
        this.cost = cost;
    }

    public @NotNull UUID getOwner() {
        return this.owner;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOwner(@NotNull UUID uuid) {
        return this.owner.equals(uuid);
    }

    public @Nullable ShopItem getProduct() {
        return this.product;
    }

    public boolean isProduct(ItemStack item) {
        return this.product.getItem().isSimilar(item);
    }

    public void setProduct(ShopItem product) {
        this.product = product;
    }

    public @Nullable ShopItem getCost() {
        return this.cost;
    }

    public boolean isCost(ItemStack item) {
        return this.cost.getItem().isSimilar(item);
    }

    public void setCost(ShopItem cost) {
        this.cost = cost;
    }

    /**
     * Returns {@code true} if both the product and cost have been defined
     * @return whether the shop data is established
     */
    public boolean isEstablished() {
        return this.product != null && this.cost != null;
    }

    public static ShopData from(PersistentDataContainer container) {
        LushContainerShops plugin = LushContainerShops.getInstance();

        UUID owner = container.get(plugin.namespacedKey("owner"), UUIDPersistentDataType.INSTANCE);
        if (owner == null) {
            throw new NullPointerException("Shop's PersistentDataContainer is missing 'owner' field");
        }

        ShopItem product = container.get(plugin.namespacedKey("product"), ShopItemPersistentDataType.INSTANCE);
        ShopItem cost = container.get(plugin.namespacedKey("cost"), ShopItemPersistentDataType.INSTANCE);
        return new ShopData(owner, product, cost);
    }
}
