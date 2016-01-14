package ru.euphoriadev.vk.view.pref;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.view.View;
import android.widget.TextView;

import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Шпщк on 03.10.15.
 */
public class MaterialListPreference extends ListPreference {
    protected String summary;
    private Activity activity;

    public MaterialListPreference(Activity context) {
        super(context, null);
        this.activity = context;
    }


    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        this.summary = summary.toString();
    }


    @Override
    protected void onClick() {
        showDialog(null);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        }



    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            titleView.setTextColor(ThemeUtils.getThemeAttrColor(getContext(), android.R.attr.textColorPrimary));
        }

        ViewUtil.setTypeface(titleView);
        ViewUtil.setTypeface(summaryView);
    }

    //    protected void updatePreference(CharSequence value) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        SharedPreferences.Editor editor = preferences.edit();
//
//        editor.putString(getKey(), value.toString());
//        editor.commit();
//    }

}
