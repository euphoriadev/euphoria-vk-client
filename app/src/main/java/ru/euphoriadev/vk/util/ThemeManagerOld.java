package ru.euphoriadev.vk.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.SettingsFragment;

/**
 * Created by Igor on 27.03.15.
 *
 * Меденжер тем
 * Данный класс устарел, т.к тут много говнокода, С скором времени будет удален
 * {@link ThemeManager}
 */
@Deprecated
public class ThemeManagerOld {

    public static final String ROBOTO_LIGHT = "Roboto-Light.ttf";
    public static final String ROBOTO_REGULAR = "Roboto-Regular.ttf";
    public static final String DROID_REGULAR = "DroidSans.ttf";
    public static final String DROID_BOLD = "DroidSans-Bold.ttf";
    public static final String SYSTEM_FONT = "Default";
    private static ThemeManagerOld instance;
    private SharedPreferences sPrefs;
    private Context mContext;
    private String mColour;

    public ThemeManagerOld(Context context) {
        this.mContext = context;
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mColour = sPrefs.getString("colour_theme", "Red").toUpperCase();

    }

    public synchronized static ThemeManagerOld get(Context context) {
        return new ThemeManagerOld(context);
    }

    public ThemeManagerOld setBasicTheme() {
        AppLoader.getLoader().applyTheme((Activity) mContext);
        return this;

    }

    public void setBackgroundColor(int color) {
        ((Activity) mContext).getWindow().setBackgroundDrawable(new ColorDrawable(color));
    }

    public int[] getThemeColors() {
        return new int[]{
                getColor(R.color.md_indigo_500),
                getColor(R.color.md_red_500),
                getColor(R.color.md_orange_500),
                getColor(R.color.md_blue_grey_500),
                getColor(R.color.md_teal_500),
                getColor(R.color.md_pink_500),
                getColor(R.color.md_deep_orange_500),
                getColor(R.color.md_brown_500),
                getColor(R.color.md_green_500),
                getColor(R.color.md_grey_900),
                getColor(R.color.black),
        };
    }

    public void putColor(int color) {
        ThemeManager.updateColourTheme(color);
//        SharedPreferences.Editor editor = sPrefs.edit();
//        String result = "Red";
//
//        final int[] colors = getThemeColors();
//        final int currentColor = getCurrentColor();
//
//        if (colors[0] == color) result = "Indigo";
//        else if (colors[1] == color) result = "Red";
//        else if (colors[2] == color) result = "Orange";
//        else if (colors[3] == color) result = "Blur_grey";
//        else if (colors[4] == color) result = "Teal";
//        else if (colors[5] == color) result = "Pink";
//        else if (colors[6] == color) result = "Deep_orange";
//        else if (colors[7] == color) result = "Brown";
//        else if (colors[8] == color) result = "Green";
//        else if (colors[9] == color) result = "Dark";
//        else if (colors[10] == color) result = "Black";
//
//        editor.putString("colour_theme", result);
//        editor.apply();;
    }

    public int getCurrentColor() {
        return ThemeManager.getThemeColor(mContext);
//        if (mColour.equalsIgnoreCase("INDIGO")) return getColor(R.color.md_indigo_500);
//        else if (mColour.equalsIgnoreCase("RED")) return getColor(R.color.md_red_500);
//        else if (mColour.equalsIgnoreCase("ORANGE")) return getColor(R.color.md_orange_500);
//        else if (mColour.equalsIgnoreCase("BLUE_GREY")) return getColor(R.color.md_blue_grey_500);
//        else if (mColour.equalsIgnoreCase("TEAL")) return getColor(R.color.md_teal_500);
//        else if (mColour.equalsIgnoreCase("PINK")) return getColor(R.color.md_pink_500);
//        else if (mColour.equalsIgnoreCase("DEEP_ORANGE")) return getColor(R.color.md_deep_orange_500);
//        else if (mColour.equalsIgnoreCase("BROWN")) return getColor(R.color.md_brown_500);
//        else if (mColour.equalsIgnoreCase("GREEN")) return getColor(R.color.md_green_500);
//        else if (mColour.equalsIgnoreCase("DARK")) return getColor(R.color.md_grey_900);
//        else if (mColour.equalsIgnoreCase("BLACK")) return getColor(R.color.black);
//        else return getColor(R.color.md_red_500);
    }



    private int getColor(int resId) {
        return ContextCompat.getColor(mContext, resId);
    }

    private Drawable getDrawable(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mContext.getResources().getDrawable(resId, mContext.getTheme());
        } else {
            return mContext.getResources().getDrawable(resId);
        }
    }

    public Drawable getHeaderDrawable() {
        if (mColour.contains("ORANGE")) return getDrawable(R.drawable.drawer_header_orange2);
        else if (mColour.contains("PURPLE")) return getDrawable(R.drawable.drawer_header_purple);
        else if (mColour.equals("RED")) return getDrawable(R.drawable.drawer_header_black);
        else if (mColour.equals("BLACK")) return getDrawable(R.drawable.drawer_header_black);
     //   else if (mColour.equals("TEAL")) return getDrawable(R.drawable.drawer_header_teal);
        else if (mColour.equals("DEEP_ORANGE")) return getDrawable(R.drawable.drawer_header_orange2);
        else return getDrawable(R.drawable.drawer_header);
    }

    public void initDivider(ListView listView) {
        if (!isShowDivider()) {
            listView.setDivider(null);
            return;
        }

        listView.setDivider(isNightTheme() ?
                getDrawable(R.drawable.divider_dialogs_dark) :
                getDrawable(R.drawable.dialog_divider));
    }

    public int getBasicColorOfTheme() {
        return ThemeManager.getThemeColor(mContext);
//        return ThemeUtils.getThemeAttrColor(mContext, R.attr.colorPrimary);
    }

    public int getDarkBasicColorOfTheme() {
        return ThemeManager.darkenColor(ThemeManager.getThemeColor(mContext));
//        return ThemeUtils.getThemeAttrColor(mContext, R.attr.colorPrimaryDark);
    }

    public int getIndicatorColor() {
        return ThemeManager.getColorAccent(mContext);
//        int indicatorColor = 0;
//        switch (mColour) {
//            case "TEAL":
//                indicatorColor = getColor(R.color.md_red_400);
//                break;
//            case "INDIGO":
//                indicatorColor = getColor(R.color.md_pink_500);
//                break;
//            case "RED":
//                indicatorColor = getColor(R.color.md_teal_600);
//                break;
//            case "ORANGE":
//                indicatorColor = getColor(R.color.md_orange_800);
//                break;
//            case "BLUE_GREY":
//                indicatorColor = getColor(R.color.md_blue_grey_600);
//                break;
//            case "BLACK":
//                indicatorColor = getColor(R.color.white);
//                break;
//            case "DEEP_ORANGE":
//                indicatorColor = getColor(R.color.md_deep_orange_500);
//                break;
//
//            default:
//                indicatorColor = ThemeUtils.getThemeAttrColor(mContext, R.attr.colorAccent);
//
//        }

    }

    public int getFabColor() {
        return ThemeManager.getColorAccent(mContext);
//        if (mColour.contains("RED")) return getColor(R.color.md_teal_700);
//        else if (mColour.contains("TEAL")) return getColor(R.color.md_red_400);
//        else if (mColour.contains("INDIGO")) return getColor(R.color.md_pink_600);
//        return ThemeUtils.getThemeAttrColor(mContext, R.attr.colorAccent);

    }

    public String getFont() {
       return sPrefs.getString("font", ROBOTO_REGULAR);
    }

    public int getPrimaryTextColor() {
        return ThemeManager.getPrimaryTextColor();
//        int color;
//        if (isLight()) {
//            color = getColor(R.color.primary_text_default_material_light);
//        } else {
//            color = getColor(R.color.primary_text_default_material_dark);
//        }
//        return color;
    }

    public int getSecondaryTextColor() {
        return ThemeManager.getSecondaryTextColor();
//        int color;
//        if (isLight()) {
//            color = getColor(R.color.secondary_text_default_material_light);
//        } else {
//            color = getColor(R.color.secondary_text_default_material_dark);
//        }
//        return color;
    }

    @Deprecated
    public boolean isLight() {
        //   return mColour.contains("LIGHT");
        return !isNightTheme();
    }

    public boolean isTheme(String themeName) {
        return mColour.equalsIgnoreCase(themeName);
    }


    public boolean isSystemFont() {
        return getFont().equals("Default");
    }

    public boolean isNightTheme() {
        return ThemeManager.isDarkTheme();
//        return sPrefs.getBoolean("is_night_theme", true);
    }
    public boolean isBlackTheme() {
        return mColour.equalsIgnoreCase("BLACK");
    }
    public boolean isShowDivider() {
        return sPrefs.getBoolean(SettingsFragment.KEY_SHOW_DIVIDER, false);
    }

}
