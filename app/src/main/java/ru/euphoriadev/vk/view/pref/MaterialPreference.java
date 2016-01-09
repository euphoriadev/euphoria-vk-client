package ru.euphoriadev.vk.view.pref;

import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 04.10.15.
 */
public class MaterialPreference extends Preference {

    public MaterialPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            TextView titleView = (TextView) view.findViewById(android.R.id.title);
            titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), android.R.attr.textColorPrimary));
        }

        ViewUtil.setTypeface(titleView);
        ViewUtil.setTypeface(summaryView);
    }
}
