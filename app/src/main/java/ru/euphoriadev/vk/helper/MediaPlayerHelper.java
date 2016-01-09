package ru.euphoriadev.vk.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.session.PlaybackState;
import android.os.IBinder;

import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.service.PlayMusicService;

/**
 * Created by Igor on 02.12.15.
 */
public class MediaPlayerHelper {
    public static final String ACTION = "action";
    public static final String VALUE = "value";

    /**
     * Запуск сервиса {@link PlayMusicService}
     *
     * @param context текущая активити
     */
    public static void startService(Context context) {
        Intent starter = new Intent(context, PlayMusicService.class);
        context.startService(starter);
    }

    /**
     * Попытка начать воспроизведение песни по указанной ссылке.
     * Если ссылка отличается от уже проигрываемой,
     * то начнется воспроизведение уже этой ссылки
     *
     * @param context текущая активити
     */
    public static void playAudio(Context context, VKAudio audio) {
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_PLAY);
        intent.putExtra(VALUE, audio);


        context.startService(intent);
    }

    /**
     * Останавливает воспроизведение
     *
     * @param context
     */
    public static void stopPlayAudio(Context context) {
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_STOP);

        context.startService(intent);
    }

    public static void pausePlayAudio(Context context) {
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_PAUSE);

        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_CLOSE);

        context.stopService(intent);
    }

    public static void playNextAudio(Context context) {
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_NEXT_AUDIO);

        context.startService(intent);
    }

    public static void playPrevAudio(Context context) {
        Intent intent = new Intent(context, PlayMusicService.class);
        intent.setAction(PlayMusicService.ACTION_PREV_AUDIO);

        context.startService(intent);
    }
}
