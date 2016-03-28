package ru.euphoriadev.vk;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ru.euphoriadev.vk.adapter.MaterialsPageAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ThemeUtils;

/**
 * Created by user on 28.09.15.
 */
public class DialogMaterialsActivity extends BaseThemedActivity {

    private TabLayout tabLayout;
    private Drawable[] tabIcons;
    private Toolbar toolbar;
    private MaterialsPageAdapter adapter;
    private Api api;

    int chat_id;
    int user_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_materials_layout);

        toolbar = (Toolbar) findViewById(R.id.materials_toolbar);

        chat_id = getIntent().getIntExtra("chat_id", 0);
        user_id = getIntent().getIntExtra("user_id", 0);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.materials_from_talk));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new MaterialsPageAdapter(this, getSupportFragmentManager(), chat_id, user_id);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < tabIcons.length; i++) {
                    if (position != i) {
                        setFilterTabAt(i, 0.5f);
                    }
                }
                setFilterTabAt(position, 1.0f);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.materials_tablayout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(!ThemeManager.isDarkTheme() ? Color.WHITE : ThemeManager.darkenColor(ThemeUtils.getThemeAttrColor(this, android.R.attr.windowBackground)));
        tabLayout.setTabTextColors(ThemeManager.alphaColor(ThemeManager.getThemeColor(this), 0.5f), ThemeManager.getThemeColor(this));

        tabIcons = new Drawable[adapter.getCount()];
//        tabIcons[0] = ContextCompat.getDrawable(this, R.drawable.ic_photo);
//        tabIcons[1] = ContextCompat.getDrawable(this, R.drawable.ic_movie);
//        tabIcons[2] = ContextCompat.getDrawable(this, R.drawable.ic_audiotrack_white);
//        tabIcons[3] = ContextCompat.getDrawable(this, R.drawable.ic_folder);

        // Need to get new pics, not from the cache
        tabIcons[0] = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo));
        tabIcons[1] = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_movie));
        tabIcons[2] = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_audiotrack));
        tabIcons[3] = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_folder));

        // set filter to tab on first selected
        setFilterTabAt(0, 1.0f);
        for (int i = 0; i < tabIcons.length; i++) {
            AndroidUtils.setFilter(tabIcons[i], ThemeManager.alphaColor(ThemeManager.getThemeColor(this), 0.5f));
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }
    }

    private void setFilterTabAt(int position, float alphaFactor) {
        AndroidUtils.setFilter(tabIcons[position], ThemeManager
                .alphaColor(ThemeManager
                        .getThemeColor(DialogMaterialsActivity.this), alphaFactor));
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
