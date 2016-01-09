package ru.euphoriadev.vk.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.SparseArray;
import android.widget.RemoteViews;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import ru.euphoriadev.vk.BasicActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.service.LongPollService;

/**.
 * Класс помощик, который управляет уведомлениями
 * Created by Igor on 26.10.15.
 */
public class NotificationsHelper {
    public static final String TAG = "NotificationsHelper";
    private static NotificationsHelper instance;

    private SparseArray<Notification> sparseNotifications;
    private NotificationManagerCompat manager;
    private NotificationCompat.InboxStyle inboxStyle;

    private Context context;
    private int lastId = 0; // постоянно увеличивающееся поле, уникальный номер каждого уведомления

    private NotificationsHelper(Context context) {
        this.context = context;

        this.sparseNotifications = new SparseArray<>();
        this.manager = NotificationManagerCompat.from(context);

//        this.inboxStyle = new NotificationCompat.InboxStyle();
    }

    public synchronized static NotificationsHelper get(Context context) {
        if (instance == null) {
            instance = new NotificationsHelper(context);
        }
        return instance;
    }

    /**
     * Создание уведомления в статус бара
     * @param title текст заголовка
     * @param message текст сообщения
     * @param ticker текст, отображаемый вверху статус-бара при создании уведомления
     * @return id уведомления
     */
    public int createNotification(String title, String message, String ticker) {
        Intent intent = new Intent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
//                .setProgress()
                .setAutoCancel(true)
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setTicker(ticker)
                .setStyle(inboxStyle);

        Notification notification = builder.getNotification();
        manager.notify(lastId, notification);

        sparseNotifications.append(lastId, notification);
        return lastId++;
    }

    /**
     * Создание прогресс уведомления
     */
    public int createProgressNotification(String title, String message, String ticker, boolean indeterminate) {
        Intent intent = new Intent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setTicker(ticker)
//                .setCategory(Notification.CATEGORY_MESSAGE)
                .setProgress(0, 100, indeterminate)
                .setContentText(message)
                .setWhen(System.currentTimeMillis());

        Notification notification = builder.getNotification();
        // пользователь не сможет его удалить
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
        manager.notify(lastId, notification);

        sparseNotifications.append(lastId, notification);
        return lastId++;
    }

    public int createInboxNotification(final PendingIntent pIntent, final String message, final String userName, final String ticker, final String largeIcon, final String summaryText, final String newLine, final int number, boolean createNewImboxStyle) {

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (createNewImboxStyle) {
            inboxStyle = null;
        }
        if (inboxStyle == null) {
            inboxStyle = new NotificationCompat.InboxStyle(builder);
        }
        Picasso.with(context).load(largeIcon).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                builder.setContentIntent(pIntent)
                        .setContentTitle(userName)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setLargeIcon(bitmap)
                        .setContentText(message)
                        .setNumber(number)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(ticker);

                inboxStyle.setSummaryText(summaryText);
                inboxStyle.addLine(newLine);

                builder.setStyle(inboxStyle);

                Notification notification = builder.getNotification();
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enable_notify_vibrate", true))
                    notification.defaults = notification.defaults | Notification.DEFAULT_VIBRATE;
                manager.notify(500, notification);

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

     //   sparseNotifications.append(lastId, notification);
        return 500;
    }

//    public int createMessageNotification(PendingIntent pIntent, String userName, String message, String photoBigUrl, int number) {
//        Picasso.with(context).load(photoBigUrl).into(new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//
//
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//            }
//        });
//    }

    public boolean contains(int id) {
        for (int i = 0; i < sparseNotifications.size(); i++) {
            if (sparseNotifications.keyAt(i) == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Удаление всех уведомлений, которые когда то было созданы этим классом
     */
    public void removeAllNotifications() {
        for (int i = 0; i < sparseNotifications.size(); i++) {
            int id = sparseNotifications.keyAt(i);

            manager.cancel(id);
        }
        sparseNotifications.clear();
    }

    public void removeNotification(int nid) {
        sparseNotifications.remove(nid);
        manager.cancel(nid);
    }
}
