package org.lushplugins.lushcontainershops.config;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.LushContainerShops;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.registry.RegistryUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigManager {
    private List<Material> containerWhitelist;
    private List<Material> signWhitelist;
    private boolean displayVisual;
    private boolean connectedContainersOnly;
    private boolean compareDisplayNames;
    private boolean compareCustomModelData;
    private Map<String, String> messages;

    public ConfigManager() {
        LushContainerShops.getInstance().saveDefaultConfig();
    }

    public void reloadConfig() {
        LushContainerShops plugin = LushContainerShops.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.containerWhitelist = RegistryUtils.fromStringList(config.getStringList("container-whitelist"), Registry.MATERIAL);
        this.signWhitelist = RegistryUtils.fromStringList(config.getStringList("sign-whitelist"), Registry.MATERIAL);
        this.displayVisual = config.getBoolean("display-visual", true);
        this.connectedContainersOnly = config.getBoolean("connected-containers-only", true);
        this.compareDisplayNames = config.getBoolean("should-compare.display-names", true);
        this.compareCustomModelData = config.getBoolean("should-compare.custom-model-data", true);
        this.messages = config.getConfigurationSection("messages").getValues(false).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (String) entry.getValue()
            ));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isWhitelistedContainer(Material material) {
        return this.containerWhitelist.contains(material);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isWhitelistedSign(Material material) {
        return this.signWhitelist.contains(material);
    }

    // TODO: Implement as dropped item visuals
    public boolean shouldDisplayVisual() {
        return this.displayVisual;
    }

    public boolean shouldAllowConnectedContainersOnly() {
        return this.connectedContainersOnly;
    }

    public boolean shouldCompareDisplayNames() {
        return this.compareDisplayNames;
    }

    public boolean shouldCompareCustomModelData() {
        return this.compareCustomModelData;
    }

    public @Nullable String getMessage(@NotNull String key) {
        return this.messages.get(key);
    }

    public @NotNull String getMessage(@NotNull String key, @NotNull String def) {
        return this.messages.getOrDefault(key, def);
    }

    public @NotNull String getMessageOrEmpty(@NotNull String key) {
        return getMessage(key, "");
    }

    public void sendMessage(@NotNull CommandSender recipient, @NotNull String key, @Nullable Function<String, String> parser) {
        String message = this.messages.get(key);
        if (message == null) {
            return;
        }

        if (parser != null) {
            message = parser.apply(message);
        }

        ChatColorHandler.sendMessage(recipient, message);
    }

    public void sendMessage(@NotNull CommandSender recipient, @NotNull String key) {
        sendMessage(recipient, key, null);
    }
}
