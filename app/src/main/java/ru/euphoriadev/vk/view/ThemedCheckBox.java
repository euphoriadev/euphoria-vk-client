package ru.euphoriadev.vk.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import ru.euphoriadev.vk.common.ThemeManager;

/**
 * Created by user on 28.12.15.
 */
public class ThemedCheckBox extends AppCompatCheckBox {
    private OnCheckedChangeListener mChangeListener;

    public ThemedCheckBox(Context context) {
        super(context);
        init(context);
    }

    public ThemedCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        super.setOnCheckedChangeListener(listener);
        mChangeListener = listener;
    }


    private void init(final Context context) {
        setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mChangeListener != null)
                    mChangeListener.onCheckedChanged(buttonView, isChecked);

                int uncheckedId = android.support.v7.appcompat.R.drawable.abc_btn_check_to_on_mtrl_000;
                int checkedId = android.support.v7.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015;
                Drawable drawable = ContextCompat.getDrawable(getContext(), isChecked ? checkedId : uncheckedId);

                drawable.setColorFilter(isChecked ? ThemeManager.getThemeColor(getContext()) : Color.DKGRAY, PorterDuff.Mode.SRC_ATOP);

                setButtonDrawable(drawable);

            }
        });
    }
}
