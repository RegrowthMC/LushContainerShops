package org.lushplugins.lushcontainershops.utils.lamp.parameter;

import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.shop.ShopContainer;
import org.lushplugins.lushcontainershops.utils.PlayerNameCache;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.Stocker;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.lang.reflect.Type;
import java.util.Collections;

public class StockerSuggestionProvider implements SuggestionProvider.Factory<BukkitCommandActor> {

    @Override
    public @Nullable SuggestionProvider<BukkitCommandActor> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<BukkitCommandActor> lamp) {
        if (!annotations.contains(Stocker.class)) {
            return null;
        }

        return (context) -> {
            RayTraceResult rayTrace = context.actor().requirePlayer().rayTraceBlocks(5);
            if (rayTrace != null) {
                ShopContainer shop = ShopContainer.from(rayTrace.getHitBlock());
                if (shop != null && shop.isOwner(context.actor().uniqueId())) {
                    return shop.getStockers().stream()
                        .map(PlayerNameCache::getPlayerName)
                        .toList();
                }
            }

            return Collections.emptyList();
        };
    }
}
