package ru.euphoriadev.vk.api.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import ru.euphoriadev.vk.api.Api;

public class VKLink implements Serializable {
    private static final long serialVersionUID = 1L;

    public String url;
    public String title;
    public String description;
    public String image_src;


    public static VKLink parse(JSONObject source) {
        VKLink link = new VKLink();
        link.url = source.optString("url");
        link.title = Api.unescape(source.optString("title"));
        link.description = Api.unescape(source.optString("description"));
        link.image_src = source.optString("image_src");
        return link;
    }

    public static VKLink parseFromGroup(JSONObject o) throws NumberFormatException, JSONException {
        VKLink link = new VKLink();
        link.url = o.optString("url");
        link.title = Api.unescape(o.optString("name"));
        link.description = Api.unescape(o.optString("desc"));
        link.image_src = o.optString("photo_100");
        return link;
    }
}
