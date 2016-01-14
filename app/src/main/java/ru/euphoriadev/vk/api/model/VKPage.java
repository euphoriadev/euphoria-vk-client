package ru.euphoriadev.vk.api.model;

import org.json.JSONObject;

import java.io.Serializable;

import ru.euphoriadev.vk.api.Api;

/**
 * Wiki page
 *
 */
public class VKPage implements Serializable{
    private static final long serialVersionUID = 1L;
    public long id;
    public long group_id;
    public String title;
    
    public static VKPage parseFromAttachment(JSONObject o){
        VKPage page = new VKPage();
        page.title = Api.unescape(o.optString("title"));
        page.id = o.optLong("id");
        page.group_id = o.optLong("group_id");
        return page;
    }
}