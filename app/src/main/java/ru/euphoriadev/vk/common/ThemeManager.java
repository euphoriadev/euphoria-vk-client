package ru.euphoriadev.vk.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.Locale;

import ru.euphoriadev.vk.BaseThemedActivity;
import ru.euphoriadev.vk.BasicActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.SettingsFragment;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 23.12.15.
 * <p/>
 * A Simple Theme Manager
 */
public class ThemeManager {
    static {
        ResourcesLoader.loadColors(AppLoader.appContext);
    }

    public static final int DEFAULT_COLOR = ResourcesLoader.getColor(R.color.md_red_500); // Red 500
    public static final int DEFAULT_ACCENT_COLOR = ResourcesLoader.getColor(R.color.md_teal_500); // Teal 500
    public static final String PREF_KEY_THEME_COLOUR = "color_theme";
    public static final String PREF_KEY_FORCED_LOCALE = SettingsFragment.KEY_FORCED_LOCALE;
    public static final String PREF_KEY_IS_DARK_THEME = SettingsFragment.KEY_IS_NIGHT_MODE;
    public static final String PREF_KEY_DRAWER_HEADER = SettingsFragment.KEY_MAKING_DRAWER_HEADER;
    public static final String PREF_KEY_BLUR_RADIUS = SettingsFragment.KEY_BLUR_RADIUS;
    public static final String PREF_KEY_MESSAGE_WALLPAPER_PATH = SettingsFragment.KEY_WALLPAPER_PATH;

    /** Drawer header states **/
    public static final int DRAWER_HEADER_DEFAULT = 0;
    public static final int DRAWER_HEADER_SOLID_BACKGROUND = 1;
    public static final int DRAWER_HEADER_BLUR_PHOTO = 2;


    public static final int[][] COLOURS = ResourcesLoader.getThemeColoursPalette(AppLoader.appContext);
    /**
     * These are the colors that go in the initial palette.
     */
    public static final int[] PALETTE = {
            COLOURS[0][5],  // Red
            COLOURS[1][5],  // Pink
            COLOURS[2][5],  // Purple
            COLOURS[3][5],  // Deep purple
            COLOURS[4][5],  // Indigo
            COLOURS[5][5],  // Blue
            COLOURS[6][5],  // Light Blue
            COLOURS[7][5],  // Cyan
            COLOURS[8][5],  // Teal
            COLOURS[9][5],  // Green
            COLOURS[10][5], // Light Green
            COLOURS[11][5], // Lime
            COLOURS[12][5], // Yellow
            COLOURS[13][5], // Amber
            COLOURS[14][5], // Orange
            COLOURS[15][5], // Deep Orange
            COLOURS[16][5], // Brown
            COLOURS[17][5], // Grey
            COLOURS[18][5], // Blue Grey
            COLOURS[19][5], // Official vk
            Color.BLACK,    // Full Black
    };
    /**
     * Index for colours
     */
    private static final int COLOUR_RED = 0;
    private static final int COLOUR_PINK = 1;
    private static final int COLOUR_PURPLE = 2;
    private static final int COLOUR_DEEP_PURPLE = 3;
    private static final int COLOUR_INDIGO = 4;
    private static final int COLOUR_BLUR = 5;
    private static final int COLOUR_LIGHT_BLUE = 6;
    private static final int COLOUR_CYAN = 7;
    private static final int COLOUR_TEAL = 8;
    private static final int COLOUR_GREEN = 9;
    private static final int COLOUR_LIGHT_GREEN = 11;
    private static final int COLOUR_LIME = 11;
    private static final int COLOUR_YELLOW = 12;
    private static final int COLOUR_AMBER = 13;
    private static final int COLOUR_ORANGE = 14;
    private static final int COLOUR_DEEP_ORANGE = 15;
    private static final int COLOUR_BROWN = 16;
    private static final int COLOUR_GREY = 17;
    private static final int COLOUR_BLUE_GREY = 18;
    private static final int COLOUT_OFFICIAL_VK = 19;
    private static final int COLOUR_BLACK = 20;
    /**
     * This configures whether the text is
     * black (0) or
     * white (1) for each color above.
     */
    private static final int[][] TEXT_MODE_COLORS = {{
            // Red
            0, 0, 0, 0, 1, 1, 1, 1, 1, 1
    }, {    // Pink
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Purple
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Deep Purple
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Indigo
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Blue
            0, 0, 0, 0, 0, 1, 1, 1, 1, 1
    }, {    // Light Blue
            0, 0, 0, 0, 0, 0, 1, 1, 1, 1
    }, {    // Cyan
            0, 0, 0, 0, 0, 0, 0, 1, 1, 1
    }, {    // Teal
            0, 0, 0, 0, 0, 1, 1, 1, 1, 1
    }, {    // Green
            0, 0, 0, 0, 0, 0, 1, 1, 1, 1
    }, {    // Light Green
            0, 0, 0, 0, 0, 0, 0, 1, 1, 1
    }, {    // Lime
            0, 0, 0, 0, 0, 0, 0, 0, 0, 1
    }, {    // Yellow
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    }, {    // Amber
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    }, {    // Orange
            0, 0, 0, 0, 0, 0, 0, 0, 1, 1
    }, {    // Deep Orange
            0, 0, 0, 0, 0, 1, 1, 1, 1, 1
    }, {    // Brown
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Grey
            0, 0, 0, 0, 0, 0, 1, 1, 1, 1
    }, {    // Blue Grey
            0, 0, 0, 0, 1, 1, 1, 1, 1, 1
    }, {    // VK Official
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Black
            1,
    }};
    /**
     * Style ids
     */
    private static final SparseIntArray sDarkThemes = new SparseIntArray(20);
    private static final SparseIntArray sLightThemes = new SparseIntArray(20);
    /**
     * Drawer Headers res
     **/
    private static final SparseIntArray sDrawerHeaders = new SparseIntArray(20);
    /**
     * Cached theme values
     */
    private static String sLocale;
    private static String sMessageWallpaperPath;
    private static int sColourTheme = -1;
    private static int sAccentColor = -1;
    private static int sThemeColor = -1;
    private static int sTextPrimaryDarkColor = -1;
    private static int sTextSecondaryDarkColor = -1;
    private static int sTextPrimaryLightColor = -1;
    private static int sTextSecondaryLightColor = -1;
    private static int sDrawerHeaderState = -1;
    private static boolean sIsDarkTheme;
    private static boolean isPreferencesLoaded;

    static {
        // Dark Styles
        sDarkThemes.append(PALETTE[0], R.style.AppTheme_Red);
        sDarkThemes.append(PALETTE[1], R.style.AppTheme_Pink);
        sDarkThemes.append(PALETTE[2], R.style.AppTheme_Purple);
        sDarkThemes.append(PALETTE[3], R.style.AppTheme_DeepPurple);
        sDarkThemes.append(PALETTE[4], R.style.AppTheme_Indigo);
        sDarkThemes.append(PALETTE[5], R.style.AppTheme_Blue);
        sDarkThemes.append(PALETTE[6], R.style.AppTheme_LightBlue);
        sDarkThemes.append(PALETTE[7], R.style.AppTheme_Cyan);
        sDarkThemes.append(PALETTE[8], R.style.AppTheme_Teal);
        sDarkThemes.append(PALETTE[9], R.style.AppTheme_Green);
        sDarkThemes.append(PALETTE[10], R.style.AppTheme_LightGreen);
        sDarkThemes.append(PALETTE[11], R.style.AppTheme_Lime);
        sDarkThemes.append(PALETTE[12], R.style.AppTheme_Yellow);
        sDarkThemes.append(PALETTE[13], R.style.AppTheme_Amber);
        sDarkThemes.append(PALETTE[14], R.style.AppTheme_Orange);
        sDarkThemes.append(PALETTE[15], R.style.AppTheme_DeepOrange);
        sDarkThemes.append(PALETTE[16], R.style.AppTheme_Brown);
        sDarkThemes.append(PALETTE[17], R.style.AppTheme_Grey);
        sDarkThemes.append(PALETTE[18], R.style.AppTheme_BlurGrey);
        sDarkThemes.append(PALETTE[19], R.style.AppTheme_Official);
        sDarkThemes.append(PALETTE[20], R.style.AppTheme_Black);

        // Light Styles
        sLightThemes.append(PALETTE[0], R.style.AppThemeLight_Red);
        sLightThemes.append(PALETTE[1], R.style.AppThemeLight_Pink);
        sLightThemes.append(PALETTE[2], R.style.AppThemeLight_Purple);
        sLightThemes.append(PALETTE[3], R.style.AppThemeLight_DeepPurple);
        sLightThemes.append(PALETTE[4], R.style.AppThemeLight_Indigo);
        sLightThemes.append(PALETTE[5], R.style.AppThemeLight_Blue);
        sLightThemes.append(PALETTE[6], R.style.AppThemeLight_LightBlue);
        sLightThemes.append(PALETTE[7], R.style.AppThemeLight_Cyan);
        sLightThemes.append(PALETTE[8], R.style.AppThemeLight_Teal);
        sLightThemes.append(PALETTE[9], R.style.AppThemeLight_Green);
        sLightThemes.append(PALETTE[10], R.style.AppThemeLight_LightGreen);
        sLightThemes.append(PALETTE[11], R.style.AppThemeLight_Lime);
        sLightThemes.append(PALETTE[12], R.style.AppThemeLight_Yellow);
        sLightThemes.append(PALETTE[13], R.style.AppThemeLight_Amber);
        sLightThemes.append(PALETTE[14], R.style.AppThemeLight_Orange);
        sLightThemes.append(PALETTE[15], R.style.AppThemeLight_DeepOrange);
        sLightThemes.append(PALETTE[16], R.style.AppThemeLight_Brown);
        sLightThemes.append(PALETTE[17], R.style.AppThemeLight_Grey);
        sLightThemes.append(PALETTE[18], R.style.AppThemeLight_BlurGrey);
        sLightThemes.append(PALETTE[19], R.style.AppThemeLight_Official);
        sLightThemes.append(PALETTE[20], R.style.AppTheme_Black);

        sDrawerHeaders.append(PALETTE[0], R.drawable.drawer_header_black);
        sDrawerHeaders.append(PALETTE[1], R.drawable.drawer_header_pink);
        sDrawerHeaders.append(PALETTE[2], R.drawable.drawer_header_purple);
        sDrawerHeaders.append(PALETTE[3], R.drawable.drawer_header_purple);
        sDrawerHeaders.append(PALETTE[4], R.drawable.drawer_header_pink);
        sDrawerHeaders.append(PALETTE[9], R.drawable.drawer_header_green);
        sDrawerHeaders.append(PALETTE[10], R.drawable.drawer_header_green);
        sDrawerHeaders.append(PALETTE[14], R.drawable.drawer_header_orange2);
        sDrawerHeaders.append(PALETTE[15], R.drawable.drawer_header_orange);
        sDrawerHeaders.append(PALETTE[19], R.drawable.drawer_header_black);
        sDrawerHeaders.append(PALETTE[20], R.drawable.drawer_header_black);

    }

    /**
     * Apply theme to activity, without {@link Activity#recreate()}
     * Loads only the main theme of style, you must call before {@link Activity#setContentView(View)}
     *
     * @param context this activity
     */
    public static void applyTheme(Activity context, boolean drawingStatusBar) {
        loadThemePreferences(context);

        // update language if needed...
        if (!sLocale.equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            Locale locale = new Locale(sLocale);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            AppLoader.appContext.getResources().updateConfiguration(config,
                    AppLoader.appContext.getResources().getDisplayMetrics());
        }

        context.setTheme(getStyle());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getWindow().setStatusBarColor(getThemeColorDark(context));
            context.getWindow().setNavigationBarColor(getThemeColorDark(context));

            if (drawingStatusBar) {
                Window window = context.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!drawingStatusBar) {
                context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                int statusBarHeight = AndroidUtils.getStatusBarHeight(context);
                View decorView = context.getWindow().getDecorView();
                View view = new View(context);
                view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight));
                view.setBackgroundColor(getThemeColor(context));
                ((ViewGroup) decorView).addView(view);
            }

//            KeyboardUtil keyboardUtil = new KeyboardUtil(context, context.findViewById(android.R.id.content));
        }
    }

    /**
     * Get current style app
     *
     * @see #updateColourTheme(int)
     */
    private static int getStyle() {
        return isDarkTheme() ? sDarkThemes.get(sColourTheme) : sLightThemes.get(sColourTheme);
    }

    public static void applyTheme(Activity activity) {
        applyTheme(activity, activity instanceof BasicActivity);
    }

    /**
     * Insert new theme color on Preferences
     *
     * @param newColourTheme The color for update
     */
    public static void updateColourTheme(int newColourTheme) {
        PrefManager.putString(PREF_KEY_THEME_COLOUR, String.valueOf(newColourTheme));
        sColourTheme = newColourTheme;
    }

    /**
     * Sets color for elements, depending on current theme.
     *
     * @deprecated View painted depending on the theme itself
     */
    @Deprecated
    public static void initViewsForTheme(Context context) {
        loadThemePreferences(context);
        if (context instanceof BaseThemedActivity) {
            boolean isDarkEnough = isColorDarkEnough(getThemeColor(context));

            BaseThemedActivity activity = (BaseThemedActivity) context;
            Toolbar toolbar = activity.getToolbar();
            if (toolbar == null) {
                return;
            }

//            toolbar.setBackgroundDrawable(new ColorDrawable(getThemeColor()));
            toolbar.setTitleTextColor(isDarkEnough ? sTextPrimaryDarkColor : sTextPrimaryLightColor);

            ViewUtil.setFilter(toolbar, isDarkEnough ? sTextPrimaryDarkColor : sTextPrimaryLightColor);

//            ViewUtil.setColors(activity.getMenu(), activity.getToolbar());
//            activity.invalidateOptionsMenu();

//            View statusBarView = activity.findViewById(R.id.statusBarBackground);
//            if (statusBarView != null) {
//                AndroidUtils.setStatusBarColor(activity, statusBarView);
//            }
        }
    }

    private static void loadThemePreferences(Context context) {
        if (!isPreferencesLoaded) {
            ResourcesLoader.loadColors(context);
            SharedPreferences prefs = AppLoader.getLoader().getPreferences();

            sLocale = prefs.getString(PREF_KEY_FORCED_LOCALE, Locale.getDefault().getLanguage());
            sMessageWallpaperPath = prefs.getString(PREF_KEY_MESSAGE_WALLPAPER_PATH, "");
            sColourTheme = Integer.parseInt(prefs.getString(PREF_KEY_THEME_COLOUR, String.valueOf(DEFAULT_COLOR)));
//          sAccentColor = Integer.parseInt(prefs.getString(PREF_KEY_COLOR_ACCENT, String.valueOf(DEFAULT_ACCENT_COLOR)));
            sIsDarkTheme = prefs.getBoolean(PREF_KEY_IS_DARK_THEME, false);
            sDrawerHeaderState = Integer.parseInt(prefs.getString(PREF_KEY_DRAWER_HEADER, String.valueOf(DRAWER_HEADER_DEFAULT)));

            sTextPrimaryDarkColor = ContextCompat.getColor(context, R.color.primary_text_default_material_dark);
            sTextPrimaryLightColor = ContextCompat.getColor(context, R.color.primary_text_default_material_light);

            sTextSecondaryDarkColor = ContextCompat.getColor(context, R.color.secondary_text_default_material_dark);
            sTextSecondaryLightColor = ContextCompat.getColor(context, R.color.secondary_text_default_material_light);

            isPreferencesLoaded = true;
        }
    }

    public static void updateThemeValues() {
        isPreferencesLoaded = false;
        sThemeColor = -1;
        sAccentColor = -1;

        loadThemePreferences(AppLoader.appContext);
    }

    /**
     * Checks whether the color is rather dark.
     * If so, it is best to show the letters more bright color
     *
     * @param color The color to check
     * @return True, if Color Dark Enough
     */
    public static boolean isColorDarkEnough(int color) {
        for (int i = 0; i < COLOURS.length; i++) {
            for (int j = 0; j < COLOURS[i].length; j++) {
                if (color == COLOURS[i][j]) {
                    return TEXT_MODE_COLORS[i][j] == 1;
                }
            }
        }
        return true;
    }

    /**
     * Darkens color on 35%
     *
     * @param color the color to darken
     * @return a new color which is darken of specified color
     */
    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f;
        color = Color.HSVToColor(hsv);
        return color;
    }

    /**
     * Lighten color on 10%
     *
     * @param color the color to lighten
     * @return a new color which is lighten of specified color
     */
    public static int lightenColor(int color) {
        return lightenColor(color, 1.1f);
    }

    /**
     * Lighten color
     *
     * @param color the color to lighten
     * @return a new color which is lighten of specified color
     */
    public static int lightenColor(int color, float lightFactor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= lightFactor;
        return Color.HSVToColor(hsv);
    }

    /**
     * Set alpha to color on 15%
     *
     * @param color the color to set alpha
     * @return a new color which is alpha of specified color
     */
    public static int alphaColor(int color) {
        return alphaColor(color, 0.85f);
    }

    /**
     * Set alpha to color
     *
     * @param color       the color to set alpha
     * @param alphaFactor the factor for alpha, range [0...1]
     * @return a new color which is alpha of specified color
     */
    public static int alphaColor(int color, float alphaFactor) {
        int alpha = Color.alpha(color);

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return Color.argb((int) (alpha * alphaFactor), red, green, blue);
    }

    public static void initDivider(ListView listView) {
        if (!PrefManager.getBoolean(SettingsFragment.KEY_SHOW_DIVIDER)) {
            listView.setDivider(null);
            return;
        }

        listView.setDivider(isDarkTheme() ?
                AndroidUtils.getDrawable(listView.getContext(), R.drawable.divider_dialogs_dark) :
                AndroidUtils.getDrawable(listView.getContext(), R.drawable.dialog_divider));
    }

    public static int getPrimaryTextColor() {
        loadThemePreferences(AppLoader.appContext);
        return isDarkTheme() ? sTextPrimaryDarkColor : sTextPrimaryLightColor;
    }

    public static int getPrimaryTextColorOnAccent(Context context) {
        loadThemePreferences(AppLoader.appContext);
        return isColorDarkEnough(getColorAccent(context)) ? sTextPrimaryDarkColor : sTextPrimaryLightColor;
    }

    public static int getPrimaryTextColorOnThemeColor(Context context) {
        loadThemePreferences(AppLoader.appContext);
        return isColorDarkEnough(getThemeColor(context)) ? sTextPrimaryDarkColor : sTextPrimaryLightColor;
    }

    public static int getSecondaryTextColor() {
        loadThemePreferences(AppLoader.appContext);
        return isDarkTheme() ? sTextSecondaryDarkColor : sTextSecondaryLightColor;
    }

    public static int getPrimaryDarkTextColor() {
        return sTextPrimaryDarkColor;
    }

    public static int getSecondaryDarkTextColor() {
        return sTextSecondaryDarkColor;
    }

    public static int getPrimaryLightTextColor() {
        return sTextPrimaryLightColor;
    }

    public static int getSecondaryLightTextColor() {
        return sTextSecondaryLightColor;
    }

    public static int getColorAccent(Context context) {
        loadThemePreferences(AppLoader.appContext);
//        return sAccentColor;

//        boolean isDarkTheme = isDarkTheme();
//        for (int i = 0; i < COLOURS.length; i++) {
//            for (int j = 0; j < COLOURS[i].length; j++) {
//                if (sColourTheme == COLOURS[i][j]) {
//                    return THEMES[i][isDarkTheme ? 1 : 3];
//                }
//            }
//        }
        if (sAccentColor == -1) {
            sAccentColor = ThemeUtils.getThemeAttrColor(context, R.attr.colorAccent);
        }
        return sAccentColor;
    }

    public static int getThemeColor(Context context) {
        loadThemePreferences(AppLoader.appContext);

//        boolean isDarkTheme = isDarkTheme();
//        for (int i = 0; i < COLOURS.length; i++) {
//            for (int j = 0; j < COLOURS[i].length; j++) {
//                if (sColourTheme == COLOURS[i][j]) {
//                    return THEMES[i][isDarkTheme ? 0 : 2];
//                }
//            }
//        }
        if (sThemeColor == -1) {
            sThemeColor = ThemeUtils.getThemeAttrColor(context, R.attr.colorPrimary);
        }
        return sThemeColor;
//        return sColourTheme;
    }

    public static int getThemeColorDark(Context context) {
        return ThemeUtils.getThemeAttrColor(context, R.attr.colorPrimaryDark);
    }

    public static int getPaletteColor() {
        return sColourTheme;
    }

    public static boolean isDarkTheme() {
        loadThemePreferences(AppLoader.appContext);
        return sIsDarkTheme;
    }

    public static boolean isLightTheme() {
        return !isDarkTheme();
    }

    public static void setDarkTheme(boolean newDarkThemeValue) {
        PrefManager.putBoolean(PREF_KEY_IS_DARK_THEME, newDarkThemeValue);
        sIsDarkTheme = newDarkThemeValue;
    }

    public static boolean isBlackThemeColor() {
        loadThemePreferences(AppLoader.appContext);
        return PALETTE[COLOUR_BLACK] == getThemeColor(AppLoader.appContext);
    }

    public static Drawable getDrawerHeader(Context context) {
        loadThemePreferences(AppLoader.appContext);

        Drawable resultHeader = null;
        switch (sDrawerHeaderState) {
            case DRAWER_HEADER_SOLID_BACKGROUND:
                resultHeader = new ColorDrawable(getThemeColor(context));
                break;
            case DRAWER_HEADER_DEFAULT:
                int drawerHeaderRed = sDrawerHeaders.get(sColourTheme);
                resultHeader = ContextCompat.getDrawable(AppLoader.appContext, drawerHeaderRed == 0 ? R.drawable.drawer_header : drawerHeaderRed);
                break;
        }
        return resultHeader;
    }

    public static String getWallpaperPath(Context context) {
        loadThemePreferences(context);
        return sMessageWallpaperPath;
    }

}
