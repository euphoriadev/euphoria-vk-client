package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by user on 13.07.15.
 */
public class VKGroup implements Serializable {

    /**
     * Group id
     */
    public long gid;

    /**
     * Community name
     */
    public String name;

    /**
     * Screen name of the community page (e.g. apiclub or club1).
     */
    public String screen_name;

    /**
     * Whether the community is closed
     */
    public int is_closed;

    /**
     * Whether a user is the community manager
     */
    public boolean is_admin;

    /**
     * Rights of the user
     *
     * @see ru.euphoriadev.vk.api.model.VKGroup.AdminLevel
     */
    public int admin_level;

    /**
     * Whether a user is a community member
     */
    public boolean is_member;

    /**
     * Community type
     */
    public int type;

    /**
     * URL of the 50px-wide community logo.
     */
    public String photo_50;

    /**
     * URL of the 100px-wide community logo.
     */
    public String photo_100;

    /**
     * URL of the 200px-wide community logo.
     */
    public String photo_200;

    /**
     * Community description text
     */
    public String description;

    /**
     * Number of community members
     */
    public long members_count;

    /**
     * Group status. Returns a string with status text that is on the group page below its name.
     */
    public String status;

    /**
     * information from the block contacts public pages
     */
    public ArrayList<Contact> contacts = new ArrayList<>();


    public static VKGroup parse(JSONObject sourse) {
        VKGroup community = new VKGroup();
        community.gid = sourse.optLong("id");
        community.name = sourse.optString("name");
        community.screen_name = sourse.optString("screen_name");
        community.is_closed = sourse.optInt("is_closed");
        community.is_admin = sourse.optLong("is_admin") == 1;
        community.is_member = sourse.optLong("is_member") == 1;
        community.admin_level = sourse.optInt("admin_level");

        String type = sourse.optString("type", "group");
        switch (type) {
            case "group":
                community.type = Type.GROUP;
                break;
            case "page":
                community.type = Type.PAGE;
                break;
            case "event":
                community.type = Type.EVENT;
                break;
        }

        community.photo_50 = sourse.optString("photo_50");
        community.photo_100 = sourse.optString("photo_100");
        community.photo_200 = sourse.optString("photo_200");

        community.description = sourse.optString("description");
        community.status = sourse.optString("status");
        community.members_count = sourse.optLong("members_count");

        JSONArray array = sourse.optJSONArray("contacts");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                community.contacts.add(Contact.parse(array.optJSONObject(i)));
            }
        }

        return community;
    }

    public static ArrayList<VKGroup> parseGroups(JSONArray array) throws JSONException {
        ArrayList<VKGroup> groups = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            VKGroup g = VKGroup.parse(array.getJSONObject(i));
            groups.add(g);
        }
        return groups;
    }


    public static class AdminLevel {
        public final static int MODERATOR = 1;
        public final static int EDITOR = 2;
        public final static int ADMIN = 3;
        private AdminLevel() {
        }
    }

    /**
     * status of the group.
     */
    public static class Status {
        public final static int OPEN = 0;
        public final static int CLOSED = 1;
        public final static int PRIVATE = 2;
        private Status() {
        }

//        public final static String BANNED = "banned";
//        public final static String DELETED = "deleted";
    }

    /**
     * Types of communities.
     */
    public static class Type {
        public final static int GROUP = 0;
        public final static int PAGE = 1;
        public final static int EVENT = 2;
        private Type() {
        }
    }


}
