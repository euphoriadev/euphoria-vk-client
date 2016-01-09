package ru.euphoriadev.vk.view.pref;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 04.10.15.
 */
public class MaterialCheckBoxPreference extends CheckBoxPreference {

    private TextView titleView;
    private TextView summaryView;
    private CheckBox checkBox;

    public MaterialCheckBoxPreference(Context context) {
        super(context);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        titleView = (TextView) view.findViewById(android.R.id.title);
        summaryView = (TextView) view.findViewById(android.R.id.summary);
        checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), android.R.attr.textColorPrimary));

            if (!isEnabled()) {
//                titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), android.R.attr.textColorSecondary));
                titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), android.R.attr.textColorSecondary));

            }
        }


        ViewUtil.setTypeface(titleView);
        ViewUtil.setTypeface(summaryView);
    }

}
