package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.Arrays;

import ru.euphoriadev.vk.FriendsFragment;
import ru.euphoriadev.vk.R;

/**
 * Created by user on 18.02.16.
 */
public class FriendsPageAdapter extends FragmentPagerAdapter {
    public static final int POSITION_ALL = 0;
    public static final int POSITION_ONLINE = 1;
    public static final int POSITION_REQUEST = 2;
    public static final int POSITION_SUGGESTIONS = 3;

    private FriendsFragment[] fragments;
    private String[] titles;
    private FragmentManager fm;

    public FriendsPageAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.fm = fm;

        titles = context.getResources().getStringArray(R.array.tab_friends_array);
        fragments = new FriendsFragment[titles.length];
    }

    @Override
    public FriendsFragment getItem(int position) {
        Log.w("FriendsPageAdapter", "getItem " + position);
        final FriendsFragment instance = FriendsFragment.newInstance(position);;
        return fragments[position] = instance;
    }


    public FriendsFragment getFragmentAt(int position) {
        return fragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    public void clear() {
        for (FriendsFragment fragment : fragments) {
            fm.beginTransaction().remove(fragment).commit();
        }

        Arrays.fill(fragments, null);
        Arrays.fill(titles, null);
        fragments = null;
        titles = null;
    }
}
