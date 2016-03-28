package ru.euphoriadev.vk.api.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_ARCHIVE = 2;
    public static final int TYPE_GIF = 3;
    public static final int TYPE_IMAGE = 4;
    public static final int TYPE_AUDIO = 5;
    public static final int TYPE_VIDEO = 6;
    public static final int TYPE_BOOK = 7;
    public static final int TYPE_UNKNOWN = 8;

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

    /**
     * The document type (audio, video, book)
     */
    public int type;

    public static VKDocument parse(JSONObject o) {
        VKDocument doc = new VKDocument();
        doc.id = o.optLong("id");
        doc.owner_id = o.optLong("owner_id");
        doc.title = o.optString("title");
        doc.url = o.optString("url");
        doc.size = o.optLong("size");
        doc.type = o.optInt("type");
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

    @Deprecated
    public boolean isGif() {
        return ext.equalsIgnoreCase("gif");
    }


    @Deprecated
    public boolean isImage() {
        return (type == TYPE_NONE || type == TYPE_UNKNOWN) ?
                ext.equalsIgnoreCase("jpg") ||
                ext.equalsIgnoreCase("jpeg") ||
                ext.equalsIgnoreCase("png") ||
                ext.equalsIgnoreCase("bmp")
                : type == TYPE_IMAGE;
    }

    @Deprecated
    public boolean isText() {
        return ext.equalsIgnoreCase("text") ||
                ext.equalsIgnoreCase("txt") ||
                ext.equalsIgnoreCase("pdf") ||
                ext.equalsIgnoreCase("fb2") || // FBReader
                ext.equalsIgnoreCase("doc") ||
                ext.equalsIgnoreCase("docx") ||
                ext.equalsIgnoreCase("odt") || // OpenOffice
                ext.equalsIgnoreCase("xml") ||
                ext.equalsIgnoreCase("html") ||
                ext.equalsIgnoreCase("java") ||
                ext.equalsIgnoreCase("json") ||
                ext.equalsIgnoreCase("djvu") ||
                ext.equalsIgnoreCase("chm") ||
                ext.equalsIgnoreCase("iml") ||
                ext.equalsIgnoreCase("properties");
    }

    // The extension list is not complete, only the most basic
    public boolean isCode() {
        return ext.equalsIgnoreCase("java") || // Java
                ext.equalsIgnoreCase("gradle") || // Gradle
                ext.equalsIgnoreCase("js")  || // JavaScript
                ext.equalsIgnoreCase("c")   || // C / Objective-C
                ext.equalsIgnoreCase("h")   || // C (Header)
                ext.equalsIgnoreCase("cpp") || // C++
                ext.equalsIgnoreCase("cs")  || // C# (C Sharp)
                ext.equalsIgnoreCase("pas") || // Delphi
                ext.equalsIgnoreCase("dpr") || // Delphi
                ext.equalsIgnoreCase("dpk") || // Delphi
                ext.equalsIgnoreCase("pp")  || // Delphi
                ext.equalsIgnoreCase("e")   || // Euphoria
                ext.equalsIgnoreCase("pm")  || // Perl
                ext.equalsIgnoreCase("cgi") || // Perl
                ext.equalsIgnoreCase("pl")  || // Perl
                ext.equalsIgnoreCase("rb")  || // Ruby
                ext.equalsIgnoreCase("rbw") || // Ruby

                ext.equalsIgnoreCase("sql")  || // SQL
                ext.equalsIgnoreCase("xml")  || // XML
                ext.equalsIgnoreCase("html") || // HTML
                ext.equalsIgnoreCase("css") ||  // CSS (HTML)
                ext.equalsIgnoreCase("php");    // PHP
    }

    // Recently VK has disallowed the executable files,
    // so users will rename the extension
    public boolean isAndroidApp() {
        return ext.contains("apk");
    }

    public boolean isJar() {
        return ext.contains("jar");
    }

    @Deprecated
    public boolean isAudio() {
        return ext.equalsIgnoreCase("mp3") ||
                ext.equalsIgnoreCase("midi") ||
                ext.equalsIgnoreCase("wav") ||
                ext.equalsIgnoreCase("ogg");
    }

    @Deprecated
    public boolean isVideo() {
        return ext.equalsIgnoreCase("3gp") ||
                ext.equalsIgnoreCase("mp4") ||
                ext.equalsIgnoreCase("avi") ||
                ext.equalsIgnoreCase("aaf") ||
                ext.equalsIgnoreCase("mpeg") ||
                ext.equalsIgnoreCase("mov") ||
                ext.equalsIgnoreCase("vob") ||
                ext.equalsIgnoreCase("wmv") ||
                ext.equalsIgnoreCase("mts");
    }

}
