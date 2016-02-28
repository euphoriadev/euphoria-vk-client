package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKNotifications implements Serializable {

    private static final long serialVersionUID = 1L;
    public ArrayList<VKNotification> notifications = new ArrayList<VKNotification>();
    public ArrayList<VKFullUser> profiles = new ArrayList<VKFullUser>();
    public ArrayList<VKGroup> groups = new ArrayList<VKGroup>();

    public static VKNotifications parse(JSONObject response) throws JSONException {
        VKNotifications full_notifications = new VKNotifications();
        full_notifications.notifications = new ArrayList<VKNotification>();
        if (response == null)
            return full_notifications;
        JSONArray array = response.optJSONArray("items");
        JSONArray profiles_array = response.optJSONArray("profiles");
        JSONArray groups_array = response.optJSONArray("groups");
        if (array == null)
            return full_notifications;
        if (profiles_array != null)
            full_notifications.profiles = VKFullUser.parseUsers(profiles_array, true);
        if (groups_array != null)
            full_notifications.groups = VKGroup.parseGroups(groups_array);
        full_notifications.notifications = VKNotification.parseNotifications(array);
        return full_notifications;
    }

}
