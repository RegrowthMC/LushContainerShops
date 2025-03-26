package org.lushplugins.lushcontainershops.shop;

import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.persistence.ShopItemPersistentDataType;
import org.lushplugins.lushcontainershops.persistence.UUIDPersistentDataType;
import org.lushplugins.lushcontainershops.persistence.Vector3iPersistentDataType;
import org.lushplugins.lushlib.utils.BlockPosition;

import java.util.Collections;
import java.util.UUID;

public abstract class ShopBlock {
    private final TileState state;
    private final UUID owner;
    private ShopItem product;
    private ShopItem cost;
    private BlockPosition containerPosition;

    public ShopBlock(
        @NotNull TileState state,
        @NotNull UUID owner,
        @Nullable ShopItem product,
        @Nullable ShopItem cost,
        @Nullable BlockPosition containerPosition
    ) {
        this.state = state;
        this.owner = owner;
        this.product = product;
        this.cost = cost;
        this.containerPosition = containerPosition;
    }

    /**
     * @param state the tile state of the block
     * @param pdc The "shop" persistent data container, cannot be the full persistent data container
     */
    public ShopBlock(TileState state, PersistentDataContainer pdc) {
        LushContainerShops plugin = LushContainerShops.getInstance();
        this.state = state;

        this.owner = pdc.get(plugin.namespacedKey("owner"), UUIDPersistentDataType.INSTANCE);
        if (owner == null) {
            throw new NullPointerException("Shop's PersistentDataContainer is missing 'owner' field");
        }

        this.product = pdc.get(plugin.namespacedKey("product"), ShopItemPersistentDataType.INSTANCE);
        this.cost = pdc.get(plugin.namespacedKey("cost"), ShopItemPersistentDataType.INSTANCE);

        Vector3i containerPositionRaw = pdc.get(plugin.namespacedKey("container_pos"), Vector3iPersistentDataType.INSTANCE);
        this.containerPosition = containerPositionRaw != null ? BlockPosition.from(state.getWorld(), containerPositionRaw) : null;
    }

    public @NotNull TileState getTileState() {
        return state;
    }

    public void updateTileStatePDC() {
        LushContainerShops plugin = LushContainerShops.getInstance();

        PersistentDataContainer pdc = this.state.getPersistentDataContainer();
        PersistentDataContainer shopPDC = pdc.getAdapterContext().newPersistentDataContainer();
        shopPDC.set(plugin.namespacedKey("owner"), UUIDPersistentDataType.INSTANCE, this.getOwner());

        if (this.product != null) {
            shopPDC.set(plugin.namespacedKey("product"), ShopItemPersistentDataType.INSTANCE, this.product);
        }

        if (this.cost != null) {
            shopPDC.set(plugin.namespacedKey("cost"), ShopItemPersistentDataType.INSTANCE, this.cost);
        }

        if (this.containerPosition != null) {
            shopPDC.set(plugin.namespacedKey("container_pos"), Vector3iPersistentDataType.INSTANCE, this.containerPosition.asVector());
        }

        pdc.set(plugin.namespacedKey("shop"), PersistentDataType.TAG_CONTAINER, shopPDC);
        this.state.update();
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
        return this.product.isValid(item);
    }

    public void setProduct(ShopItem product) {
        this.product = product;
    }

    public @Nullable ShopItem getCost() {
        return this.cost;
    }

    public boolean isCost(ItemStack item) {
        return this.cost.isValid(item);
    }

    public void setCost(ShopItem cost) {
        this.cost = cost;
    }

    /**
     * Returns {@code true} if both the product, cost and container position have been defined
     * @return whether the shop data is established
     */
    public boolean isEstablished() {
        return this.product != null && this.cost != null && this.containerPosition != null;
    }

    public @Nullable BlockPosition getContainerPosition() {
        return this.containerPosition;
    }

    public @Nullable ShopContainer getShopContainer() {
        return this.containerPosition != null ? ShopContainer.from(this.containerPosition.getBlock()) : null;
    }

    public abstract @Nullable Container findPotentialContainer();

    public boolean linkContainer(Container container) {
        ShopContainer shopContainer = ShopContainer.from(container);
        if (shopContainer != null) {
            if (!this.isOwner(shopContainer.owner())) {
                return false;
            }

            Vector3i shopPosition = BlockPosition.from(this.getTileState()).asVector();
            shopContainer.addShop(shopPosition);
        } else {
            Vector3i shopPosition = BlockPosition.from(this.getTileState()).asVector();
            ShopContainer newShopContainer = new ShopContainer(container, this.getOwner(), Collections.singleton(shopPosition));
            newShopContainer.updateContainerStatePDC();
        }

        this.containerPosition = BlockPosition.from(container);
        this.updateTileStatePDC();
        return true;
    }

    public void unlinkContainer() {
        if (this.containerPosition == null) {
            return;
        }

        ShopContainer shopContainer = ShopContainer.from(this.containerPosition.getBlock());
        if (shopContainer == null) {
            return;
        }

        unlinkContainer(shopContainer);
    }

    public void unlinkContainer(Container container) {
        ShopContainer shopContainer = ShopContainer.from(container);
        if (shopContainer == null) {
            return;
        }

        unlinkContainer(shopContainer);
    }

    public void unlinkContainer(ShopContainer shopContainer) {
        shopContainer.removeShop(BlockPosition.from(this.getTileState()).asVector());
        shopContainer.updateContainerStatePDC();
    }
}
