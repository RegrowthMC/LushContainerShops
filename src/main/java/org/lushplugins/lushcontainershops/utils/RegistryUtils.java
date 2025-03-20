package org.lushplugins.lushcontainershops.utils;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.Nullable;

public class RegistryUtils {

    public static <T extends Keyed> @Nullable T parseString(String string, Registry<T> registry) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(string.toLowerCase());
        return namespacedKey != null ? registry.get(namespacedKey) : null;
    }
}
