package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by user on 21.06.15.
 */
public class VKChat implements Serializable {

    /**
     * Chat ID, positive number.
     */
    public long id;

    /**
     * Type of chat.
     */
    public String type;

    /**
     * Chat title.
     */
    public String title;

    /**
     * ID of the chat starter, positive number
     */
    public long admin_id;

    /**
     * List of chat participants' IDs.
     */
    public ArrayList<Long> users = new ArrayList<>();

    /**
     * URL of chat image with width size of 50px.
     */
    public String photo_50;

    /**
     * URL of chat image with width size of 50px.
     */
    public String photo_100;

    /**
     * URL of chat image with width size of 50px.
     */
    public String photo_200;


    public static VKChat parse(JSONObject sourse) {
        VKChat chat = new VKChat();
        chat.id = sourse.optLong("id");
        chat.type = sourse.optString("type");
        chat.title = sourse.optString("title");
        chat.admin_id = sourse.optLong("admin_id");
        chat.photo_50 = sourse.optString("photo_50");
        chat.photo_100 = sourse.optString("photo_100");
        chat.photo_200 = sourse.optString("photo_200");

        JSONArray array = sourse.optJSONArray("users");
        if (array != null)
            for (int i = 0; i < array.length(); i++) {
                chat.users.add(array.optLong(i));
            }
        return chat;
    }

}
