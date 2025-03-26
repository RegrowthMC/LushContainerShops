package org.lushplugins.lushcontainershops.utils;

public class StringUtils extends org.lushplugins.lushlib.utils.StringUtils {

    public static String shortenString(String string, int length) {
        if (string.length() > length) {
            return string.substring(0, length - 1).concat("...");
        } else {
            return string;
        }
    }
}
