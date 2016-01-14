package ru.euphoriadev.vk.util;

import android.content.SharedPreferences;
import android.os.Build;

/**
 * Created by Igor on 13.01.16.
 * <p/>
 * Simple updater of Preferences
 */
public class PrefManager {
    /** Default values */
    public static final String DEFAULT_STRING_VALUE = "";
    public static final int DEFAULT_INT_VALUE = -1;
    public static final boolean DEFAULT_BOOLEAN_VALUE = false;

    /** Preferences for all App */
    private static final SharedPreferences sPreferences = AppLoader.getLoader().getPreferences();
    /** Editor for change values of Preference */
    private static final SharedPreferences.Editor sEditor = sPreferences.edit();


    /**
     * Set a String value in the preferences editor and apply
     *
     * @param key   the name of the preference to modify.
     * @param value the new value for the preference.
     */
    public static void putString(String key, String value) {
        sEditor.putString(key, value);
        apply();
    }

    /**
     * Set a int value in the preferences editor and apply
     *
     * @param key   the name of the preference to modify.
     * @param value the new value for the preference.
     */
    public static void putInt(String key, int value) {
        sEditor.putInt(key, value);
        apply();
    }

    /**
     * Set a long value in the preferences editor and apply
     *
     * @param key   the name of the preference to modify.
     * @param value the new value for the preference.
     */
    public static void putLong(String key, long value) {
        sEditor.putLong(key, value);
        apply();
    }

    /**
     * Set a boolean value in the preferences editor and apply
     *
     * @param key   the name of the preference to modify.
     * @param value the new value for the preference.
     */
    public static void putBoolean(String key, boolean value) {
        sEditor.putBoolean(key, value);
        apply();
    }

    /**
     * Retrieve a String value from the preferences
     *
     * @param key      the name of the preference to retrieve.
     * @param defValue value to return if this preference does not exist.
     */
    public static String getString(String key, String defValue) {
        return sPreferences.getString(key, defValue);
    }

    /**
     * Retrieve a String value from the preferences
     *
     * @param key the name of the preference to retrieve
     * @return value from preference. If value does not exist returns {@link PrefManager#DEFAULT_STRING_VALUE}
     */
    public static String getString(String key) {
        return getString(key, DEFAULT_STRING_VALUE);
    }

    /**
     * Retrieve an int value from the preferences.
     *
     * @param key      the name of the preference to retrieve.
     * @param defValue value to return if this preference does not exist.
     * @return the preference value if it exists, or defValue.
     */
    public static int getInt(String key, int defValue) {
        return sPreferences.getInt(key, defValue);
    }

    /**
     * Retrieve an int value from the preferences.
     *
     * @param key the name of the preference to retrieve.
     * @return the preference value if it exists, or defValue.
     */
    public static int getInt(String key) {
        return getInt(key, DEFAULT_INT_VALUE);
    }

    /**
     * Retrieve a boolean value from the preferences.
     *
     * @param key      the name of the preference to retrieve.
     * @param defValue value to return if this preference does not exist.
     * @return the preference value if it exists, or defValue.
     */
    public static boolean getBoolean(String key, boolean defValue) {
        return sPreferences.getBoolean(key, defValue);
    }

    /**
     * /**
     * Retrieve a boolean value from the preferences.
     *
     * @param key the name of the preference to retrieve.
     * @return the preference value if it exists, or defValue.
     */
    public static boolean getBoolean(String key) {
        return getBoolean(key, DEFAULT_BOOLEAN_VALUE);
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
