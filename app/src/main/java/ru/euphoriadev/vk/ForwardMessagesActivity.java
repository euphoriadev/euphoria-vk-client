package ru.euphoriadev.vk;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.euphoriadev.vk.adapter.MessageAdapter;
import ru.euphoriadev.vk.adapter.MessageItem;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by user on 10.06.15.
 */
public class ForwardMessagesActivity extends BaseThemedActivity {

    Api api;
    ListView lvForwardMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lvForwardMessages = new ListView(this);
        lvForwardMessages.setPadding(0, AndroidUtils.getStatusBarHeight(this), 0, 0);
        lvForwardMessages.setDividerHeight(0);
        setContentView(lvForwardMessages);

        api = Api.get();
        getForwardMessages();

    }

    private void getForwardMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VKMessage> apiMessages = (ArrayList<VKMessage>) getIntent().getSerializableExtra("messages");
                    ArrayList<Integer> uids = new ArrayList<>();

                    for (VKMessage m : apiMessages) {
                        uids.add(m.uid);
                    }

                    ArrayList<VKUser> profiles = api.getProfiles(uids, null, null, null, null);
                    HashMap<Integer, VKUser> mapPhotos = new HashMap<>();
                    for (VKUser user : profiles) {
                        mapPhotos.put(user.user_id, user);
                    }

                    final ArrayList<MessageItem> messageItems = new ArrayList<>();
                    for (VKMessage m : apiMessages) {
                        VKUser user = mapPhotos.get(m.uid);
                        m.chat_id = 1;
                        m.read_state = true;
                        messageItems.add(new MessageItem(m, user));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lvForwardMessages.setAdapter(new MessageAdapter(ForwardMessagesActivity.this, messageItems, 0, 0));
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
