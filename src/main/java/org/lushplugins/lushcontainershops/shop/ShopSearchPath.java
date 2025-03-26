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
}
