package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.euphoriadev.vk.MaterialsFragment;
import ru.euphoriadev.vk.R;

/**
 * Created by Igor on 28.09.15.
 */
public class MaterialsPageAdapter extends FragmentPagerAdapter {

    public static final int POSITION_PICTURES = 0;
    public static final int POSITION_VIDEO = 1;
    public static final int POSITION_AUDIO = 2;
    public static final int POSITION_DOC = 3;
    private String[] tabs;
    private MaterialsFragment[] fragments;

    public MaterialsPageAdapter(Context context, FragmentManager fm) {
        super(fm);

        tabs = context.getResources().getStringArray(R.array.tabs_material_of_dialog_array);
        fragments = new MaterialsFragment[tabs.length];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public Fragment getItem(int position) {
        fragments[position] = MaterialsFragment.newInstance(position);
        return fragments[position];
    }

    public MaterialsFragment[] getFragments() {
        return fragments;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
