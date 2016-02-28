package ru.euphoriadev.vk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 14.02.16.
 */
public class CircleCheckBox extends CheckBox {
    private OnCheckedChangeListener changeListener;

    public CircleCheckBox(Context context) {
        super(context);
        init(context);
    }

    public CircleCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        setButtonDrawable(R.drawable.ic_vector_unselected);
        ViewUtil.setFilter(CircleCheckBox.this, ThemeManager.getSecondaryTextColor());
        setAlpha(0.5f);

        changeListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setButtonDrawable(R.drawable.ic_selected);
                    ViewUtil.setFilter(CircleCheckBox.this, ThemeManager.getThemeColor(context));
                    setAlpha(1f);
                } else {
                    buttonView.setButtonDrawable(R.drawable.ic_vector_unselected);
                    ViewUtil.setFilter(CircleCheckBox.this, ThemeManager.getSecondaryTextColor());
                    setAlpha(0.5f);
                }
            }
        };
        setOnCheckedChangeListener(changeListener);
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        if (changeListener == null) {
            changeListener = listener;
        }
        super.setOnCheckedChangeListener(listener);
    }
}
