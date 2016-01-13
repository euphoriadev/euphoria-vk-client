package ru.euphoriadev.vk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import java.util.concurrent.TimeUnit;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.ThreadExecutor;

/**
 * Created by Igor on 06.09.15.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("resend_failed_msg", false)) {
            // не включен RFN
            return;
        }
        if (!AndroidUtils.isInternetConnection(context)) {
            // нет интернет подключения
            return;
        }
        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper helper = DBHelper.get(context);
                    SQLiteDatabase database = helper.getWritableDatabase();

                    Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.FAILED_MESSAGES_TABLE, null);
                    if (cursor.getCount() > 0) {
                        Account account = new Account(context).restore();
                        Api api = Api.init(account);

                        while (cursor.moveToNext()) {
                            int _id = cursor.getInt(cursor.getColumnIndex(DBHelper._ID));
                            int user_id = cursor.getInt(cursor.getColumnIndex(DBHelper.USER_ID));
                            int chat_id = cursor.getInt(cursor.getColumnIndex(DBHelper.CHAT_ID));
                            String body = cursor.getString(cursor.getColumnIndex(DBHelper.BODY));

                            api.sendMessage((long) user_id, (long) chat_id, body, null, null, null, null, null, null, null, null);
                            database.delete(DBHelper.FAILED_MESSAGES_TABLE, DBHelper._ID + " = " + _id, null);

                            TimeUnit.MILLISECONDS.sleep(200);
                        }
                    }
                    cursor.close();
                    helper.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
