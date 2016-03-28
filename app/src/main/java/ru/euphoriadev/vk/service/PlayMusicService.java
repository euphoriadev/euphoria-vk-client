package ru.euphoriadev.vk.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import ru.euphoriadev.vk.MusicPlayerActivity;
import ru.euphoriadev.vk.R;
import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.helper.DBHelper;

/**
 * Created by Igor on 06.10.15.
 */
public class PlayMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "ru.euphoriadev.vk.ACTION_PLAY";
    public static final String ACTION_PAUSE = "ru.euphoriadev.vk.ACTION_PAUSE";
    public static final String ACTION_STOP = "ru.euphoriadev.vk.ACTION_STOP";
    public static final String ACTION_CLOSE = "ru.euphoriadev.vk.ACTION_CLOSE";
    public static final String ACTION_NEXT_AUDIO = "ru.euphoriadev.vk.ACTION_NEXT";
    public static final String ACTION_PREV_AUDIO = "ru.euphoriadev.vk.ACTION_PREV";
    private final static String TAG = "PlayMusicService";

    private String lastPlayUrl;
    private VKAudio mAudio;
    private MediaPlayer mMediaPlayer;
    private SQLiteDatabase mDatabase;
    private NotificationCompat.Builder mBuilder;
    private LocalBinder mBinder = new LocalBinder();


    @Override
    public void onCreate() {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();
        mDatabase = DBHelper.get(this).getWritableDatabase();
    }

    private void createNotification(VKAudio audio) {
        Intent starterActivity = new Intent(getApplicationContext(), MusicPlayerActivity.class);
        starterActivity.putExtra("audio", mAudio);

        Intent playIntent = new Intent(getApplicationContext(), PlayMusicService.class).setAction(ACTION_PLAY);
        Intent pauseIntent = new Intent(getApplicationContext(), PlayMusicService.class).setAction(ACTION_PAUSE);
        Intent prevIntent = new Intent(getApplicationContext(), PlayMusicService.class).setAction(ACTION_PREV_AUDIO);
        Intent nextIntent = new Intent(getApplicationContext(), PlayMusicService.class).setAction(ACTION_NEXT_AUDIO);

        if (mBuilder == null) {
            mBuilder = new NotificationCompat.Builder(this);
        }
        if (audio == null) {
            audio = mAudio;
        }
//        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        mBuilder.setContentTitle(audio.title)
                .setContentText(audio.artist)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, starterActivity, 0))
                .addAction(R.drawable.ic_fast_rewind, null, PendingIntent.getService(getApplicationContext(), 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(audio.isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow, null, PendingIntent.getService(getApplicationContext(), 0, mAudio.isPlaying ? pauseIntent : playIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(R.drawable.ic_fast_forward, null, PendingIntent.getService(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

//        ComponentName componentName = new ComponentName(getApplicationContext(), MediaPlayerReceiver.class);
//        MediaMetadataCompat.Builder metadata = new MediaMetadataCompat.Builder();
//        metadata.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audio.artist)
////                .putString(MediaMetadata.METADATA_KEY_ALBUM, audio.l))
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audio.title)
//                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audio.duration).build();
////                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, audio.)
////                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, songs.size())
////                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
//
//
//        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
//                .setActions(PlaybackStateCompat.ACTION_PLAY)
//                .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
//                .build();
//
//        MediaSessionCompat mediaSession = new MediaSessionCompat(getApplicationContext(), BuildConfig.APPLICATION_ID, componentName, null);
//        mediaSession.setMetadata(metadata.build());
//        mediaSession.setPlaybackState(state);
//        mediaSession.setCallback(new MediaSessionCallback());
//        mediaSession.setActive(true);
//
//        // Apply the media style template
//        builder.setStyle(new NotificationCompat.MediaStyle()
//                        .setShowActionsInCompactView(1 /* #1: pause button */)
//                        .setMediaSession(mediaSession.getSessionToken()));


        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        startForeground(500, notification);
    }

    public VKAudio getAudio() {
        return mAudio;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        VKAudio value = (VKAudio) intent.getSerializableExtra("value");

        if (action == null) {
            return START_NOT_STICKY;
        }

        Log.w(TAG, "action - " + action);
        switch (action) {
            case ACTION_PLAY:
                mAudio = value;
                mAudio.isPlaying = true;
                createNotification(value);
                startPlay(value.url);
                break;

            case ACTION_PAUSE:
                mAudio.isPlaying = false;
                pausePlay();
                break;

            case ACTION_STOP:
                stopPlay();
                break;

            case ACTION_NEXT_AUDIO:
                mAudio.isPlaying = true;
                playNextAudio();
                break;

            case ACTION_PREV_AUDIO:
                mAudio.isPlaying = true;
                playPrevAudio();
                break;

            case ACTION_CLOSE:

                stopForeground(true);
                stopSelf();
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void playNextAudio() {
        if (lastPlayUrl == null) {
            return;
        }
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = DBHelper.get(this).getWritableDatabase();
        }
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + DBHelper.AUDIOS_TABLE +
                        " WHERE " + DBHelper.URL +
                        " = '" + lastPlayUrl + "'",
                null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return;
        }

        if (cursor.moveToNext()) {
            String nextUrl = cursor.getString(5);
            startPlay(nextUrl);
        }
        cursor.close();

    }

    public void playPrevAudio() {
        if (lastPlayUrl == null) {
            return;
        }
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = DBHelper.get(this).getWritableDatabase();
        }
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + DBHelper.AUDIOS_TABLE +
                        " WHERE " + DBHelper.URL +
                        " = ?",
                new String[]{lastPlayUrl});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return;
        }

        if (cursor.moveToPrevious()) {
            String nextUrl = cursor.getString(5);
            startPlay(nextUrl);
        }
        cursor.close();
    }

    public void startPlay(String url) {
        Log.i(TAG, "startPlay vie url = " + url);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (!url.equalsIgnoreCase(lastPlayUrl)) {
            release();
        }
        lastPlayUrl = url;
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        } else if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            return;
        }
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pausePlay() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void resumePlay() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void stopPlay() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

        release();
    }

    public void release() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        stopForeground(false);
        release();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        release();
//        if (mStopAfterPlaying) {
//            stopSelf();
//        }
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return true;
    }

    public class LocalBinder extends Binder {
        public PlayMusicService getService() {
            return PlayMusicService.this;
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            startPlay(lastPlayUrl);
        }

        @Override
        public void onPause() {
            pausePlay();
        }

        public void onSeekTo(long pos) {
//            seek(pos);
        }

        @Override
        public void onSkipToNext() {
//            playNext();
        }

        @Override
        public void onSkipToPrevious() {
//            release();
        }
    }
}
