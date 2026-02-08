package org.lushplugins.lushcontainershops.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
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
import org.lushplugins.lushcontainershops.utils.component.LimitedFlattener;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.libraries.chatcolor.ModernChatColorHandler;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonAutoDetect;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonInclude;
import org.lushplugins.lushlib.libraries.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

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

    public @NotNull Material getMaterial() {
        return this.material;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getItemName() {
        if (this.displayName != null) {
            return ChatColorHandler.translate(this.displayName);
        } else {
            return ChatColorHandler.translate(StringUtils.makeFriendly(this.material.key().value().replace("_", " ")));
        }
    }

    public Integer getCustomModelData() {
        return this.customModelData;
    }

    public boolean isSimilar(@NotNull ItemStack itemStack) {
        if (this.material != itemStack.getType()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return this.displayName == null && this.customModelData == null;
        }

        ConfigManager configManager = LushContainerShops.getInstance().getConfigManager();
        if (configManager.shouldCompareDisplayNames()) {
            String displayName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null;
            if (!Objects.equals(this.displayName, displayName)) {
                return false;
            }
        }

        if (configManager.shouldCompareCustomModelData()) {
            Integer customModelData = itemMeta.hasCustomModelData() ? itemMeta.getCustomModelData() : null;
            if (!Objects.equals(this.customModelData, customModelData)) {
                return false;
            }
        }

        return true;
    }

    public boolean isValid(@NotNull ItemStack itemStack) {
        if (!isSimilar(itemStack)) {
            return false;
        }

        if (this.amount != itemStack.getAmount()) {
            return false;
        }

        return true;
    }

    public String asString() {
        return "%s %s".formatted(this.amount, this.getItemName());
    }

    public String asString(int lineCharLimit) {
        return StringUtils.shortenString(asString(), lineCharLimit);
    }

    public Component asTextComponent() {
        return ModernChatColorHandler.translate(this.asString());
    }

    public Component asTextComponent(int lineCharLimit) {
        Component component = ModernChatColorHandler.translate(this.asString());

        LimitedFlattener flattern = new LimitedFlattener(lineCharLimit);
        ComponentFlattener.basic().flatten(component, flattern);
        return flattern.getResult();
    }

    public ShopItem withAmount(int amount) {
        return new ShopItem(
            this.material,
            amount,
            this.displayName,
            this.customModelData
        );
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

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount values must be 1 or more");
        }

        Material material = RegistryUtils.parseString(String.join("_", materialData), Registry.MATERIAL);
        if (material == null) {
            throw new IllegalArgumentException("'%s' is not a valid material".formatted(String.join(" ", materialData)));
        }

        return new ShopItem(
            material,
            amount,
            null,
            null
        );
    }
}
