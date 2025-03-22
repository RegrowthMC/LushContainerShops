package org.lushplugins.lushcontainershops.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.HangingSign;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.persistence.ShopItemPersistentDataType;
import org.lushplugins.lushcontainershops.persistence.UUIDPersistentDataType;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;

import java.util.List;

/**
 * A basic wrapper for a shop sign containing the sign, and it's parsed shop data
 * @param sign the sign
 * @param data the shop's data
 */
public record ShopSign(@NotNull Sign sign, @NotNull ShopData data) {

    /**
     * Returns {@code true} if both the product and cost have been defined
     * @return whether the shop data is established
     */
    public boolean isEstablished() {
        return this.data.isEstablished();
    }

    private void updateLines(List<Component> lines) {
        int lineCharLimit = this.sign instanceof HangingSign ? 10 : 15;

        lines.set(0, ModernChatColorHandler.translate(LushContainerShops.getInstance().getConfigManager().getMessageOrEmpty("header-color") + "[Shop]"));

        ShopItem product = this.data.getProduct();
        if (product != null) {
            lines.set(1, product.asTextComponent(lineCharLimit));
        } else {
            // TODO: Make line configurable?
            lines.set(1, Component.text("click product")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decorate(TextDecoration.ITALIC));
        }

        ShopItem cost = this.data.getCost();
        if (cost != null) {
            lines.set(2, cost.asTextComponent(lineCharLimit));
        } else if (product != null) {
            // TODO: Make line configurable?
            lines.set(2, Component.text("click cost")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decorate(TextDecoration.ITALIC));
        }

        if (!data.isEstablished()) {
            lines.set(3, ModernChatColorHandler.translate(LushContainerShops.getInstance().getConfigManager().getMessageOrEmpty("not-setup")));
        } else {
            // TODO: Add other statuses
            lines.set(3, ModernChatColorHandler.translate(LushContainerShops.getInstance().getConfigManager().getMessageOrEmpty("in-stock")));
        }
    }

    private void updatePersistentDataContainer() {
        PersistentDataContainer container = this.sign.getPersistentDataContainer();
        LushContainerShops plugin = LushContainerShops.getInstance();

        PersistentDataContainer shopContainer = container.getAdapterContext().newPersistentDataContainer();
        shopContainer.set(plugin.namespacedKey("owner"), UUIDPersistentDataType.INSTANCE, this.data.getOwner());

        ShopItem product = this.data.getProduct();
        if (product != null) {
            shopContainer.set(plugin.namespacedKey("product"), ShopItemPersistentDataType.INSTANCE, product);
        }

        ShopItem cost = this.data.getCost();
        if (cost != null) {
            shopContainer.set(plugin.namespacedKey("cost"), ShopItemPersistentDataType.INSTANCE, cost);
        }

        container.set(plugin.namespacedKey("shop"), PersistentDataType.TAG_CONTAINER, shopContainer);
        this.sign.update();
    }

    public void updateSign(List<Component> lines) {
        updateLines(lines);
        updatePersistentDataContainer();
    }

    public void updateSign() {
        SignSide side = this.sign.getSide(Side.FRONT);
        updateSign(side.lines());
        this.sign.update();
    }

    public Block getAttachedTo() {
        BlockFace attachedDirection;
        if (this.sign instanceof HangingSign) {
            attachedDirection = BlockFace.UP;
        } else if (this.sign.getBlockData() instanceof WallSign signData) {
            attachedDirection = signData.getFacing().getOppositeFace();
        } else {
            attachedDirection = BlockFace.DOWN;
        }

        Location blockLocation = this.sign.getLocation().add(attachedDirection.getDirection());
        return this.sign.getWorld().getBlockAt(blockLocation);
    }

    public static @Nullable ShopSign from(Block block) {
        if (!LushContainerShops.getInstance().getConfigManager().isWhitelistedSign(block.getType())) {
            return null;
        }

        if (!(block.getWorld().getBlockState(block.getLocation()) instanceof Sign sign)) {
            return null;
        }

        return ShopSign.from(sign);
    }

    public static @Nullable ShopSign from(Sign sign) {
        PersistentDataContainer container = sign.getPersistentDataContainer();
        PersistentDataContainer shopContainer = container.get(LushContainerShops.getInstance().namespacedKey("shop"), PersistentDataType.TAG_CONTAINER);
        if (shopContainer == null) {
            return null;
        }

        ShopData shopData = ShopData.from(shopContainer);
        return new ShopSign(sign, shopData);
    }
}
