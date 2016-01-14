package ru.euphoriadev.vk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.util.Account;

public class EternallOnlineService extends Service {

    private Api api;
    private Account account;
    private boolean keepEternalOnline;
    private boolean keepPhantomlOnline;
    private String lastAction = "";

    public EternallOnlineService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        api = Api.get();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("EternallOnlineService", "onStartCommand");
        final String action = intent != null ? intent.getStringExtra("online_status").toUpperCase() : "";
        if (lastAction.equals(action)) {
            // попытка запустить один и то же статус второй раз, ничего не делаем
            return START_NOT_STICKY;
        }
        lastAction = action;
        switch (action) {
            case "ETERNAL":
                keepPhantomlOnline = false;
                keepEternalOnline = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (keepEternalOnline) {
                            try {
                                // обновляем статус каждые пять минут
                                // вечный онайлн
                                api.setOnline(null, null);
                                TimeUnit.MINUTES.sleep(5);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();
                break;

            case "PHANTOM":
                keepEternalOnline = false;
                keepPhantomlOnline = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (keepPhantomlOnline) {
                            try {
                                // тут придется обновлять каждую 15 сек
                                // однако фишка в том, что дата последнего захода не изменится
                                api.setOffline();
                                TimeUnit.SECONDS.sleep(14);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();
                break;

            case "OFF":
                keepEternalOnline = false;
                keepPhantomlOnline = false;
                stopSelf();
                // оффлайн

        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.w("EternallOnlineService", "onDestroy");
    }
}
