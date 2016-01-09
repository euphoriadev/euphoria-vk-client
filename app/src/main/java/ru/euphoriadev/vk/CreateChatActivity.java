package ru.euphoriadev.vk;

import android.os.*;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.internal.widget.ThemeUtils;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import ru.euphoriadev.vk.adapter.ChoiceUserAdapter;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.KException;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.KeyboardUtil;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Igor on 01.11.15.
 */
public class CreateChatActivity extends BaseThemedActivity {
    ThemeManagerOld manager;
    Api api;

    ListView lv;
    Toolbar toolbar;
    EditText et;
    ChoiceUserAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_chat_activity);

        setStatusBarColor(ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary));

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        lv = (ListView) findViewById(R.id.lvCreateChat);
        et = (EditText) findViewById(R.id.etCreateChat);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(R.string.creating_chat);

        api = Api.get();

        loadUsers();
    }

    private void loadUsers() {
        if (!Utils.isInternetConnection(this)) {
            Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();
            return;
        }
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VKUser> friends = api.getFriends(api.getUserId(), "hints", null, null, null, null);
                    adapter = new ChoiceUserAdapter(CreateChatActivity.this, friends);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lv.setAdapter(adapter);
                        }
                    });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                this.overridePendingTransition(R.anim.diagonaltranslate_right,
                        R.anim.diagonaltranslate_right2);
                break;

            default:
                if (adapter.checkedUsers.isEmpty()) {
                    Toast.makeText(this, "Выберите хотя бы двух участников беседы", Toast.LENGTH_LONG).show();
                    break;
                }
                createChat(et.getText().toString());
        }
        return super.onOptionsItemSelected(item);
    }

    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//            AndroidBug5497Workaround.assistActivity(this);
//            KeyboardUtil keyboardUtil = new KeyboardUtil(this, this.findViewById(android.R.id.content));
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//
//            View statusBarView = new View(this);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight());
//            params.gravity = Gravity.TOP;
//            LinearLayout rootLayout = new LinearLayout(this);
//            rootLayout.setLayoutParams(params);
//            rootLayout.addView(statusBarView);
//
//            statusBarView.setLayoutParams(params);
//            statusBarView.setVisibility(View.VISIBLE);
//            ((ViewGroup) getWindow().getDecorView()).addView(rootLayout);
//            //status bar height
//         //   statusBarView.getLayoutParams().height = getStatusBarHeight();
//            statusBarView.setBackgroundColor(color);


            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//            getWindow().addFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


            //status bar height
            View statusBar = findViewById(R.id.statusBarBackground);
            statusBar.getLayoutParams().height = getStatusBarHeight();
            statusBar.setBackgroundColor(color);
        } else {
            findViewById(R.id.statusBarBackground).setVisibility(View.GONE);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
