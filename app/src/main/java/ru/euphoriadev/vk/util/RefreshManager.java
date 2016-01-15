package ru.euphoriadev.vk.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Igor on 28.12.15.
 * <p/>
 * Allow Register the element for update Preferences.
 * <p/>
 * For example, this way you can update values of {@link android.view.View}
 * when you change settings
 *
 * @see Refreshable
 */
public class RefreshManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    /** Array of Registered items */
    private static final ArrayList<RefreshItem> sRefreshables = new ArrayList<>();
    private static RefreshManager sInstance;

    /**
     * Use if needed listen all changes of preferences,
     */
    public static final String PREF_ALL_KEYS = "all_keys";

    /**
     * Initialize RefreshManager, once, when the class is loaded
     */
    static {
        sInstance = new RefreshManager();
    }

    /**
     * Private Constructor, you should not call him
     */
    private RefreshManager() {
        PreferenceManager.getDefaultSharedPreferences(AppLoader.appContext).registerOnSharedPreferenceChangeListener(this);
    }


    /**
     * Register Object for update preferences,
     * Use {@link RefreshManager#PREF_ALL_KEYS} if needed listen all changes of preferences
     *
     * @param object  Refreshable for called
     * @param prefKey a Preferences key wait for updates
     */
    public static void registerForChangePreferences(Refreshable object, String prefKey) {
        RefreshItem item = new RefreshItem(prefKey, object);
        if (!sRefreshables.contains(item)) {
            sRefreshables.add(item);
        }
    }

    /**
     * Unregister element for listing changes settings
     *
     * @param object The Object that you want unregister
     */
    public static void unregisterForChangePreferences(Refreshable object) {
        if (checkIfEquals(object)) {
            removeAll(object);
        }
    }

    /**
     * Removes all elements, leaving it empty,
     * and trim to size for to free up memory
     */
    public static void clear() {
        sRefreshables.clear();
        sRefreshables.trimToSize();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for (int i = 0; i < sRefreshables.size(); i++) {
            RefreshItem item = sRefreshables.get(i);
            if (item.preferenceKey.equals(key)) {
                Refreshable refreshable = item.reference.get();
                if (refreshable != null) {
                    refreshable.onRefresh(key);
                }
            }
        }
    }

    private static boolean checkIfEquals(String prefKey) {
        if (sRefreshables.isEmpty()) {
            return false;
        }

        for (int i = 0; i < sRefreshables.size(); i++) {
            RefreshItem item = sRefreshables.get(i);
            if (item.preferenceKey.equals(prefKey)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkIfEquals(Refreshable refreshable) {
        if (sRefreshables.isEmpty()) {
            return false;
        }

        for (int i = 0; i < sRefreshables.size(); i++) {
            RefreshItem item = sRefreshables.get(i);
            Refreshable ref = item.reference.get();
            if (ref != null) {
                if (ref == refreshable) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeAll(Refreshable refreshable) {
        for (int i = 0; i < sRefreshables.size(); i++) {
            RefreshItem item = sRefreshables.get(i);
            Refreshable ref = item.reference.get();
            if (ref != null && ref == refreshable) {
                sRefreshables.remove(i);
            }
        }
    }

    /**
     * A simple class to store {@link Refreshable} and {@link String prefKey}.
     * <p/>
     * Refreshable "wrapped" in a {@link WeakReference}
     * because the Garbage Collector may deem it necessary,
     * to delete the object from memory, for example: the activity is closed
     */
    private static class RefreshItem {
        public String preferenceKey;
        public WeakReference<Refreshable> reference;

        public RefreshItem(String prefKey, Refreshable refreshable) {
            this.preferenceKey = prefKey;
            this.reference = new WeakReference<>(refreshable);
        }
    }
}
