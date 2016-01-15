package ru.euphoriadev.vk.view.pref;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.ViewGroup;
import android.widget.SeekBar;

import ru.euphoriadev.vk.PrefsFragment;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.PrefManager;
import ru.euphoriadev.vk.util.RefreshManager;
import ru.euphoriadev.vk.util.Refreshable;

/**
 * Created by user on 14.01.16.
 */
public class ProgressBarPreference extends MaterialPreference implements Refreshable {
    AppCompatSeekBar seekBar;

    public ProgressBarPreference(Context context) {
        super(context);

        seekBar = new AppCompatSeekBar(context);
        RefreshManager.registerForChangePreferences(this, PrefsFragment.KEY_BLUR_RADIUS);
    }

    @Override
    protected void onClick() {
        super.onClick();

        createAlert();
    }

    public AppCompatSeekBar getSeekBar() {
        return seekBar;
    }

    private void createAlert() {
        seekBar.setPadding(AndroidUtils.pxFromDp(getContext(), 16),
                AndroidUtils.pxFromDp(getContext(), 6),
                AndroidUtils.pxFromDp(getContext(), 16),
                AndroidUtils.pxFromDp(getContext(), 6));
        seekBar.setProgress(PrefManager.getInt(getKey()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getTitle());
        builder.setMessage(getTitle() + ": " + seekBar.getProgress());
        builder.setView(seekBar);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefManager.putInt(getKey(), seekBar.getProgress());

                if (getOnPreferenceChangeListener() != null) {
                    getOnPreferenceChangeListener().onPreferenceChange(ProgressBarPreference.this, seekBar.getProgress());
                }
                dialog.dismiss();

            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((ViewGroup) seekBar.getParent()).removeAllViews();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dialog.setMessage(getTitle() + ": " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onRefresh(String prefKey) {
        String value = PrefManager.getString(PrefsFragment.KEY_MAKING_DRAWER_HEADER);
        setEnabled(value.equalsIgnoreCase("2"));
    }
}
