package org.lushplugins.lushcontainershops.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PlayerNameCache {
    private static final Cache<UUID, String> NAME_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(1))
        .build();

    public static String getPlayerName(UUID uuid) {
        try {
            return NAME_CACHE.get(uuid, () -> Bukkit.getOfflinePlayer(uuid).getName());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
