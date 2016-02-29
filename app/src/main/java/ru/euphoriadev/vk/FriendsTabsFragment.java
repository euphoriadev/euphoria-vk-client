package ru.euphoriadev.vk;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.euphoriadev.vk.adapter.FriendsPageAdapter;
import ru.euphoriadev.vk.common.ResourcesLoader;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.util.ThemeUtils;

/**
 * Created by user on 18.02.16.
 */
public class FriendsTabsFragment extends Fragment {

    FriendsPageAdapter adapter;
    private AppCompatActivity activity;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs_friends, container, false);

        activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(getResources().getString(R.string.friends));

        adapter = new FriendsPageAdapter(activity, activity.getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount());

        if (!ThemeManager.isDarkTheme()) {
            viewPager.setBackgroundColor(ResourcesLoader.getColor(R.color.md_grey_100));
        }


        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(ThemeManager.isBlackThemeColor() ? Color.WHITE : ThemeManager.getThemeColor(getActivity()));
        tabLayout.setBackgroundColor(!ThemeManager.isDarkTheme() ? Color.WHITE : ThemeManager.darkenColor(ThemeUtils.getThemeAttrColor(activity, android.R.attr.windowBackground)));
        tabLayout.setTabTextColors(ThemeManager.isBlackThemeColor() ? Color.WHITE : ThemeManager.alphaColor(ThemeManager.getThemeColor(activity), 0.5f), ThemeManager.isBlackThemeColor() ? Color.WHITE : ThemeManager.getThemeColor(activity));
//      not work =(
//       loadFriends();
        return rootView;
    }


//    public void notifyFragments(ArrayList<VKUser> friends) {
//        for (int i = 0; i < adapter.getCount(); i++) {
//            FriendsFragment fragment = adapter.getFragmentAt(i);
//            if (fragment != null) {
//                fragment.updateAdapter(friends);
//            }
//        }
//        adapter.notifyDataSetChanged();
//    }


//    public void loadFriends() {
//        ThreadExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    friends = Api.get().getFriends(Api.get().getUserId(), "hints", null, null, null, null);
//                    Log.w("FriendsAdapter", "size friends: " + friends.size());
//                    if (ArrayUtil.isEmpty(friends)) {
//                        return;
//                    }
//
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            notifyFragments(friends);
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((BasicActivity) getActivity()).hideSpinner();
        adapter.clear();
        System.gc();
    }
}
