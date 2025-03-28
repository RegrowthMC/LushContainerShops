package org.lushplugins.lushcontainershops.utils;

import org.bukkit.block.BlockFace;

public class BlockFaceUtils {

    public static BlockFace rotateClockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> throw new IllegalStateException("Unexpected value: " + face);
        };
    }
}
