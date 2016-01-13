package ru.euphoriadev.vk.util;

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

import java.util.Locale;

import ru.euphoriadev.vk.BaseThemedActivity;
import ru.euphoriadev.vk.BasicActivity;
import ru.euphoriadev.vk.R;

/**
 * Created by Igor on 23.12.15.
 *
 * A Simple Theme Manager
 */
public class ThemeManager {
    public static final int DEFAULT_COLOR = 0xffe51c23; // Red 500
    public static final int DEFAULT_ACCENT_COLOR = 0xff00897b; // Teal 500

    public static final String PREF_KEY_THEME_COLOUR = "color_theme";
    public static final String PREF_KEY_COLOR_ACCENT = "color_accent";
    public static final String PREF_KEY_FORCED_LOCALE = "forced_locale";
    public static final String PREF_KEY_IS_DARK_THEME = "is_dark_theme";
    public static final String PREF_KEY_DRAWER_HEADER = "making_drawer_header";
    public static final String PREF_KEY_MESSAGE_WALLPAPER_PATH = "message_wallpaper_path";

    /** Drawer header states **/
    public static final int DRAWER_HEADER_DEFAULT = 0;
    public static final int DRAWER_HEADER_SOLID_BACKGROUND = 1;
    public static final int DRAWER_HEADER_BLUR_PHOTO = 2;

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
    private static final int COLOUR_BLACK = 19;
    /**
     * Colours, copied from http://www.google.com/design/spec/style/color.html#color-ui-color-palette
     */
    private static final int[][] COLOURS = {{
            // Red
            0xfffde0dc, 0xfff9bdbb, 0xfff69988, 0xfff36c60,
            0xffe84e40, 0xffe51c23, 0xffdd191d, 0xffd01716,
            0xffc41411, 0xffb0120a
    }, {    // Pink
            0xfffce4ec, 0xfff8bbd0, 0xfff48fb1, 0xfff06292,
            0xffec407a, 0xffe91e63, 0xffd81b60, 0xffc2185b,
            0xffad1457, 0xff880e4f
    }, {    // Purple
            0xfff3e5f5, 0xffe1bee7, 0xffce93d8, 0xffba68c8,
            0xffab47bc, 0xff9c27b0, 0xff8e24aa, 0xff7b1fa2,
            0xff6a1b9a, 0xff4a148c
    }, {    // Deep Purple
            0xffede7f6, 0xffd1c4e9, 0xffb39ddb, 0xff9575cd,
            0xff7e57c2, 0xff673ab7, 0xff5e35b1, 0xff512da8,
            0xff4527a0, 0xff311b92
    }, {    // Indigo
            0xffe8eaf6, 0xffc5cae9, 0xff9fa8da, 0xff7986cb,
            0xff5c6bc0, 0xff3f51b5, 0xff3949ab, 0xff303f9f,
            0xff283593, 0xff1a237e
    }, {    // Blue
            0xffe7e9fd, 0xffd0d9ff, 0xffafbfff, 0xff91a7ff,
            0xff738ffe, 0xff5677fc, 0xff4e6cef, 0xff455ede,
            0xff3b50ce, 0xff2a36b1
    }, {    // Light Blue
            0xffe1f5fe, 0xffb3e5fc, 0xff81d4fa, 0xff4fc3f7,
            0xff29b6f6, 0xff03a9f4, 0xff039be5, 0xff0288d1,
            0xff0277bd, 0xff01579b
    }, {    // Cyan
            0xffe0f7fa, 0xffb2ebf2, 0xff80deea, 0xff4dd0e1,
            0xff26c6da, 0xff00bcd4, 0xff00acc1, 0xff0097a7,
            0xff00838f, 0xff006064
    }, {    // Teal
            0xffe0f2f1, 0xffb2dfdb, 0xff80cbc4, 0xff4db6ac,
            0xff26a69a, 0xff009688, 0xff00897b, 0xff00796b,
            0xff00695c, 0xff004d40
    }, {    // Green
            0xffd0f8ce, 0xffa3e9a4, 0xff72d572, 0xff42bd41,
            0xff2baf2b, 0xff259b24, 0xff0a8f08, 0xff0a7e07,
            0xff056f00, 0xff0d5302
    }, {    // Light Green
            0xfff1f8e9, 0xffdcedc8, 0xffc5e1a5, 0xffaed581,
            0xff9ccc65, 0xff8bc34a, 0xff7cb342, 0xff689f38,
            0xff558b2f, 0xff33691e
    }, {    // Lime
            0xfff9fbe7, 0xfff0f4c3, 0xffe6ee9c, 0xffdce775,
            0xffd4e157, 0xffcddc39, 0xffc0ca33, 0xffafb42b,
            0xff9e9d24, 0xff827717
    }, {    // Yellow
            0xfffffde7, 0xfffff9c4, 0xfffff59d, 0xfffff176,
            0xffffee58, 0xffffeb3b, 0xfffdd835, 0xfffbc02d,
            0xfff9a825, 0xfff57f17
    }, {    // Amber
            0xfffff8e1, 0xffffecb3, 0xffffe082, 0xffffd54f,
            0xffffca28, 0xffffc107, 0xffffb300, 0xffffa000,
            0xffff8f00, 0xffff6f00
    }, {    // Orange
            0xfffff3e0, 0xffffe0b2, 0xffffcc80, 0xffffb74d,
            0xffffa726, 0xffff9800, 0xfffb8c00, 0xfff57c00,
            0xffef6c00, 0xffe65100
    }, {    // Deep Orange
            0xfffbe9e7, 0xffffccbc, 0xffffab91, 0xffff8a65,
            0xffff7043, 0xffff5722, 0xfff4511e, 0xffe64a19,
            0xffd84315, 0xffbf360c
    }, {    // Brown
            0xffefebe9, 0xffd7ccc8, 0xffbcaaa4, 0xffa1887f,
            0xff8d6e63, 0xff795548, 0xff6d4c41, 0xff5d4037,
            0xff4e342e, 0xff3e2723
    }, {    // Grey
            0xfffafafa, 0xfff5f5f5, 0xffeeeeee, 0xffe0e0e0,
            0xffbdbdbd, 0xff9e9e9e, 0xff757575, 0xff616161,
            0xff424242, 0xff212121, 0xff000000, 0xffffffff
    }, {    // Blue Grey
            0xffeceff1, 0xffeceff1, 0xffb0bec5, 0xff90a4ae,
            0xff78909c, 0xff607d8b, 0xff546e7a, 0xff455a64,
            0xff37474f, 0xff263238
    }, {
            0x000
    }};

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
            COLOURS[19][0]  // Full Black
    };

    /**
     * This configures whether the text is black (0) or white (1) for each color above.
     */
    private static final int[][] TEXT_MODE_COLORS = {{
            // Red
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Pink
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Purple
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Deep Purple
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Indigo
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Blue
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Light Blue
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Cyan
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Teal
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Green
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Light Green
            0, 0, 0, 0, 1, 1, 1, 1, 1, 1
    }, {    // Lime
            0, 0, 0, 0, 0, 0, 1, 1, 1, 1
    }, {    // Yellow
            0, 0, 0, 0, 0, 0, 0, 1, 1, 1
    }, {    // Amber
            0, 0, 0, 0, 0, 1, 1, 1, 1, 1
    }, {    // Orange
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Deep Orange
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Brown
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Grey
            0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0
    }, {    // Blue Grey
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {   // Full Black
            1,
    }};

    /**
     * Configures of Themes for set applications
     * <p/>
     * 0 index - colorPrimary for Dark theme
     * 1 index - colorAccent for Dark theme
     * <p/>
     * 2 index - colorPrimary for Light theme
     * 4 index - colorAccent for Light theme
     */
    public static final int[][] THEMES = new int[][]{{
            // Red
            COLOURS[COLOUR_RED][5], COLOURS[COLOUR_TEAL][5],
            COLOURS[COLOUR_RED][4], COLOURS[COLOUR_TEAL][4],
    }, {     // Pink
            COLOURS[COLOUR_PINK][5], COLOURS[COLOUR_CYAN][4],
            COLOURS[COLOUR_PINK][4], COLOURS[COLOUR_CYAN][4],
    }, {    // Purple
            COLOURS[COLOUR_PURPLE][5], COLOURS[COLOUR_PURPLE][4],
            COLOURS[COLOUR_PURPLE][4], COLOURS[COLOUR_PURPLE][4],
    }, {    // Deep Purple
            COLOURS[COLOUR_DEEP_PURPLE][6], COLOURS[COLOUR_DEEP_PURPLE][4],
            COLOURS[COLOUR_DEEP_PURPLE][4], COLOURS[COLOUR_DEEP_PURPLE][4],
    }, {    // Indigo
            COLOURS[COLOUR_INDIGO][5], COLOURS[COLOUR_PINK][4],
            COLOURS[COLOUR_INDIGO][4], COLOURS[COLOUR_PINK][3],
    }, {    // Blue
            COLOURS[COLOUR_BLUR][6], COLOURS[COLOUR_CYAN][4],
            COLOURS[COLOUR_BLUR][4], COLOURS[COLOUR_CYAN][3],
    }, {    // Light Blue
            COLOURS[COLOUR_LIGHT_BLUE][6], COLOURS[COLOUR_LIGHT_BLUE][4],
            COLOURS[COLOUR_LIGHT_BLUE][5], COLOURS[COLOUR_LIGHT_BLUE][3],
    }, {    // Cyan
            COLOURS[COLOUR_CYAN][6], COLOURS[COLOUR_PINK][5],
            COLOURS[COLOUR_CYAN][5], COLOURS[COLOUR_CYAN][4],
    }, {    // Teal
            COLOURS[COLOUR_TEAL][7], COLOURS[COLOUR_RED][5],
            COLOURS[COLOUR_CYAN][4], COLOURS[COLOUR_CYAN][4],
    }, {    // Green
            COLOURS[COLOUR_GREEN][7], COLOURS[COLOUR_YELLOW][6],
            COLOURS[COLOUR_GREEN][4], COLOURS[COLOUR_YELLOW][4],
    }, {    // Light Green
            COLOURS[COLOUR_LIGHT_GREEN][6], COLOURS[COLOUR_AMBER][5],
            COLOURS[COLOUR_LIGHT_GREEN][4], COLOURS[COLOUR_AMBER][4],
    }, {    // Lime
            COLOURS[COLOUR_LIME][5], COLOURS[COLOUR_RED][4],
            COLOURS[COLOUR_LIME][4], COLOURS[COLOUR_RED][4],
    }, {    // Yellow
            COLOURS[COLOUR_YELLOW][6], COLOURS[COLOUR_LIGHT_GREEN][5],
            COLOURS[COLOUR_YELLOW][5], COLOURS[COLOUR_LIGHT_GREEN][4],
    }, {    // Amber
            COLOURS[COLOUR_AMBER][6], COLOURS[COLOUR_RED][5],
            COLOURS[COLOUR_AMBER][5], COLOURS[COLOUR_RED][4],
    }, {    // Orange
            COLOURS[COLOUR_ORANGE][7], COLOURS[COLOUR_GREEN][5],
            COLOURS[COLOUR_ORANGE][5], COLOURS[COLOUR_GREEN][4],
    }, {    // Deep Orange
            COLOURS[COLOUR_DEEP_ORANGE][6], COLOURS[COLOUR_CYAN][5],
            COLOURS[COLOUR_DEEP_ORANGE][5], COLOURS[COLOUR_CYAN][4],
    }, {    // Brown
            COLOURS[COLOUR_BROWN][5], COLOURS[COLOUR_LIGHT_BLUE][9],
            COLOURS[COLOUR_BROWN][4], COLOURS[COLOUR_GREEN][9],
    }, {    // Grey
            COLOURS[COLOUR_GREY][6], COLOURS[COLOUR_GREY][0],
            COLOURS[COLOUR_GREY][4], COLOURS[COLOUR_GREY][0],
    }, {    // Blue Grey
            COLOURS[COLOUR_BLUE_GREY][6], COLOURS[COLOUR_YELLOW][0],
            COLOURS[COLOUR_BLUE_GREY][4], COLOURS[COLOUR_YELLOW][0],

    }, {    // Black
            COLOURS[COLOUR_BLACK][0], COLOURS[COLOUR_GREY][0],
            COLOURS[COLOUR_BLACK][0], COLOURS[COLOUR_GREY][0],

    }};

    /**
     * Style ids
     */
    private static final SparseIntArray sDarkThemes = new SparseIntArray(20);
    private static final SparseIntArray sLightThemes = new SparseIntArray(20);

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
        sDarkThemes.append(PALETTE[19], R.style.AppTheme_Black);

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
        sLightThemes.append(PALETTE[19], R.style.AppTheme_Black);

    }
    /** Drawer Headers res **/
    private static final SparseIntArray sDrawerHeaders = new SparseIntArray(20);
    static {
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
    }

    /**
     * Cached themes values
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

            KeyboardUtil keyboardUtil = new KeyboardUtil(context, context.findViewById(android.R.id.content));
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
        SharedPreferences.Editor editor = AppLoader.getLoader().getPreferences().edit();
        editor.putString(PREF_KEY_THEME_COLOUR, String.valueOf(newColourTheme));
        editor.apply();

        sColourTheme = newColourTheme;
    }


    public static void setDarkTheme(boolean newDarkThemeValue) {
        SharedPreferences.Editor editor = AppLoader.getLoader().getPreferences().edit();
        editor.putBoolean(PREF_KEY_IS_DARK_THEME, newDarkThemeValue);
        editor.apply();

        sIsDarkTheme = newDarkThemeValue;
    }

    /**
     * Sets color for elements, depending on current theme.
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
            SharedPreferences prefs = AppLoader.getLoader().getPreferences();

            sLocale = prefs.getString(PREF_KEY_FORCED_LOCALE, Locale.getDefault().getLanguage());
            sMessageWallpaperPath = prefs.getString(PREF_KEY_MESSAGE_WALLPAPER_PATH, "");
            sColourTheme = Integer.parseInt(prefs.getString(PREF_KEY_THEME_COLOUR, String.valueOf(DEFAULT_COLOR)));
//          sAccentColor = Integer.parseInt(prefs.getString(PREF_KEY_COLOR_ACCENT, String.valueOf(DEFAULT_ACCENT_COLOR)));
            sIsDarkTheme = prefs.getBoolean(PREF_KEY_IS_DARK_THEME, true);
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

    public static int getPrimaryTextColor() {
        loadThemePreferences(AppLoader.appContext);
        return isDarkTheme() ? sTextPrimaryDarkColor : sTextPrimaryLightColor;
    }

    public static int getPrimaryTextColorOnAccent(Context context) {
        loadThemePreferences(AppLoader.appContext);
        return isColorDarkEnough(getColorAccent(context)) ? sTextPrimaryDarkColor : sTextPrimaryLightColor;
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
