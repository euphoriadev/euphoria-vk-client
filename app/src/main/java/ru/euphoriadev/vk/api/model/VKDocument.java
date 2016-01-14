package ru.euphoriadev.vk.api.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Document ID.
     */
    public long id;

    /**
     * ID of the user or group who uploaded the document.
     */
    public long owner_id;

    /**
     * Document title.
     */
    public String title;

    /**
     * Document size (in bytes).
     */
    public long size;

    /**
     * Document extension.
     */
    public String ext;

    /**
     * Document URL for downloading.
     */
    public String url;

    /**
     * URL of the 100x75px image (if the file is graphical).
     */
    public String photo_100;

    /**
     * URL of the 130x100px image (if the file is graphical).
     */
    public String photo_130;

    /**
     * An access key using for get information about hidden objects.
     */
    public String access_key;


    public static VKDocument parse(JSONObject o) {
        VKDocument doc = new VKDocument();
        doc.id = o.optLong("id");
        doc.owner_id = o.optLong("owner_id");
        doc.title = o.optString("title");
        doc.url = o.optString("url");
        doc.size = o.optLong("size");
        doc.ext = o.optString("ext");
        doc.photo_130 = o.optString("photo_130", null);
        doc.photo_100 = o.optString("photo_100", null);
        doc.access_key = o.optString("access_key", null);
        return doc;
    }

    public static ArrayList<VKDocument> parseDocs(JSONArray array) throws JSONException {
        ArrayList<VKDocument> docs = new ArrayList<VKDocument>();
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                Object item = array.get(i);
                if (!(item instanceof JSONObject))
                    continue;
                JSONObject o = (JSONObject) item;
                VKDocument doc = VKDocument.parse(o);
                docs.add(doc);
            }
        }
        return docs;
    }

    public CharSequence toAttachmentString() {
        StringBuilder result = new StringBuilder("doc").append(owner_id).append('_').append(id);
        if (!TextUtils.isEmpty(access_key)) {
            result.append('_');
            result.append(access_key);
        }
        return result;
    }

    public boolean isGif() {
        return ext.equalsIgnoreCase("gif");
    }

    public boolean isImage() {
        return ext.equalsIgnoreCase("jpg") ||
                ext.equalsIgnoreCase("jpeg") ||
                ext.equalsIgnoreCase("png") ||
                ext.equalsIgnoreCase("bmp");
    }

}
