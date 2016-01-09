package ru.euphoriadev.vk.view.pref;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.view.View;
import android.widget.TextView;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 03.10.15.
 */
public class MaterialPreferenceCategory extends PreferenceCategory {

    public MaterialPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);

//        Toast.makeText(getContext(),String.valueOf(titleView.getTextSize()), Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            titleView.setTransformationMethod(null);
            titleView.setTextSize(14);
            titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), R.attr.colorAccent));
        }

        ViewUtil.setTypeface(titleView);
    }
}
