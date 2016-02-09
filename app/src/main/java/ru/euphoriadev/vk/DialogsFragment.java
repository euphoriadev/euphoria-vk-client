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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseArray;
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
import java.util.HashSet;
import java.util.List;

import ru.euphoriadev.vk.adapter.DialogAdapter;
import ru.euphoriadev.vk.adapter.DialogItem;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.interfaces.RunnableToast;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.PrefManager;
import ru.euphoriadev.vk.util.ThemeManager;
import ru.euphoriadev.vk.util.ThemeManagerOld;
import ru.euphoriadev.vk.util.ThreadExecutor;
import ru.euphoriadev.vk.util.VKInsertHelper;
import ru.euphoriadev.vk.util.ViewUtil;
import ru.euphoriadev.vk.view.fab.FloatingActionButton;
import ru.euphoriadev.vk.vkapi.VKApi;

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
        ((BaseThemedActivity) getActivity())
                .getSupportActionBar()
                .setSubtitle(activity.getString(R.string.dialogs_number) + PrefManager.getInt("message_count", 0));
        ((BasicActivity) getActivity()).getToolbar();
        ViewUtil.setColor(((BasicActivity) getActivity()).getToolbar(), ThemeManager.getPrimaryTextColorOnThemeColor(getActivity()));

        listView = (ListView) rootView.findViewById(R.id.lvMess);
        AndroidUtils.setEdgeGlowColor(listView, ThemeManager.getThemeColor(getActivity()));
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


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setColorNormal(ThemeManager.getColorAccent(getActivity()));
//        fab.setImageDrawable(iconPlus);
        fab.setShadow(true);
        fab.setColorPressed(ViewUtil.getPressedColor(fab.getColorNormal()));
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


    private void downloadDialog(final int id, final int chatid, final String userName) {
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
    private void getDialogsFrom(SQLiteDatabase database) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.DIALOGS_TABLE +
                " LEFT JOIN " + DBHelper.USERS_TABLE +
                " ON " + DBHelper.DIALOGS_TABLE + "." + DBHelper.USER_ID +
                " = " + DBHelper.USERS_TABLE + "." + DBHelper.USER_ID, null);

        // Cursor is empty
        if (cursor.getCount() <= 0) {
            cursor.close();
            return;
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

            dialogItems.add(new DialogItem(m, u));
        }
        cursor.close();
    }

    /**
     * Вставка или обновление пользователей в таблицу
     *
     * @param database база данных
     * @param users    список пользователей, которые улетят в бд
     */
    private void insertUsersTo(SQLiteDatabase database, List<VKUser> users, boolean withTransaction) {
        VKInsertHelper.updateUsers(database, users, withTransaction);
    }

    /**
     * Вставка сообщений в таблицу {@link DBHelper#DIALOGS_TABLE}
     *
     * @param database        бд
     * @param messages        список сообщений, которые необходимо занести в бд
     * @param withTransaction применить транзекцию, для более быстрой вставки
     */
    private void insertMessagesTo(SQLiteDatabase database, ArrayList<VKMessage> messages, boolean withTransaction) {
        VKInsertHelper.insertDialogs(database, messages, withTransaction);
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
        new AsyncLoadDialogsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if (AndroidUtils.isInternetConnection(getActivity())) {
            VKApi.messages().getDialogs().count(1).execute(new VKApi.VKOnResponseListener() {
                @Override
                public void onResponse(JSONObject responseJson) {
                    final JSONObject response = responseJson.optJSONObject("response");
                    if (response == null) {
                        return;
                    }
                    int messageCount = response.optInt("count");
                    PrefManager.putInt("message_count", messageCount);
                    if (getActivity() == null || ((BasicActivity) getActivity()).getSupportActionBar() == null) {
                        return;
                    }
                    ((BaseThemedActivity) getActivity())
                            .getSupportActionBar()
                            .setSubtitle(activity.getString(R.string.dialogs_number) + messageCount);
                }

                @Override
                public void onError(VKApi.VKException exception) {

                }
            });
        }


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


    private void loadOldDialogs(final int count) {
        swipeLayout.setRefreshing(true);
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<Integer, VKUser> mapUsers = new HashMap<>();

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
                        isEmpty = api.getMessagesHistory(user_id, chat_id, 0, 10, false).isEmpty();

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


        Log.w("DialogFragment", "onCreateMenu = " + searchView);
        if (searchView != null) {
            final ArrayList<DialogItem> cleanDialogs = new ArrayList<>();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
            searchView.setQueryHint(getString(R.string.search));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    Log.w("DialogFragment", "onQueryTextSubmit = " + s);
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

                    HashMap<Integer, VKUser> mapUsers = new HashMap<>();
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
        ((BasicActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        setRefreshing(false);
        if (adapter != null) {
            adapter.disconnectLongPoll();
            adapter.clear();
            adapter = null;
        }
        super.onDestroy();
    }

    private class AsyncLoadDialogsTask extends AsyncTask<Void, Boolean, Void> {
        private final String TAG = "AsyncLoadDialogsTask";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.w(TAG, "onPreExecute: start");
            setRefreshing(true);

            if (dialogItems == null) {
                dialogItems = new ArrayList<>(30);
            }
            database = DBHelper.get(getActivity()).getWritableDatabase();;

            // get dialogs from database
            getDialogsFrom(database);
            if (!dialogItems.isEmpty()) {
                updateAdapter();
            }
            Log.w(TAG, "onPreExecute: end");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.w(TAG, "onPostExecute: start");
            setRefreshing(false);

            System.gc();
            Log.w(TAG, "onPostExecute: end");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            Log.w(TAG, "doInBackground: start");
            // if user not have internet connection
            if (!AndroidUtils.isInternetConnection(getActivity())) {
                AndroidUtils.runOnUi(new RunnableToast(getActivity(), R.string.check_internet, true));
                return null;
            }

            // load messages from network;
            Log.w(TAG, "download messages from net....");
            ArrayList<VKMessage> newDialogs = getDialogsFromNet();
            Log.w(TAG, "downloading done.");
            if (newDialogs.isEmpty()) {
                return null;
            }

            SparseArray<VKUser> users = new SparseArray<>(30);
            for (int i = 0; i < newDialogs.size(); i++) {
                VKMessage dialog = newDialogs.get(i);
                users.append(dialog.uid, null);
            }

            HashSet<Integer> keySet = AndroidUtils.keySet(users);
            // load new users from network
            Log.w(TAG, "download users from net...");
            ArrayList<VKUser> newUsers = getUsersFromNet(keySet);
            Log.w(TAG, "download users done.");

            users.clear();
            for (int i = 0; i < newUsers.size(); i++) {
                VKUser user = newUsers.get(i);
                users.put(user.user_id, user);
            }

            // update items in ListView for show new content
            dialogItems.clear();
            for (int i = 0; i < newDialogs.size(); i++) {
                VKMessage message = newDialogs.get(i);
                dialogItems.add(new DialogItem(message, users.get(message.uid)));
            }
            publishProgress(null);
            setRefreshing(false);

            // update database
            deleteMessagesFrom(database);
            insertMessagesTo(database, newDialogs, true);
            insertUsersTo(database, newUsers, true);

            newDialogs.clear();
            newDialogs.trimToSize();
            newDialogs = null;

            newUsers.clear();
            newUsers.trimToSize();
            newUsers = null;

            keySet.clear();
            keySet = null;

            users.clear();
            users = null;
            return null;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
            updateAdapter();
        }

        private void updateAdapter() {
            if (adapter == null) {
                adapter = new DialogAdapter(getActivity(), dialogItems);
                listView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

            if (!adapter.isConnected()) {
                adapter.connectToLongPollService();
            }
        }

        private ArrayList<VKMessage> getDialogsFromNet() {
            try {
                return Api.get().getMessagesDialogs(0, 30, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>(0);
        }

        private ArrayList<VKUser> getUsersFromNet(Collection<Integer> uids) {
            try {
                return Api.get().getProfiles(uids, null, null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>(0);
        }

    }

}


