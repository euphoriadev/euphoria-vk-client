package ru.euphoriadev.vk.api.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Country {
    public long cid;
    public String name;

    public static Country parse(JSONObject o){
        Country c = new Country();
        c.cid = o.optLong("id");
        c.name = o.optString("title");
        return c;
    }
}
