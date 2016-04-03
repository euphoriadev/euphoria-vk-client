package ru.euphoriadev.vk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.euphoriadev.vk.async.LowThread;
import ru.euphoriadev.vk.napi.VKApi;

/**
 * Created by user on 31.03.16.
 */
public class CrazyTypingService extends Service {
    private static final String TAG = "CrazyTypingService";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_REMOVE = "remove";
    public static final String PEER_ID = "peer_id";

    private static List<Long> users = Collections.synchronizedList((new ArrayList<Long>()));
    private TypingThread thread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        thread = new TypingThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            users.clear();
            stopSelf();
            return START_NOT_STICKY;
        }
        long peerId = intent.getLongExtra(PEER_ID, 0);
        String action = intent.getAction();
        switch (action) {
            case ACTION_ADD:
                users.add(peerId);
                Log.i(TAG, "onStartCommand: add peer id " + peerId);
                Toast.makeText(CrazyTypingService.this, "Crazy typing для этого диалога включен", Toast.LENGTH_SHORT).show();
                break;

            case ACTION_REMOVE:
                users.remove(peerId);
                Log.i(TAG, "onStartCommand: remove peer id " + peerId);
                Toast.makeText(CrazyTypingService.this, "Crazy typing для этого диалога выключен", Toast.LENGTH_SHORT).show();
                break;
        }
        if (users.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!thread.isAlive() && thread.getState() == Thread.State.NEW) {
            thread.start();
        }
        return START_STICKY;
    }

    public static boolean contains(long peerId) {
        return users.contains(peerId);
    }

    private class TypingThread extends LowThread {
        @Override
        public void run() {
            super.run();
            Log.i(TAG, "TypingThread is running");

            while (!users.isEmpty()) {
                try {
                    for (int i = 0; i < users.size(); i++) {
                        VKApi.messages()
                                .setActivity()
                                .peerId(users.get(i))
                                .executeTry();
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
