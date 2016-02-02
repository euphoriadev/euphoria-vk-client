package ru.euphoriadev.vk;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;

import ru.euphoriadev.vk.adapter.MaterialsPageAdapter;
import ru.euphoriadev.vk.adapter.MessageAdapter;
import ru.euphoriadev.vk.adapter.MessageItem;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAttachment;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.ThreadExecutor;

/**
 * Created by user on 28.09.15.
 */
public class DialogMaterialsActivity extends BaseThemedActivity {

    TabLayout tabLayout;
    Toolbar toolbar;
    MessageAdapter messageAdapter;
    MaterialsPageAdapter adapter;
    Api api;

    int chat_id;
    int user_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_materials_layout);

        toolbar = (Toolbar) findViewById(R.id.materials_toolbar);
        tabLayout = (TabLayout) findViewById(R.id.materials_tablayout);

        chat_id = getIntent().getIntExtra("chat_id", 0);
        user_id = getIntent().getIntExtra("user_id", 0);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.materials_from_talk));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new MaterialsPageAdapter(this, getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);

        ArrayList<MessageItem> messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages, user_id, chat_id);

        api = Api.get();
        loadAtts();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public MessageAdapter getAdapter() {
        return messageAdapter;
    }

    public void loadAtts() {
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = 0;
                    while (true) {
                        // скачиваем 1.5 сообщений
                        ArrayList<VKMessage> vkMessages = api.getMessagesHistoryWithExecute(user_id, chat_id, 0, count);
                        if (vkMessages.isEmpty()) {
                            // если offset > общего кол-во, то список будет пуст
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getSupportActionBar().setSubtitle("done");
                                }
                            });
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getSupportActionBar().setSubtitle("loading...");
                            }
                        });
                        count = count + vkMessages.size();

                        for (final MaterialsFragment f : adapter.getFragments()) {

                            ArrayList<MessageItem> messages = f.getAdapter().getMessages();
                            VKUser user = new VKUser();
                            for (VKMessage m : vkMessages) {
                                ArrayList<VKAttachment> atts = m.attachments;
                                if (!atts.isEmpty()) {
                                    for (VKAttachment att : atts) {
                                        switch (att.type) {
                                            case VKAttachment.TYPE_PHOTO:
                                                if (f.getPosition() == MaterialsPageAdapter.POSITION_PICTURES) {
                                                    messages.add(new MessageItem(m, user));
                                                }
                                                break;

                                            case VKAttachment.TYPE_DOC:
                                                if (f.getPosition() == MaterialsPageAdapter.POSITION_DOC) {
                                                    messages.add(new MessageItem(m, user));
                                                }
                                                break;

                                            case VKAttachment.TYPE_AUDIO:
                                                if (f.getPosition() == MaterialsPageAdapter.POSITION_AUDIO) {
                                                    messages.add(new MessageItem(m, user));
                                                }
                                                break;


                                            case VKAttachment.TYPE_VIDEO:
                                                if (f.getPosition() == MaterialsPageAdapter.POSITION_VIDEO) {
                                                    messages.add(new MessageItem(m, user));
                                                }
                                                break;
                                        }
                                    }
                                }

                            }
                            f.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    f.getAdapter().notifyDataSetChanged();
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
