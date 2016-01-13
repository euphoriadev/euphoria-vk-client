package ru.euphoriadev.vk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import ru.euphoriadev.vk.adapter.FriendsAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.FileLogger;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.AndroidUtils;

import java.util.ArrayList;

/**
 * Created by user on 16.07.15.
 */
public class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int ORDER_BY_ONLINE = 1;
    private static final int ORDER_BY_OFFLINE = 2;

    private Account account;
    private Api api;
    private ThemeManagerOld tm;
    private ListView listView;
    private Activity activity;
    private TabLayout tabLayout;
    private FriendsAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<VKUser> friends;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        activity = getActivity();

        tm = ThemeManagerOld.get(activity);
        api = Api.get();

      //  tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout_friends);;
      //  tabLayout.setVisibility(View.GONE);
        listView = (ListView) rootView.findViewById(R.id.lvFriends);
        tm.initDivider(listView);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container_friends);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.BLACK);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VKUser item = (VKUser) parent.getItemAtPosition(position);

                Intent intent = new Intent(activity, MessageHistoryActivity.class);
                intent.putExtra("user_id", item.user_id);
                intent.putExtra("chat_id", 0L);
                intent.putExtra("photo_50", item.photo_50);
                intent.putExtra("fullName", item.toString());
                intent.putExtra("user_count", 0L);
                startActivity(intent);
            }
        });

        String[] items = getActivity().getResources().getStringArray(R.array.friends_sort_array);

        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((BasicActivity) getActivity()).addSpinnerAdapter(sAdapter, new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderBy(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setHasOptionsMenu(true);
        loadFriends(false);
        return rootView;
    }

    private void loadFriends(final boolean onlyUpdate) {
        if (!AndroidUtils.isInternetConnection(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_internet, Toast.LENGTH_LONG).show();
            return;
        }
        refreshLayout.setRefreshing(true);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    friends = api.getFriends(api.getUserId(), "hints", null,null, null, null);
                    if (onlyUpdate) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                        return;
                    }
                    adapter = new FriendsAdapter(activity, friends);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(adapter);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private void orderBy(int mode) {
        switch (mode) {
            case ORDER_BY_OFFLINE:
                adapter.filterByOffline();
                break;

            case ORDER_BY_ONLINE:
                adapter.filterByOnline();
                break;

            default: if (adapter != null) adapter.filter("");
        }
    }

    @Override
    public void onRefresh() {
        loadFriends(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu;
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        FileLogger.w("GroupsFragment", "onCreateMenu = " + searchView);
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
            searchView.setQueryHint(getString(R.string.search));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    adapter.filter(s);
                    return true;
                }
            });
        }


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.clear();
        }

        super.onDestroy();

    }
}
