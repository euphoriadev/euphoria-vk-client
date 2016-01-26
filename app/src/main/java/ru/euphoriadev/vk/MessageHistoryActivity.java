package ru.euphoriadev.vk;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.euphoriadev.vk.adapter.BaseArrayAdapter;
import ru.euphoriadev.vk.adapter.ChatMember;
import ru.euphoriadev.vk.adapter.ChatMemberAdapter;
import ru.euphoriadev.vk.adapter.MessageAdapter;
import ru.euphoriadev.vk.adapter.MessageCursorAdapter;
import ru.euphoriadev.vk.adapter.MessageItem;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKAttachment;
import ru.euphoriadev.vk.api.model.VKFullUser;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.service.LongPollService;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.Encrypter;
import ru.euphoriadev.vk.util.FileLogger;
import ru.euphoriadev.vk.util.PrefManager;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThemeUtils;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.VKInsertHelper;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.util.YandexTranslator;
import ru.euphoriadev.vk.view.fab.FloatingActionButton;

/**
 * Created by Igor on 15.03.15.
 */

public class MessageHistoryActivity extends BaseThemedActivity {

    private ListView lvHistory;
    private EditText etMessageText;
    private ArrayList<MessageItem> history;
    private MessageCursorAdapter cursorAdapter;
    private MessageAdapter adapter;

    private SQLiteDatabase database;
    private DBHelper helper;
    private ExecutorService executor;

    private Api api;
    private int uid;
    private int chat_id;
    private long lastTypeNotification;
    private boolean forceClose;
    private boolean hideTyping;
    private boolean isButtonPositionOflistView;
    private boolean isLoadingOldMessages = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_history_activity);

        String fullName = getIntent().getExtras().getString("fullName");
        uid = getIntent().getExtras().getInt("user_id");
        chat_id = getIntent().getExtras().getInt("chat_id");
        int users_count = getIntent().getExtras().getInt("users_count");
        boolean isOnline = getIntent().getExtras().getBoolean("online");
        boolean from_saved = getIntent().getExtras().getBoolean("from_saved", false);
        boolean from_service = getIntent().getExtras().getBoolean("from_sarvice", false);

        api = Api.get();
        if (from_service) LongPollService.messageCount = 0;

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        hideTyping = sPrefs.getBoolean("hide_typing", false);

        lvHistory = (ListView) findViewById(R.id.lvHistory);
        lvHistory.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        lvHistory.setStackFromBottom(true);

        etMessageText = (EditText) findViewById(R.id.messageText);
        etMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!hideTyping && System.currentTimeMillis() - lastTypeNotification > 5000)
                    setTyping();
            }
        });
        ViewUtil.setTypeface(etMessageText);

        FloatingActionButton fabSend = (FloatingActionButton) findViewById(R.id.fabMessageSebd);
        fabSend.setColorNormal(ThemeUtils.getThemeAttrColor(this, R.attr.colorAccent));
        fabSend.setColorPressed(ViewUtil.getPressedColor(fabSend.getColorNormal()));
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(etMessageText.getText().toString());
            }
        });
        if (!PrefManager.getBoolean(PrefsFragment.KEY_USE_CAT_ICON_SEND)) {
            fabSend.setImageResource(R.drawable.ic_keyboard_arrow_right);
        }

        ViewUtil.setFilter(fabSend, ThemeManager.getPrimaryTextColorOnAccent(this));

        etMessageText.getBackground().setColorFilter(ThemeManager.isDarkTheme() ? Color.parseColor("#424242") : Color.WHITE, PorterDuff.Mode.MULTIPLY);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitleTextAppearance(this, android.R.attr.textAppearanceSmall);
        toolbar.setSubtitleTextColor(Color.WHITE);

        final View vshadow = findViewById(R.id.vshadow_history);

//        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(fullName);
        if (chat_id != 0) {
            getSupportActionBar().setSubtitle(users_count + " " + (getResources().getString(R.string.members)));
        } else {
            getSupportActionBar().setSubtitle(isOnline ? getResources().getString(R.string.online) : getResources().getString(R.string.offline));
        }
        ViewUtil.setTypeface(toolbar);

        lvHistory.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (adapter == null) {
                    return;
                }
                if (scrollState == SCROLL_STATE_IDLE) {
                    adapter.isScrolling = false;
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.isScrolling = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // Если находимся в конце списка
                if (visibleItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
                    // то скрываем тень
                    isButtonPositionOflistView = true;
                    if (vshadow.getVisibility() == View.VISIBLE) vshadow.setVisibility(View.GONE);
                } else {
                    isButtonPositionOflistView = false;
                    // а когда начинаем прокручивать, то она появляется
                    if (vshadow.getVisibility() == View.GONE) vshadow.setVisibility(View.VISIBLE);
                }

//                // если находися на 10 position списка - грузим старые сообщеньки
                if (!isLoadingOldMessages && adapter != null && firstVisibleItem <= 20) {
                    isLoadingOldMessages = true;
                    getOldMessages(30, adapter.getCount());
                }
//                Log.w("ListView", "firstVisibleItem - " + firstVisibleItem + ", visibleItemCount " + visibleItemCount);

            }
        });
        lvHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MessageItem item = (MessageItem) parent.getItemAtPosition(position);
                adapter.toggleSelection(item);
                invalidateOptionsMenu();
                return true;
            }
        });

        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MessageItem messageItem = (MessageItem) parent.getItemAtPosition(position);

                if (adapter.isInMultiSelectMode()) {
                    adapter.toggleSelection(messageItem);
                    return;
                }

                makeMessageOptionsDialog(messageItem);
            }

        });


        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMessageText.getText().toString().length() != 0)
                    sendMessage(etMessageText.getText().toString().trim());
            }
        });
        fabSend.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String decryptMessage = encrypt(etMessageText.getText().toString().trim());
                sendMessage(decryptMessage);
                return true;
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chat_id != 0) getChatMembers();

            }
        });

        executor = Executors.newSingleThreadExecutor();
        getMessages(from_saved);
        loadWallpaperFromSD();
    }

    private void makeMessageOptionsDialog(final MessageItem messageItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageHistoryActivity.this);
        builder.setItems(R.array.message_dialog_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Переслать
                        break;
                    case 1:
                        // Копировать
                        AndroidUtils.copyTextToClipboard(MessageHistoryActivity.this, messageItem.message.body);
                        Toast.makeText(MessageHistoryActivity.this, R.string.message_copied, Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        // Расшифровать
                        String decryptMessage = decryptMessage(messageItem.message.body);
                        // если расшифровывание прошло удачно
                        if (decryptMessage != null) {
                            messageItem.message.body = decryptMessage;
                            adapter.notifyDataSetChanged();
                        } else {
                            // произошла ошибка, либо не та шифровка в настройках, либо это обычное сообщение
                            Toast.makeText(MessageHistoryActivity.this, R.string.error_decrypting, Toast.LENGTH_LONG).show();
                        }
                        break;

                    case 3:
                        final String[] translatedMessage = new String[1];
                        // Переводчик
                        AlertDialog.Builder alerBuilder = new AlertDialog.Builder(MessageHistoryActivity.this);
                        alerBuilder.setTitle(getResources().getString(R.string.translator))
                                .setMessage(getResources().getString(R.string.translation_text))
                                .setPositiveButton("OK", null)
                                .setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!TextUtils.isEmpty(translatedMessage[0])) {
                                            AndroidUtils.copyTextToClipboard(MessageHistoryActivity.this, translatedMessage[0]);
                                            Toast.makeText(MessageHistoryActivity.this, R.string.message_copied, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                        final AlertDialog alertDialog = alerBuilder.create();
                        alertDialog.show();


                        YandexTranslator translator = new YandexTranslator(MessageHistoryActivity.this);
                        translator.translateAsync(messageItem.message.body, YandexTranslator.Language.ENGLISH.toString(), YandexTranslator.Language.RUSSIAN.toString(), new YandexTranslator.OnCompleteListener() {
                            @Override
                            public void onCompleteTranslate(YandexTranslator translator, String message) {
                                alertDialog.setMessage(message);
                                translatedMessage[0] = message;
                            }
                        });
                        break;

                    case 4:
                        // Удалить
                        ArrayList<MessageItem> messages = new ArrayList<>();
                        messages.add(messageItem);
                        deleteMessages(messages);

                        if (adapter.getMessages().remove(messageItem)) {
                            database.execSQL("DELETE FROM " + DBHelper.MESSAGES_TABLE + " WHERE " + DBHelper.MESSAGE_ID + " = " + messageItem.message.mid);
                            adapter.notifyDataSetChanged();
                        }
                        break;

                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getAllMessages() {

        final boolean[] stop = {false};
        final int[] count = {
                0, // общее кол-во
                0, // отправленные
                0, // полученные сообщения
        };
        final Boolean[] isHideDialog = {false};

        // https://developer.android.com/training/material/shadows-clipping.html
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageHistoryActivity.this);
        builder
                .setTitle(R.string.statistics_messages)
                .setMessage("")
                .setCancelable(false)
                .setNegativeButton("Hide", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isHideDialog[0] = true;
                        Toast.makeText(MessageHistoryActivity.this, "Диалог появиться снова, когда статистика сообщений будет успешно загруженна", Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stop[0] = true;
                    }
                });

        alert = builder.create();
        alert.show();

        if (AndroidUtils.isInternetConnection(getApplicationContext()))
            new Thread() {
                @Override
                public void run() {
                    try {

                        // будем показывать 20 самых часто используемых слов
                        final HashMap<String, Integer> mapWords = new HashMap<>();

                        String sql;
                        // если зашли в диалог с пользователем
                        if (chat_id == 0) {
                            sql = "SELECT * FROM " + DBHelper.STATS_MESSAGES_TABLE + " WHERE chat_id = 0 AND user_id = " + uid;
                        } else {
                            // если в чат
                            sql = "SELECT * FROM " + DBHelper.STATS_MESSAGES_TABLE + " WHERE chat_id = " + chat_id;
                        }
                        Cursor cursor = database.rawQuery(sql, null);

                        while (cursor.moveToNext()) {
                            int totalCount = cursor.getInt(cursor.getColumnIndex(DBHelper.TOTAL_COUNT));
                            int outCount = cursor.getInt(cursor.getColumnIndex(DBHelper.OUTGOING_COUNT));
                            int inCount = cursor.getInt(cursor.getColumnIndex(DBHelper.INCOMING_COUNT));

                            count[0] = totalCount;
                            count[1] = outCount;
                            count[2] = inCount;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alert.setMessage(
                                        getString(R.string.all)
                                                + String.format("%,d", count[0]) + "\n"
                                                + getString(R.string.outgoing) + String.format("%,d", count[1]) + "\n"
                                                + getString(R.string.incoming) + String.format("%,d", count[2]));

                            }
                        });

                        //   final long startTime = System.currentTimeMillis();
                        long wordsCount = 0;
                        while (!stop[0]) {

                            ArrayList<VKMessage> dialogs = api.getMessagesHistoryWithExecute(uid, chat_id, api.getUserId(), count[0]);

                            if (dialogs.isEmpty()) {
                                stop[0] = true;
                                FileLogger.w("Dialogs", "IS EMPTY");
                                FileLogger.w("Dialogs", "STOPPING...");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MessageHistoryActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }

                            for (VKMessage message : dialogs) {
                                count[0]++;
                                if (message.is_out) {
                                    count[1]++;
                                } else {
                                    count[2]++;
                                }
                                wordsCount = wordsCount + AndroidUtils.getWordsCount(message.body);
                            }
                            dialogs.clear();
                            dialogs.trimToSize();
                            dialogs = null;

                            final long finalWordsCount = wordsCount;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    // Для нахождение процентов надо 100 разделить на (сумма/посчитанное кол-во сообщений)
                                    alert.setMessage(
                                            getString(R.string.all_words) + String.format("%,d", finalWordsCount) + "\n" +
                                                    getString(R.string.all) + String.format("%,d", count[0]) + "\n"
                                                    + getString(R.string.outgoing) + String.format("%,d", count[1]) + "\n"
                                                    + getString(R.string.incoming) + String.format("%,d", count[2]) +
                                                    "\n");

                                }
                            });

                        }

                        ContentValues cv = new ContentValues();
                        cv.put(DBHelper.USER_ID, uid);
                        cv.put(DBHelper.CHAT_ID, chat_id);
                        cv.put(DBHelper.TOTAL_COUNT, count[0]);
                        cv.put(DBHelper.OUTGOING_COUNT, count[1]);
                        cv.put(DBHelper.INCOMING_COUNT, count[2]);

                        if (cursor.getCount() > 0) {
                            if (chat_id == 0)
                                database.update(DBHelper.STATS_MESSAGES_TABLE, cv, "chat_id = 0 AND user_id = " + uid, null);
                            else
                                database.update(DBHelper.STATS_MESSAGES_TABLE, cv, "chat_id = " + chat_id, null);
                        } else {
                            database.insert(DBHelper.STATS_MESSAGES_TABLE, null, cv);
                        }
                        cursor.close();
                        cv.clear();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!alert.isShowing() && isHideDialog[0]) {
                                    alert.show();
                                }
//                                SimpleDateFormat sdf = new SimpleDateFormat("s");
                                // Toast.makeText(MessageHistoryActivity.this, "Статистика подсчитанна за " + sdf.format(System.currentTimeMillis() - startTime) + " сек.", Toast.LENGTH_LONG).show();
                            }
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                        stop[0] = true;
                        alert.setMessage("Error...\n" + e.toString());
                    }
                }
            }.start();
        else alert.setMessage(getResources().getString(R.string.check_internet));
    }

    public String encrypt(String message) {
        String encryptMessageValue = PreferenceManager.getDefaultSharedPreferences(MessageHistoryActivity.this).getString("encrypt_messages", "hex");
        String text = message;
        switch (encryptMessageValue.toUpperCase()) {
            case "HEX":
                text = Encrypter.encodeHEX(text.getBytes());
                break;
            case "BASE":
                text = Encrypter.encodeBase64(text.getBytes());
                break;
            case "MD5":
                text = Encrypter.encodeMD5(text);
                break;
            case "BINARY":
                text = Encrypter.encodeBinary(text);
                break;
            case "3DES":
                text = new String(Encrypter.encodeDES3(text), Charset.forName("UTF-8"));
                break;
            case "HASHCODE":
                text = String.valueOf(Encrypter.encodeHashCode(text));
                break;

        }
        return text;
    }

    public String decryptMessage(String message) {
        String encryptMessageValue = PreferenceManager.getDefaultSharedPreferences(MessageHistoryActivity.this).getString("encrypt_messages", "hex");
        String text = message;
        switch (encryptMessageValue.toUpperCase()) {
            case "HEX":
                text = Encrypter.decodeHEX(text);
                break;
            case "BASE":
                text = Encrypter.decodeBase64(text.getBytes());
                break;
            case "MD5":
                text = null;
                break;
            case "BINARY":
                text = Encrypter.decodeBinary(text);
                break;
            case "3DES":
                text = Encrypter.decodeDES3(text.getBytes());
                break;
            case "HASHCODE":
                text = null;
                break;

        }
        return text;
    }

    /**
     * Загрузка сообщений
     *
     * @param fromSavedMessages загрузить сохраненные сообщения из бд
     */
    private void getMessages(boolean fromSavedMessages) {
        helper = DBHelper.get(this);
        database = helper.getWritableDatabase();

        final Cursor cursor;
        final String sql;

        // если чат
        if (chat_id != 0) {
            sql = "SELECT * FROM " + DBHelper.MESSAGES_TABLE +
                    " LEFT JOIN " + DBHelper.USERS_TABLE +
                    " ON " + DBHelper.MESSAGES_TABLE + "." + DBHelper.USER_ID +
                    " = " + DBHelper.USERS_TABLE + "." + DBHelper.USER_ID +
                    " WHERE " + DBHelper.MESSAGES_TABLE + "." + DBHelper.CHAT_ID +
                    " = " + chat_id;
        } else {
            sql = "SELECT * FROM " + DBHelper.MESSAGES_TABLE + " WHERE user_id = " + uid + " AND chat_id = 0";
        }
        // читаем сообщения
        cursor = database.rawQuery(sql, null);

        // загружаем CursorAdapter. который оптимизирован
        // для большого кол-ва сообщений
        if (fromSavedMessages) {
            String saveSql = "SELECT * FROM " + DBHelper.SAVED_MESSAGES_TABLE + "_" + uid;
            Cursor saveCursor = database.rawQuery(saveSql, null);
            cursorAdapter = new MessageCursorAdapter(this, saveCursor, saveSql, chat_id, uid);
            lvHistory.setAdapter(cursorAdapter);
            lvHistory.setSelection(cursorAdapter.getCount());

            if (cursorAdapter.getCount() > 500) {
                lvHistory.setFastScrollEnabled(true);
            }
            return;
        }

        history = new ArrayList<>(30);
        adapter = new MessageAdapter(this, history, uid, chat_id);
        adapter.setCloseListener(new BaseArrayAdapter.OnMultiModeCloseListener() {
            @Override
            public void onClose() {
                Log.w("MessageActivity", "onClose ActionMode");
                invalidateOptionsMenu();
            }
        });
        // если сообщения есть, то заполняем
        if (cursor.getCount() > 0) {
            fillList(history, cursor);

            lvHistory.setAdapter(adapter);
            lvHistory.setSelection(history.size());
        }
        cursor.close();

        // если нет интернета
        if (!AndroidUtils.isInternetConnection(this)) {
            return;
        }

        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VKMessage> vkMessages = api.getMessagesHistory(uid, chat_id, 0, 30);

                    // если чат, то обновляем юзеров
                    HashMap<Integer, VKUser> mapUsers = null;
                    if (chat_id != 0) {
                        mapUsers = new HashMap<>();
                        for (VKMessage message : vkMessages) {
                            mapUsers.put(message.uid, null);
                        }


                        ArrayList<VKUser> vkUsers = api.getProfiles(mapUsers.keySet(), null, null, null, null);
                        for (VKUser user : vkUsers) {
                            mapUsers.put(user.user_id, user);
                            helper.addUserToDB(user);
                        }
                        vkUsers.clear();
                        vkUsers.trimToSize();
                    }

                    history.clear();
                    for (VKMessage vkMessage : vkMessages) {
                        history.add(0, new MessageItem(vkMessage, mapUsers == null ? VKUser.EMPTY_USER : mapUsers.get(vkMessage.uid)));
                    }
                    if (mapUsers != null) {
                        mapUsers.clear();
                        mapUsers = null;
                    }

                    // обновляем список
                    lvHistory.post(new Runnable() {
                        @Override
                        public void run() {
                            if (lvHistory.getAdapter() != null) {
                                adapter.notifyDataSetChanged();
                            } else {
                                lvHistory.setAdapter(adapter);
                            }
                            lvHistory.setSelection(adapter.getCount());
//                            lvHistory.setAdapter(adapter);
//                                adapter.notifyDataSetChanged();
                            // по неизвестным мне причиным, обновление адаптера вызывает краш
                            // на 4.1-4.4
                        }
                    });

                    // удаляем все сообщения, что бы загрузить на их место новые
                    Cursor c = database.rawQuery(sql, null);
                    while (c.moveToNext()) {
                        int _id = c.getInt(0);
                        //  database.delete(DBHelper.MESSAGES_TABLE, "_id = " + _id, null);
                        database.execSQL("DELETE FROM " + DBHelper.MESSAGES_TABLE + " WHERE _id = " + _id);
                    }
                    c.close();
                    VKInsertHelper.insertDialogs(database, vkMessages, true);
                    vkMessages.clear();
                    vkMessages.trimToSize();

//                    if (chat_id != 0 & uid != 0)
                    adapter.connectToLongPoll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setTyping() {
        if (!AndroidUtils.isInternetConnection(this)) {
            return;
        }
        this.lastTypeNotification = System.currentTimeMillis();
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    api.setMessageActivity(uid, chat_id, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getChatMembers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VKFullUser> chatUsers = api.getChatUsers(chat_id, "photo_50");
                    HashMap<Integer, VKFullUser> mapInvitedBy = new HashMap<>();
                    for (VKFullUser u : chatUsers) {
                        mapInvitedBy.put(u.invited_by, null);
                    }

                    ArrayList<VKFullUser> profiles = api.getProfilesFull(mapInvitedBy.keySet(), null, "photo_50", null, null, null);
                    for (VKFullUser u : profiles) {
                        mapInvitedBy.put(u.uid, u);
                    }

                    ArrayList<ChatMember> members = new ArrayList<>();
                    for (VKFullUser u : chatUsers) {
                        members.add(new ChatMember(u, mapInvitedBy.get(u.invited_by)));
                    }

                    final ChatMemberAdapter chatMemberAdapter = new ChatMemberAdapter(MessageHistoryActivity.this, members);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MessageHistoryActivity.this)
                                    .setTitle(R.string.title_members)
                                    .setPositiveButton("ОК", null)
                                    .setNeutralButton("Покинуть", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            exitFromChat();
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.add, null)
                                    .setAdapter(chatMemberAdapter, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getOldMessages(final int count, final long offset) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
//                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                try {
                    VKUser emptyUser = VKUser.EMPTY_USER;
                    HashMap<Integer, VKUser> mapUsers = null;
                    ArrayList<VKMessage> oldMessages = api.getMessagesHistory(uid, chat_id, offset, count);
                    if (oldMessages.isEmpty()) {
                        isLoadingOldMessages = false;
                        return;
                    }

                    // если это чат. то загружаем пользователей
                    if (chat_id > 0) {
                        mapUsers = new HashMap<>();

                        for (int i = 0; i < oldMessages.size(); i++) {
                            VKMessage m = oldMessages.get(i);
                            mapUsers.put(m.uid, null);
                        }

                        ArrayList<VKUser> apiProfiles = api.getProfiles(mapUsers.keySet(), null, null, null, null);
                        for (int i = 0; i < apiProfiles.size(); i++) {
                            VKUser user = apiProfiles.get(i);
                            mapUsers.put(user.user_id, user);
                        }
                    }


                    for (int i = 0; i < oldMessages.size(); i++) {
                        VKMessage message = oldMessages.get(i);
                        history.add(0, new MessageItem(message, mapUsers == null ? emptyUser : mapUsers.get(message.uid)));
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isLoadingOldMessages = false;
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                lvHistory.setSelection(lvHistory.getFirstVisiblePosition() + count);
                            }

                        }
                    });
                    VKInsertHelper.insertMessages(database, oldMessages, true);
//                    adapter.getMessages().trimToSize();
                    if (oldMessages != null) {
                        oldMessages.clear();
                        oldMessages.trimToSize();
                        oldMessages = null;
                    }
//                    if (tempMessageList != null) {
//                        tempMessageList.clear();
//                        tempMessageList.trimToSize();
//                        tempMessageList = null;
//                    }
                    if (mapUsers != null) {
                        mapUsers.clear();
                        mapUsers = null;
                    }
                    System.gc();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendMessage(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (text.equalsIgnoreCase("КЕК") || text.equalsIgnoreCase("ЛОЛ")) {
            FileLogger.w("MessageHistoryActivity", "an attempt to send word KEK");
            Toast.makeText(this, "Сообщение с данным текстом нельзя отправить", Toast.LENGTH_LONG).show();
            return;
        }
        final String finalText = text;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final VKMessage message = new VKMessage();
                message.body = finalText;
                message.uid = api.getUserId();
                message.chat_id = chat_id;
                message.is_out = true;
                message.date = System.currentTimeMillis() / 1000;

                VKUser user = new VKUser();
                final MessageItem item = new MessageItem(message, user, MessageItem.Status.SENDING);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etMessageText.setText("");
                        adapter.getMessages().add(item);
                        adapter.notifyDataSetChanged();
                        lvHistory.smoothScrollToPosition(adapter.getCount());
                    }
                });

                try {
                    int mid = api.sendMessage(uid, chat_id, message.body, null, null, null, null, null, null, null, null);
                    item.status = MessageItem.Status.SENT;
                    item.message.mid = mid;

                    VKInsertHelper.sValues.clear();
                    VKInsertHelper.insertMessage(database, message);
                    VKInsertHelper.sValues.clear();
                    VKInsertHelper.insertDialog(database, message);
                } catch (Exception e) {
                    e.printStackTrace();
                    item.setStatus(MessageItem.Status.ERROR);
                    if (PrefManager.getBoolean("resend_failed_msg", true)) {
                        ContentValues cv = new ContentValues();
                        cv.put(DBHelper.USER_ID, uid);
                        cv.put(DBHelper.CHAT_ID, chat_id);
                        cv.put(DBHelper.BODY, message.body);
                        database.insert(DBHelper.FAILED_MESSAGES_TABLE, null, cv);
                    }
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void deleteMessages(final ArrayList<MessageItem> messages) {
        final ArrayList<MessageItem> items = new ArrayList<>(messages);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Integer> mids = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        mids.add(items.get(i).message.mid);
                    }
                    api.deleteMessage(mids);

                    mids.clear();
                    mids.trimToSize();

                    history.removeAll(items);

//                    for (int i = 0; i < messages.size(); i++) {
//                        MessageItem deletedMessage = messages.get(i);
//
//                        for (int j = 0; j < history.size(); j++) {
//                            MessageItem historyItem = history.get(i);
//                            if (historyItem.message.mid == deletedMessage.message.mid) {
//                                history.remove(j);
//                            }
//                        }
//                    }

                    items.clear();
                    items.trimToSize();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void fillList(ArrayList<MessageItem> list, Cursor cursor) {
        VKUser emptyUser = new VKUser();
        history.ensureCapacity(cursor.getCount());
        while (cursor.moveToNext()) {
            String body = cursor.getString(4);
            String photo = chat_id != 0 ? cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_50)) : null;
            String firstName = chat_id != 0 ? cursor.getString(cursor.getColumnIndex(DBHelper.FIRST_NAME)) : null;
            String lastName = chat_id != 0 ? cursor.getString(cursor.getColumnIndex(DBHelper.LAST_NAME)) : null;
            int out = cursor.getInt(7);
            int read_state = cursor.getInt(6);
            int date = cursor.getInt(5);
            int mid = cursor.getInt(1);

            VKMessage message = new VKMessage();

            message.mid = mid;
            message.chat_id = chat_id;
            message.date = date;
            message.body = body;
            message.is_out = out == 1;
            message.read_state = read_state == 1;

            VKUser user = null;
            if (firstName != null || lastName != null) {
                user = new VKUser();
                user.photo_50 = photo;
                user.first_name = firstName;
                user.last_name = lastName;
            }

            list.add(0, new MessageItem(message, user == null ? emptyUser : user));
        }
    }

    private void exitFromChat() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    api.removeUserFromChat(chat_id, Api.get().getUserId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        finish();
    }

    private void translateAllMessages(final String languageTo) {
        final String translateTextWait = getResources().getString(R.string.translate_text_please_wait);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.translator);
        builder.setCancelable(false);
        builder.setMessage(translateTextWait);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                forceClose = true;
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();


        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                YandexTranslator translator = new YandexTranslator(MessageHistoryActivity.this);
                int count = 0;
                for (int i = 0; i < history.size(); i++) {
                    if (forceClose) {
                        break;
                    }
                    MessageItem item = history.get(i);
                    final String text = translator.translate(item.message.body,
                            YandexTranslator.Language.AUTO_DETECT.toString(),
                            languageTo);

                    if (text.equals("[error]")) {
                        // error. stopping translate
                        forceClose = true;
                        AndroidUtils.runOnUi(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MessageHistoryActivity.this, R.string.check_internet, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    if (!TextUtils.isEmpty(text)) {
                        item.message.body = text;
                    }
                    count++;
                    final int finalCount = count;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setMessage(translateTextWait + "\n" + "Переведенно " + finalCount + " сообщений из " + history.size());
                        }
                    });
                    translateAttachMessages(item.message, translator, languageTo);
                }
                translator.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    private void translateAttachMessages(VKMessage message, YandexTranslator translator, String languageTo) {
        if (message.attachments.isEmpty()) {
            return;
        }

        for (int i = 0; i < message.attachments.size(); i++) {
            // если вложение не содержит пересланного сообщения
            final VKAttachment attachment = message.attachments.get(i);
            if (!attachment.type.equals(VKAttachment.TYPE_MESSAGE)) {
                continue;
            }

            VKMessage item = attachment.message;
            String text = translator.translate(item.body,
                    YandexTranslator.Language.AUTO_DETECT.toString(),
                    languageTo);

            if (text != null) {
                item.body = text;
            }
        }
    }

    private void pickImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 100);
    }


    private void loadWallpaperFromSD() {
        String path = ThemeManager.getWallpaperPath(this);
        if (!TextUtils.isEmpty(path)) {
            ImageView ivWallpaper = (ImageView) findViewById(R.id.ivWallpaper);
            Picasso.with(this)
                    .load(new File(path))
                    .into(ivWallpaper);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.w("MessageActivity", "requestCode: " + requestCode);
        Log.w("MessageActivity", "resultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            PrefManager.putString(ThemeManager.PREF_KEY_MESSAGE_WALLPAPER_PATH, filePath);
            ThemeManager.updateThemeValues();
            loadWallpaperFromSD();

            Log.w("MessageActivity", "image path is " + filePath);
        }
    }

    @Override
    public void onBackPressed() {
        if (adapter.isInMultiSelectMode()) {
            adapter.disableMultiSelectMode();
            invalidateOptionsMenu();
            return;
        }
        super.onBackPressed();
        this.overridePendingTransition(R.anim.diagonaltranslate_right, R.anim.diagonaltranslate_right2);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (adapter.isInMultiSelectMode()) {
            menu.findItem(R.id.menuMessageAttach).setVisible(false);
            menu.findItem(R.id.menuStatsMessages).setVisible(false);
            menu.findItem(R.id.menuUpdateMessages).setVisible(false);
            menu.findItem(R.id.menuMessageMaterialsOfDialog).setVisible(false);
            menu.findItem(R.id.menuMessageTranslateAll).setVisible(false);
            menu.findItem(R.id.menuMessageAttach).setVisible(false);
            menu.findItem(R.id.menuWallpaper).setVisible(false);

            menu.findItem(R.id.menuMessageDelete).setVisible(true);
        } else {
            menu.findItem(R.id.menuMessageAttach).setVisible(true);
            menu.findItem(R.id.menuStatsMessages).setVisible(true);
            menu.findItem(R.id.menuUpdateMessages).setVisible(true);
            menu.findItem(R.id.menuMessageTranslateAll).setVisible(true);
            menu.findItem(R.id.menuMessageMaterialsOfDialog).setVisible(true);
            menu.findItem(R.id.menuMessageAttach).setVisible(true);
            menu.findItem(R.id.menuWallpaper).setVisible(true);

            menu.findItem(R.id.menuMessageDelete).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuStatsMessages:
                getAllMessages();
                break;

            case R.id.menuUpdateMessages:
                adapter.clear();
                getMessages(false);
                break;

            case R.id.menuMessageMaterialsOfDialog:
                Intent intent = new Intent(getApplicationContext(), DialogMaterialsActivity.class);
                intent.putExtra("user_id", uid);
                intent.putExtra("chat_id", chat_id);
                startActivity(intent);
                break;

            case R.id.menuMessageTranslateAll:
                YandexTranslator.Language[] languages = YandexTranslator.Language.values();
                final CharSequence[] items = new CharSequence[languages.length];
                for (int i = 0; i < languages.length; i++) {
                    items[i] = languages[i].name();
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(MessageHistoryActivity.this);
                builder.setTitle("На какой язык перевести?");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        translateAllMessages(items[which].toString());
                    }
                });
                builder.create().show();
                break;

            case R.id.menuMessageDelete:
                deleteMessages(adapter.getSelectedItems());
                adapter.disableMultiSelectMode();
                break;

            case R.id.menuWallpaper: pickImageFromGallery();
                break;

            case R.id.menuHideShowTime:
                adapter.toggleStateTime();
                break;

            case R.id.menuMessageAttach:
                Toast.makeText(this, "Еще не реализовано", Toast.LENGTH_LONG).show();
                break;

            case android.R.id.home:
                finish();
                this.overridePendingTransition(R.anim.diagonaltranslate_right,
                        R.anim.diagonaltranslate_right2);
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (cursorAdapter != null) {
            cursorAdapter.close();
            cursorAdapter = null;
        }
        if (adapter != null) {
            adapter.unregisterLongPoll();
            adapter.clear();
            adapter = null;
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
//        VKLongPoll.get(this).unregister("MessageHistory");
        System.gc();
        super.onDestroy();

    }


}

