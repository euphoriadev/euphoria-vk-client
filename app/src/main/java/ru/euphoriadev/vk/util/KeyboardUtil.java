package ru.euphoriadev.vk.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by user on 11.10.15.
 */
public class KeyboardUtil {
    private final String TAG = "KeyboardUtil";
    private View decorView;
    private View contentView;
    private float initialDpDiff = -1;
    //a small helper to allow showing the editText focus
    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Log.w(TAG, "onGlobalLayout");
            Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            decorView.getWindowVisibleDisplayFrame(r);

            // get the height diff as dp
            float heightDiffDp = UIUtils.convertPixelsToDp(decorView.getRootView().getHeight() - (r.bottom - r.top), decorView.getContext());

            // set the initialDpDiff at the beginning. (on my phone this was 73dp)
            if (initialDpDiff == -1) {
                initialDpDiff = heightDiffDp;
            }

            //if it could be a keyboard add the padding to the view
            if ((heightDiffDp - initialDpDiff) > 100) { // if more than 100 pixels, its probably a keyboard...
                // check if the padding is 0 (if yes set the padding for the keyboard)
//                if (contentView.getPaddingBottom() == 0) {
                    // set the padding of the contentView for the keyboard
                    contentView.setPadding(0, 0, 0, (int) UIUtils.convertDpToPixel((heightDiffDp - initialDpDiff), decorView.getContext()));
//                }
            } else {
                //check if the padding is != 0 (if yes reset the padding)
                if (contentView.getPaddingBottom() != 0) {
//                  reset the padding of the contentView
                    contentView.setPadding(0, 0, 0, 0);
                }
            }
//            Log.w(TAG, "Size: " + heightDiffDp);

            // некоторые клавиатуры, такие как Swype имеют
            // панель, которая появляется только при наборе.
            // поэтому необходимо считывать размера при каждом вызове onGlobalLayout
        }
    };

    public KeyboardUtil(Activity act, View contentView) {
        this.decorView = act.getWindow().getDecorView();
        this.contentView = contentView;

        //only required on newer android versions. it was working on API level 19
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    /**
     * Helper to hide the keyboard
     *
     * @param act
     */
    public static void hideKeyboard(Activity act) {
        if (act != null && act.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void enable() {
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void disable() {
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public static class UIUtils {
        /**
         * This method converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent px equivalent to dp depending on device density
         */
        public static float convertDpToPixel(float dp, Context context) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float px = dp * (metrics.densityDpi / 160f);
            return px;
        }

        /**
         * This method converts device specific pixels to density independent pixels.
         *
         * @param px      A value in px (pixels) unit. Which we need to convert into db
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent dp equivalent to px value
         */
        public static float convertPixelsToDp(float px, Context context) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = px / (metrics.densityDpi / 160f);
            return dp;
        }
    }
}