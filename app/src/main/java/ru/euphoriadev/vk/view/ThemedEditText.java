package ru.euphoriadev.vk.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import ru.euphoriadev.vk.util.ThemeManager;

/**
 * Created by user on 28.12.15.
 */
public class ThemedEditText extends AppCompatCheckBox {

    public ThemedEditText(Context context) {
        super(context);
    }

    public ThemedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(Context context) {

    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        for (int i = 0; i < state.length; i++) {
            if (state[i] == android.R.attr.state_focused) {
                getBackground().setColorFilter(ThemeManager.getThemeColor(getContext()), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }
}
