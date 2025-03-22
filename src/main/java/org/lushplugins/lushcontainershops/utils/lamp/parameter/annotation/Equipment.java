package org.lushplugins.lushcontainershops.utils.lamp.parameter.annotation;

import org.bukkit.inventory.EquipmentSlot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Equipment {
    EquipmentSlot value();
}
