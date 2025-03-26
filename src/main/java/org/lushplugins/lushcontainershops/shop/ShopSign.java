package org.lushplugins.lushcontainershops.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.*;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.config.ConfigManager;
import org.lushplugins.lushcontainershops.utils.SignUtils;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;
import org.lushplugins.lushlib.utils.BlockPosition;

import java.util.List;
import java.util.UUID;

public class ShopSign extends ShopBlock {

    /**
     * A basic wrapper for a shop sign containing the sign, and it's parsed shop data
     * @param sign the sign
     * @param owner the shop's owner
     * @param product the product
     * @param cost the cost
     * @param containerPosition the connected shop container's position
     */
    public ShopSign(@NotNull Sign sign, @NotNull UUID owner, @Nullable ShopItem product, @Nullable ShopItem cost, @Nullable BlockPosition containerPosition) {
        super(sign, owner, product, cost, containerPosition);
    }

    /**
     * @param state the sign
     * @param pdc The "shop" persistent data container, cannot be the full persistent data container
     */
    public ShopSign(Sign state, PersistentDataContainer pdc) {
        super(state, pdc);
    }

    @Override
    public @NotNull Sign getTileState() {
        return (Sign) super.getTileState();
    }

    private void updateSignStateLines(List<Component> lines) {
        int lineCharLimit = this.isHanging() ? 10 : 15;

        lines.set(0, ModernChatColorHandler.translate(LushContainerShops.getInstance().getConfigManager().getMessageOrEmpty("header-color") + "[Shop]"));

        ShopItem product = this.getProduct();
        if (product != null) {
            lines.set(1, product.asTextComponent(lineCharLimit));
        } else {
            // TODO: Make line configurable?
            lines.set(1, Component.text("click product")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decorate(TextDecoration.ITALIC));
        }

        ShopItem cost = this.getCost();
        if (cost != null) {
            lines.set(2, cost.asTextComponent(lineCharLimit));
        } else if (product != null) {
            // TODO: Make line configurable?
            lines.set(2, Component.text("click cost")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decorate(TextDecoration.ITALIC));
        }

        ShopContainer shopContainer = this.getShopContainer();
        if (!this.isEstablished() || shopContainer == null) {
            lines.set(3, ModernChatColorHandler.translate(LushContainerShops.getInstance().getConfigManager().getMessageOrEmpty("not-setup")));
        } else {
            // TODO: Add other statuses ShopContainer#contains(ShopItem)
            lines.set(3, ModernChatColorHandler.translate(LushContainerShops.getInstance().getConfigManager().getMessageOrEmpty("in-stock")));
        }
    }

    public void updateSignState(List<Component> lines) {
        updateSignStateLines(lines);
        updateTileStatePDC();
    }

    public void updateSignState() {
        Sign state = this.getTileState();
        SignSide side = state.getSide(Side.FRONT);
        updateSignState(side.lines());
        state.update();
    }

    public boolean isHanging() {
        return this.getTileState() instanceof HangingSign;
    }

    public Block getAttachedTo() {
        return SignUtils.getAttachedTo(this.getTileState());
    }

    @Override
    public @Nullable Container findPotentialContainer() {
        ConfigManager configManager = LushContainerShops.getInstance().getConfigManager();

        Block attachedTo = this.getAttachedTo();
        if (configManager.shouldAllowConnectedContainersOnly()) {
            if (!configManager.isWhitelistedContainer(attachedTo.getType())) {
                return null;
            }

            if ((attachedTo.getWorld().getBlockState(attachedTo.getLocation()) instanceof Container container)) {
                return container;
            }
        }

        Integer[][] relativePositions;
        if (this.isHanging()) {
            relativePositions = ShopSearchPath.HANGING_SIGN;
        } else {
            relativePositions = ShopSearchPath.SIGN;
        }

        for (Integer[] relativePosition : relativePositions) {
            Block relativeBlock = attachedTo.getRelative(
                relativePosition[0],
                relativePosition[1],
                relativePosition[2]
            );

            if (!configManager.isWhitelistedContainer(relativeBlock.getType())) {
                continue;
            }

            if ((relativeBlock.getWorld().getBlockState(relativeBlock.getLocation()) instanceof Container container)) {
                return container;
            }
        }

        return null;
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
        PersistentDataContainer pdc = sign.getPersistentDataContainer();
        PersistentDataContainer shopPDC = pdc.get(LushContainerShops.getInstance().namespacedKey("shop"), PersistentDataType.TAG_CONTAINER);
        return shopPDC != null ? new ShopSign(sign, shopPDC) : null;
    }
}
