package org.lushplugins.lushcontainershops.shop;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.persistence.UUIDPersistentDataType;
import org.lushplugins.lushcontainershops.persistence.Vector3iPersistentDataType;
import org.lushplugins.lushcontainershops.utils.InventoryUtils;

import java.util.*;

public record ShopContainer(Container container, UUID owner, Set<Vector3i> shops) {

    public ShopContainer(Container container, UUID owner, Set<Vector3i> shops) {
        this.container = container;
        this.owner = owner;
        this.shops = new HashSet<>(shops);
    }

    public boolean contains(ShopItem product) {
        return InventoryUtils.contains(this.container.getInventory(), product);
    }

    public boolean isOwner(UUID owner) {
        return this.owner.equals(owner);
    }

    public void addShop(Vector3i shopPosition) {
        if (this.shops.contains(shopPosition)) {
            return;
        }

        this.shops.add(shopPosition);
        updateContainerStatePDC();
    }

    public void removeShop(Vector3i shopPosition) {
        if (!this.shops.contains(shopPosition)) {
            return;
        }

        this.shops.remove(shopPosition);
        updateContainerStatePDC();
    }

    public List<ShopSign> getShopSigns() {
        World world = this.container.getWorld();
        List<Vector3i> inactiveShops = new ArrayList<>();
        List<ShopSign> shopSigns = this.shops.stream()
            .map(position -> {
                Block block = world.getBlockAt(
                    position.x(),
                    position.y(),
                    position.z());

                ShopSign shop = ShopSign.from(block);
                if (shop == null) {
                    inactiveShops.add(position);
                }

                return shop;
            })
            .filter(Objects::nonNull)
            .toList();

        this.shops.removeAll(inactiveShops);

        return shopSigns;
    }

    public void updateContainerStatePDC() {
        PersistentDataContainer pdc = this.container.getPersistentDataContainer();
        LushContainerShops plugin = LushContainerShops.getInstance();

        if (this.shops.isEmpty()) {
            pdc.remove(plugin.namespacedKey("shop_container"));
            this.container.update();
            return;
        }

        PersistentDataContainer shopContainerPDC = pdc.getAdapterContext().newPersistentDataContainer();
        shopContainerPDC.set(plugin.namespacedKey("owner"), UUIDPersistentDataType.INSTANCE, this.owner);
        shopContainerPDC.set(
            plugin.namespacedKey("shops"),
            PersistentDataType.LIST.listTypeFrom(Vector3iPersistentDataType.INSTANCE),
            this.shops.stream().toList()
        );

        pdc.set(plugin.namespacedKey("shop_container"), PersistentDataType.TAG_CONTAINER, shopContainerPDC);
        this.container.update();
    }

    public static @Nullable ShopContainer from(Block block) {
        if (!LushContainerShops.getInstance().getConfigManager().isWhitelistedContainer(block.getType())) {
            return null;
        }

        if (!(block.getWorld().getBlockState(block.getLocation()) instanceof Container container)) {
            return null;
        }

        return ShopContainer.from(container);
    }

    public static @Nullable ShopContainer from(Container container) {
        PersistentDataContainer pdc = container.getPersistentDataContainer();
        LushContainerShops plugin = LushContainerShops.getInstance();

        PersistentDataContainer shopContainerPDC = pdc.get(plugin.namespacedKey("shop_container"), PersistentDataType.TAG_CONTAINER);
        if (shopContainerPDC == null) {
            return null;
        }

        UUID owner = shopContainerPDC.get(plugin.namespacedKey("owner"), UUIDPersistentDataType.INSTANCE);
        List<Vector3i> shops = shopContainerPDC.get(
            plugin.namespacedKey("shops"),
             PersistentDataType.LIST.listTypeFrom(Vector3iPersistentDataType.INSTANCE)
        );

        return new ShopContainer(container, owner, new HashSet<>(shops));
    }
}
