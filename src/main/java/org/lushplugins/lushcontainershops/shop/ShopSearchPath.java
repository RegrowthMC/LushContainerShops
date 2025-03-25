package org.lushplugins.lushcontainershops.shop;

/**
 * Paths are relative to the attached block NOT the sign
 */
public class ShopSearchPath {
    public static final Integer[][] SIGN = {
        {0, 0, 0}, // Attached
        {0, -1, 0}, // Below
        {0, 1, 0} // Above
    };
    public static final Integer[][] HANGING_SIGN = {
        {0, 0, 0}, // Attached
        {0, -2, 0}, // 2 Below
        {0, -3, 0}, // 3 Below
        {0, -4, 0} // 4 Below
    };
    // TODO: Remove in favour of using block locations stored in PDC
    public static final Integer[][] ADJACENT_BLOCKS = {
        {0, 0, -1}, // North
        {1, 0, 0}, // East
        {0, 0, 1}, // South
        {-1, 0, 0}, // West
        {0, 1, 0}, // Above
        {0, -1, 0} // Below
    };
}
