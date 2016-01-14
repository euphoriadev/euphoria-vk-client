package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKAttachment implements Serializable {
    public static final String TYPE_LINK = "link";
    public static final String TYPE_GRAFITY = "graffiti";
    public static final String TYPE_NOTE = "note";
    public static final String TYPE_POLL = "poll";
    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_DOC = "doc";
    public static final String TYPE_WALL = "wall";
    public static final String TYPE_PADE = "page";
    public static final String TYPE_GIFT = "gift";
    public static final String TYPE_STICKER = "sticker";
    public static final String TYPE_ALUMB = "album";
    public static final String TYPE_GEO = "geo";
    private static final long serialVersionUID = 1L;
    public long id;//used only for wall post attached to message
    public String type; //photo,posted_photo,video,audio,link,note,app,poll,doc,geo,message,page,album
    public VKPhoto photo;
    //public Photo posted_photo;
    public VKVideo video;
    public VKAudio audio;
    public VKLink link;
    public VKNote note;
    public VKGraffiti graffiti;
    public VkApp app;
    public VKPoll poll;
    public Geo geo;
    public VKDocument document;
    public VKMessage message;
    public VKWallMessage wallMessage;
    public VKPage page;
    public VKGift gift;
    public VKSticker sticker;
    public VKAlbum album;

    public static ArrayList<VKAttachment> parseAttachments(JSONArray attachments, long from_id, long copy_owner_id, JSONObject geo_json) {
        ArrayList<VKAttachment> attachments_arr = new ArrayList<>();
        if (attachments != null) {
            int size = attachments.length();
            for (int j = 0; j < size; ++j) {
                Object att = attachments.opt(j);
                if (!(att instanceof JSONObject))
                    continue;
                JSONObject json_attachment = (JSONObject) att;
                VKAttachment attachment = new VKAttachment();
                attachment.type = json_attachment.optString("type");
                switch (attachment.type) {
                    case "photo":
                    case "posted_photo":
                        JSONObject x = json_attachment.optJSONObject("photo");
                        if (x != null)
                            attachment.photo = VKPhoto.parse(x);
                        break;
                    case "graffiti":
                        attachment.graffiti = VKGraffiti.parse(json_attachment.optJSONObject("graffiti"));
                        break;
                    case "link":
                        attachment.link = VKLink.parse(json_attachment.optJSONObject("link"));
                        break;
                    case "audio":
                        attachment.audio = VKAudio.parse(json_attachment.optJSONObject("audio"));
                        break;
                    case "note":
                        attachment.note = VKNote.parse(json_attachment.optJSONObject("note"));
                        break;
                    case "video":
                        attachment.video = VKVideo.parseForAttachments(json_attachment.optJSONObject("video"));
                        break;
                    case "poll":
                        attachment.poll = VKPoll.parse(json_attachment.optJSONObject("poll"));
                        if (attachment.poll.owner_id == 0) {
                            //это устарело потому что поля copy_owner_id больше нет при парсинге
                            //if(copy_owner_id!=0)
                            //    attachment.poll.owner_id=copy_owner_id;
                            //else
                            attachment.poll.owner_id = from_id;
                        }
                        break;
                    case "doc":
                        attachment.document = VKDocument.parse(json_attachment.optJSONObject("doc"));
                        break;
                    case "wall":
                        attachment.wallMessage = VKWallMessage.parse(json_attachment.optJSONObject("wall"));
                        break;
                    case "page":
                        attachment.page = VKPage.parseFromAttachment(json_attachment.optJSONObject("page"));
                        break;
                    case "gift":
                        attachment.gift = VKGift.parse(json_attachment.optJSONObject("gift"));
                        break;
                    case "album":
                        attachment.album = VKAlbum.parseFromAttachment(json_attachment.optJSONObject("album"));
                        break;
                    case "sticker":
                        attachment.sticker = VKSticker.parse(json_attachment.optJSONObject("sticker"));
                        break;

                }
                attachments_arr.add(attachment);
            }
        }

        //Geo тоже добавляем в attacmnets если он есть
        if (geo_json != null) {
            VKAttachment a = new VKAttachment();
            a.type = "geo";
            a.geo = Geo.parse(geo_json);
            attachments_arr.add(a);
        }
        return attachments_arr;
    }

    public static ArrayList<VKAttachment> parseArray(JSONArray source) {
       return parseAttachments(source, 0, 0, null);
    }
}
