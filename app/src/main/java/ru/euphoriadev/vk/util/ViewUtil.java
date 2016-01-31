package ru.euphoriadev.vk.util;

import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by Igor on 19.12.15.
 * <p/>
 * A simple utils for {@link android.view.View}
 */
public class ViewUtil {
    /**
     * Views for update typefaces
     */
    private static final Set<TextView> sSetViews = Collections.newSetFromMap(new WeakHashMap<TextView, Boolean>());

    /**
     * Specify an optional color filter for the drawable
     *
     * @param view  A {@link View} which it is necessary to apply {@link ColorFilter}
     * @param color The ARGB source color used with the specified Porter-Duff mode
     */
    public static void setFilter(View view, int color) {
        ColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
        if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.getDrawable().setColorFilter(colorFilter);
        } else {
            Drawable drawable = view.getBackground();
            if (drawable != null) drawable.setColorFilter(colorFilter);
        }
    }

    /**
     * Specify an optional color filter for the drawable
     */
    public static void setFilter(ViewGroup group, int color) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                setFilter((ViewGroup) child, color);
            } else {
                setFilter(child, color);
            }
        }
    }

    public static Drawable setColorFilter(Drawable drawable, int color) {
        ColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        drawable.setColorFilter(colorFilter);
        return drawable;
    }

    /**
     * Set {@link Typeface} to {@link TextView} or child,
     * if font not in cache - puts it.
     * So you do not have to decode the font every time
     *
     * @param view         View to set Typeface
     * @param typefaceName name of the font, which is Assets folder
     */
    public static void setTypeface(TextView view, String typefaceName) {
        Typeface typeface = TypefaceManager.getTypeface(view.getContext(), typefaceName);

        if (view.getTypeface() != typeface) {
            view.setTypeface(typeface);
        }
        addViewToSet(view);
    }


    /**
     * Set {@link Typeface} to {@link TextView} or child,
     * if font not in cache - puts it.
     * So you do not have to decode the font every time
     *
     * @param view       View to set Typeface
     * @param fontFamily value of {@link TypefaceManager.FontFamily}
     * @param textWeight value of {@link TypefaceManager.TextWeight}
     */
    public static void setTypeface(TextView view, int fontFamily, int textWeight) {
        Typeface typeface = TypefaceManager.getTypeface(view.getContext(), fontFamily, textWeight);

        if (view.getTypeface() != typeface) {
            view.setTypeface(typeface);
            addViewToSet(view);
        }
    }

    /**
     * Get {@link Typeface} from Preferences,
     * and set to {@link TextView} or child,
     * if font not in cache - puts it.
     * So you do not have to decode the font every time
     *
     * @param view View to set Typeface
     */
    public static void setTypeface(TextView view) {
        setTypeface(view, TypefaceManager.getFontFamily(), TypefaceManager.getTextWeight());
    }

    /**
     * Set {@link Typeface} from Preferences,
     * to all {@link View} of {@link ViewGroup}
     *
     * @param group the group to set typeface
     */
    public static void setTypeface(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                ViewUtil.setTypeface((TextView) child);
            } else if (child instanceof ViewGroup) {
                setTypeface((ViewGroup) child);
            }
        }
    }

    /**
     * Set {@link} Typeface to {@link AlertDialog}
     */
    public static void setTypeface(AlertDialog alert, String title) {
        TextView alertMessage = (TextView) alert.findViewById(android.R.id.message);
        if (alertMessage != null) {
            setTypeface(alertMessage);
        }

        TextView alertTitle = (TextView) alert.findViewById(android.R.id.title);
        if (alertTitle != null) {
            setTypeface(alertTitle);
        } else {
            alert.setTitle(createTypefaceSpan(title));
        }
    }

    public static SpannableString createTypefaceSpan(CharSequence title) {
        Typeface typeface = TypefaceManager.getTypeface(AppLoader.appContext, TypefaceManager.getFontFamily(), TypefaceManager.getTextWeight());
        SpannableString newTitle = new SpannableString(title);
        newTitle.setSpan(new TypefaceSpan("", typeface), 0, newTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return newTitle;
    }

    /**
     * Get pressed color from color,
     * color will be a slightly "sunken"
     *
     * @return dark pressed color
     */
    public static int getPressedColor(int color) {
        return ThemeManager.darkenColor(color);
    }

    public static void setColors(Menu menu, Toolbar toolbar) {
        // Toolbar navigation icon
        final int textColor = ThemeManager.isColorDarkEnough(ThemeManager.getThemeColor(toolbar.getContext())) ?
                ThemeManager.getPrimaryDarkTextColor() :
                ThemeManager.getPrimaryLightTextColor();

        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(navigationIcon);
        }


        // Other icons
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            Drawable newIcon = menuItem.getIcon();
            if (newIcon != null) {
                newIcon.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
                menuItem.setIcon(newIcon);
            }
        }
    }

    /**
     * Update {@link Typeface} at all Views, which were changed this class
     */
    public static void refreshViewsForTypeface() {
        TypefaceManager.updateTypefaceValues();
        Log.w("ViewUtil", "Set size is = " + sSetViews.size());
        synchronized (sSetViews) {
            for (TextView tv : sSetViews) {
                if (tv == null) continue;
                setTypeface(tv);
            }
        }
    }

    private static void addViewToSet(TextView view) {
        synchronized (sSetViews) {
            if (!sSetViews.contains(view)) {
                sSetViews.add(view);
            }
        }
    }
}
