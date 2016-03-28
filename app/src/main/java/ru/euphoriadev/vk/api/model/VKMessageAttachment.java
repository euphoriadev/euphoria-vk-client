package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Igor on 04.02.16.
 */
public class VKMessageAttachment {
    /**
     * Types of materials
     */
    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_DOC = "doc";
    public static final String TYPE_LINK = "link";

    /**
     * Current media type
     */
    public String type;
    /**
     * Containing the new value of start_message_id
     */
    public int next_from;
    /**
     * Depending on the value of "type" to use one of objects.
     */
    public VKAudio audio;
    /**
     * Use, if type == "photo"
     */
    public VKPhoto photo;
    /**
     * Use, if type == "video"
     */
    public VKVideo video;
    /**
     * Use, if type == "doc"
     */
    public VKDocument doc;


    public static VKMessageAttachment parse(JSONObject source, String type, int next_from) throws JSONException {
        VKMessageAttachment attachment = new VKMessageAttachment();
        attachment.type = type;
        attachment.next_from = next_from;

        switch (type) {
            case TYPE_AUDIO:
                attachment.audio = VKAudio.parse(source.optJSONObject(TYPE_AUDIO));
                break;
            case TYPE_VIDEO:
                attachment.video = VKVideo.parse(source.optJSONObject(TYPE_VIDEO));
                break;
            case TYPE_PHOTO:
                attachment.photo = VKPhoto.parse(source.optJSONObject(TYPE_PHOTO));
                break;
            case TYPE_DOC:
                attachment.doc = VKDocument.parse(source.optJSONObject(TYPE_DOC));
                break;

        }
        return attachment;
    }

    public static ArrayList<VKMessageAttachment> parseArray(JSONArray items, String type, int next_from) throws JSONException {
        ArrayList<VKMessageAttachment> attachments = new ArrayList<>(items.length());
        if (items == null || items.length() <= 0) {
            return attachments;
        }

        for (int i = 0; i < items.length(); i++) {
            attachments.add(parse(items.optJSONObject(i), type, next_from));
        }
        return attachments;
    }
}
