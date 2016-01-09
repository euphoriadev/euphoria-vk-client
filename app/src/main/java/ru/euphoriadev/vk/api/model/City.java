package ru.euphoriadev.vk.api.model;

import org.json.JSONObject;

public class City {
    public long cid;
    public String title = "";

    public static City parse(JSONObject o) {
        City c = new City();
        c.cid = o.optLong("id");
        c.title = o.optString("title");
        return c;
    }

    @Override
    public String toString() {
        return title;
    }
}
