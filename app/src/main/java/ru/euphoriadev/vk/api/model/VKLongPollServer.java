package ru.euphoriadev.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Igor on 19.12.15.
 */
public class VKLongPollServer implements Serializable {
    public String key;
    public String server;
    public Long ts;
    public Long pts;

    public static VKLongPollServer parse(JSONObject source) {
        VKLongPollServer lps = new VKLongPollServer();

        lps.key = source.optString("key");
        lps.server = source.optString("server");
        lps.ts = source.optLong("ts");
        lps.pts = source.optLong("pts");
        return lps;
    }
}
