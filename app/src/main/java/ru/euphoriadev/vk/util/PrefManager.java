package ru.euphoriadev.vk.util;

import android.content.SharedPreferences;
import android.os.Build;

/**
 * Created by Igor on 13.01.16.
 * <p/>
 * Simple updater of Preferences
 */
public class PrefManager {
    /** Preferences for all App */
    private static final SharedPreferences sPreferences = AppLoader.getLoader().getPreferences();
    /** Editor for change values of Preference */
    private static final SharedPreferences.Editor sEditor = sPreferences.edit();


    /**
     * Set a String value in the preferences editor and apply
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     */
    public static void putString(String key, String value) {
        sEditor.putString(key, value);
        apply();
    }

    /**
     * Set a int value in the preferences editor and apply
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     */
    public static void putInt(String key, int value) {
        sEditor.putInt(key, value);
        apply();
    }

    /**
     * Set a long value in the preferences editor and apply
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     */
    public static void putLong(String key, long value) {
        sEditor.putLong(key, value);
        apply();
    }

    /**
     * Set a boolean value in the preferences editor and apply
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     */
    public static void putBoolean(String key, boolean value) {
        sEditor.putBoolean(key, value);
        apply();
    }

    /**
     * Apply changes to Preferences.
     * If build version < 9 - use old method {@link SharedPreferences.Editor#commit()}
     */
    private static void apply() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            sEditor.apply();
        } else {
            sEditor.commit();
        }
    }
}
