package ru.euphoriadev.vk;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.squareup.picasso.Picasso;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKChat;
import ru.euphoriadev.vk.api.model.VKFullUser;
import ru.euphoriadev.vk.util.Account;

import java.util.ArrayList;

/**
 * Created by user on 19.06.15.
 */
public class ChatSettingActivity extends AppCompatActivity {

    EditText tvTitle;
    ImageView ivPhoto;
    ListView lvUsers;

    Api api;
    Account account;

    long chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_settings_activity);

        tvTitle = (EditText) findViewById(R.id.tvChatTitle);
        ivPhoto = (ImageView) findViewById(R.id.tvChatIcon);
        lvUsers = (ListView) findViewById(R.id.chatUsers);

        tvTitle.setTextColor(Color.BLACK);

        chatId = getIntent().getLongExtra("chat_id", 0L);

        api = Api.get();

        tvTitle.setImeOptions(EditorInfo.IME_ACTION_DONE);
        tvTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    editChat(v.getText().toString());
                    Toast.makeText(ChatSettingActivity.this, "Название чата измененно", Toast.LENGTH_LONG).show();
                    return true;
            }
        });
        getChatInfo();
    }



    public void getChatInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final VKChat chat = api.getChat(chatId);
                    ArrayList<VKFullUser> chatUsers = api.getChatUsers(chatId, "photo_50");
                    final ArrayList<String> profiles = new ArrayList<String>();
                    for (VKFullUser user : chatUsers) {
                        profiles.add(user.first_name  + " " + user.last_name);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTitle.setText(chat.title);
                            Picasso.with(getApplicationContext()).load(chat.photo_100).into(ivPhoto);

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, profiles);
                            lvUsers.setAdapter(adapter);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void editChat(final String newTitle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    api.editChat(chatId, newTitle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
