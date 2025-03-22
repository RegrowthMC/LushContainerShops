package org.lushplugins.lushcontainershops.utils.lamp.parameter;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation.Equipment;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.parameter.ContextParameter;

import java.lang.reflect.Type;

public class EquipmentContextParameter implements ContextParameter.Factory<BukkitCommandActor> {

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> ContextParameter<BukkitCommandActor, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<BukkitCommandActor> lamp) {
        if (parameterType != ItemStack.class) {
            return null;
        }

        Equipment equipmentAnnotation = annotations.get(Equipment.class);
        if (equipmentAnnotation == null) {
            return null;
        }

        return (parameter, context) -> (T) context.actor().requirePlayer().getInventory().getItem(equipmentAnnotation.value());
    }
}
