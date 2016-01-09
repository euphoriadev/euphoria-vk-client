package ru.euphoriadev.vk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.helper.MediaPlayerHelper;
import ru.euphoriadev.vk.service.PlayMusicService;

/**
 * Created by user on 03.12.15.
 */
public class MediaPlayerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        final VKAudio value = (VKAudio) intent.getSerializableExtra("value");

        switch (action) {
            case PlayMusicService.ACTION_PLAY:
                MediaPlayerHelper.playAudio(context, value);
                break;

            case PlayMusicService.ACTION_PAUSE:
                MediaPlayerHelper.pausePlayAudio(context);
                break;

            case PlayMusicService.ACTION_STOP:
                MediaPlayerHelper.stopPlayAudio(context);
                break;

            case PlayMusicService.ACTION_CLOSE:
                MediaPlayerHelper.stopService(context);
                break;

        }
    }
}
