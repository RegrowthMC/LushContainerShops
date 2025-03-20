package org.lushplugins.lushcontainershops.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushcontainershops.utils.RegistryUtils;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;
import org.lushplugins.lushlib.utils.DisplayItemStack;
import org.lushplugins.lushlib.utils.StringUtils;

import java.util.Arrays;

public class ShopItem {
    private final DisplayItemStack item;

    public ShopItem(DisplayItemStack item) {
        this.item = item;
    }

    public DisplayItemStack getItem() {
        return item;
    }

    public int getAmount() {
        return item.getAmount().getMin();
    }

    public String getItemName() {
        if (item.hasDisplayName()) {
            return item.getDisplayName();
        } else {
            return StringUtils.makeFriendly(item.getType().key().value().replace("_", " "));
        }
    }

    public String asString() {
        return asString(15);
    }

    public String asString(int lineCharLimit) {
        StringBuilder builder = new StringBuilder()
            .append(this.getAmount())
            .append(' ');

        int remainingSpace = lineCharLimit - builder.length();
        if (remainingSpace > 0) {
            String itemName = getItemName();

            // builder.insert(0, "&#FF6969"); // TODO: Replace with item name's colour

            if (itemName.length() > remainingSpace) {
                builder
                    .append(itemName, 0, remainingSpace - 1)
                    .append("...");
            } else {
                builder.append(itemName);
            }
        }

        return builder.toString();
    }

    public Component asTextComponent() {
        return ModernChatColorHandler.translate(this.asString());
    }

    public Component asTextComponent(int lineCharLimit) {
        return ModernChatColorHandler.translate(this.asString(lineCharLimit));
    }

    public static @NotNull ShopItem from(ItemStack item) {
        return new ShopItem(DisplayItemStack.builder(item)
            .setDisplayName(null)
            .setLore(null)
            .setEnchantGlow(null)
            .setSkullTexture(null)
            .build());
    }

    public static @NotNull ShopItem parseString(@NotNull String item) throws IllegalArgumentException {
        String[] itemData = item.split(" ");

        String[] materialData;
        int amount;
        try {
            amount = Integer.parseInt(itemData[0]);
            materialData = Arrays.copyOfRange(itemData, 1, itemData.length);
        } catch (NumberFormatException ignored) {
            materialData = itemData;
            amount = 1;
        }

        Material material = RegistryUtils.parseString(String.join("_", materialData), Registry.MATERIAL);
        if (material == null) {
            throw new IllegalArgumentException();
        }

        return new ShopItem(DisplayItemStack.builder()
            .setType(material)
            .setAmount(amount)
            .build());
    }
}
