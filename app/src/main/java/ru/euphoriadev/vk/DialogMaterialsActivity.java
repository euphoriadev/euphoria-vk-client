package ru.euphoriadev.vk;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ru.euphoriadev.vk.adapter.MaterialsPageAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.util.ThemeUtils;

/**
 * Created by user on 28.09.15.
 */
public class DialogMaterialsActivity extends BaseThemedActivity {

    TabLayout tabLayout;
    Toolbar toolbar;
    MaterialsPageAdapter adapter;
    Api api;

    int chat_id;
    int user_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_materials_layout);

        toolbar = (Toolbar) findViewById(R.id.materials_toolbar);
        tabLayout = (TabLayout) findViewById(R.id.materials_tablayout);

        chat_id = getIntent().getIntExtra("chat_id", 0);
        user_id = getIntent().getIntExtra("user_id", 0);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.materials_from_talk));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new MaterialsPageAdapter(this, getSupportFragmentManager(), chat_id, user_id);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(!ThemeManager.isDarkTheme() ? Color.WHITE : ThemeManager.darkenColor(ThemeUtils.getThemeAttrColor(this, android.R.attr.windowBackground)));
        tabLayout.setTabTextColors(ThemeManager.alphaColor(ThemeManager.getThemeColor(this), 0.5f), ThemeManager.getThemeColor(this));

        api = Api.get();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
