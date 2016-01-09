package ru.euphoriadev.vk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.SharedPreferencesCompat;


public class Account {

    public static final String API_ID = "4510232"; // ID TimeVK
    public String access_token; // токен
    public String fullName, photo, status = "";
    public long user_id; // ID пользователя

    SharedPreferences prefs;
    Editor editor;

    public Account(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
    }

    public static Account get(Context context) {
        return new Account(context);
    }

    /**
     * Сохранение настроек аккаунта в {@link SharedPreferences}
     */
    public void save() {
        editor.putString("access_token", access_token);
        editor.putLong("user_id", user_id);

        editor.putString("fullName", fullName);
        editor.putString("photo", photo);
        editor.putString("status", status);

        //   editor.apply();
//        if (Build.VERSION.SDK_INT < 16)
            editor.commit();
//        else editor.apply();
    }

    /**
     * Обновление значение переменной
     * @param key Ключ, для обновление
     * @param value Переменная для изминения
     */
    public void update(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Обновления значение
     * @param key Ключ, для обновление
     * @param value long переменная для изминения
     */
    public void update(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Восстановленние переменных аккаунта
     * @return
     */
    public Account restore() {
        access_token = prefs.getString("access_token", null);
        user_id = prefs.getLong("user_id", 0);

        fullName = prefs.getString("fullName", "");
        photo = prefs.getString("photo", "");
        status = prefs.getString("status", "");
        return this;
    }

    /**
     * Удаление аккаунта, т.е выход
     */
    public void clear() {
        editor.remove("access_token");
        editor.remove("user_id");
        editor.remove("photo");
        editor.remove("fullName");
        editor.apply();
    }
}
