package ru.euphoriadev.vk;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ru.euphoriadev.vk.adapter.MessageAdapter;
import ru.euphoriadev.vk.adapter.MessageItem;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.async.ThreadExecutor;
import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by Igor on 13.11.15.
 */
public class ImportantMessagesActivity extends BaseThemedActivity {

    private ListView listView;
    private Toolbar toolbar;
    private MessageAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        ThemeManagerOld.get(this).setBasicTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.important_layout_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View shadow = findViewById(R.id.toolbarShadow);
            shadow.setVisibility(View.GONE);
        }
        listView = (ListView) findViewById(R.id.lvImportantMessages);
        toolbar = getToolbar();

//        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.important_messages);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadMessages();
    }

    private void loadMessages() {
        if (!AndroidUtils.hasConnection(this)) {
            Toast.makeText(this, R.string.check_internet, Toast.LENGTH_LONG).show();
            return;
        }
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // загружаем важные сообщений
                    ArrayList<VKMessage> vkMessages = Api.get().getMessages(0, 8, false, 200);
                    if (vkMessages.isEmpty()) {
                        return;
                    }

                    HashMap<Integer, VKUser> mapUsers = new HashMap<>();
                    for (int i = 0; i < vkMessages.size(); i++) {
                        VKMessage message = vkMessages.get(i);
                        message.chat_id = 1;
                        mapUsers.put(message.uid, null);
                    }

                    // находим юзеров по id сообщения
                    Set<Integer> uids = mapUsers.keySet();
                    ArrayList<VKUser> vkUsers = Api.get().getProfiles(uids, null, null, null, null);
                    for (int i = 0; i < vkUsers.size(); i++) {
                        VKUser user = vkUsers.get(i);
                        mapUsers.put(user.user_id, user);
                    }

                    final ArrayList<MessageItem> messageItems = new ArrayList<>();
                    for (int i = 0; i < vkMessages.size(); i++) {
                        messageItems.add(new MessageItem(vkMessages.get(i), mapUsers.get(vkMessages.get(i).uid)));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new MessageAdapter(ImportantMessagesActivity.this, messageItems, 0, 0);
                            listView.setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        adapter.unregisterLongPoll();
        // если история неуспела загрузится, то adapter == null
        if (adapter != null) {
            adapter.clear();
        }
    }
}
