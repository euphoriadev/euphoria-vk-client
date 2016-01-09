package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKGift implements Serializable {

    /**
     * User id who sent the gift, or 0 if the sender is hidden
     */
    public long from_id;

    /**
     * Gift ID
     */
    public long id;

    /**
     * Text of the message attached to the gift
     */
    public String message;

    /**
     * Time to send gift in unixtime format
     */
    public long date;

    /**
     * URL image gift size 256x256px;
     */
    public String thumb_256;


    public VKGift(JSONObject sourse) {
        id = sourse.optLong("id");
        from_id = sourse.optLong("from_id");
        date = sourse.optLong("date");

        if (sourse.has("gift")) {
            thumb_256 = sourse.optJSONObject("gift").optString("thumb_256");
        } else {
            thumb_256 = sourse.optString("thumb_256");
        }
    }

    public static VKGift parse(JSONObject sourse) {
        return new VKGift(sourse);
    }

    public static ArrayList<VKGift> parseGifts(JSONArray array) {
        ArrayList<VKGift> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            VKGift gift = new VKGift(array.optJSONObject(i));
            list.add(gift);
        }
        return list;
    }
}