package org.lushplugins.lushcontainershops.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushcontainershops.config.ConfigManager;
import org.lushplugins.lushcontainershops.utils.RegistryUtils;
import org.lushplugins.lushcontainershops.utils.StringUtils;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonAutoDetect;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonInclude;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonAutoDetect(
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.ANY
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopItem {
    private final Material material;
    private final int amount;
    private final String displayName;
    private final Integer customModelData;

    @JsonCreator
    public ShopItem(
        @JsonProperty("material")
        @NotNull Material material,

        @JsonProperty("amount")
        int amount,

        @JsonProperty("display_name")
        @Nullable String displayName,

        @JsonProperty("custom_model_data")
        @Nullable Integer customModelData
    ) {
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.customModelData = customModelData;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getItemName() {
        if (this.displayName != null) {
            return this.displayName;
        } else {
            return StringUtils.makeFriendly(this.material.key().value().replace("_", " "));
        }
    }

    public boolean isValid(@NotNull ItemStack itemStack) {
        if (this.material != itemStack.getType()) {
            return false;
        }

        if (this.amount != itemStack.getAmount()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return this.displayName == null && this.customModelData == null;
        }

        ConfigManager configManager = LushContainerShops.getInstance().getConfigManager();
        if (configManager.shouldCompareDisplayNames() && this.displayName != null && !itemMeta.getDisplayName().equals(this.displayName)) {
            return false;
        }

        if (configManager.shouldCompareCustomModelData() && this.customModelData != null && (!itemMeta.hasCustomModelData() || itemMeta.getCustomModelData() != this.customModelData)) {
            return false;
        }

        return true;
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
        String displayName = null;
        Integer customModelData = null;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasDisplayName()) {
                displayName = itemMeta.getDisplayName();
            }

            if (itemMeta.hasCustomModelData()) {
                customModelData = itemMeta.getCustomModelData();
            }
        }

        return new ShopItem(
            item.getType(),
            item.getAmount(),
            displayName,
            customModelData
        );
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

        return new ShopItem(
            material,
            amount,
            null,
            null
        );
    }
}
