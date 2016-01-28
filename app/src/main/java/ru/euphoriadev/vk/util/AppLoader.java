package ru.euphoriadev.vk.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.euphoriadev.vk.PrefsFragment;
import ru.euphoriadev.vk.R;

/**
 * Created by Igor on 16.11.15.
 * Базовый класс, который позволяет поддерживать глобальное состояние приложения
 */
public class AppLoader extends Application {
    public static final String TAG = "AppLoader";
    public static final String APP_DIR = "Euphoria";

    public static volatile Context appContext;
    /** Cached important preferences */
    public boolean isDarkTheme;
    public boolean writeLog;
    public String themeName;
    public String forcedLocale;
    public String makingDrawerHeader;
    private SharedPreferences sPrefs;
    private ExecutorService mExecutor;
    private Handler mHandler;

    public static AppLoader getLoader(Context applicationContext) {
        return (AppLoader) applicationContext;
    }

    /** Methods for themes **/

    public static AppLoader getLoader() {
        return (AppLoader) appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        Log.i(TAG, "onCreate");

        sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mExecutor = Executors.newSingleThreadExecutor();
        mHandler = new Handler(appContext.getMainLooper());

        updatePreferences();
        createAppDir();

        CrashManager.init();
    }

    /**
     * Apply theme to activity, without {@link Activity#recreate()}
     * @param withLocale true if you want to change language
     */
    public void applyTheme(Activity activity, boolean withLocale, boolean drawingStatusBar) {
        ThemeManager.applyTheme(activity, drawingStatusBar);
//        int styleId = 0;
//        switch (themeName.toUpperCase()) {
//            case "DARK":      styleId = R.style.AppBaseTheme_Dark; break;
//            case "BLACK":     styleId = R.style.AppBaseTheme_Black ;break;
//            case "INDIGO":    styleId = isDarkTheme ? R.style.AppBaseTheme_Indigo : R.style.AppBaseThemeLight_Indigo; break;
//            case "RED":       styleId = isDarkTheme ? R.style.AppBaseTheme_Red    : R.style.AppBaseThemeLight_Red;    break;
//            case "ORANGE":    styleId = isDarkTheme ? R.style.AppBaseTheme_Orange : R.style.AppBaseThemeLight_Orange; break;
//            case "BLUE_GREY": styleId = isDarkTheme ? R.style.AppBaseTheme_Blue_Grey : R.style.AppBaseThemeLight_Blue_Grey; break;
//            case "PINK":      styleId = isDarkTheme ? R.style.AppBaseTheme_Pink : R.style.AppBaseThemeLight_Pink; break;
//            case "TEAL":      styleId = isDarkTheme ? R.style.AppBaseTheme_Teal : R.style.AppBaseThemeLight_Teal; break;
//            case "GREEN":      styleId = isDarkTheme ? R.style.AppBaseTheme_Green : R.style.AppBaseThemeLight_Green; break;
//            case "DEEP_ORANGE": styleId = isDarkTheme ? R.style.AppBaseTheme_Deep_Orange : R.style.AppBaseThemeLight_Deep_Orange; break;
//            case "BROWN":     styleId = isDarkTheme ? R.style.AppBaseTheme_Brown : R.style.AppBaseThemeLight_Brown; break;
//
//            default:
//                styleId = R.style.AppBaseTheme_Indigo;
//        }
//        activity.setTheme(styleId);
//
//        if (withLocale) {
//            if (!forcedLocale.equalsIgnoreCase(Locale.getDefault().getLanguage())) {
//                Locale locale = new Locale(forcedLocale);
//                Locale.setDefault(locale);
//                Configuration config = new Configuration();
//                config.locale = locale;
//                appContext.getResources().updateConfiguration(config,
//                        appContext.getResources().getDisplayMetrics());
//            }
//        }
//
//        if (drawingStatusBar)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = activity.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }


    }

    /**
     * Apply theme to activity, without {@link Activity#recreate()}
     * @param activity
     */
    public void applyTheme(Activity activity) {
        applyTheme(activity, true, false);
    }

    /**
     * Получение Header-а шторки, основывайсь на текущей теме.
     * @param withSolidBackground учитывать настройки
     * @return
     */
    public Drawable getDrawerHeader(Context context, boolean withSolidBackground) {
        if (makingDrawerHeader.equals("blur_photo")) {
            return null;
        }
        if (withSolidBackground) {
            if (makingDrawerHeader.equals("solid_background")) {
                return new ColorDrawable(ThemeUtils.getThemeAttrColor(context, R.attr.colorPrimary));
            }
        }
        int drawableId;
        switch (themeName.toUpperCase()) {
            case "DARK":   drawableId = R.drawable.drawer_header_dark; break;
            case "BLACK":  drawableId = R.drawable.drawer_header_black;  break;
            case "INDIGO": drawableId = R.drawable.drawer_header; break;
            case "RED":    drawableId = R.drawable.drawer_header_black;  break;
            case "ORANGE": drawableId = R.drawable.drawer_header_orange2; break;
            case "BLUE_GREY": drawableId = R.drawable.drawer_header; break;
            case "PINK":  drawableId = R.drawable.drawer_header_pink; break;
            case "TEAL":  drawableId = R.drawable.drawer_header_black; break;
            case "DEEP_ORANGE": drawableId = R.drawable.drawer_header_orange2; break;
            case "BROWN": drawableId = R.drawable.drawer_header; break;
            case "GREEN": drawableId = R.drawable.drawer_header_green; break;

            default: drawableId = R.drawable.drawer_header; break;
        }
        return appContext.getResources().getDrawable(drawableId);
    }

    public SharedPreferences getPreferences() {
        return sPrefs;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public ExecutorService getExecutor() {
        return mExecutor;
    }

    public File getExternalFilesDir() {
        return Environment.getExternalStorageDirectory();
    }

    public File getAppDir() {
        final File file = new File(getExternalFilesDir().getAbsolutePath() + "/" + APP_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * Обновление настроек
     */
    public void updatePreferences() {
        isDarkTheme = sPrefs.getBoolean(PrefsFragment.KEY_IS_NIGHT_MODE, true);
        forcedLocale = sPrefs.getString(PrefsFragment.KEY_FORCED_LOCALE, Locale.getDefault().getLanguage());
        writeLog = sPrefs.getBoolean(PrefsFragment.KEY_WRITE_LOG, true);
        makingDrawerHeader = sPrefs.getString(PrefsFragment.KEY_MAKING_DRAWER_HEADER, "Default");
    }

    /**
     * Создание папки приложения
     * @return true если папку создалась, false = если папка уже была ранее создана
     */
    private boolean createAppDir() {
        File file = new File(Environment.getExternalStorageDirectory(), APP_DIR);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }


}
