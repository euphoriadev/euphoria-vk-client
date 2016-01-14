package ru.euphoriadev.vk.view.pref;

import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

import ru.euphoriadev.vk.util.RefreshManager;
import ru.euphoriadev.vk.util.Refreshable;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 04.10.15.
 */
public class MaterialPreference extends Preference {

    private TextView titleView;
    private TextView summaryView;

    public MaterialPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        titleView = (TextView) view.findViewById(android.R.id.title);
        summaryView = (TextView) view.findViewById(android.R.id.summary);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), isEnabled() ? android.R.attr.textColorPrimary : android.R.attr.textColorSecondary));

//            titleView.setEnabled(isEnabled());
//            summaryView.setEnabled(isEnabled());
        }

        ViewUtil.setTypeface(titleView);
        ViewUtil.setTypeface(summaryView);
    }

}
