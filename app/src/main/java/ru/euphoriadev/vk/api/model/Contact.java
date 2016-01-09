package ru.euphoriadev.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    public Long user_id;
    public String desc;
    public String email;
    public String phone;

    public static Contact parse(JSONObject o) {
        Contact c = new Contact();
        c.user_id = o.optLong("user_id");
        c.desc = o.optString("desc");
        c.email = o.optString("email");
        c.phone = o.optString("phone");

        return c;
    }
}
