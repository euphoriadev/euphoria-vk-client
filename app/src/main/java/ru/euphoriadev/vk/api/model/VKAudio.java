package ru.euphoriadev.vk.api.model;

import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import ru.euphoriadev.vk.api.Api;

import java.io.Serializable;

public class VKAudio implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Audio ID.
     */
    public long id;

    /**
     * Audio owner ID.
     */
    public long owner_id;

    /**
     * Artist name.
     */
    public String artist;

    /**
     * Audio file title.
     */
    public String title;

    /**
     * Duration (in seconds).
     */
    public long duration;

    /**
     * Link to mp3.
     */
    public String url;

    /**
     * ID of the lyrics (if available) of the audio file.
     */
    public long lyrics_id;

    /**
     * ID of the album containing the audio file (if assigned).
     */
    public long album_id;

    /**
     * Genre ID. See the list of audio genres.
     */
    public long genre;

    /**
     * An access key using for get information about hidden objects.
     */
    public String access_key;

    /**
     * User, also allows know whether the song is playing in application
     */
    public boolean isPlaying;

    public static VKAudio parse(JSONObject o) {
        VKAudio audio = new VKAudio();
        audio.id = Long.parseLong(o.optString("id"));
        audio.owner_id = Long.parseLong(o.optString("owner_id"));
        audio.artist = Api.unescape(o.optString("artist"));
        audio.title = Api.unescape(o.optString("title"));
        audio.duration = Long.parseLong(o.optString("duration"));
        audio.url = o.optString("url", null);
        audio.genre = o.optLong("genre_id", -1);
        // Если возвращает -1, можно попробовать переименовать "genre_id" на "genre"
        audio.access_key = o.optString("access_key", null);

        String tmp = o.optString("lyrics_id");
        if (tmp != null && !tmp.equals(""))//otherwise lyrics_id=null
            audio.lyrics_id = Long.parseLong(tmp);
        return audio;
    }

    public CharSequence toAttachmentString() {
        StringBuilder result = new StringBuilder("audio").append(owner_id).append('_').append(id);
        if (!TextUtils.isEmpty(access_key)) {
            result.append('_');
            result.append(access_key);
        }
        return result;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }

    /**
     * Audio object genres.
     */
    public final static class Genre {

        private Genre() {
        }

        public final static int ROCK = 1;
        public final static int POP = 2;
        public final static int RAP_AND_HIPHOP = 3;
        public final static int EASY_LISTENING = 4;
        public final static int DANCE_AND_HOUSE = 5;
        public final static int INSTRUMENTAL = 6;
        public final static int METAL = 7;
        public final static int DUBSTEP = 8;
        public final static int JAZZ_AND_BLUES = 9;
        public final static int DRUM_AND_BASS = 10;
        public final static int TRANCE = 11;
        public final static int CHANSON = 12;
        public final static int ETHNIC = 13;
        public final static int ACOUSTIC_AND_VOCAL = 14;
        public final static int REGGAE = 15;
        public final static int CLASSICAL = 16;
        public final static int INDIE_POP = 17;
        public final static int OTHER = 18;
        public final static int SPEECH = 19;
        public final static int ALTERNATIVE = 21;
        public final static int ELECTROPOP_AND_DISCO = 22;
    }
}