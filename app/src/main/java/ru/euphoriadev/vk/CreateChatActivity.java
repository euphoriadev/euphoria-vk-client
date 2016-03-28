package ru.euphoriadev.vk;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.ChoiceUserAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.KException;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.sqlite.CursorBuilder;
import ru.euphoriadev.vk.sqlite.VKInsertHelper;
import ru.euphoriadev.vk.sqlite.VKSqliteHelper;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ArrayUtil;

//import android.support.v7.internal.widget.ThemeUtils;

/**
 * Created by Igor on 01.11.15.
 */
public class CreateChatActivity extends BaseThemedActivity {
    Api api;

    ListView lv;
    Toolbar toolbar;
    EditText et;
    ChoiceUserAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.creating_chat);

        View header = View.inflate(this, R.layout.chat_create_header, null);
        et = (EditText) header.findViewById(R.id.etCreateChat);

        lv = (ListView) findViewById(R.id.lvCreateChat);
        lv.addHeaderView(header);

        api = Api.get();
        loadUsers();
    }

    private void loadUsers() {
        if (!AndroidUtils.hasConnection(this)) {
            Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();
            return;
        }
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase database = DBHelper.get(CreateChatActivity.this).getWritableDatabase();

                    ArrayList<VKUser> friends = VKSqliteHelper.getAllFriends(database);
                    if (ArrayUtil.isEmpty(friends)) {
                        friends = api.getFriends(api.getUserId(), "hints", null, null, null, null);
                    }


                    adapter = new ChoiceUserAdapter(CreateChatActivity.this, friends);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lv.setAdapter(adapter);
                        }
                    });
                    ArrayList<VKUser> newFriends = api.getFriends(api.getUserId(), "hints", null, null, null, null);
                    if (!ArrayUtil.isEmpty(newFriends)) {
                        friends.clear();
                        friends.addAll(newFriends);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
                                adapter.notifyDataSetChanged();
                            }
                        });
                        updateUsers(database, friends);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add("Создать").setIcon(R.drawable.ic_done);
        MenuItemCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    private void createChat(final String title) {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    api.сreateChat(adapter.checkedUsers, title);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                } catch (IOException | JSONException | KException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateUsers(SQLiteDatabase database, ArrayList<VKUser> users) {
        Cursor cursor = CursorBuilder.create()
                .selectAllFrom(DBHelper.FRIENDS_TABLE)
                .where(DBHelper.USER_ID, Api.get().getUserId())
                .cursor(database);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                this.overridePendingTransition(R.anim.diagonaltranslate_right,
                        R.anim.diagonaltranslate_right2);
                break;

            default:
                if (adapter.checkedUsers.size() < 2) {
                    Toast.makeText(this, "Выберите хотя бы двух участников беседы", Toast.LENGTH_LONG).show();
                    break;
                }
                createChat(et.getText().toString());
        }
        return super.onOptionsItemSelected(item);
    }
}
