package ru.euphoriadev.vk.api.model;

import org.json.JSONObject;

/**
 * Created by Igor on 29.11.15.
 */
public class VKResolveScreenName {
    public static final String TYPE_USER =  "user";
    public static final String TYPE_GROUP = "group";
    public static final String TYPE_APP =   "application";
    public static final String TYPE_PAGE =  "page";

    public int object_id;
    public String type;

    public static VKResolveScreenName parse(JSONObject source) {
        VKResolveScreenName screenName = new VKResolveScreenName();

        screenName.object_id = source.optInt("object_id");
        screenName.type = source.optString("type");
        return screenName;
    }


}
