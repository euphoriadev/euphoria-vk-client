package ru.euphoriadev.vk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.SparseArray;

/**
 * Created by Igor on 21.12.15.
 * <p/>
 * The Typeface manager for Fonts.
 * The main feature is the caching of fonts to save memory
 */

public class TypefaceManager {
    /** Cache of fonts for reuse */
    private static final SparseArray<Typeface> sTypefaceCache = new SparseArray<>();

    /** Preferences key for fonts */
    public static final String PREF_KEY_FONT_FAMILY = "font_family";
    public static final String PREF_KEY_TEXT_WEIGHT = "text_weight";

    /** Default Roboto fonts */
    public static final String ROBOTO_TINT = "Roboto-Thin.ttf";
    public static final String ROBOTO_LIGHT = "Roboto-Light.ttf";
    public static final String ROBOTO_REGULAR = "Roboto-Regular.ttf";
    public static final String ROBOTO_MEDIUM = "Roboto-Medium.ttf";
    public static final String ROBOTO_BOLD = "Roboto-Bold.ttf";

    /** Roboto Condensed fonts */
    public static final String ROBOTO_CONDENSED_LIGHT = "RobotoCondensed-Light.ttf";
    public static final String ROBOTO_CONDENSED_REGULAR = "RobotoCondensed-Regular.ttf";
    public static final String ROBOTO_CONDENSED_BOLD = "RobotoCondensed-Bold.ttf";

    /** Droid Sans fonts */
    public static final String DROID_REGULAR = "DroidSans.ttf";
    public static final String DROID_BOLD = "DroidSans-Bold.ttf";

    /** Default fonts */
    public static final String DEFAULT_FONT = "Default_font";
    public static final String DEFAULT_FONT_BOLD = "Default_font_Bold";


    /** Cached preferences value */
    private static int mFontFamily = -1;
    private static int mTextWeight = -1;

    /**
     * Obtain {@link Typeface} from Assets folder or cache
     * if cache not equal this typeface - put it
     * So you do not have to decode the font every time
     *
     * @param context      this activity or Context of {@link android.view.View} through which it can access for Res
     * @param typefaceName name of the font, which is Assets folder
     */
    public static Typeface getTypeface(Context context, String typefaceName) {
        if (TextUtils.isEmpty(typefaceName)) {
            return null;
        }
        switch (typefaceName) {
            case DEFAULT_FONT:
                return Typeface.DEFAULT;
            case DEFAULT_FONT_BOLD:
                return Typeface.DEFAULT_BOLD;
        }

        Typeface typeface = sTypefaceCache.get(typefaceName.hashCode());
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
            sTypefaceCache.append(typefaceName.hashCode(), typeface);
        }
        return typeface;
    }

    /**
     * Obtain {@link Typeface}
     *
     * @param context    this activity or Context of {@link android.view.View} through which it can access for Res
     * @param fontFamily value of {@link FontFamily}
     * @param textWeight value of {@link TextWeight}
     */
    public static Typeface getTypeface(Context context, int fontFamily, int textWeight) {
        String typefaceName = "";
        switch (fontFamily) {
            case FontFamily.ROBOTO:
                switch (textWeight) {
                    case TextWeight.THIN: typefaceName = ROBOTO_TINT; break;
                    case TextWeight.LIGHT: typefaceName = ROBOTO_LIGHT; break;
                    case TextWeight.NORMAL: typefaceName = ROBOTO_REGULAR; break;
                    case TextWeight.MEDIUM: typefaceName = ROBOTO_MEDIUM; break;
                    case TextWeight.BOLD: typefaceName = ROBOTO_BOLD; break;
                }
                break;

            case FontFamily.ROBOTO_CONDENSED:
                switch (textWeight) {
                    case TextWeight.THIN:
                    case TextWeight.LIGHT:
                        typefaceName = ROBOTO_CONDENSED_LIGHT;
                        break;
                    case TextWeight.NORMAL:
                        typefaceName = ROBOTO_CONDENSED_REGULAR;
                        break;
                    case TextWeight.MEDIUM:
                    case TextWeight.BOLD:
                        typefaceName = ROBOTO_CONDENSED_BOLD;
                        break;
                }
                break;

            case FontFamily.DROID_SANS:
                switch (textWeight) {
                    case TextWeight.THIN:
                    case TextWeight.LIGHT:
                    case TextWeight.NORMAL:
                    case TextWeight.MEDIUM:
                        typefaceName = DROID_REGULAR;
                        break;
                    case TextWeight.BOLD:
                        typefaceName = DROID_BOLD;
                        break;
                }

            case FontFamily.SYSTEM_FONT:
                switch (textWeight) {
                    case TextWeight.THIN:
                    case TextWeight.LIGHT:
                    case TextWeight.NORMAL:
                    case TextWeight.MEDIUM:
                        typefaceName = DEFAULT_FONT;
                        break;
                    case TextWeight.BOLD:
                        typefaceName = DEFAULT_FONT_BOLD;
                        break;
                }

                break;
        }
        return getTypeface(context, typefaceName);
    }

    /**
     * Get bold {@link Typeface}
     * Algorithm is based on the fact that the weight
     * of font is increased by 1
     */
    public static Typeface getBoldTypeface(Context context) {
        int fontFamily = getFontFamily();
        int textWeight = getTextWeight();

        return TypefaceManager.getTypeface(context, fontFamily, textWeight != TextWeight.BOLD && textWeight != 0 ? textWeight + 1 : textWeight);

    }

    /**
     * Get Font Family value from {@link SharedPreferences}
     */
    public static int getFontFamily() {
        if (mFontFamily == -1) {
            SharedPreferences preferences = AppLoader.getLoader().getPreferences();
            mFontFamily = Integer.parseInt(preferences.getString(PREF_KEY_FONT_FAMILY, String.valueOf(FontFamily.ROBOTO)));
        }
        return mFontFamily;
    }

    /**
     * Get Text Weight value from {@link SharedPreferences}
     */
    public static int getTextWeight() {
        if (mTextWeight == -1) {
            SharedPreferences preferences = AppLoader.getLoader().getPreferences();
            mTextWeight = Integer.parseInt(preferences.getString(PREF_KEY_TEXT_WEIGHT, String.valueOf(TextWeight.NORMAL)));
        }
        return mTextWeight;
    }

    /**
     * Update cached typeface values for new
     */
    public static void updateTypefaceValues() {
        mTextWeight = -1;
        mFontFamily = -1;

        getTextWeight();
        getFontFamily();
    }


    /**
     * Available fonts family on assets folder
     */
    public class FontFamily {
        public static final int ROBOTO = 0;
        public static final int ROBOTO_CONDENSED = 1;
        public static final int DROID_SANS = 2;
        public static final int SYSTEM_FONT = 3;
    }

    /**
     * Styles of font
     */
    public class TextWeight {
        public static final int NORMAL = 0;
        public static final int THIN = 1;
        public static final int LIGHT = 2;
        public static final int MEDIUM = 3;
        public static final int BOLD = 4;
    }


}
