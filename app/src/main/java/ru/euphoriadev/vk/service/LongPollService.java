package ru.euphoriadev.vk.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

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
import ru.euphoriadev.vk.util.VKUpdateController;

/**
 * Created by Igor on 13.11.15.
 * <p/>
 * The operating principle of a Long Poll connection is that the
 * server withholds the request it receives until an event occurs or
 * the time indicated in the wait parameter runs out (since some proxy servers
 * terminate the connection after 30 seconds.
 * <p/>
 * See for more: http://vk.com/dev/using_longpoll
 */
public class LongPollService extends Service {

    public static final String TAG = "VKOnLongPoll";
    public static int messageCount = 0;
    private Handler mHandler;
    private Thread mThread;
    private Api api;
    private SharedPreferences preferences;
    private NotificationsHelper notificationsHelper;
    private boolean isRunning;
    private int lastSendMessageUid;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        mHandler = new Handler(Looper.getMainLooper());
        notificationsHelper = NotificationsHelper.get(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isRunning = true;

        api = Api.get();
        LongPollRunner runner = new LongPollRunner();
        this.mThread = new Thread(runner);
        this.mThread.start();
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
        if (array.length() == 0) {
            return;
        }

        // that would not be falsity positives
        final boolean[] useCase7 = {true};
        for (int i = 0; i < array.length(); ++i) {
            if (!isRunning) break;

            final int finalI = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final JSONArray arrayItem = array.optJSONArray(finalI);
                        int type = (Integer) arrayItem.opt(0);

                        /**
                         0,$message_id,0 — удаление сообщения с указанным local_id
                         1,$message_id,$flags — замена флагов сообщения (FLAGS:=$flags)
                         2,$message_id,$mask[,$user_id] — установка флагов сообщения (FLAGS|=$mask)
                         3,$message_id,$mask[,$user_id] — сброс флагов сообщения (FLAGS&=~$mask)
                         4,$message_id,$flags,$from_id,$timestamp,$subject,$text,$attachments — добавление нового сообщения. Если сообщение отправлено в беседе, $from_id содержит id беседы + 2000000000.
                         6,$peer_id,$local_id — прочтение всех входящих сообщений с $peer_id вплоть до $local_id включительно
                         7,$peer_id,$local_id — прочтение всех исходящих сообщений с $peer_id вплоть до $local_id включительно
                         8,-$user_id,$extra — друг $user_id стал онлайн, $extra не равен 0, если в mode был передан флаг 64, в младшем байте (остаток от деления на 256) числа $extra лежит идентификатор платформы
                         9,-$user_id,$flags — друг $user_id стал оффлайн ($flags равен 0, если пользователь покинул сайт (например, нажал выход) и 1, если оффлайн по таймауту (например, статус away))


                         10,$peer_id,$mask — сброс флагов фильтрации по папкам для чата/собеседника с $peer_id. Соответствует операции directories &= ~mask.
                         11,$peer_id,$flags — замена флагов фильтрации по папкам для чата/собеседника с $peer_id.
                         12,$peer_id,$mask — установка флагов фильтрации по папкам для чата/собеседника с $peer_id. Соответствует операции directories |= mask.


                         13,$peer_id,$flags — замена флагов всех сообщений с заданным peer_id (применяется только к сообщениям, у которых на текущий момент не установлены флаги 128 (deleted) и 64 (spam))
                         14,$peer_id,$mask — установка флагов всех сообщений с заданным peer_id (FLAGS|=$mask) (применяется только к сообщениям, у которых на текущий момент не установлены флаги 128 (deleted) и 64 (spam))
                         15,$peer_id,$mask — сброс флагов всех сообщений с заданным peer_id (FLAGS&=~$mask) (применяется только к сообщениям, у которых на текущий момент не установлены флаги 128 (deleted) и 64 (spam))


                         51,$chat_id,$self — один из параметров (состав, тема) беседы $chat_id были изменены. $self - были ли изменения вызваны самим пользователем
                         61,$user_id,$flags — пользователь $user_id начал набирать текст в диалоге. событие должно приходить раз в ~5 секунд при постоянном наборе текста. $flags = 1
                         62,$user_id,$chat_id — пользователь $user_id начал набирать текст в беседе $chat_id.
                         70,$user_id,$call_id — пользователь $user_id совершил звонок имеющий идентификатор $call_id.
                         80,$count,0 — новый счетчик непрочитанных в левом меню стал равен $count.
                         114,{ $peerId, $sound, $disabled_until } — изменились настройки оповещений, где peerId — $peer_id чата/собеседника, sound — 1 || 0, включены/выключены звуковые оповещения, disabled_until — выключение оповещений на необходимый срок (-1: навсегда, 0: включены, other: timestamp, когда нужно включить).
                         */

                        switch (type) {
                            // удалил сообщение
                            case 1:
                                VKUpdateController.getInstance().updateMessageListenersForDelete(arrayItem.optInt(1));
                                break;
                            // Новое сообщение
                            case 4:
                                final VKMessage message = VKMessage.parse(arrayItem);
                                if (message == null) break;

                                useCase7[0] = false;
                                VKUpdateController.getInstance().updateMessageListenersForNew(message);

                                if (preferences.getBoolean("enable_notify", true)) {
                                    if (message.is_out) {
                                        break;
                                    }


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
                                    lastSendMessageUid = message.uid;
                                }
                                break;

                            // прочитал сообщение
                            case 6:
                            case 7:
                                if (useCase7[0]) {
                                    VKUpdateController.getInstance().updateMessageListenersForRead(arrayItem.getInt(2));
                                }
                                break;

                            // друг стал онлайн
                            case 8:
                                VKUpdateController.getInstance().updateUserListenersForOnline(Math.abs(arrayItem.optInt(1)));
                                break;

                            // друг стал оффлайн
                            case 9:
                                VKUpdateController.getInstance().updateUserListenersForOffline(Math.abs(arrayItem.optInt(1)));
                                break;

                            // пользователь набираует сообщение
                            case 61:
                                VKUpdateController.getInstance().updateUserListenersForTyping(arrayItem.optInt(1), 0);
                                break;

                            // пользователь набирает текст В ЧАТЕ
                            case 62:
                                VKUpdateController.getInstance().updateUserListenersForTyping(arrayItem.optInt(1), arrayItem.optInt(2));
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
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        isRunning = false;
        mHandler = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class LongPollRunner implements Runnable {

        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Log.w(TAG, "LongPoll started!");

                VKLongPollServer pollServer;
                // we try as long until the user will not have Internet connection
                while (true) {
                    // user do not have Internet connection
                    if (!AndroidUtils.hasConnection(LongPollService.this)) {
                        Thread.sleep(3000);
                        continue;
                    }
                    pollServer = Api.get().getLongPollServer(null, null);
                    break;
                }

                while (isRunning) {
                    try {
                        if (!AndroidUtils.hasConnection(LongPollService.this)) {
                            Thread.sleep(3000);
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

                        Log.i(TAG, "response = " + updates);

                        if (updates.length() == 0) {
                            pollServer.ts = tsResponse;
                        } else {
                            pollServer.ts = tsResponse;
                            processResponse(updates);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
