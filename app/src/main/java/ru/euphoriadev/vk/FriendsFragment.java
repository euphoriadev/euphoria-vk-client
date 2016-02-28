package ru.euphoriadev.vk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.FriendsAdapter;
import ru.euphoriadev.vk.adapter.FriendsPageAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKFullUser;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ArrayUtil;
import ru.euphoriadev.vk.common.ThemeManager;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.util.VKInsertHelper;

/**
 * Created by Igor on 16.07.15.
 */
public class FriendsFragment extends AbstractFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int ORDER_BY_ONLINE = 1;
    private static final int ORDER_BY_OFFLINE = 2;
    int position;
    private ListView listView;
    private Activity activity;
    private FriendsAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<VKUser> friends;

    public static FriendsFragment newInstance(int position) {
        Log.i("FriendsFragment", "newInstance");
        Bundle args = new Bundle();
        args.putInt("position", position);

        FriendsFragment fragment = new FriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("FriendsFragment", "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        activity = getActivity();
        position = getArguments().getInt("position");

        listView = (ListView) rootView.findViewById(R.id.lvFriends);
        ThemeManager.initDivider(listView);

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

//        String[] items = getActivity().getResources().getStringArray(R.array.friends_sort_array);

//        ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, items);
//        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        ((BasicActivity) getActivity()).addSpinnerAdapter(sAdapter, new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                orderBy(position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        loadSuggestions();
//        setHasOptionsMenu(true);
        return rootView;
    }

    public void orderFriends() {
        switch (position) {
            case FriendsPageAdapter.POSITION_ONLINE:
                orderBy(ORDER_BY_ONLINE);
                break;
        }
    }

    public void loadSuggestions() {
        if (!AndroidUtils.isInternetConnection(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_internet, Toast.LENGTH_LONG).show();
            return;
        }

        if (position != FriendsPageAdapter.POSITION_ONLINE) {
            setRefreshing(true);
        }

        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = DBHelper.get(getActivity()).getWritableDatabase();

                switch (position) {
                    case FriendsPageAdapter.POSITION_ALL:
                        try {
                            friends = getUsersFrom(database);
                            if (!friends.isEmpty()) {
                                updateAdapter(friends);
                            }

                            friends = Api.get().getFriends(Api.get().getUserId(), "hints", null, null, null, null);
                            updateAdapter(friends);

                            updateUsers(database, friends);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case FriendsPageAdapter.POSITION_ONLINE:
                        ArrayList<VKUser> users = getOnlineFriendsFrom(database);
                        updateAdapter(users);
                        break;

                    case FriendsPageAdapter.POSITION_REQUEST:
                        ArrayList<VKUser> followers = getFollowersUsers();
                        updateAdapter(followers);
                        break;

                    case FriendsPageAdapter.POSITION_SUGGESTIONS:
                        try {
                            ArrayList<VKUser> suggestions = Api.get().getSuggestions("mutual", VKUser.FIELDS_DEFAULT);
                            updateAdapter(suggestions);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;


                }
                setRefreshing(false);
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

        }
    }

    public void updateAdapter(final ArrayList<VKUser> list) {
        if (ArrayUtil.isEmpty(list)) {
            return;
        }
        AndroidUtils.runOnUi(new Runnable() {
            @Override
            public void run() {
                if (adapter == null) {
                    adapter = new FriendsAdapter(getActivity(), list);
                    listView.setAdapter(adapter);

                    Log.w("FriendsFragment", "set adapter, position " + position + ", size " + list.size());
                } else {
                    if (!ArrayUtil.isEmpty(list)) {
                        adapter.notifyDataSetChanged();
                        Log.w("FriendsFragment", "update adapter, position " + position + ", size " + list.size());

                    }

                }
                orderFriends();
            }
        });
    }

    public ArrayList<VKUser> getUsersFrom(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.FRIENDS_TABLE +
                " WHERE " + DBHelper.USER_ID +
                " = " + Api.get().getUserId(), null);

        ArrayList<Integer> ids = new ArrayList<>();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(2);
                ids.add(id);
            }
        }
        cursor.close();

        return DBHelper.get(getActivity()).getUsersFromDB(ArrayUtil.toIntArray(ids));
    }

    public ArrayList<VKUser> getOnlineFriendsFrom(SQLiteDatabase database) {
        ArrayList<VKUser> onlineUsers = new ArrayList<>();
        ArrayList<VKUser> allUsers = getUsersFrom(database);

        if (ArrayUtil.isEmpty(allUsers)) {
            return onlineUsers;
        }

        for (int i = 0; i < allUsers.size(); i++) {
            VKUser user = allUsers.get(i);
            if (user.online) {
                onlineUsers.add(user);
            }
        }

        try {
            return onlineUsers;
        } finally {
            allUsers.clear();
            allUsers.trimToSize();
            allUsers = null;
        }
    }

    public ArrayList<VKUser> getFollowersUsers() {
        try {
            return Api.get().getFollowers(Api.get().getUserId(), 0, 1000, VKUser.FIELDS_DEFAULT, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUsers(SQLiteDatabase database, ArrayList<VKUser> users) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.FRIENDS_TABLE +
                " WHERE " + DBHelper.USER_ID +
                " = " + Api.get().getUserId(), null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int index = cursor.getInt(0);

                database.delete(DBHelper.FRIENDS_TABLE, "_id = ".concat(String.valueOf(index)), null);
            }
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        for (int i = 0; i < users.size(); i++) {
            cv.put(DBHelper.USER_ID, Api.get().getUserId());
            cv.put(DBHelper.FRIEND_ID, users.get(i).user_id);

            database.insert(DBHelper.FRIENDS_TABLE, null, cv);
        }
        VKInsertHelper.updateUsers(database, users, true);
    }

    @Override
    public void onRefresh() {
        adapter.clear();
        loadSuggestions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu;
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        }

        Log.w("GroupsFragment", "onCreateMenu = " + searchView);
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
        ((BasicActivity) getActivity()).hideSpinner();
//        if (adapter != null) {
//            adapter.clear();
//        }

        super.onDestroy();
    }

    @Override
    public void setRefreshing(final boolean refreshing) {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(refreshing);
            }
        });
    }
}
