package ru.euphoriadev.vk.view.pref;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 04.10.15.
 */
public class MaterialSwitchPreference extends SwitchPreference {

    public MaterialSwitchPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (view instanceof ViewGroup) {
                setLayout((ViewGroup) view);
            }

            //  titleView.setTransformationMethod(null);
            // titleView.setTextSize(android.R.attr.textAppearanceSmall);
            titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), android.R.attr.textColorPrimary));

        }

        ViewUtil.setTypeface(titleView);

    }

    @SuppressLint("NewApi")
    private void setLayout(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        for (int n = 0; n < count; ++n) {
            View childView = viewGroup.getChildAt(n);
            if (childView instanceof Switch) {
                final Switch switchView = (Switch) childView;

                viewGroup.removeView(switchView);

                final SwitchCompat switchCompat = new SwitchCompat(getContext());
                viewGroup.addView(switchCompat);
                switchCompat.setFocusable(false);
                switchCompat.setClickable(false);
                switchCompat.setChecked(getSharedPreferences().getBoolean(getKey(), false));
                setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        switchCompat.setChecked(isChecked());
                        return true;
                    }
                });
                return;
            } else if (childView instanceof ViewGroup)
                setLayout((ViewGroup) childView);
        }
    }
}
