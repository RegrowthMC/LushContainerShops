package org.lushplugins.lushcontainershops.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class Vector3iPersistentDataType implements PersistentDataType<int[], Vector3i> {
    public static final Vector3iPersistentDataType INSTANCE = new Vector3iPersistentDataType();

    @Override
    public @NotNull Class<int[]> getPrimitiveType() {
        return int[].class;
    }

    @Override
    public @NotNull Class<Vector3i> getComplexType() {
        return Vector3i.class;
    }

    @Override
    public int @NotNull [] toPrimitive(@NotNull Vector3i complex, @NotNull PersistentDataAdapterContext context) {
        return new int[]{
            complex.x(),
            complex.y(),
            complex.z()
        };
    }

    @Override
    public @NotNull Vector3i fromPrimitive(int[] primitive, @NotNull PersistentDataAdapterContext context) {
        return new Vector3i(
            primitive[0],
            primitive[1],
            primitive[2]
        );
    }
}
