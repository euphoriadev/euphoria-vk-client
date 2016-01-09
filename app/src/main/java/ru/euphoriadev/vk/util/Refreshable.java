package ru.euphoriadev.vk.util;

/**
 * Created by Igor on 28.12.15.
 * <p/>
 * Callback for changes Preferences of the application
 *
 * @see android.preference.PreferenceManager
 * @see RefreshManager
 */
public interface Refreshable {
    /**
     * Called when preferences are changed
     * @param prefKey The key of preferences that was just changed by application
     */
    void onRefresh(String prefKey);
}
