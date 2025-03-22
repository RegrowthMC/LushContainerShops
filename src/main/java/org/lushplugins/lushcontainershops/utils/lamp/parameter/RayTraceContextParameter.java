package org.lushplugins.lushcontainershops.utils.lamp.parameter;

import org.bukkit.block.Block;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.RayTrace;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.parameter.ContextParameter;

import java.lang.reflect.Type;

public class RayTraceContextParameter implements ContextParameter.Factory<BukkitCommandActor> {

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> ContextParameter<BukkitCommandActor, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<BukkitCommandActor> lamp) {
        if (parameterType != Block.class) {
            return null;
        }

        RayTrace rayTraceAnnotation = annotations.get(RayTrace.class);
        if (rayTraceAnnotation == null) {
            return null;
        }

        return (parameter, context) -> {
            RayTraceResult rayTrace = context.actor().requirePlayer().rayTraceBlocks(rayTraceAnnotation.distance());
            return rayTrace != null ? (T) rayTrace.getHitBlock() : null;
        };
    }
}
