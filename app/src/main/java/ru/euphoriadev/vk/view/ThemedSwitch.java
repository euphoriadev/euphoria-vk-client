package ru.euphoriadev.vk.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import ru.euphoriadev.vk.util.ThemeManager;

/**
 * Created by user on 27.12.15.
 */
public class ThemedSwitch extends SwitchCompat {

    private Resources mRes;

    public ThemedSwitch(Context context) {
        super(context);
        init(context);
    }

    public ThemedSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mRes = context.getResources();

        DrawableCompat.setTintList(getThumbDrawable(), getSwitchThumbColorStateList());
//        DrawableCompat.setTintList(getTrackDrawable(), getSwitchTrackColorStateList());
    }


    private ColorStateList getSwitchThumbColorStateList() {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];

        // Disabled state
        states[0] = new int[]{-android.R.attr.state_enabled};
        colors[0] = (Color.DKGRAY);

        // Checked state
        states[1] = new int[]{android.R.attr.state_checked};
        colors[1] = ThemeManager.DEFAULT_COLOR;

        // Unchecked enabled state state
        states[2] = new int[0];
        colors[2] = (Color.WHITE);

        return new ColorStateList(states, colors);
    }

    private ColorStateList getSwitchTrackColorStateList() {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];

        // Disabled state
        states[0] = new int[]{-android.R.attr.state_enabled};
        colors[0] = Color.RED;

        // Checked state
        states[1] = new int[]{android.R.attr.state_checked};
        colors[1] = Color.argb(0x4D, // 30% alpha
                Color.red(ThemeManager.DEFAULT_COLOR),
                Color.green(ThemeManager.DEFAULT_COLOR),
                Color.blue(ThemeManager.DEFAULT_COLOR));

        // Unchecked enabled state state
        states[2] = new int[0];
        colors[2] = (Color.GRAY);

        return new ColorStateList(states, colors);
    }
}
