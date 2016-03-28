package ru.euphoriadev.vk.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.napi.VKApi;
import ru.euphoriadev.vk.util.AndroidUtils;

/**
 * Created by Igor on 17.03.16.
 */
public class UploadService extends Service {
    private static final String TAG = "UploadService";

    private static final String DATA = "data";
    private VKApi.VKDocUploader uploader;
    private NotificationCompat.Builder builder;
    private ArrayList<File> uploadingFiles;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate");

        uploader = VKApi.getDocUploader();
        uploadingFiles = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final File file = (File) intent.getSerializableExtra(DATA);
            if (!file.exists()) {
                return START_NOT_STICKY;
            }
            final int id = file.hashCode();
            uploader.uploadFile(file, new VKApi.VKOnProgressListener<VKDocument>() {
                @Override
                public void onStart(File file) {
                    updateNotification(file.getName(), 0, false, id);
                    uploadingFiles.add(file);
                }

                @Override
                public void onProgress(byte[] buffer, int progress, long totalSize) {
                    updateNotification(String.format("%s / %s",
                            AndroidUtils.convertBytes(AndroidUtils.getCurrentSizeBy(progress, totalSize)),
                            AndroidUtils.convertBytes(totalSize)), progress, false, id);
                }

                @Override
                public void onSuccess(VKDocument uploadObject) {
                    updateNotification(null, 100, true, id);
                    stopAfterEmpty(file);
                }

                @Override
                public void onError(VKApi.VKException e) {
                    updateNotification(null, -1, false, id);
                    stopAfterEmpty(file);
                }
            });
        }
        return START_STICKY;
    }

    public static void upload(Context context, File file) {
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra(DATA, file);
        context.startService(intent);
    }

    private void stopAfterEmpty(File file) {
        uploadingFiles.remove(file);
        if (uploadingFiles.isEmpty()) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.w(TAG, "onDestroy");
    }

    private int mLastId;
    private void updateNotification(String message, int progress, boolean success, int notificationId) {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        if (mLastId != notificationId) {
            mLastId = notificationId;
            builder = null;
        }
        if (builder == null) {
            builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setContentTitle(message)
                    .setTicker(message)
                    .setProgress(100, 0, false)
                    .setWhen(System.currentTimeMillis());
        } else if (success) {
            builder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
            builder.setProgress(0, 0, false);
            builder.setWhen(System.currentTimeMillis());
            builder.setContentText(getResources().getString(R.string.success));
            builder.setTicker(getResources().getString(R.string.success));
        } else if (progress == -1) {
            builder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
            builder.setTicker(getResources().getString(R.string.error));
            builder.setProgress(0, 0, false);
            builder.setContentText(getResources().getString(R.string.error));
        } else {
            builder.setTicker(null);
            builder.setContentText(message);
            builder.setProgress(100, progress, false);
        }


        Notification notification = builder.getNotification();
        if (!success && progress != -1) {
            // пользователь не сможет его удалить
            notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
        }
        managerCompat.notify(notificationId, notification);

    }
}
