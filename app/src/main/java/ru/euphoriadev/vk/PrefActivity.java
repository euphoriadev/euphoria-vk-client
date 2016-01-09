package ru.euphoriadev.vk;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import ru.euphoriadev.vk.util.AppLoader;
import ru.euphoriadev.vk.util.Utils;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by Igor on 09.02.15.
 */
public class PrefActivity extends BaseThemedActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
//        AppLoader.getLoader().applyTheme(PrefActivity.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefs_activity);

        getSupportActionBar().setTitle(R.string.prefs);

//        TextView tvTitleToolbar = (TextView) findViewById(R.id.toolbarTitle);
//        tvTitleToolbar.setText(R.string.prefs);

        ViewUtil.setTypeface(getToolbar());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        View statusBarView = findViewById(R.id.statusBarPrefs);
        Utils.setStatusBarColor(this, statusBarView);

        getFragmentManager().beginTransaction()
                .replace(R.id.container_prefs, new PrefsFragment()).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
