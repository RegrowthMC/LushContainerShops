package org.lushplugins.lushcontainershops.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.jetbrains.annotations.Nullable;

public class BlockUtils {

    public static @Nullable BlockFace getConnectedChestDirection(Block block) {
        if (!(block.getBlockData() instanceof Chest chest)) {
            return null;
        }

        return switch (chest.getType()) {
            case LEFT -> BlockFaceUtils.rotateClockwise(chest.getFacing());
            case RIGHT -> BlockFaceUtils.rotateClockwise(chest.getFacing()).getOppositeFace();
            default -> null;
        };
    }

    public static @Nullable Block getConnectedChest(Block block) {
        BlockFace direction = getConnectedChestDirection(block);
        if (direction == null) {
            return null;
        }

        return block.getRelative(direction);
    }
}
