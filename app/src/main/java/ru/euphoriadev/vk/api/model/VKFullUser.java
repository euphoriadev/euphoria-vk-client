package ru.euphoriadev.vk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.euphoriadev.vk.api.Api;

import java.io.Serializable;
import java.util.ArrayList;

//Fields are optional. Should be null if not populated
public class VKFullUser implements Serializable {
    private static final long serialVersionUID = 1L;
    public long uid;
    public String first_name = "DELETED";
    public String last_name = "";
    public String nickname;
    public Integer sex = null;
    public Boolean online = null;
    public Boolean online_mobile = null;
    public String birthdate; //bdate
    /**
     * В запрос надо добваить photo_50
     * URL квадратной фотографии 50х50
     */
    public String photo;
    /**
     * В запрос надо добавить photo_200
     * URL необрезанной фотографии 200х200
     */
    public String photo_big;        //photo_200_orig
    /**
     * В запрос надо добавить photo_200
     * URL квадратной фотографии 200х200, <b>у некоторых пользователей, которые загружали фотографию давно её нет</b>
     */
    public String photo_200;
    /**
     * <b>В запрос надо добавить photo_100</b>
     * URL квадратной фотографии 100х100
     */
    public String photo_medium_rec;    //photo_100 квадратная
    /**
     * В запрос надо добавить photo_max
     * URL квадратной фотографии максимального формата, <b>у некоторых пользователей, которые загружали фотографию давно её нет</b>
     */
    public String photo_max;
    /**
     * В запрос надо добавить photo_max_orig
     * URL необрезанной фотографии максимального формата
     */
    public String photo_max_orig;    //photo_max_orig обычно квадратная, может быть не у всех
    /**
     * В запрос надо добавить photo_400_orig
     * URL необрезанной фотографии формата 400х400
     */
    public String photo_400_orig;
 //   public long city;
 //   public Integer country = null;
    public Integer timezone = null;
    public String lists;
    public String domain;
    public Integer rate = null;
    public Integer university = null; //if education
    public String university_name; //if education
    public Integer faculty = null; //if education
    public String faculty_name; //if education
    public Integer graduation = null; //if education
    public Boolean has_mobile = null;
    public String home_phone;
    public String mobile_phone;
    public String status;
    public Integer relation;
    public String friends_list_ids = null;
    public long last_seen;
    public int albums_count;
    public int videos_count;
    public int audios_count;
    public int notes_count;
    public int friends_count;
    public int user_photos_count;
    public int user_videos_count;
    public int followers_count;
    public long invited_by;
    //public int subscriptions_count;
    //public int online_friends_count;
    public String phone;//for getByPhones
    public int groups_count;
    //relation_partner
    public Long relation_partner_id;
    public String relation_partner_first_name;
    public String relation_partner_last_name;

    //new connections fields
    public String twitter;
    public String facebook;
    public String facebook_name;
    public String skype;
    public String livejounal;

    //new additional fields
    public String interests;
    public String movies;
    public String tv;
    public String books;
    public String games;
    public String about;

    public City city;


    public Country country;
    public Counters counters;

    public static VKFullUser parse(JSONObject source) {
        VKFullUser u = new VKFullUser();
        u.uid = source.optLong("id");
        if (!source.isNull("first_name"))
            u.first_name = Api.unescape(source.optString("first_name"));
        if (!source.isNull("last_name"))
            u.last_name = Api.unescape(source.optString("last_name"));
        if (!source.isNull("nickname"))
            u.nickname = Api.unescape(source.optString("nickname"));
        if (!source.isNull("screen_name"))
            u.domain = source.optString("screen_name");
        if (!source.isNull("online"))
            u.online = source.optInt("online") == 1;
        if (!source.isNull("online_mobile"))
            u.online_mobile = source.optInt("online_mobile") == 1;
        else
            //if it's not there it means false
            u.online_mobile = false;
        if (!source.isNull("sex"))
            u.sex = source.optInt("sex");
        if (!source.isNull("bdate"))
            u.birthdate = source.optString("bdate");
        if (!source.isNull("city"))
            u.city = City.parse(source.optJSONObject("city"));
        if (source.has("country"))
            u.country = Country.parse(source.optJSONObject("country"));
        if (source.has("counters"))
            u.counters = Counters.parse(source.optJSONObject("counters"));
        if (!source.isNull("timezone"))
            u.timezone = source.optInt("timezone");
        if (!source.isNull("photo_50"))
            u.photo = source.optString("photo_50");
        if (!source.isNull("photo_100"))
            u.photo_medium_rec = source.optString("photo_100");
        if (!source.isNull("photo_200_orig"))
            u.photo_big = source.optString("photo_200_orig");
        if (!source.isNull("photo_200"))
            u.photo_200 = source.optString("photo_200");
        if (!source.isNull("photo_max"))
            u.photo_max = source.optString("photo_max");
        if (!source.isNull("photo_max_orig"))
            u.photo_max_orig = source.optString("photo_max_orig");
        if (!source.isNull("photo_400_orig"))
            u.photo_400_orig = source.optString("photo_400_orig");
        if (!source.isNull("has_mobile"))
            u.has_mobile = source.optInt("has_mobile") == 1;
        if (!source.isNull("home_phone"))
            u.home_phone = source.optString("home_phone");
        if (!source.isNull("mobile_phone"))
            u.mobile_phone = source.optString("mobile_phone");
        if (!source.isNull("rate"))
            u.rate = source.optInt("rate");
        if (source.has("faculty"))
            u.faculty = source.optInt("faculty");
        if (!source.isNull("faculty_name"))
            u.faculty_name = source.optString("faculty_name");
        if (source.has("university"))
            u.university = source.optInt("university");
        if (!source.isNull("university_name"))
            u.university_name = source.optString("university_name");
        if (source.has("graduation"))
            u.graduation = source.optInt("graduation");
        if (!source.isNull("status"))
            u.status = Api.unescape(source.optString("status"));
        if (!source.isNull("relation"))
            u.relation = source.optInt("relation");
        if (!source.isNull("lists")) {
            JSONArray array = source.optJSONArray("lists");
            if (array != null) {
                String ids = "";
                for (int i = 0; i < array.length() - 1; ++i)
                    ids += array.optString(i) + ",";
                ids += array.optString(array.length() - 1);
                u.friends_list_ids = ids;
            }
        }
        if (!source.isNull("last_seen")) {
            JSONObject object = source.optJSONObject("last_seen");
            if (object != null)
                u.last_seen = object.optLong("time");
        }
        if (!source.isNull("counters")) {
            JSONObject object = source.optJSONObject("counters");
            if (object != null) {
                u.albums_count = object.optInt("albums");
                u.videos_count = object.optInt("videos");
                u.audios_count = object.optInt("audios");
                u.notes_count = object.optInt("notes");
                u.friends_count = object.optInt("friends");
                u.user_photos_count = object.optInt("user_photos");
                u.user_videos_count = object.optInt("user_videos");
                //u.online_friends_count = object.optInt("online_friends");
                u.followers_count = object.optInt("followers");
                //u.subscriptions_count = object.optInt("subscriptions");
                u.groups_count = object.optInt("groups");
            }
        }
        if (!source.isNull("relation_partner")) {
            JSONObject object = source.optJSONObject("relation_partner");
            if (object != null) {
                u.relation_partner_id = object.optLong("id");
                u.relation_partner_first_name = object.optString("first_name");
                u.relation_partner_last_name = object.optString("last_name");
            }
        }

        if (!source.isNull("twitter"))
            u.twitter = source.optString("twitter");
        if (!source.isNull("facebook"))
            u.facebook = source.optString("facebook");
        if (!source.isNull("facebook_name"))
            u.facebook_name = source.optString("facebook_name");
        if (!source.isNull("skype"))
            u.skype = source.optString("skype");
        if (!source.isNull("livejounal"))
            u.livejounal = source.optString("livejounal");

        if (!source.isNull("interests"))
            u.interests = source.optString("interests");
        if (!source.isNull("movies"))
            u.movies = source.optString("movies");
        if (!source.isNull("tv"))
            u.tv = source.optString("tv");
        if (!source.isNull("books"))
            u.books = source.optString("books");
        if (!source.isNull("games"))
            u.games = source.optString("games");
        if (!source.isNull("about"))
            u.about = source.optString("about");

        if (!source.isNull("invited_by"))
            u.invited_by = source.optLong("invited_by");

        return u;
    }

    public static VKFullUser parseFromNews(JSONObject jprofile) throws JSONException {
        VKFullUser m = new VKFullUser();
        m.uid = jprofile.getLong("id");
        m.first_name = Api.unescape(jprofile.optString("first_name"));
        m.last_name = Api.unescape(jprofile.optString("last_name"));
        m.photo = jprofile.optString("photo_50");
        m.photo_medium_rec = jprofile.optString("photo_100");
        if (jprofile.has("sex"))
            m.sex = jprofile.optInt("sex");
        if (!jprofile.isNull("online"))
            m.online = jprofile.optInt("online") == 1;
        return m;
    }

    public static VKFullUser parseFromGetByPhones(JSONObject o) throws JSONException {
        VKFullUser u = new VKFullUser();
        u.uid = o.getLong("id");
        u.first_name = Api.unescape(o.optString("first_name"));
        u.last_name = Api.unescape(o.optString("last_name"));
        u.phone = o.optString("phone");
        return u;
    }

    public static ArrayList<VKFullUser> parseUsers(JSONArray array) throws JSONException {
        return parseUsers(array, false);
    }

    public static ArrayList<VKFullUser> parseUsers(JSONArray array, boolean from_notifications) throws JSONException {
        ArrayList<VKFullUser> users = new ArrayList<VKFullUser>();
        //it may be null if no users returned
        //no users may be returned if we request users that are already removed
        if (array == null)
            return users;

        for (int i = 0; i < array.length(); ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKFullUser u;
            if (from_notifications)
                u = VKFullUser.parseFromNotifications(o);
            else
                u = VKFullUser.parse(o);
            users.add(u);
        }
        return users;
    }

    public static ArrayList<VKFullUser> parseUsersForGetByPhones(JSONArray array) throws JSONException {
        ArrayList<VKFullUser> users = new ArrayList<VKFullUser>();
        //it may be null if no users returned
        //no users may be returned if we request users that are already removed
        if (array == null)
            return users;
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            if (array.get(i) == null || (!(array.get(i) instanceof JSONObject)))
                continue;
            JSONObject o = (JSONObject) array.get(i);
            VKFullUser u = VKFullUser.parseFromGetByPhones(o);
            users.add(u);
        }
        return users;
    }

    public static VKFullUser parseFromFave(JSONObject jprofile) throws JSONException {
        VKFullUser m = new VKFullUser();
        m.uid = Long.parseLong(jprofile.getString("id"));
        m.first_name = Api.unescape(jprofile.optString("first_name"));
        m.last_name = Api.unescape(jprofile.optString("last_name"));
        m.photo_medium_rec = jprofile.optString("photo_100");
        if (!jprofile.isNull("online"))
            m.online = jprofile.optInt("online") == 1;
        if (!jprofile.isNull("online_mobile"))
            m.online_mobile = jprofile.optInt("online_mobile") == 1;
        else
            //if it's not there it means false
            m.online_mobile = false;
        return m;
    }

    public static VKFullUser parseFromNotifications(JSONObject jprofile) throws JSONException {
        VKFullUser m = new VKFullUser();
        m.uid = jprofile.getLong("id");
        m.first_name = Api.unescape(jprofile.optString("first_name"));
        m.last_name = Api.unescape(jprofile.optString("last_name"));
        m.photo_medium_rec = jprofile.optString("photo_100");
        m.photo = jprofile.optString("photo_50");
        return m;
    }

    @Override
    public String toString() {
        return first_name + " " + last_name;
    }

    public static class LastActivity {
        public boolean online;
        public long last_seen;

        public static LastActivity parse(JSONObject o) {
            LastActivity u = new LastActivity();
            u.online = o.optInt("online") == 1;
            u.last_seen = o.optLong("time");
            return u;
        }
    }
}
