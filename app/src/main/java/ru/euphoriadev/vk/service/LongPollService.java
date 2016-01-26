package ru.euphoriadev.vk.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.euphoriadev.vk.MessageHistoryActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKLongPollServer;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.helper.NotificationsHelper;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.FileLogger;

/**
 * Created by Igor on 13.11.15.
 */
public class LongPollService extends Service {

    public static final String TAG = "VKOnLongPoll";
    public static int messageCount = 0;
    @Deprecated
    private SparseArray<VKOnLongPollListener> mListeners;
    private VKOnDialogListener dialogListener;
    private Handler mHandler;
    private Thread mThread;
    private Api api;
    private SharedPreferences preferences;
    private NotificationsHelper notificationsHelper;
    private LocalBinder mBinder = new LocalBinder();
    private boolean isRunning;
    private int lastSendMessageUid;

    @Override
    public void onCreate() {
        super.onCreate();
        FileLogger.i(TAG, "onCreate");

        mListeners = new SparseArray<>();
        mHandler = new Handler(Looper.getMainLooper());
        notificationsHelper = NotificationsHelper.get(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isRunning = true;

        api = Api.get();
        Runnable longPollRun = new Runnable() {
            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    Log.w(TAG, "LongPoll started!");
                    VKLongPollServer pollServer = api.getLongPollServer(null, null);

                    while (isRunning) {
                        if (!AndroidUtils.isInternetConnection(LongPollService.this)) {
                            Thread.sleep(5000);
                            continue;
                        }
                        String request = "http://" + pollServer.server + "?act=a_check&key=" + pollServer.key + "&ts=" + pollServer.ts + "&wait=25&mode=2";
                        String response = Api.sendRequestInternal(request, "", false);
                        JSONObject root = new JSONObject(response);

                        if (root.has("failed")) {
                            // произошла ошибка, пробуем сначала
                            Log.w(TAG, "JSON has failed: " + root.toString());
                            Thread.sleep(1500);
                            pollServer = api.getLongPollServer(null, null);
                            continue;
                        }
                        long tsResponse = root.optLong("ts");
                        JSONArray updates = root.getJSONArray("updates");

                        Log.w(TAG, "while...");
                        Log.i(TAG, "response = " + updates);

                        if (updates.length() == 0) {
                            pollServer.ts = tsResponse;
                        } else {
                            pollServer.ts = tsResponse;
                            processResponse(updates);
                        }


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        this.mThread = new Thread(longPollRun);
        this.mThread.start();
    }

    public void register(String tag, VKOnLongPollListener listener) {
        if (mListeners.get(tag.hashCode()) == null) {
            mListeners.put(tag.hashCode(), listener);
        }
    }

    public boolean inRegistered(String tag) {
        return mListeners.get(tag.hashCode()) != null;
    }

    public void unregister(String tag) {
        mListeners.remove(tag.hashCode());
    }

    public void unregisterAll() {
        mListeners.clear();
        dialogListener = null;
    }

    public void setDialogListener(VKOnDialogListener listener) {
        this.dialogListener = listener;
    }

    private void runOnUiThread(Runnable command) {
        // если из новго потока
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(command);
        } else {
            command.run();
        }
    }


    /**
     * Отправляем запрос каждые 30 сек на сервер,
     * что бы узнать, пришли ли новые сообщения
     *
     * @param array
     */
    private void processResponse(final JSONArray array) {

        if (mListeners == null) {
            return;
        }
        if (array.length() == 0) {
            return;
        }
        for (int i = 0; i < array.length(); ++i) {
            if (!isRunning) break;

            final int finalI = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final JSONArray arrayItem = (JSONArray) array.opt(finalI);
                        int type = (Integer) arrayItem.opt(0);

                        /**
                         0,$message_id,0 — удаление сообщения с указанным local_id
                         1,$message_id,0 — удаление сообщения с указанным local_id
                         2,$message_id,$mask[,$user_id] — установка флагов сообщения (FLAGS|=$mask)
                         3,$message_id,$mask[,$user_id] — сброс флагов сообщения (FLAGS&=~$mask)
                         4,$message_id,$flags,$from_id,$timestamp,$subject,$text,$attachments — добавление нового сообщения
                         6,$peer_id,$local_id — прочтение всех входящих сообщений с $peer_id вплоть до $local_id включительно
                         7,$peer_id,$local_id — прочтение всех исходящих сообщений с $peer_id вплоть до $local_id включительно
                         8,-$user_id,$extra — друг $user_id стал онлайн, $extra не равен 0, если в mode был передан флаг 64, в младшем байте (остаток от деления на 256) числа $extra лежит идентификатор платформы (таблица ниже)
                         9,-$user_id,$flags — друг $user_id стал оффлайн ($flags равен 0, если пользователь покинул сайт (например, нажал выход) и 1, если оффлайн по таймауту (например, статус away))

                         51,$chat_id,$self — один из параметров (состав, тема) беседы $chat_id были изменены. $self - были ли изменения вызваны самим пользователем

                         61,$user_id,$flags — пользователь $user_id начал набирать текст в диалоге. событие должно приходить раз в ~5 секунд при постоянном наборе текста. $flags = 1
                         62,$user_id,$chat_id — пользователь $user_id начал набирать текст в беседе $chat_id.

                         70,$user_id,$call_id — пользователь $user_id совершил звонок имеющий идентификатор $call_id.
                         80,$count,0 — новый счетчик непрочитанных в левом меню стал равен $count.
                         */

                        switch (type) {
                            // Новое сообщение
                            case 4:
                                final VKMessage message = VKMessage.parse(arrayItem);
                                if (message == null) return;


                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.valueAt(i).onNewMessage(message);

                                }
                                if (dialogListener != null) {
                                    dialogListener.onNewMessage(message);
                                }

                                if (preferences.getBoolean("enable_notify", true)) {
                                    if (message.is_out) {
                                        break;
                                    }

//                                    if (mListeners.indexOfKey("MessageAdapter".hashCode()) == -1) {
//                                        // мы сейчас в этом диалоге
//                                    }
                                    VKUser user = DBHelper.get(LongPollService.this).getUserFromDB(message.uid);
                                    if (user != null) {
                                        Intent intent = new Intent(LongPollService.this, MessageHistoryActivity.class);
                                        intent.putExtra("user_id", message.uid);
                                        intent.putExtra("chat_id", message.chat_id);
                                        intent.putExtra("fullName", message.isChat() ? message.title : user.toString());
                                        intent.putExtra("photo_50", user.photo_50);
                                        intent.putExtra("users_count", message.users_count);
                                        intent.putExtra("online", user.online);
                                        intent.putExtra("from_saved", false);
                                        intent.putExtra("from_service", true);
                                        messageCount++;
                                        PendingIntent pIntent = PendingIntent.getActivity(LongPollService.this, 0, intent, 0);
                                        String textMessage = user.first_name + ": " + message.body;
                                        String textSummary = messageCount + " " + (getApplicationContext().getResources().getString(R.string.new_messages));
                                        notificationsHelper.createInboxNotification(pIntent, textMessage, user.toString(), textMessage, user.photo_50, textSummary, textMessage, messageCount, lastSendMessageUid != message.uid);
                                    }
                                    lastSendMessageUid = (int) message.uid;
                                }
                            break;

                            // прочитал сообщение
                            case 6:
                            case 7:
                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.valueAt(i).onReadMessage(arrayItem.getInt(2));
                                }

//                                if (preferences.getBoolean("enable_notify", true) {
//
//                                }
                                break;


                            // пользователь набираует сообщение
                            case 61:
                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.valueAt(i).onUserTyping(0, arrayItem.optLong(1));
                                }
                                break;

                            // пользователь набирает текст В ЧАТЕ
                            case 62:

                                for (int i = 0; i < mListeners.size(); i++) {
                                    mListeners.valueAt(i).onUserTyping(arrayItem.optLong(2),
                                            arrayItem.optLong(1));
                                }
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileLogger.i(TAG, "onDestroy");

        isRunning = false;
        mHandler = null;
        mListeners.clear();
        dialogListener = null;
    }


    public interface VKOnLongPollListener {
        /**
         * Пришло новое сообщение
         *
         * @param message
         */
        void onNewMessage(VKMessage message);

        /**
         * Пользователь начал набирать текст
         *
         * @param uid
         * @param chat_id TODO: if (chat_id != 0) is chat
         */
        void onUserTyping(long chat_id, long uid);

        /**
         * Пользователь прочитал мое сообщение
         */
        void onReadMessage(long message_id);

    }

    public interface VKOnDialogListener {

        void onNewMessage(VKMessage message);
    }

    public class LocalBinder extends Binder {

        public LongPollService getService() {
            return LongPollService.this;
        }
    }

}
