package ru.euphoriadev.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Igor on 24.06.15.
 */
public class VKSticker implements Serializable {

    public long id;
    public long product_id;
    public int width;
    public int height;
    public String photo_64;
    public String photo_128;
    public String photo_256;
    public String photo_352;

    public static VKSticker parse(JSONObject sourse) {
        VKSticker sticker = new VKSticker();
        sticker.id = sourse.optLong("id");
        sticker.product_id = sourse.optLong("product_id");
        sticker.photo_64 = sourse.optString("photo_64");
        sticker.photo_128 = sourse.optString("photo_128");
        sticker.photo_256 = sourse.optString("photo_256");
        sticker.photo_352 = sourse.optString("photo_352");
        sticker.width = sourse.optInt("width");
        sticker.height = sourse.optInt("height");
        return sticker;
    }
}
