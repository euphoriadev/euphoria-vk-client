package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.euphoriadev.vk.api.Api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class VKNote implements Serializable {
    private static final long serialVersionUID = 1L;
    public long nid;
    public long owner_id;
    public String title;
    public String text;
    public long date = 0;
    public long ncom = -1;
    //public long read_ncom=-1;

    public static VKNote parse(JSONObject o) {
        VKNote note = new VKNote();
        note.nid = o.optLong("id");

        //в новости "добавил заметку" заметка приходит по-старому - баг в API
        if (!o.has("id") && o.has("nid"))
            note.nid = o.optLong("nid");

        note.owner_id = o.optInt("owner_id");
        note.title = Api.unescape(o.optString("title"));
        note.ncom = o.optLong("comments");

        //в новости "добавил заметку" заметка приходит по-старому - баг в API
        if (!o.has("comments") && o.has("ncom"))
            note.ncom = o.optLong("ncom");

        //note.read_ncom = o.optLong("read_comments");
        note.text = o.optString("text");
        note.date = o.optLong("date");
        return note;
    }

    public static ArrayList<VKNote> parseNotes(JSONArray array) {
        ArrayList<VKNote> notes = new ArrayList<>(array.length());

        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            notes.add(VKNote.parse(o));
        }
        return notes;
    }
}
