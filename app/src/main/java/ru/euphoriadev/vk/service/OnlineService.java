package ru.euphoriadev.vk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import ru.euphoriadev.vk.SettingsFragment;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.PrefManager;

public class OnlineService extends IntentService {

    private Api api;
    private Account account;
    private boolean keepEternalOnline;
    private boolean keepPhantomlOnline;
    private String lastAction = "";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public OnlineService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        api = Api.get();
        PrefManager.putBoolean(SettingsFragment.KEY_IS_LIVE_ONLINE_SERVICE, true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent != null ? intent.getStringExtra("online_status").toUpperCase() : "";
        if (lastAction.equals(action)) {
            // попытка запустить один и то же статус второй раз, ничего не делаем
            return;
        }
        lastAction = action;
        switch (action) {
            case "ETERNAL":
                keepPhantomlOnline = false;
                keepEternalOnline = true;
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

                break;

            case "PHANTOM":
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

                break;

            case "OFF":
                keepEternalOnline = false;
                keepPhantomlOnline = false;
                stopSelf();
                // оффлайн
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PrefManager.putBoolean(SettingsFragment.KEY_IS_LIVE_ONLINE_SERVICE, true);

        Log.w("OnlineService", "onDestroy");
    }
}
