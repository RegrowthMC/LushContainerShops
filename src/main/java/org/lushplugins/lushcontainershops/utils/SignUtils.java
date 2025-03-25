package org.lushplugins.lushcontainershops.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.HangingSign;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;

public class SignUtils {

    public static Block getAttachedTo(Sign sign) {
        BlockFace attachedDirection;
        if (sign instanceof HangingSign) {
            attachedDirection = BlockFace.UP;
        } else if (sign.getBlockData() instanceof WallSign signData) {
            attachedDirection = signData.getFacing().getOppositeFace();
        } else {
            attachedDirection = BlockFace.DOWN;
        }

        Location blockLocation = sign.getLocation().add(attachedDirection.getDirection());
        return sign.getWorld().getBlockAt(blockLocation);
    }
}
