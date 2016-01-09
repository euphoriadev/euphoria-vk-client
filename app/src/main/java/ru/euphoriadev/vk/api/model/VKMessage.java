package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.euphoriadev.vk.api.Api;

import java.io.Serializable;
import java.util.ArrayList;

public class VKMessage implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 	Message ID. (Not returned for forwarded messages), positive number
     */
    public long mid;

    /**
     *  For an incoming message, the user ID of the author. For an outgoing message, the user ID of the receiver.
     */
    public long uid;

    /**
     *  Date (in Unix time) when the message was sent.
     */
    public long date;

    /**
     *  Title of message or chat.
     */
    public String title;

    /**
     *  Message text
     */
    public String body;

    /**
     *  Message status (false — not read, true — read). (Not returned for forwarded messages
     */
    public boolean read_state;

    /**
     *  type (false — received, true — sent). (Not returned for forwarded messages.)
     */
    public boolean is_out;

    /**
     *  List of media-attachments;
     */
    public ArrayList<VKAttachment> attachments = new ArrayList<>();

    /**
     *  Chat ID
     */
    public long chat_id;

    /**
     *  User IDs of chat participants
     */
    public ArrayList<Long> chat_members;

    /**
     *  ID of user who started the chat.
     */
    public Long admin_id;

    /**
     *  Number of chat participants.
     */
    public long users_count;

    /**
     *  Whether the message is deleted (false — no, true — yes).
     */
    public boolean is_deleted;

    /**
     *  Whether the message is important
     */
    public boolean is_important;

    /**
     *  Whether the message contains smiles (false — no, true — yes).
     */
    public boolean emoji;

    /**
     *  URL of chat image with width size of 50px
     */
    public String photo_50;

    /**
     *  URL of chat image with width size of 100px
     */
    public String photo_100;

    /**
     *  URL of chat image with width size of 200px
     */
    public String photo_200;

    /**
     * The count of unread messages
     */
    public long unread;

    /**
     * Field transferred, if a service message
     */
    public String action;


    public int flag;


    @Deprecated
    public static VKMessage parse(JSONObject o, boolean from_history, long history_uid, boolean from_chat, long me) throws NumberFormatException, JSONException {
        VKMessage m = new VKMessage();
        if (from_chat) {
            long from_id = o.getLong("user_id");
            m.uid = from_id;
            m.is_out = (from_id == me);
        } else if (from_history) {
            m.uid = history_uid;
            Long from_id = o.getLong("from_id");
            m.is_out = !(from_id == history_uid);
        } else {
            //тут не очень, потому что при получении списка диалогов если есть моё сообщение, которое я написал в беседу, то в нём uid будет мой. Хотя в других случайх uid всегда собеседника.
            // TODO: Впрочем, это вполне нормально
            m.uid = o.getLong("user_id");
            m.is_out = o.optInt("out") == 1;
        }
        m.mid = o.optLong("id");
        m.date = o.optLong("date");
        if (o.has("users_count")) m.users_count = o.optLong("users_count");
        m.title = Api.unescape(o.optString("title"));
        m.body = Api.unescapeWithSmiles(o.optString("body"));
        m.read_state = (o.optInt("read_state") == 1);
        if (o.has("chat_id"))
            m.chat_id = o.getLong("chat_id");

        //for dialog list
        JSONArray tmp = o.optJSONArray("chat_active");
        if (tmp != null && tmp.length() != 0) {
            m.chat_members = new ArrayList<Long>();
            for (int i = 0; i < tmp.length(); ++i)
                m.chat_members.add(tmp.getLong(i));
        }

        JSONArray attachments = o.optJSONArray("attachments");
        JSONObject geo_json = o.optJSONObject("geo");
        m.attachments = VKAttachment.parseAttachments(attachments, 0, 0, geo_json);

        //parseArray fwd_messages and add them to attachments
        JSONArray fwd_messages = o.optJSONArray("fwd_messages");
        if (fwd_messages != null) {
            for (int i = 0; i < fwd_messages.length(); ++i) {
                JSONObject fwd_message_json = fwd_messages.getJSONObject(i);
                VKMessage fwd_message = VKMessage.parse(fwd_message_json, false, 0, false, 0);
                VKAttachment att = new VKAttachment();
                att.type = "message";
                att.message = fwd_message;
                m.attachments.add(att);
            }
        }
//        m.json = o;

        return m;
    }


    public static final int UNREAD = 1;       // сообщение не прочитано
    public static final int OUTBOX = 2;       // исходящее сообщение
    public static final int REPLIED = 4;      // на сообщение был создан ответ
    public static final int IMPORTANT = 8;    // помеченное сообщение
    public static final int CHAT = 16;        // сообщение отправлено через диалог
    public static final int FRIENDS = 32;     // сообщение отправлено другом
    public static final int SPAM = 64;        // сообщение помечено как "Спам"
    public static final int DELETED = 128;    // сообщение удалено (в корзине)
    public static final int FIXED = 256;      // сообщение проверено пользователем на спам
    public static final int MEDIA = 512;      // сообщение содержит медиаконтент
    public static final int BESEDA = 8192;    // беседа

    public VKMessage(String body, boolean isOut) {
        this.body = body;
        this.is_out = isOut;
        this.date = System.currentTimeMillis() / 1000;
    }

    public VKMessage() {

    }

    public static VKMessage parse(JSONObject source) {
        VKMessage message = new VKMessage();
        message.mid = source.optLong("id");
        message.uid = source.optLong("user_id");
        message.chat_id = source.optLong("chat_id");
        message.date = source.optLong("date");
        message.is_out = source.optLong("out") == 1;
        message.read_state = source.optLong("read_state") == 1;
        message.title = Api.unescape(source.optString("title"));
        message.body = Api.unescapeWithSmiles(source.optString("body"));
        message.users_count = source.optLong("users_count");
        message.is_deleted = source.optLong("deleted") == 1;
        message.is_important = source.optLong("important") == 1;
        message.emoji = source.optLong("emoji") == 1;
        message.action = source.optString("action");
        message.photo_50 = source.optString("photo_50");
        message.photo_100 = source.optString("photo_100");
        message.photo_200 = source.optString("photo_200");

        JSONArray atts = source.optJSONArray("attachments");
        if (atts != null) {
            message.attachments = VKAttachment.parseArray(atts);
        }

        JSONArray fwdMessages = source.optJSONArray("fwd_messages");
        if (fwdMessages != null) {
            for (int i = 0; i < fwdMessages.length(); i++) {
                VKMessage fwd_msg = VKMessage.parse(fwdMessages.optJSONObject(i));
                VKAttachment att = new VKAttachment();
                att.type = VKAttachment.TYPE_MESSAGE;
                att.message = fwd_msg;
                message.attachments.add(att);
            }
        }

        JSONArray chat_active = source.optJSONArray("chat_active");
        if (chat_active != null) {
            for (int i = 0; i < chat_active.length(); i++) {
                message.chat_members = new ArrayList<>();
                message.chat_members.add(chat_active.optLong(i));
            }
        }

        // TODO: from_id возврвщается только тогда, когда получаем историю
        long from_id = source.optLong("from_id", -1);
        if (from_id != -1 && message.chat_id != 0) {
            message.uid = from_id;
        }
//        message.json = source;
        return message;
    }

    public static ArrayList<VKMessage> parseArray(JSONArray array) throws JSONException {
        ArrayList<VKMessage> vkMessages = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            VKMessage message;
            // В новых версиях api обьект message возвращается в нем же
            if (item.has("message")) {
                message = VKMessage.parse(item.optJSONObject("message"));
                message.unread = item.optInt("unread");
            } else {
                message = VKMessage.parse(item);
            }

            vkMessages.add(message);
        }

        return vkMessages;
    }

    // parse from long poll (update[])
    public static VKMessage parse(JSONArray a) throws JSONException {
        VKMessage m = new VKMessage();
        m.mid = a.optLong(1);
        m.flag = a.optInt(2);
        m.uid = a.optInt(3);
        m.date = a.optLong(4);
        m.title = Api.unescape(a.optString(5));
        m.body = Api.unescapeWithSmiles(a.optString(6));
        m.read_state = ((m.flag & UNREAD) == 0);
        m.is_out = (m.flag & OUTBOX) != 0;
        if ((m.flag & BESEDA) != 0) {
            m.chat_id = a.optLong(3) - 2000000000;// & 63;//cut 6 last digits
            JSONObject o = a.optJSONObject(7);
            m.uid = o.optLong("from");
        }
       // m.attachment = a.getJSONArray(7); TODO
        m.attachments = VKAttachment.parseArray(a.optJSONArray(7));
        return m;
    }

    public boolean isChat() {
       return chat_id != 0;
    }

    public static ArrayList<SearchDialogItem> parseSearchedDialogs(JSONArray array) {
        ArrayList<SearchDialogItem> items = new ArrayList<SearchDialogItem>();
        if (array == null)
            return items;
        try {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                if (array.get(i) == null || (!(array.get(i) instanceof JSONObject)))
                    continue;
                JSONObject o = (JSONObject) array.get(i);
                SearchDialogItem item = new SearchDialogItem();
                String type = o.getString("type");
                item.str_type = type;
                switch (type) {
                    case "profile":
                        item.type = SearchDialogItem.SDIType.USER;
                        item.user = VKFullUser.parse(o);
                        break;
                    case "chat":
                        item.type = SearchDialogItem.SDIType.CHAT;
                        VKMessage m = new VKMessage();
                        m.chat_id = o.getLong("id");
                        m.admin_id = o.getLong("admin_id");
                        m.title = o.getString("title");
                        JSONArray users = o.optJSONArray("users");
                        if (users != null && users.length() != 0) {
                            m.chat_members = new ArrayList<Long>();
                            for (int j = 0; j < users.length(); j++)
                                m.chat_members.add(users.getLong(j));
                        }
                        item.chat = m;
                        break;
                    default:
                        item.type = SearchDialogItem.SDIType.EMAIL;
                        item.email = o.optString("email");
                        break;
                }
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VKMessage message = (VKMessage) o;

        if (chat_id != message.chat_id) return false;
        if (date != message.date) return false;
        if (is_deleted != message.is_deleted) return false;
        if (is_important != message.is_important) return false;
        if (is_out != message.is_out) return false;
        if (mid != message.mid) return false;
        if (read_state != message.read_state) return false;
        if (uid != message.uid) return false;
        if (users_count != message.users_count) return false;
        if (admin_id != null ? !admin_id.equals(message.admin_id) : message.admin_id != null) return false;
        if (attachments != null ? !attachments.equals(message.attachments) : message.attachments != null) return false;
        if (body != null ? !body.equals(message.body) : message.body != null) return false;
        if (chat_members != null ? !chat_members.equals(message.chat_members) : message.chat_members != null)
            return false;
        if (photo_100 != null ? !photo_100.equals(message.photo_100) : message.photo_100 != null) return false;
        if (photo_200 != null ? !photo_200.equals(message.photo_200) : message.photo_200 != null) return false;
        if (photo_50 != null ? !photo_50.equals(message.photo_50) : message.photo_50 != null) return false;
        if (title != null ? !title.equals(message.title) : message.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (mid ^ (mid >>> 32));
        result = 31 * result + (int) (uid ^ (uid >>> 32));
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (read_state ? 1 : 0);
        result = 31 * result + (is_out ? 1 : 0);
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        result = 31 * result + (int) (chat_id ^ (chat_id >>> 32));
        result = 31 * result + (chat_members != null ? chat_members.hashCode() : 0);
        result = 31 * result + (admin_id != null ? admin_id.hashCode() : 0);
        result = 31 * result + (int) (users_count ^ (users_count >>> 32));
        result = 31 * result + (is_deleted ? 1 : 0);
        result = 31 * result + (is_important ? 1 : 0);
        result = 31 * result + (photo_50 != null ? photo_50.hashCode() : 0);
        result = 31 * result + (photo_100 != null ? photo_100.hashCode() : 0);
        result = 31 * result + (photo_200 != null ? photo_200.hashCode() : 0);
        result = 31 * result + flag;
        return result;
    }
}
