package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ViewUtil;

/**
 * Created by user on 23.12.15.
 */
public class BaseThemedActivity extends AppCompatActivity {
    private final String TAG = "BaseThemedActivity";
    private Toolbar mToolbar;
    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeManager.initViewsForTheme(this);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        Log.w(TAG, "onCreateOptionsMenu");
//        final Toolbar toolbar = getToolbar();
//        if (toolbar != null) {
//            ViewUtil.setColors(menu, toolbar);
//        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        Log.w(TAG, "onPrepareOptionsMenu");
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            ViewUtil.setColors(menu, toolbar);
        }
        return true;
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        getSupportActionBar().setTitle(titleId);
    }

    public void addSpinnerAdapter(ArrayAdapter<String> adapter, AdapterView.OnItemSelectedListener listener) {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.toolbarSpinner);
        spinner.setVisibility(View.VISIBLE);
        spinner.setOnItemSelectedListener(listener);
        spinner.setAdapter(adapter);

        ViewUtil.setTypeface(spinner);
    }

    public void hideSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.toolbarSpinner);
        spinner.setVisibility(View.GONE);
    }
}
