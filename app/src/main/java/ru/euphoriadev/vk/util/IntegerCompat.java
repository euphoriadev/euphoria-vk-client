package ru.euphoriadev.vk.util;

import android.os.Build;

/**
 * Created by Igor on 05.03.16.
 * <p/>
 * Helper for accessing features in {@link java.lang.Integer}
 * introduced after API level 4 in a backwards compatible fashion
 */
public class IntegerCompat {
    /**
     * Compares two {@code int} values
     *
     * @param lhs the left value
     * @param rhs the right value
     * @return 0 if lhs = rhs, less than 0 if lhs &lt; rhs, and greater than 0
     * if lhs &gt; rhs
     */
    public static int compare(int lhs, int rhs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // available only from KitKat (19)
            return Integer.compare(lhs, rhs);
        } else {
            return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
        }
    }

}
