package ru.euphoriadev.vk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.GroupsAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKGroup;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.FileLogger;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ThreadExecutor;

/**
 * Created by user on 13.07.15.
 */
public class GroupsFragment extends Fragment {

    Activity activity;
    Account account;
    Api api;
    ThemeManagerOld tm;
    GroupsAdapter adapter;
    ListView listView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        activity = getActivity();

        tm = new ThemeManagerOld(activity);
        api = Api.get();

        listView = (ListView) rootView.findViewById(R.id.lvGroups);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VKGroup group = (VKGroup) parent.getItemAtPosition(position);

                if (adapter.isInMultiSelectMode()) {
                    adapter.toggleSelection(group);
                }
                getActivity().invalidateOptionsMenu();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                VKGroup group = (VKGroup) parent.getItemAtPosition(position);

                adapter.toggleSelection(group);
                getActivity().invalidateOptionsMenu();
                return true;
            }
        });
        ThemeManagerOld.get(getActivity()).initDivider(listView);

        setHasOptionsMenu(true);
        loadGroups();

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        Toolbar toolbar = ((BasicActivity) getActivity()).getToolbar();
        prepareToolbar(toolbar, menu);
        super.onPrepareOptionsMenu(menu);
    }

    private void prepareToolbar(Toolbar toolbar, Menu menu) {
        if (adapter == null) return;

        menu.clear();
        if (adapter.isInMultiSelectMode()) {
            getActivity().getMenuInflater().inflate(R.menu.main_menu, menu);
            toolbar.setTitle(String.format("%s selected group", adapter.getSelectedItems().size()));

            menu.findItem(R.id.menu_delete).setVisible(true);
            menu.findItem(R.id.action_search).setVisible(false);
        } else {
            inflateSearchView(getActivity().getMenuInflater(), menu);
            toolbar.setTitle(R.string.groups);

            menu.findItem(R.id.menu_delete).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                leaveFrom(adapter.getSelectedItems());
                adapter.disableMultiSelectMode();
                getActivity().invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (adapter.isInMultiSelectMode()) {
            adapter.disableMultiSelectMode();
        }
    }

    private void leaveFrom(final ArrayList<VKGroup> groups) {
        final ArrayList<VKGroup> vkGroups = new ArrayList<>(groups);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (VKGroup group : vkGroups) {
                    try {
                        Thread.sleep(300);
                        api.leaveGroup(group.gid);
                        adapter.remove(group);
                        updateListView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                vkGroups.clear();
                vkGroups.trimToSize();
            }
        });
    }

    private void updateListView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadGroups() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper helper = DBHelper.get(activity);
                    SQLiteDatabase database = helper.getWritableDatabase();

                    final ArrayList<VKGroup> listGroups = new ArrayList<>();
                    Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_GROUP_TABLE +
                            " LEFT JOIN " + DBHelper.GROUPS_TABLE +
                            " ON " + DBHelper.USER_GROUP_TABLE + "." + DBHelper.GROUP_ID +
                            " = " + DBHelper.GROUPS_TABLE + "." + DBHelper.GROUP_ID +
                            " WHERE " + DBHelper.USER_GROUP_TABLE + "." + DBHelper.USER_ID + " = " + api.getUserId(), null);

                    // Получаем все группы текущего пользователя
                    if (cursor.getCount() > 0)
                        while (cursor.moveToNext()) {
                            int gid = cursor.getInt(cursor.getColumnIndex(DBHelper.GROUP_ID));
                            int type = cursor.getInt(cursor.getColumnIndex(DBHelper.TYPE));
                            int membersCount = cursor.getInt(cursor.getColumnIndex(DBHelper.MEMBERS_COUNT));
//                          int is_closed = cursor.getInt(cursor.getColumnIndex(DBHelper.IS_CLOSED));
//                          int is_admin = cursor.getInt(cursor.getColumnIndex(DBHelper.IS_ADMIN));
//                          int admin_level = cursor.getInt(cursor.getColumnIndex(DBHelper.ADMIN_LEVER));
                            String name = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
                            String screen_name = cursor.getString(cursor.getColumnIndex(DBHelper.SCREEN_NAME));
//                          String description = cursor.getString(cursor.getColumnIndex(DBHelper.DESCRIPTION));
//                          String status = cursor.getString(cursor.getColumnIndex(DBHelper.STATUS));
                            String photo = cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_50));

                            VKGroup group = new VKGroup();
                            group.gid = gid;
                            group.type = type;
//                          group.is_closed = is_closed;
//                          group.is_admin = is_admin == 1;
//                          group.admin_level = admin_level;
                            group.name = name;
                            group.screen_name = screen_name;
                            group.members_count = membersCount;
//                          group.description = description;
//                          group.status = status;
                            group.photo_50 = photo;

                            listGroups.add(group);
                        }
                    cursor.close();

                    // заносим эти группы в адаптер и обновляем
                    adapter = new GroupsAdapter(activity, listGroups);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(adapter);
                        }
                    });

                    // Если есть подключение к интернету, то сначала удаляем список групп
                    // а потом на их место загружаем новые и заносим в базу данных
                    if (AndroidUtils.isInternetConnection(activity)) {
                        ArrayList<VKGroup> apiGroups = api.getGroups(api.getUserId());
                        if (!apiGroups.isEmpty()) {
                            Cursor c = database.rawQuery("SELECT * FROM " + DBHelper.USER_GROUP_TABLE + " WHERE user_id = " + api.getUserId(), null);
                            while (c.moveToNext()) {
                                int id = c.getInt(c.getColumnIndex(DBHelper._ID));
                                database.delete(DBHelper.USER_GROUP_TABLE, "_id = " + id, null);
                            }
                            c.close();
                        }

                        database.beginTransaction();
                        ContentValues cv = new ContentValues();
                        for (VKGroup g : apiGroups) {
                            cv.clear();

                            cv.put(DBHelper.USER_ID, api.getUserId());
                            cv.put(DBHelper.GROUP_ID, g.gid);
                            database.insert(DBHelper.USER_GROUP_TABLE, null, cv);
                            cv.remove(DBHelper.USER_ID);

                            cv.put(DBHelper.TYPE, g.type);
//                          cv.put(DBHelper.IS_CLOSED, g.is_closed);
//                          cv.put(DBHelper.IS_ADMIN, g.is_admin);
//                          cv.put(DBHelper.ADMIN_LEVER, g.admin_level);
                            cv.put(DBHelper.NAME, g.name);
                            cv.put(DBHelper.SCREEN_NAME, g.screen_name);
//                          cv.put(DBHelper.DESCRIPTION, g.description);
//                          cv.put(DBHelper.STATUS, g.status);
                            cv.put(DBHelper.MEMBERS_COUNT, g.members_count);
                            cv.put(DBHelper.PHOTO_50, g.photo_50);

                            if (database.update(DBHelper.GROUPS_TABLE, cv, "group_id = " + g.gid, null) == 0) {
                                database.insert(DBHelper.GROUPS_TABLE, null, cv);
                            }

                            // listGroups.add(g);
                        }
                        database.setTransactionSuccessful();
                        database.endTransaction();

                        listGroups.clear();
                        for (VKGroup group : apiGroups) {
                            listGroups.add(group);
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (listView.getAdapter() != null) {
                                    adapter.notifyDataSetChanged();
                                } else listView.setAdapter(adapter);
                            }
                        });
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflateSearchView(inflater, menu);

    }

    private void inflateSearchView(MenuInflater inflater, Menu menu) {
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
                    searchGroups(s);
                    return true;
                }
            });
        }
    }

    private void searchGroups(String q) {
        adapter.filter(q);
    }
}
