package ru.euphoriadev.vk;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.LogWriter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.AppCompatButton;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import ru.euphoriadev.vk.adapter.DialogAdapter;
import ru.euphoriadev.vk.adapter.DialogItem;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.VKApi;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.FileLogger;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.fab.FloatingActionButton;

/**
 * Created by Igor on 10.07.15.
 */
public class DialogsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "DialogsFragment";
    Activity activity;

    Api api;
    Account account;
    ThemeManagerOld tm;

    SwipeRefreshLayout swipeLayout;
    ListView listView;
    AppCompatButton footerButton;
    View rootView;

    DialogAdapter adapter;
    ArrayList<DialogItem> dialogItems;

    SQLiteDatabase database = null;
    SharedPreferences preferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dialogs, container, false);
        setHasOptionsMenu(true);

        activity = getActivity();
        api = Api.get();

        tm = new ThemeManagerOld(activity);

        ((BasicActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        ((BasicActivity) getActivity()).hideSpinner();

        listView = (ListView) rootView.findViewById(R.id.lvMess);
        tm.initDivider(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogItem item = (DialogItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), MessageHistoryActivity.class);
                intent.putExtra("user_id", item.user.user_id);
                intent.putExtra("chat_id", item.message.chat_id);
                intent.putExtra("fullName", item.message.isChat() ? item.message.title : item.user.toString());
                intent.putExtra("photo_50", item.user.photo_50);
                intent.putExtra("users_count", item.message.users_count);
                intent.putExtra("online", item.user.online);
                intent.putExtra("from_saved", false);
                startActivity(intent);

//                 Анимация переходов между активити
                getActivity().overridePendingTransition(R.anim.diagonaltranslate_left,
                        R.anim.diagonaltranslate_left2);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final DialogItem item = (DialogItem) parent.getItemAtPosition(position);

                CharSequence[] items = new CharSequence[]{
                        activity.getString(R.string.download_dialog),
                        activity.getString(R.string.delete_dialog)
                };

                final boolean exists = DBHelper.get(getActivity()).exists(database, DBHelper.SAVED_MESSAGES_TABLE + "_" + item.user.user_id);
                if (exists) {
                    items = new CharSequence[]{
                            activity.getString(R.string.load_saved_dialog),
                            activity.getString(R.string.delete_dialog)
                    };
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (exists) {
                                    Intent intent = new Intent(getActivity(), MessageHistoryActivity.class);
                                    intent.putExtra("user_id", item.user.user_id);
                                    intent.putExtra("chat_id", item.message.chat_id);
                                    intent.putExtra("fullName", item.message.isChat() ? item.message.title : item.user.toString());
                                    intent.putExtra("photo_50", item.user.photo_50);
                                    intent.putExtra("users_count", item.message.users_count);
                                    intent.putExtra("online", item.user.online);
                                    intent.putExtra("from_saved", true);
                                    startActivity(intent);
                                    return;
                                }
                                downloadDialog(item.message.uid, item.message.chat_id, item.user.toString());
                                break;

                            case 1:
                                deleteDialog(item.user.user_id, item.message.chat_id);
                                break;
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container_messenges);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.BLACK);

//        IconicsDrawable iconPlus = new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_create);
//        iconPlus.sizeDp(18);
//        if (tm.isBlackTheme()) {
//            iconPlus.color(Color.BLACK);
//        } else {
//            iconPlus.color(Color.WHITE);
//        }


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setColorNormal(ThemeManager.getColorAccent(getActivity()));
//        fab.setImageDrawable(iconPlus);
        fab.setShadow(true);
        fab.setColorPressed(ViewUtil.getPressedColor(fab.getColorNormal()));
        fab.setColorRipple(getActivity().getResources().getColor(R.color.ripple_material_dark));
        fab.attachToListView(listView, swipeLayout);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateChatActivity.class));
            }
        });
        ViewUtil.setFilter(fab, ThemeManager.getPrimaryTextColorOnAccent(getActivity()));
//        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//        p.setBehavior(new FloatingActionButtonBehavior());
//        fab.setLayoutParams(p);


        preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        footerButton = new AppCompatButton(getActivity(), null, android.R.attr.borderlessButtonStyle);
        footerButton.setText(getString(R.string.load_old));
        footerButton.setTextColor(fab.getColorNormal());
        footerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadOldDialogs(30);
            }
        });
        footerButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                loadOldDialogs(200);
                return true;
            }
        });
        listView.addFooterView(footerButton);

        //  listView.setAdapter(adapter);
        account = new Account(getActivity()).restore();
        loadDialogs(false);

        return rootView;
    }

    @Override
    public void onRefresh() {
        if (AndroidUtils.isInternetConnection(getActivity())) {
            loadDialogs(true);
        } else {
            AndroidUtils.showToast(activity, getResources().getString(R.string.check_internet), true);
            swipeLayout.setRefreshing(false);

//            Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), R.string.check_internet, Snackbar.LENGTH_LONG).show();
        }
    }

    private void downloadDialog(final long id, final long chatid, final String userName) {
        if (chatid != 0) {
            Toast.makeText(getActivity(), "Sorry, can't download the chat. Wait for updates", Toast.LENGTH_LONG).show();
            return;
        }

        Intent notificationIntent = new Intent();
        final PendingIntent contentIntent = PendingIntent.getActivity(getActivity(),
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // создаем уведомление с прогресс баром
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
        builder.setContentIntent(contentIntent)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(userName)
                .setContentText(activity.getResources().getString(R.string.download_dialog_with))
                .setWhen(System.currentTimeMillis())
                .setProgress(100, 0, true)
                .setTicker(activity.getString(R.string.started_download_dialog));

        Notification notification = builder.build();
        // пользователь не сможет его удалить
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.notify(userName.hashCode(), notification);

        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isDownloaded = false;
                try {
                    int count = 0;
                    DBHelper.get(getActivity()).createTableSavedMessages(database, id);

                    // транзеция нужна, что бы ускорить вставку данных в базу
//                    database.compileStatement()
//                    database.setLockingEnabled(false);
                    ContentValues cv = new ContentValues();
                    while (true) {
                        // скачиваем 1.5 сообщений
                        ArrayList<VKMessage> vkMessages = api.getMessagesHistoryWithExecute(id, chatid, account.user_id, count);
                        if (vkMessages.isEmpty()) {
                            // если offset > общего кол-во, то список будет пуст
                            break;
                        }
                        // загружаем в бд
                        database.beginTransaction();
                        for (VKMessage msg : vkMessages) {
                            cv.put(DBHelper.MESSAGE_ID, msg.mid);
                            cv.put(DBHelper.USER_ID, msg.uid);
                            cv.put(DBHelper.CHAT_ID, msg.chat_id);
                            cv.put(DBHelper.BODY, msg.body);
                            cv.put(DBHelper.DATE, msg.date);
                            cv.put(DBHelper.READ_STATE, msg.read_state);
                            cv.put(DBHelper.IS_OUT, msg.is_out);
                            database.insert(DBHelper.SAVED_MESSAGES_TABLE + "_" + id, null, cv);

                            cv.clear();
                            count++; // собственно сам offset
                        }
                        vkMessages.clear();
                        vkMessages.trimToSize();
                        database.setTransactionSuccessful();
                        database.endTransaction();

                        isDownloaded = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    notificationManager.cancel(userName.hashCode());

                    // создаем уведомление с прогресс баром
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
                    builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setContentTitle(userName)
                            .setContentText(getActivity().getString(isDownloaded ? R.string.download_history_successful : R.string.error))
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true);

                    Notification notification = builder.build();
                    notificationManager.notify(userName.hashCode(), notification);
                }
            }
        });
    }

    /**
     * Получение всех сообщений их таблици {@link DBHelper#DIALOGS_TABLE}
     * Если кол-во записей в {@link Cursor} <= 0, то вернет пустой список
     *
     * @param database бд
     * @return new ArrayList
     * @see ArrayList
     * @see Cursor
     */
    private ArrayList<DialogItem> getDialogsFrom(SQLiteDatabase database) {
        ArrayList<DialogItem> listDialogs = new ArrayList<>(30);
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.DIALOGS_TABLE +
                " LEFT JOIN " + DBHelper.USERS_TABLE +
                " ON " + DBHelper.DIALOGS_TABLE + "." + DBHelper.USER_ID +
                " = " + DBHelper.USERS_TABLE + "." + DBHelper.USER_ID, null);

        // Cursor is empty
        if (cursor.getCount() <= 0) {
            cursor.close();
            return listDialogs;
        }

        while (cursor.moveToNext()) {
            int uid = cursor.getInt(cursor.getColumnIndex(DBHelper.USER_ID));
            String firstNameUser = cursor.getString(cursor.getColumnIndex(DBHelper.FIRST_NAME)) + "";
            String lastNameUser = cursor.getString(cursor.getColumnIndex(DBHelper.LAST_NAME)) + "";
            String photo50User = cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_50));

            int chatIdMessage = cursor.getInt(cursor.getColumnIndex(DBHelper.CHAT_ID));
            int isOutMessage = cursor.getInt(cursor.getColumnIndex(DBHelper.IS_OUT));
            int readStateMessage = cursor.getInt(cursor.getColumnIndex(DBHelper.READ_STATE));
            int dateMessage = cursor.getInt(cursor.getColumnIndex(DBHelper.DATE));
            int unreadCountMessages = cursor.getInt(cursor.getColumnIndex(DBHelper.UNREAD_COUNT));
            int userCountMessage = cursor.getInt(cursor.getColumnIndex(DBHelper.USERS_COUNT));
            String titleMessage = cursor.getString(cursor.getColumnIndex(DBHelper.TITLE));
            String bodyMessage = cursor.getString(cursor.getColumnIndex(DBHelper.BODY));

            VKMessage m = new VKMessage();
            VKUser u = new VKUser();

            m.uid = uid;
            m.chat_id = chatIdMessage;
            m.is_out = isOutMessage == 1;
            m.read_state = readStateMessage == 1;
            m.date = dateMessage;
            m.title = titleMessage;
            m.body = bodyMessage;
            m.users_count = userCountMessage;
            m.unread = unreadCountMessages;

            u.user_id = uid;
            u.first_name = firstNameUser;
            u.last_name = lastNameUser;
            u.photo_50 = photo50User;

            listDialogs.add(new DialogItem(m, u));
        }
        cursor.close();
        return listDialogs;
    }

    /**
     * Вставка или обновление пользователей в таблицу
     *
     * @param database база данных
     * @param users    список пользователей, которые улетят в бд
     */
    private void insertUsersTo(SQLiteDatabase database, Collection<VKUser> users, boolean withTransaction) {
        Log.w(TAG, "Insert users to database");
        if (withTransaction) {
            database.beginTransaction();
        }
        ContentValues cv = new ContentValues();
        for (VKUser user : users) {

            cv.put(DBHelper.USER_ID, user.user_id);
            cv.put(DBHelper.FIRST_NAME, user.first_name);
            cv.put(DBHelper.LAST_NAME, user.last_name);
            cv.put(DBHelper.LAST_SEEN, user.last_name);
//          cv.put(DBHelper.SCREEN_NAME, user.screen_name);
            cv.put(DBHelper.ONLINE, user.online);
            cv.put(DBHelper.ONLINE_MOBILE, user.online_mobile);
            cv.put(DBHelper.STATUS, user.status);
            cv.put(DBHelper.PHOTO_50, user.photo_50);
//          cv.put(DBHelper.PHOTO_100, user.photo_50);

            if (database.update(DBHelper.USERS_TABLE, cv, "user_id = ?", new String[]{String.valueOf(user.user_id)}) == 0)
                database.insert(DBHelper.USERS_TABLE, null, cv);
        }
        if (withTransaction) {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
        cv.clear();
    }

    /**
     * Вставка сообщений в таблицу {@link DBHelper#DIALOGS_TABLE}
     *
     * @param database бд
     * @param messages список сообщений, которые необходимо занести в бд
     * @param withTransaction применить транзекцию, для более быстрой вставки
     */
    private void insertMessagesTo(SQLiteDatabase database, ArrayList<VKMessage> messages, boolean withTransaction) {
        Log.w(TAG, "Insert messages to database");
        ContentValues cv = new ContentValues();
        if (withTransaction) {
            database.beginTransaction();
        }
        for (int i = 0; i < messages.size(); i++) {
            VKMessage m = messages.get(i);

            cv.put(DBHelper.TITLE, m.title);
            cv.put(DBHelper.BODY, m.body);
            cv.put(DBHelper.USER_ID, m.uid);
            cv.put(DBHelper.CHAT_ID, m.chat_id);
            cv.put(DBHelper.UNREAD_COUNT, m.unread);
            cv.put(DBHelper.DATE, m.date);
            cv.put(DBHelper.PHOTO_50, m.photo_50);
            cv.put(DBHelper.IS_OUT, m.is_out);
            cv.put(DBHelper.USERS_COUNT, m.users_count);
            cv.put(DBHelper.READ_STATE, m.read_state);

            database.insert(DBHelper.DIALOGS_TABLE, null, cv);
        }
        if (withTransaction) {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
        cv.clear();
    }

    /**
     * Обратная операция, удаление всех сообщений из таблици {@link DBHelper#DIALOGS_TABLE}.
     * Может понадобится перед добавлением новых сообщений
     *
     * @param database база данных
     * @see #insertMessagesTo(SQLiteDatabase, ArrayList, boolean)
     */
    private void deleteMessagesFrom(SQLiteDatabase database) {
        Log.w(TAG, "Delete messages from database");
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.DIALOGS_TABLE, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                database.delete(DBHelper.DIALOGS_TABLE, "_id = ?", new String[]{String.valueOf(id)});

//              database.execSQL("DELETE FROM ? WHERE _id = ?", new Object[]{DBHelper.DIALOGS_TABLE, id});
            }

        }
        cursor.close();
    }


    private void loadDialogs(final boolean onlyUpdate) {
        VKApi.init(VKApi.VKAccount.from(account));
        VKApi.messages().getDialogs().count(1).execute(new VKApi.VKOnResponseListener() {
            @Override
            public void onResponse(JSONObject responseJson) {
                        ((BaseThemedActivity) getActivity())
                        .getSupportActionBar()
                        .setSubtitle(activity.getString(R.string.dialogs_number) + responseJson.optJSONObject("response").optInt("count"));
            }

            @Override
            public void onError(VKApi.VKException exception) {

            }
        });
//        if (!AndroidUtils.isInternetConnection(getActivity())) {
//            Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), R.string.check_internet, Snackbar.LENGTH_LONG).show();
//        }
        getDialogs(onlyUpdate);

    }

    private void setRefreshing(final boolean refreshing) {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(refreshing);
            }
        });
    }

    @Override
    public void onDetach() {
        swipeLayout.setRefreshing(false);
        super.onDetach();
    }

    /**
     * Загрузка сообщений в список
     * @param onlyUpdate если надо только обновить список
     */
    private void getDialogs(final boolean onlyUpdate) {
        setRefreshing(true);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (database == null || !database.isOpen()) {
                        database = DBHelper.get(getActivity()).getWritableDatabase();
                    }
                    if (dialogItems == null) {
                        dialogItems = new ArrayList<>(30);
                    }

                    // если попросили только обновить диалоги
                    if (onlyUpdate && AndroidUtils.isInternetConnection(getActivity())) {
                        Log.w(TAG, "Only update dialogs");

                        ArrayList<VKMessage> vkDialogs = api.getMessagesDialogs(0, 30, null, null);
                        deleteMessagesFrom(database);
                        insertMessagesTo(database, vkDialogs, true);

                        HashMap<Long, VKUser> mapUsers = new HashMap<>();
                        for (int i = 0; i < vkDialogs.size(); i++) {
                            mapUsers.put(vkDialogs.get(i).uid, null);
                        }

                        ArrayList<VKUser> vkUsers = api.getProfiles(mapUsers.keySet(), null, null, null, null);
                        insertUsersTo(database, vkUsers, true);

                        mapUsers.clear();
                        mapUsers = null;

                        vkUsers.clear();
                        vkUsers.trimToSize();
                        vkUsers = null;

                        vkDialogs.clear();
                        vkDialogs.trimToSize();
                        vkDialogs = null;

                        ArrayList<DialogItem> dialogsDatabase = getDialogsFrom(database);
                        if (!dialogsDatabase.isEmpty()) {
                            dialogItems.clear();
                            dialogItems.addAll(dialogsDatabase);
//
                            dialogsDatabase.clear();
                            dialogsDatabase.trimToSize();
                            dialogsDatabase = null;

                            updateListView(dialogItems);
                            setRefreshing(false);
                        }
                        return;
                    }


                    Log.w(TAG, "Get dialogs from database");
                    ArrayList<DialogItem> dialogsFromDatabase = getDialogsFrom(database);
                    if (dialogsFromDatabase.isEmpty()) {
                        getDialogs(true);
                        return;
                    } else {
                        dialogItems.addAll(dialogsFromDatabase);

                        updateListView(dialogItems);

                        dialogsFromDatabase.clear();
                        dialogsFromDatabase.trimToSize();
                        dialogsFromDatabase = null;
                    }
                    if (!AndroidUtils.isInternetConnection(getActivity())) {
                        // no connection to internet...
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), R.string.check_internet, Toast.LENGTH_LONG).show();
                            }
                        });
                        setRefreshing(false);
                        return;
                    }

                    // получаем диалоги из вк
                    ArrayList<VKMessage> vkDialogs = api.getMessagesDialogs(0, 30, null, null);
                    HashMap<Long, VKUser> mapUsers = new HashMap<>();

                    for (int i = 0; i < vkDialogs.size(); i++) {
                        mapUsers.put(vkDialogs.get(i).uid, null);
                    }

                    // получаем пользователей из сервера вк
                    ArrayList<VKUser> vkUsers = api.getProfiles(mapUsers.keySet(), null, null, null, null);
                    for (int i = 0; i < vkUsers.size(); i++) {
                        VKUser u = vkUsers.get(i);
                        mapUsers.put(u.user_id, u);
                    }
                    dialogItems.clear();
                    for (int i = 0; i < vkDialogs.size(); i++) {
                        VKMessage m = vkDialogs.get(i);
                        VKUser u = mapUsers.get(m.uid);

                        dialogItems.add(new DialogItem(m, u));
                    }
                    updateListView(dialogItems);
                    setRefreshing(false);

                    deleteMessagesFrom(database);
                    insertMessagesTo(database, vkDialogs, true);
                    insertUsersTo(database, mapUsers.values(), true);

                    mapUsers.clear();
                    mapUsers = null;

                    vkDialogs.clear();
                    vkDialogs.trimToSize();
                    vkDialogs = null;

                    vkUsers.clear();
                    vkUsers.trimToSize();
                    vkUsers = null;

                } catch (Exception e) {
                    e.printStackTrace();
                    FileLogger.e(TAG, "Error get messages", e);
                } finally {
                    setRefreshing(false);
                }
            }
        });
    }

    private void updateListView(final ArrayList<DialogItem> items) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "Update Adapter");

                if (adapter == null) {
                    adapter = new DialogAdapter(getActivity(), items);
                    listView.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
                if (!adapter.isConnected()) {
                    adapter.connectToLongPollService();
                }
            }
        });
    }

    private void loadOldDialogs(final int count) {
        swipeLayout.setRefreshing(true);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<Long, VKUser> mapUsers = new HashMap<>();

                    ArrayList<VKMessage> dialogs = api.getMessagesDialogs(adapter.getCount(), count, null, null);
                    if (dialogs.isEmpty()) {
                        // если конец списка диалогов
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                footerButton.setVisibility(View.GONE);
                                listView.removeFooterView(footerButton);
                            }
                        });
                    }
                    for (VKMessage message : dialogs) {
                        mapUsers.put(message.uid, null);
                    }

                    ArrayList<VKUser> users = api.getProfiles(mapUsers.keySet(), null, null, null, null);
                    for (VKUser user : users) {
                        mapUsers.put(user.user_id, user);
                    }

                    for (VKMessage message : dialogs) {
                        dialogItems.add(new DialogItem(message, mapUsers.get(message.uid)));
                    }
                    mapUsers.clear();
                    users.clear();
                    users.trimToSize();
                    dialogs.clear();
                    dialogs.trimToSize();


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            swipeLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    /**
     * Удаление диалога
     * TODO: на метод наложено ограничение, за один вызов нельзя удалить больше 10000 сообщений
     */
    private void deleteDialog(final long user_id, final long chat_id) {
        if (!AndroidUtils.isInternetConnection(getActivity())) {
            Toast.makeText(getActivity(), R.string.check_internet, Toast.LENGTH_LONG).show();
            return;
        }

        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isEmpty = false;
                    while (!isEmpty) {
                        // если true - удалили все сообщения
                        // еще раз вызывать удаление не нужно
                        isEmpty = api.getMessagesHistory(user_id, chat_id, 0, 10).isEmpty();

                        api.deleteMessageDialog(user_id, chat_id);
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.getDialogs().remove(adapter.search(user_id, chat_id));
                            adapter.notifyDataSetChanged();

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.w(TAG, "onCreateOptionsMenu");
        // Inflate the menu;
        inflater.inflate(R.menu.main_menu, menu);

        // важные
        MenuItem itemImportant = menu.add(R.string.important_messages).setIcon(R.drawable.ic_star_white);
        MenuItemCompat.setShowAsAction(itemImportant, MenuItemCompat.SHOW_AS_ACTION_NEVER);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        }


        FileLogger.w("DialogFragment", "onCreateMenu = " + searchView);
        if (searchView != null) {
            final ArrayList<DialogItem> cleanDialogs = new ArrayList<>();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
            searchView.setQueryHint(getString(R.string.search));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    FileLogger.w("DialogFragment", "onQueryTextSubmit = " + s);
                    cleanDialogs.addAll(dialogItems);
                    searchDialogs(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    if (cleanDialogs.isEmpty()) return false;
                    adapter.getDialogs().clear();
                    adapter.getDialogs().addAll(cleanDialogs);
                    adapter.notifyDataSetChanged();

                    cleanDialogs.clear();
                    cleanDialogs.trimToSize();

                    onRefresh();
                    return false;
                }
            });
        }
        ViewUtil.setColors(menu, ((BaseThemedActivity) getActivity()).getToolbar());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        ViewUtil.setColors(menu, ((BaseThemedActivity) getActivity()).getToolbar());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(getActivity().getResources().getString(R.string.important_messages))) {
            startActivity(new Intent(getActivity(), ImportantMessagesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchDialogs(final String q) {
        swipeLayout.setRefreshing(true);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<VKMessage> searchDialogs = api.searchMessages(q, 0, 50, 0);
                    if (searchDialogs.isEmpty()) {
                        return;
                    }

                    HashMap<Long, VKUser> mapUsers = new HashMap<>();
                    for (VKMessage m : searchDialogs) {
                        mapUsers.put(m.uid, null);
                    }

                    ArrayList<VKUser> profiles = api.getProfiles(mapUsers.keySet(), null, "photo_50", null, null);
                    for (VKUser u : profiles) {
                        mapUsers.put(u.user_id, u);
                    }

                    dialogItems.clear();
                    for (VKMessage m : searchDialogs) {
                        dialogItems.add(new DialogItem(m, mapUsers.get(m.uid)));
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            swipeLayout.setRefreshing(false);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        setRefreshing(false);
        if (adapter != null) {
            adapter.disconnectLongPoll();
            adapter.clear();
            adapter = null;
        }
        super.onDestroy();
    }

}


