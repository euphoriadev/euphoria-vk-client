package ru.euphoriadev.vk.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import ru.euphoriadev.vk.api.model.AudioAlbum;
import ru.euphoriadev.vk.api.model.BannArg;
import ru.euphoriadev.vk.api.model.City;
import ru.euphoriadev.vk.api.model.CommentList;
import ru.euphoriadev.vk.api.model.Counters;
import ru.euphoriadev.vk.api.model.Country;
import ru.euphoriadev.vk.api.model.FriendsList;
import ru.euphoriadev.vk.api.model.GroupBanItem;
import ru.euphoriadev.vk.api.model.GroupTopic;
import ru.euphoriadev.vk.api.model.Newsfeed;
import ru.euphoriadev.vk.api.model.PhotoTag;
import ru.euphoriadev.vk.api.model.SearchDialogItem;
import ru.euphoriadev.vk.api.model.VKAlbum;
import ru.euphoriadev.vk.api.model.VKAttachment;
import ru.euphoriadev.vk.api.model.VKAudio;
import ru.euphoriadev.vk.api.model.VKChat;
import ru.euphoriadev.vk.api.model.VKComment;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.api.model.VKFullUser;
import ru.euphoriadev.vk.api.model.VKGift;
import ru.euphoriadev.vk.api.model.VKGroup;
import ru.euphoriadev.vk.api.model.VKLink;
import ru.euphoriadev.vk.api.model.VKLongPollServer;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKMessageAttachment;
import ru.euphoriadev.vk.api.model.VKNote;
import ru.euphoriadev.vk.api.model.VKNotifications;
import ru.euphoriadev.vk.api.model.VKPhoto;
import ru.euphoriadev.vk.api.model.VKPoll;
import ru.euphoriadev.vk.api.model.VKResolveScreenName;
import ru.euphoriadev.vk.api.model.VKStatus;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.api.model.VKVideo;
import ru.euphoriadev.vk.api.model.VKWallMessage;
import ru.euphoriadev.vk.util.Account;

/**
 * VK Api. Модифицированная мной версия от разработчиков Kate Mobile
 * SDK ВКонтакте для основных мобильных платформ: iOS, Android и Windows Phone
 * — сокращает время интеграции API ВКонтакте и предоставляет дополнительные преимущества
 *
 * Подробнее - http://vk.com/dev/main
 *
 * TODO: Некоторые методы были скрыти от пользователей
 * Такие как gifts.send - отправка подарка
 * Полный список можно получить здесь https://vkapi.zf-projects.ru/methods-list
 *
 */
public class Api {
    public static final String BASE_URL = "https://api.vk.com/method/";
    public static final String API_VERSION = "5.14";
    static final String TAG = "Kate.Api";
    private final static int MAX_TRIES = 3;
    //TODO: it's not faster, even slower on slow devices. Maybe we should add an option to disable it. It's only good for paid internet connection.
    static boolean enable_compression = true;
    private static Api mSingleton;
    String language = Locale.getDefault().getLanguage();
    String access_token;
    String api_id;
    private Account mAccount;

    private Api(String access_token, String api_id) {
        this.access_token = access_token;
        this.api_id = api_id;
    }

    private Api(Account account) {
        this(account.access_token, Account.API_ID);
        this.mAccount = account;
    }

    public static Api init(String access_token, String api_id) {
        if (mSingleton == null) {
            mSingleton = new Api(access_token, api_id);
        }
        return mSingleton;
    }

    public static synchronized Api init(Account account) {
        if (mSingleton == null) {
            mSingleton = new Api(account);
        }
        return mSingleton;
    }

    public static Api get() {
        return mSingleton;
    }

    public static String sendRequestInternal(String url, String body, boolean is_post) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
//            connection.connect();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(is_post);
            connection.setDoInput(true);
            connection.setRequestMethod(is_post ? "POST" : "GET");
            if (enable_compression)
                connection.setRequestProperty("Accept-Encoding", "gzip");
            if (is_post)
                connection.getOutputStream().write(body.getBytes("UTF-8"));
            int code = connection.getResponseCode();
            Log.i(TAG, "code=" + code);
            //It may happen due to keep-alive problem http://stackoverflow.com/questions/1440957/httpurlconnection-getresponsecode-returns-1-on-second-invocation
            if (code == -1)
                throw new WrongResponseCodeException("Network error");
            //может стоит проверить на код 200
            //on error can also read error stream from connection.
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc = connection.getHeaderField("Content-Encoding");
            if (enc != null && enc.equalsIgnoreCase("gzip"))
                is = new GZIPInputStream(is);
            return Utils.convertStreamToString(is);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    public static String unescape(String text) {
        if (text == null)
            return null;
        return text.replace("&amp;", "&").replace("&quot;", "\"").replace("<br>", "\n").replace("&gt;", ">").replace("&lt;", "<")
                .replace("<br/>", "\n").replace("&ndash;", "-").trim();
        //Баг в API
        //amp встречается в сообщении, br в Ответах тип comment_photo, gt lt на стене - баг API, ndash в статусе когда аудио транслируется
        //quot в тексте сообщения из LongPoll - то есть в уведомлении
    }

    public static String unescapeWithSmiles(String text) {
        return unescape(text)
                //May be useful to someone
                //.replace("\uD83D\uDE0A", ":-)")
                //.replace("\uD83D\uDE03", ":D")
                //.replace("\uD83D\uDE09", ";-)")
                //.replace("\uD83D\uDE06", "xD")
                //.replace("\uD83D\uDE1C", ";P")
                //.replace("\uD83D\uDE0B", ":p")
                //.replace("\uD83D\uDE0D", "8)")
                //.replace("\uD83D\uDE0E", "B)")
                //
                //.replace("\ud83d\ude12", ":(")  //F0 9F 98 92
                //.replace("\ud83d\ude0f", ":]")  //F0 9F 98 8F
                //.replace("\ud83d\ude14", "3(")  //F0 9F 98 94
                //.replace("\ud83d\ude22", ":'(")  //F0 9F 98 A2
                //.replace("\ud83d\ude2d", ":_(")  //F0 9F 98 AD
                //.replace("\ud83d\ude29", ":((")  //F0 9F 98 A9
                //.replace("\ud83d\ude28", ":o")  //F0 9F 98 A8
                //.replace("\ud83d\ude10", ":|")  //F0 9F 98 90
                //
                //.replace("\ud83d\ude0c", "3)")  //F0 9F 98 8C
                //.replace("\ud83d\ude20", ">(")  //F0 9F 98 A0
                //.replace("\ud83d\ude21", ">((")  //F0 9F 98 A1
                //.replace("\ud83d\ude07", "O:)")  //F0 9F 98 87
                //.replace("\ud83d\ude30", ";o")  //F0 9F 98 B0
                //.replace("\ud83d\ude32", "8o")  //F0 9F 98 B2
                //.replace("\ud83d\ude33", "8|")  //F0 9F 98 B3
                //.replace("\ud83d\ude37", ":X")  //F0 9F 98 B7
                //
                //.replace("\ud83d\ude1a", ":*")  //F0 9F 98 9A
                //.replace("\ud83d\ude08", "}:)")  //F0 9F 98 88
                //.replace("\u2764", "<3")  //E2 9D A4
                //.replace("\ud83d\udc4d", ":like:")  //F0 9F 91 8D
                //.replace("\ud83d\udc4e", ":dislike:")  //F0 9F 91 8E
                //.replace("\u261d", ":up:")  //E2 98 9D
                //.replace("\u270c", ":v:")  //E2 9C 8C
                //.replace("\ud83d\udc4c", ":ok:")  //F0 9F 91 8C
                ;
    }

    public int getUserId() {
        return (int) mAccount.user_id;
    }

    public Account getAccount() {
        return mAccount;
    }

    public void setAccount(Account account) {
        this.mAccount = account;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public void setApiId(String api_id) {
        this.api_id = api_id;
    }

    /**
     * utils methods**
     */
    private void checkError(JSONObject root, String url) throws JSONException, KException {
        if (!root.isNull("error")) {
            JSONObject error = root.getJSONObject("error");
            int code = error.getInt("error_code");
            String message = error.getString("error_msg");
            KException e = new KException(code, message, url);
            if (code == 14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code == 17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
        if (!root.isNull("execute_errors")) {
            JSONArray errors = root.getJSONArray("execute_errors");
            if (errors.length() == 0)
                return;
            // only first error is processed if there are multiple
            JSONObject error = errors.getJSONObject(0);
            int code = error.getInt("error_code");
            String message = error.getString("error_msg");
            KException e = new KException(code, message, url);
            if (code == 14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code == 17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
    }

    public JSONObject sendRequest(VKParams params) throws JSONException, IOException, KException {
        return sendRequest(params, false);
    }

    private JSONObject sendRequest(VKParams params, boolean is_post) throws IOException, JSONException, KException {
        String url = getSignedUrl(params, is_post);
        String body = "";
        if (is_post)
            body = params.getParamsString();
        Log.i(TAG, "url=" + url);
        if (body.length() != 0)
            Log.i(TAG, "body = ".concat(body));
        String response = "";
        for (int i = 1; i <= MAX_TRIES; ++i) {
            try {
                if (i != 1)
                    Log.i(TAG, "try " + i);
                response = sendRequestInternal(url, body, is_post);
                break;
            } catch (IOException ex) {
                processNetworkException(i, ex);
            }
        }
        Log.i(TAG, "response = ".concat(response));
        JSONObject root = new JSONObject(response);
        checkError(root, url);
        return root;
    }

    private void processNetworkException(int i, IOException ex) throws IOException {
        ex.printStackTrace();
        if (i == MAX_TRIES)
            throw ex;
    }

    private String getSignedUrl(VKParams params, boolean is_post) {
        params.put("access_token", access_token);
        if (!params.contains("v"))
            params.put("v", API_VERSION);
        if (!params.contains("lang"))
            params.put("lang", language);

        String args = "";
        if (!is_post)
            args = params.getParamsString();

        return BASE_URL + params.method_name + "?" + args;
    }

    /**
     * API methods **
     */
    //http://vk.com/dev/database.getCities
    public ArrayList<City> getCitiesByCountry(int country, String q) throws IOException, JSONException, KException {
        VKParams params = new VKParams("database.getCities");
        params.put("country_id", country);
        params.put("q", q);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<City> cities = new ArrayList<City>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = (JSONObject) array.get(i);
                City c = City.parse(o);
                cities.add(c);
            }
        }
        return cities;
    }

    //http://vk.com/dev/database.getCitiesById
    public ArrayList<City> getCitiesById(Collection<Long> cids) throws IOException, JSONException, KException {
        if (cids == null || cids.size() == 0)
            return null;
        VKParams params = new VKParams("database.getCitiesById");
        params.put("city_ids", arrayToString(cids));
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<City> cities = new ArrayList<City>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = (JSONObject) array.get(i);
                City c = City.parse(o);
                cities.add(c);
            }
        }
        return cities;
    }

    <T> String arrayToString(Collection<T> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        StringBuilder buffer = new StringBuilder(32);
        for (Object item : items) {
            buffer.append(item);
            buffer.append(',');
        }
        return buffer.toString();
    }

    //http://vk.com/dev/database.getCountries
    public ArrayList<Country> getCountries(Integer need_full, String code) throws IOException, JSONException, KException {
        VKParams params = new VKParams("database.getCountries");
        params.put("need_all", need_full);
        params.put("code", code);
        if (need_full != null && need_full == 1)
            params.put("count", 1000);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<Country> countries = new ArrayList<Country>();
        int category_count = array.length();
        for (int i = 0; i < category_count; i++) {
            JSONObject o = (JSONObject) array.get(i);
            Country c = Country.parse(o);
            countries.add(c);
        }
        return countries;
    }

    //http://vk.com/dev/database.getCountriesById
    public ArrayList<Country> getCountriesById(Collection<Long> cids) throws IOException, JSONException, KException {
        if (cids == null || cids.size() == 0)
            return null;
        VKParams params = new VKParams("database.getCountriesById");
        String str_cids = arrayToString(cids);
        params.put("country_ids", str_cids);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        ArrayList<Country> countries = new ArrayList<Country>();
        int category_count = array.length();
        for (int i = 0; i < category_count; i++) {
            JSONObject o = (JSONObject) array.get(i);
            Country c = Country.parse(o);
            countries.add(c);
        }
        return countries;
    }

    //*** methods for users ***//
    //http://vk.com/dev/users.get
    public ArrayList<VKFullUser> getProfilesFull(Collection<Integer> uids, Collection<String> domains, String fields, String name_case, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        if (uids == null && domains == null)
            return null;
        if ((uids != null && uids.size() == 0) || (domains != null && domains.size() == 0))
            return null;
        VKParams params = new VKParams("users.get");
        if (uids != null && uids.size() > 0)
            params.put("user_ids", arrayToString(uids));
        if (domains != null && domains.size() > 0)
            params.put("user_ids", arrayToString(domains));
        params.put("fields", fields);
        params.put("name_case", name_case);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKFullUser.parseUsers(array);
    }

    public VKFullUser getProfileFull(long user_id, String fields) throws JSONException, IOException, KException {
        VKParams params = new VKParams("users.get");
        params.put("user_id", user_id);
        params.put("fields", fields);
        addCaptchaParams(null, null, params);

        JSONObject root = sendRequest(params);
        JSONArray response = root.optJSONArray("response");
        return VKFullUser.parse(response.optJSONObject(0));
    }

    public ArrayList<VKUser> getProfiles(Collection<Integer> uids, Collection<String> domains, String name_case, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        if (uids == null && domains == null)
            return null;
        if ((uids != null && uids.size() == 0) || (domains != null && domains.size() == 0))
            return null;
        VKParams params = new VKParams("users.get");
        if (uids != null && uids.size() > 0)
            params.put("user_ids", arrayToString(uids));
        if (domains != null && domains.size() > 0)
            params.put("user_ids", arrayToString(domains));
        params.put("fields", VKUser.FIELDS_DEFAULT);
        params.put("name_case", name_case);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKUser.parseUsers(array);
    }

    public VKUser getProfile(int user_id) {
        try {
            ArrayList<Integer> uids = new ArrayList<>();
            uids.add(user_id);

            return getProfiles(uids, null, null, null, null).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * methods for friends **
     */
    //http://vk.com/dev/friends.get
    public ArrayList<VKFullUser> getFriendsFull(Long user_id, String fields, String order, Integer lid, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.get");
        params.put("fields", fields);
        params.put("user_id", user_id);
        params.put("list_id", lid);
        params.put("order", order);

        /** UPD: У меня все работает */
        // сортировка по популярности не даёт запросить друзей из списка
        //     if(lid==null)
        //         params.put("order","hints");

        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKFullUser> users = new ArrayList<VKFullUser>();
        //if there are no friends "response" will not be array
        if (array == null)
            return users;
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKFullUser u = VKFullUser.parse(o);
            users.add(u);
        }
        return users;
    }

    //http://vk.com/dev/friends.get
    public ArrayList<VKUser> getFriends(long user_id, String order, Integer count, Integer lid, String captcha_key, String captcha_sid) throws JSONException, IOException, KException {
        VKParams params = new VKParams("friends.get");
        params.put("user_id", user_id);
        params.put("fields", VKUser.FIELDS_DEFAULT);
        params.put("list_id", lid);
        params.put("count", count);
        params.put("order", order);

        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");

        return VKUser.parseUsers(array);
    }

    //http://vk.com/dev/friends.getOnline
    public ArrayList<Long> getOnlineFriends(Long uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.getOnline");
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                Long id = array.optLong(i, -1);
                if (id != -1) users.add(id);
            }
        }
        return users;
    }

    //http://vk.com/dev/friends.getAppUsers
    public ArrayList<Long> getAppUsersFriends() throws JSONException, IOException, KException {
        VKParams params = new VKParams("friends.getAppUsers");
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");

        ArrayList<Long> users = new ArrayList<>();
        if (array != null)
            for (int i = 0; i < array.length(); i++) {
                users.add(array.optLong(i, -1));
            }
        return users;
    }

    //http://vk.com/dev/likes.getList
    public ArrayList<Long> getLikeUsers(String item_type, long item_id, long owner_id, String filter) throws IOException, JSONException, KException {
        VKParams params = new VKParams("likes.getList");
        params.put("type", item_type);
        params.put("owner_id", owner_id);
        params.put("item_id", item_id);
        params.put("filter", filter); //likes - default, copies 
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                Long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    //http://vk.com/dev/friends.getMutual
    public ArrayList<Long> getMutual(Long target_uid, Long source_uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.getMutual");
        params.put("target_uid", target_uid);
        params.put("source_uid", source_uid);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                Long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    /**
     * methods for photos **
     */
    //http://vk.com/dev/photos.getAlbums
    public ArrayList<VKAlbum> getAlbums(Long oid, Collection<Long> aids, Integer need_system, Integer need_covers, Integer photo_sizes) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getAlbums");
        params.put("owner_id", oid);
        params.put("album_ids", arrayToString(aids));
        params.put("need_system", need_system);
        params.put("need_covers", need_covers);
        params.put("photo_sizes", photo_sizes);
        JSONObject root = sendRequest(params);
        ArrayList<VKAlbum> albums = new ArrayList<VKAlbum>();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        if (array == null)
            return albums;
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKAlbum a = VKAlbum.parse(o);
            if (a.title.equals("DELETED"))
                continue;
            albums.add(a);
        }
        return albums;
    }

    //http://vk.com/dev/photos.get
    public ArrayList<VKPhoto> getPhotos(Long uid, Long aid, Integer offset, Integer count, boolean rev) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.get");
        params.put("owner_id", uid);
        params.put("album_id", aid);
        params.put("extended", "1");
        params.put("offset", offset);
        params.put("limit", count);
        if (rev)
            params.put("rev", 1);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        if (array == null)
            return new ArrayList<VKPhoto>();
        ArrayList<VKPhoto> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/dev/photos.getUserPhotos
    public ArrayList<VKPhoto> getUserPhotos(Long uid, Integer offset, Integer count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getUserPhotos");
        params.put("user_id", uid);
        params.put("sort", "0");
        params.put("count", count);
        params.put("offset", offset);
        params.put("extended", 1);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        if (array == null)
            return new ArrayList<VKPhoto>();
        ArrayList<VKPhoto> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/dev/photos.getAll
    public ArrayList<VKPhoto> getAllPhotos(Long owner_id, Integer offset, Integer count, boolean extended) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getAll");
        params.put("owner_id", owner_id);
        params.put("offset", offset);
        params.put("count", count);
        params.put("extended", extended ? 1 : 0);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        if (array == null)
            return new ArrayList<VKPhoto>();
        ArrayList<VKPhoto> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/dev/photos.getComments
    public CommentList getPhotoComments(Long pid, Long owner_id, int offset, int count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getComments");
        params.put("photo_id", pid);
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        params.put("sort", "asc");
        params.put("need_likes", "1");
        JSONObject root = sendRequest(params);
        CommentList commnets = new CommentList();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        commnets.count = response.getInt("count");
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKComment comment = VKComment.parse(o);
            commnets.comments.add(comment);
        }
        return commnets;
    }

    //http://vk.com/dev/notes.getComments
    public CommentList getNoteComments(Long nid, Long owner_id, int offset, int count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notes.getComments");
        params.put("note_id", nid);
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        CommentList commnets = new CommentList();
        commnets.count = response.getInt("count");
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKComment comment = VKComment.parseNoteComment(o);
            commnets.comments.add(comment);
        }
        return commnets;
    }

    //http://vk.com/dev/video.getComments
    public CommentList getVideoComments(long video_id, Long owner_id, int offset, int count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.getComments");
        params.put("video_id", video_id);
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        params.put("need_likes", "1");
        JSONObject root = sendRequest(params);
        CommentList commnets = new CommentList();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        commnets.count = response.getInt("count");
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKComment comment = VKComment.parse(o);
            commnets.comments.add(comment);
        }
        return commnets;
    }

    //http://vk.com/dev/photos.getAllComments
    public ArrayList<VKComment> getAllPhotoComments(Long owner_id, Long album_id, int offset, int count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getAllComments");
        params.put("owner_id", owner_id);
        params.put("album_id", album_id);
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        params.put("need_likes", "1");
        ArrayList<VKComment> commnets = new ArrayList<VKComment>();
        @SuppressWarnings("unused")
        JSONObject root = sendRequest(params);
        //здесь ещё приходит pid - photo_id
        //вынести парсящий код чтобы не было дублирования
        //JSONArray array = root.getJSONArray("response");
        //int category_count = array.length();
        //for(int i = 0; i<category_count; ++i) {
        //    JSONObject o = (JSONObject)array.get(i);
        //    Comment comment = new Comment();
        //    comment.cid = Long.parseLong(o.getString("comment_id"));
        //    comment.from_id = Long.parseLong(o.getString("from_id"));
        //    comment.date = Long.parseLong(o.getString("date"));
        //    comment.message = unescape(o.getString("message"));
        //    commnets.add(comment);
        //}
        return commnets;
    }

    //http://vk.com/dev/photos.createComment
    public long createPhotoComment(Long pid, Long owner_id, String message, Long reply_to_cid, Collection<String> attachments, boolean from_group, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.createComment");
        params.put("photo_id", pid);
        params.put("owner_id", owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message", message);
        params.put("reply_to_comment", reply_to_cid);
        params.put("attachments", arrayToString(attachments));
        if (from_group)
            params.put("from_group", "1");
        JSONObject root = sendRequest(params, true);
        long message_id = root.optLong("response");
        return message_id;
    }

    //http://vk.com/dev/photos.editComment
    public boolean editPhotoComment(long cid, long pid, Long owner_id, String message, Collection<String> attachments, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.editComment");
        params.put("comment_id", cid);
        params.put("photo_id", pid);//probably not reqiured - missing in docs
        params.put("owner_id", owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message", message);
        params.put("attachments", arrayToString(attachments));
        JSONObject root = sendRequest(params, true);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/notes.createComment
    public long createNoteComment(Long nid, Long owner_id, String message, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notes.createComment");
        params.put("note_id", nid);
        params.put("owner_id", owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message", message);
        //if (reply_to != null && !reply_to.equals(""))
        //    params.put("reply_to", reply_to);
        JSONObject root = sendRequest(params, true);
        long message_id = root.optLong("response");
        return message_id;
    }

    //http://vk.com/dev/notes.editComment
    public boolean editNoteComment(long cid, Long owner_id, String message, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notes.editComment");
        params.put("comment_id", cid);
        params.put("owner_id", owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message", message);
        JSONObject root = sendRequest(params, true);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/video.createComment
    public long createVideoComment(Long video_id, Long owner_id, String message, Collection<String> attachments, boolean from_group, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.createComment");
        params.put("video_id", video_id);
        params.put("owner_id", owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message", message);
        params.put("attachments", arrayToString(attachments));
        if (from_group)
            params.put("from_group", "1");
        JSONObject root = sendRequest(params, true);
        long message_id = root.optLong("response");
        return message_id;
    }

    //http://vk.com/dev/video.editComment
    public boolean editVideoComment(long cid, Long owner_id, String message, Collection<String> attachments, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.editComment");
        params.put("comment_id", cid);
        params.put("owner_id", owner_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        params.put("message", message);
        params.put("attachments", arrayToString(attachments));
        JSONObject root = sendRequest(params, true);
        int response = root.optInt("response");
        return response == 1;
    }

    private void addCaptchaParams(String captcha_key, String captcha_sid, VKParams params) {
        params.put("captcha_sid", captcha_sid);
        params.put("captcha_key", captcha_key);
    }

    /**
     * methods for messages
     *
     * @throws KException **
     */
    //http://vk.com/dev/messages.get
    public ArrayList<VKMessage> getMessages(long time_offset, int filters, boolean is_out, int count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.get");
        if (is_out)
            params.put("out", "1");
        if (time_offset != 0)
            params.put("time_offset", time_offset);
        if (count != 0)
            params.put("count", count);
        if (filters != 0)
            params.put("filters", filters);

        params.put("preview_length", "0");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKMessage> messages = parseMessages(array);
        return messages;
    }

    //http://vk.com/dev/messages.getHistory
    public ArrayList<VKMessage> getMessagesHistory(long uid, long chat_id, long offset, int count, boolean rev) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.getHistory");
        if (chat_id <= 0)
            params.put("user_id", uid);
        else
            params.put("chat_id", chat_id);
        params.put("offset", offset);
        if (count != 0)
            params.put("count", count);
        params.put("rev", rev);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKMessage.parseArray(array);
    }

    //http://vk.com/dev/messages.getDialogs
    public ArrayList<VKMessage> getMessagesDialogs(long offset, int count, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.getDialogs");
        if (offset != 0)
            params.put("offset", offset);
        if (count != 0)
            params.put("count", count);
        params.put("preview_length", "50");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return parseMessages(array);
    }

    // http://vk.com/dev/messages.getHistoryAttachments
    public ArrayList<VKMessageAttachment> getHistoryAttachments(int peer_id, String media_type, Integer offset, Integer count, Boolean photo_sizes) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.getHistoryAttachments");
        params.put("peer_id", peer_id);
        params.put("media_type", media_type);
        params.put("offset", offset);
        params.put("count", count);
        params.put("count", count);
        params.put("photo_sizes", photo_sizes);

        JSONObject request = sendRequest(params);
        JSONObject response = request.optJSONObject("response");
        return VKMessageAttachment.parseArray(response.optJSONArray("items"), media_type, response.optInt("next_from"));
    }


    //http://vk.com/dev/messages.getLongPollServer
    public VKLongPollServer getLongPollServer(String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.getLongPollServer");
        params.put("need_pts", true);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");

        return VKLongPollServer.parse(response);
    }

    // https://vk.com/dev/messages.getLongPollHistory
    public ArrayList<VKMessage> getLongPollHistory(long ts, Long pts, long preview_length, boolean onlines, Long events_limit, Long msgs_limit, Long max_msg_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("messages.getLongPollHistory");
        params.put("ts", ts);
        params.put("pts", pts);
        params.put("preview_length", preview_length);
        params.put("onlines", onlines);
        params.put("events_limit", events_limit);
        params.put("msgs_limit", msgs_limit);
        params.put("max_msg_id", max_msg_id);

        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONObject messages = response.optJSONObject("messages");

        return VKMessage.parseArray(messages.optJSONArray("items"));
    }

    //http://vk.com/dev/messages.setActivity
    public Integer setMessageActivity(Integer uid, Integer chat_id, boolean typing) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.setActivity");
        params.put("user_id", uid);
        params.put("chat_id", chat_id);
        if (typing)
            params.put("type", "typing");
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    private ArrayList<VKMessage> parseMessages(JSONArray array) throws JSONException {
//        ArrayList<VKMessage> messages = new ArrayList<VKMessage>();
//        if (array != null) {
//            int category_count = array.length();
//            for (int i = 0; i < category_count; ++i) {
//                JSONObject o = array.getJSONObject(i);
////                VKMessage m = VKMessage.parseArray(o, from_history, history_uid, from_chat, me);
//                VKMessage m = VKMessage.parseArray(o);
//                messages.add(m);
//            }
//        }
        return VKMessage.parseArray(array);
    }

    //http://vk.com/dev/messages.send
    public Integer sendMessage(int uid, long chat_id, String message, String title, String type, Collection<String> attachments, ArrayList<Long> forward_messages, String lat, String lon, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.send");
        if (chat_id <= 0)
            params.put("user_id", uid);
        else
            params.put("chat_id", chat_id);
        params.put("message", message);
        params.put("title", title);
        params.put("type", type);
        params.put("attachment", arrayToString(attachments));
        params.put("forward_messages", arrayToString(forward_messages));
        params.put("lat", lat);
        params.put("long", lon);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        return root.optInt("response");
    }

    //http://vk.com/dev/messages.markAsNew
    //http://vk.com/dev/messages.markAsRead
    @Deprecated
    public String markAsNewOrAsRead(ArrayList<Long> mids, boolean as_read) throws IOException, JSONException, KException {
        if (mids == null || mids.size() == 0)
            return null;
        VKParams params;
        if (as_read)
            params = new VKParams("messages.markAsRead");
        else
            params = new VKParams("messages.markAsNew");
        params.put("message_ids", arrayToString(mids));
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/messages.markAsRead
    public Long markAsRead(ArrayList<Long> mids, Long peer_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("messages.markAsRead");
        params.put("message_ids", arrayToString(mids));
        params.put("peer_id", peer_id);
        JSONObject root = sendRequest(params);
        Long response = root.optLong("response");
        return response;
    }

    //http://vk.com/dev/messages.markAsNew
    @Deprecated
    public Long markAsNew(ArrayList<Long> mids) throws JSONException, IOException, KException {
        VKParams params = new VKParams("messages.markAsNew");
        params.put("message_ids", arrayToString(mids));
        JSONObject root = sendRequest(params);
        Long response = root.optLong("response");
        return response;
    }

    // https://vk.com/dev/messages.markAsImportant
    public ArrayList<Long> markAsImportant(ArrayList<Long> mids, boolean important) throws JSONException, IOException, KException {
        VKParams params = new VKParams("messages.markAsImportant");
        params.put("message_ids", arrayToString(mids));
        params.put("important", important);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<Long> message_ids = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            message_ids.add(array.optLong(i));
        }
        return message_ids;
    }

    //http://vk.com/dev/messages.delete
    public Long deleteMessage(Collection<Integer> message_ids) throws IOException, JSONException, KException {
        if (message_ids.isEmpty()) return null;

        VKParams params = new VKParams("messages.delete");
        params.put("message_ids", arrayToString(message_ids));
        JSONObject root = sendRequest(params);
        //не парсим ответ - там приходят отдельные флаги для каждого удалённого сообщения
        // TODO: Может быть, это так, но у меня возвращает 1
        return root.optLong("response", -1);
    }

    // Получение 1.5к сообщений в истории перписки
    public ArrayList<VKMessage> getMessagesHistoryWithExecute(Integer user_id, Integer chat_id, long me, long offset) throws JSONException, IOException, KException {
        String var;
        if (chat_id != 0) {
            var = "\"chat_id\":"+ chat_id + ",\n" +
                    "\"user_id\":"+ 0 + ",\n";
        } else var = "\"user_id\":"+ user_id + ",\n";

        String code =
                "var offset = " + offset + ";\n" +
                        "var v = " + API_VERSION + ";\n" +
                        "var a = API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset, \n" +
                        "\"count\":200, \n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "var b =  API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset + 200, \n" +
                        "\"count\":200,\n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "var c =  API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset + 400, \n" +
                        "\"count\":200,\n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "var d =  API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset + 600, \n" +
                        "\"count\":200,\n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "var e =  API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset + 800, \n" +
                        "\"count\":200,\n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "var k =  API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset + 1000, \n" +
                        "\"count\":200,\n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "var j =  API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset + 1200, \n" +
                        "\"count\":100,\n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "var i =  API.messages.getHistory({\n" +
                        var +
                        "\"offset\":offset + 1300, \n" +
                        "\"count\":200,\n" +
                        "\"v\":v\n" +
                        "});\n" +
                        "\n" +
                        "return a.items + b.items + c.items + d.items + e.items + k.items + j.items + i.items;";

        VKParams params = new VKParams("execute");
        params.put("code", code);
        JSONObject root = sendRequest(params);
        JSONArray response = root.optJSONArray("response");
        return parseMessages(response);
    }

    /**
     * for status**
     */
    //http://vk.com/dev/status.get
    public VKStatus getStatus(Long uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("status.get");
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        JSONObject obj = root.optJSONObject("response");
        VKStatus status = new VKStatus();
        if (obj != null) {
            status.text = unescape(obj.getString("text"));
            JSONObject jaudio = obj.optJSONObject("audio");
            if (jaudio != null)
                status.audio = VKAudio.parse(jaudio);
        }
        return status;
    }

    //http://vk.com/dev/status.set
    public String setStatus(String status_text) throws IOException, JSONException, KException {
        VKParams params = new VKParams("status.set");
        params.put("text", status_text);
        JSONObject root = sendRequest(params);
        Object response_id = root.opt("response");
        if (response_id != null)
            return String.valueOf(response_id);
        return null;
    }

    /**
     * methods for wall
     *
     * @throws KException **
     */
    //http://vk.com/dev/wall.get
    public ArrayList<VKWallMessage> getWall(long owner_id, int count, int offset, String filter) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.get");
        params.put("owner_id", owner_id);
        if (count > 0)
            params.put("count", count);
        params.put("offset", offset);
        params.put("filter", filter); //owner, others, all - default
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKWallMessage> wmessages = new ArrayList<VKWallMessage>();
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKWallMessage wm = VKWallMessage.parse(o);
            wmessages.add(wm);
        }
        return wmessages;
    }

    /**
     * methods for news
     *
     * @throws KException **
     */
    //http://vk.com/dev/newsfeed.get
    //always returns about 33-35 items
    public Newsfeed getNews(Long start_time, long count, Long end_time, Integer offset, String from, String source_ids, String filters, Integer max_photos, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.get");
        params.put("filters", filters);
        params.put("start_time", start_time);
        params.put("end_time", end_time);
        if (count != 0)
            params.put("count", count);
        params.put("offset", offset);
        params.put("from", from);
        params.put("source_ids", source_ids);
        params.put("max_photos", max_photos);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return Newsfeed.parse(root, false);
    }


    //http://vk.com/dev/newsfeed.getRecommended
    public Newsfeed getRecommendedNews(Long start_time, long count, Long end_time, Integer offset, String from, Integer max_photos, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.getRecommended");
        params.put("start_time", start_time);
        params.put("end_time", end_time);
        if (count != 0)
            params.put("count", count);
        params.put("offset", offset);
        params.put("from", from);
        params.put("max_photos", max_photos);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        Newsfeed parse = Newsfeed.parse(root, false);
        return parse;
    }

    //http://vk.com/dev/newsfeed.getComments
    public Newsfeed getNewsComments() throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.getComments");
        params.put("last_comments", "1");
        params.put("count", "50");
        JSONObject root = sendRequest(params);
        return Newsfeed.parse(root, true);
    }

    /**
     * for audio **
     */
    //http://vk.com/dev/audio.get
    public ArrayList<VKAudio> getAudio(Integer owner_id, Integer album_id, long count, long offset, Collection<Long> aids, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.get");
        if (owner_id != null)
            params.put("owner_id", owner_id);
        params.put("audio_ids", arrayToString(aids));//не документировано - возможно уже не работает - возможно нужно использовать audio.getById
        params.put("album_id", album_id);
        params.put("count", count);
        params.put("offset", offset);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return parseAudioList(array);
    }

    // http://vk.com/dev/audio.getCount
    public Long getAudioCount(Long owner_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("audio.getCount");
        params.put("owner_id", owner_id);

        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    //http://vk.com/dev/audio.getById
    public ArrayList<VKAudio> getAudioById(String audios, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.getById");
        params.put("audios", audios);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array);
    }

    //http://vk.com/dev/audio.getLyrics
    public String getLyricsAudio(Long id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.getLyrics");
        params.put("lyrics_id", id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optString("text");
    }

    //http://vk.com/dev/audio.search
    public ArrayList<VKAudio> searchAudio(String q, String sort, String lyrics, Long count, Long offset, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.search");
        params.put("q", q);
        params.put("sort", sort);
        params.put("lyrics", lyrics);
        params.put("count", count);
        params.put("offset", offset);
        params.put("auto_complete", "1");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return parseAudioList(array);
    }

    //http://vk.com/dev/audio.delete
    public String deleteAudio(Long aid, Long oid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.delete");
        params.put("audio_id", aid);
        params.put("album_id", aid);//Баг в Api - это лишний параметр
        params.put("owner_id", oid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/audio.add
    public String addAudio(Long aid, Long oid, Long gid, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.add");
        params.put("audio_id", aid);
        params.put("owner_id", oid);
        params.put("group_id", gid);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    // http://vk.com/dev/audio.restore
    // TODO: Если время хранения удаленной аудиозаписи истекло (обычно это 20 минут), сервер вернет ошибку 202 (Cache expired).
    public VKAudio restoreAudio(Long audio_id, Long owner_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("audio.restore");
        params.put("audio_id", audio_id);
        params.put("owner_id", owner_id);

        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return VKAudio.parse(response);
    }


    private ArrayList<VKAudio> parseAudioList(JSONArray array) throws JSONException {
        ArrayList<VKAudio> audios = new ArrayList<VKAudio>(array.length());
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) { //get(0) is integer, it is audio count
                JSONObject o = (JSONObject) array.get(i);
                audios.add(VKAudio.parse(o));
            }
        }
        return audios;
    }

    /**
     * for video **
     */
    //http://vk.com/dev/video.get
    public ArrayList<VKVideo> getVideo(String videos, int owner_id, Long album_id, String width, Long count, Long offset, String access_key) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.get");
        params.put("videos", videos);
        params.put("owner_id", owner_id);
        params.put("width", width);
        params.put("count", count);
        params.put("offset", offset);
        params.put("album_id", album_id);
        params.put("access_key", access_key);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        ArrayList<VKVideo> videoss = new ArrayList<VKVideo>();
        if (response == null)
            return videoss;
        JSONArray array = response.optJSONArray("items");
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                JSONObject o = (JSONObject) array.get(i);
                VKVideo video = VKVideo.parse(o);
                videoss.add(video);
            }
        }
        return videoss;
    }

    //http://vk.com/dev/video.getUserVideos
    public ArrayList<VKVideo> getUserVideo(Long user_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.getUserVideos");
        params.put("user_id", user_id);
        params.put("count", "50");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKVideo> videos = new ArrayList<VKVideo>();
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                JSONObject o = (JSONObject) array.get(i);
                videos.add(VKVideo.parse(o));
            }
        }
        return videos;
    }

    /**
     * for crate album **
     */
    //http://vk.com/dev/photos.createAlbum
    public VKAlbum createAlbum(String title, Long gid, String privacy, String comment_privacy, String description) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.createAlbum");
        params.put("title", title);
        params.put("group_id", gid);
        params.put("privacy", privacy);
        params.put("comment_privacy", comment_privacy);
        params.put("description", description);
        JSONObject root = sendRequest(params);
        JSONObject o = root.optJSONObject("response");
        if (o == null)
            return null;
        return VKAlbum.parse(o);
    }

    //http://vk.com/dev/photos.editAlbum
    public String editAlbum(long aid, Long oid, String title, String privacy, String comment_privacy, String description) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.editAlbum");
        params.put("album_id", String.valueOf(aid));
        params.put("owner_id", oid);
        params.put("title", title);
        params.put("privacy", privacy);
        params.put("comment_privacy", comment_privacy);
        params.put("description", description);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        return response_code != null ? String.valueOf(response_code) : null;
    }

    /**
     * for notes **
     */
    //http://vk.com/dev/notes.get
    public ArrayList<VKNote> getNotes(Integer uid, Collection<Long> nids, String sort, Long count, Long offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notes.get");
        params.put("user_id", uid);
        params.put("note_ids", arrayToString(nids));
        params.put("sort", sort);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");

        return VKNote.parseNotes(array);
    }

    //http://vk.com/dev/notes.delete
    public String deleteNote(Long nid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notes.delete");
        params.put("note_id", nid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/photos.getUploadServer
    public String getPhotoUploadServer(long album_id, Long group_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getUploadServer");
        params.put("album_id", album_id);
        params.put("group_id", group_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/photos.getWallUploadServer
    public String getWallUploadServer(Long user_id, Long group_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getWallUploadServer");
        params.put("user_id", user_id);
        params.put("group_id", group_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/audio.getUploadServer
    public String getAudioUploadServer() throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.getUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/photos.getMessagesUploadServer
    public String getPhotoMessageUploadServer() throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getMessagesUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/photos.getProfileUploadServer
    public String getPhotoProfileUploadServer() throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getProfileUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/photos.save
    public ArrayList<VKPhoto> savePhoto(String server, String photos_list, Long aid, Long group_id, String hash, String caption) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.save");
        params.put("server", server);
        params.put("photos_list", photos_list);
        params.put("album_id", aid);
        params.put("group_id", group_id);
        params.put("hash", hash);
        params.put("caption", caption);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        ArrayList<VKPhoto> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/dev/photos.saveWallPhoto
    public ArrayList<VKPhoto> saveWallPhoto(String server, String photo, String hash, Long user_id, Long group_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.saveWallPhoto");
        params.put("server", server);
        params.put("photo", photo);
        params.put("hash", hash);
        params.put("user_id", user_id);
        params.put("group_id", group_id);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        ArrayList<VKPhoto> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/dev/audio.save
    public VKAudio saveAudio(String server, String audio, String hash, String artist, String title) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.save");
        params.put("server", server);
        params.put("audio", audio);
        params.put("hash", hash);
        params.put("artist", artist);
        params.put("title", title);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return VKAudio.parse(response);
    }

    //http://vk.com/dev/photos.saveMessagesPhoto
    public ArrayList<VKPhoto> saveMessagesPhoto(String server, String photo, String hash) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.saveMessagesPhoto");
        params.put("server", server);
        params.put("photo", photo);
        params.put("hash", hash);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        ArrayList<VKPhoto> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/dev/photos.saveProfilePhoto
    public String[] saveProfilePhoto(String server, String photo, String hash) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.saveProfilePhoto");
        params.put("server", server);
        params.put("photo", photo);
        params.put("hash", hash);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        String src = response.optString("photo_src");
        String hash1 = response.optString("photo_hash");
        String[] res = new String[]{src, hash1};
        return res;
    }

    private ArrayList<VKPhoto> parsePhotos(JSONArray array) throws JSONException {
        ArrayList<VKPhoto> photos = new ArrayList<VKPhoto>();
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKPhoto p = VKPhoto.parse(o);
            photos.add(p);
        }
        return photos;
    }

    //http://vk.com/dev/wall.addComment
    public long createWallComment(Long owner_id, Long post_id, String text, Long reply_to_cid, Collection<String> attachments, boolean from_group, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.addComment");
        params.put("owner_id", owner_id);
        params.put("post_id", post_id);
        params.put("text", text);
        params.put("reply_to_comment", reply_to_cid);
        params.put("attachments", arrayToString(attachments));
        if (from_group)
            params.put("from_group", "1");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        JSONObject response = root.getJSONObject("response");
        long cid = response.optLong("comment_id");
        return cid;
    }

    //http://vk.com/dev/wall.editComment 
    public boolean editWallComment(long cid, Long owner_id, String text, Collection<String> attachments, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.editComment");
        params.put("comment_id", cid);
        params.put("owner_id", owner_id);
        params.put("message", text);
        params.put("attachments", arrayToString(attachments));
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/wall.post
    public long createWallPost(long owner_id, String text, Collection<String> attachments, String export, boolean only_friends, boolean from_group, boolean signed, String lat, String lon, Long publish_date, Long post_id, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.post");
        params.put("owner_id", owner_id);
        params.put("attachments", arrayToString(attachments));
        params.put("lat", lat);
        params.put("long", lon);
        params.put("message", text);
        if (export != null && export.length() != 0)
            params.put("services", export);
        if (from_group)
            params.put("from_group", "1");
        if (only_friends)
            params.put("friends_only", "1");
        if (signed)
            params.put("signed", "1");
        params.put("publish_date", publish_date);
        if (post_id > 0)
            params.put("post_id", post_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        JSONObject response = root.getJSONObject("response");
        long res_post_id = response.optLong("post_id");
        return res_post_id;
    }

    //http://vk.com/dev/wall.repost
    public VKWallMessage repostWallPost(String object, String message, Long gid, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.repost");
        params.put("group_id", gid);
        params.put("message", message);
        params.put("object", object);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        VKWallMessage wall = new VKWallMessage();
        wall.id = response.optLong("post_id");
        wall.like_count = response.optInt("likes_count");
        wall.reposts_count = response.optInt("reposts_count");
        return wall;
    }

    //http://vk.com/dev/wall.getComments
    public CommentList getWallComments(Long owner_id, Long post_id, int offset, int count, boolean reverse_order) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.getComments");
        params.put("post_id", post_id);
        params.put("owner_id", owner_id);
        /*
        if (sort != null)
            params.put("sort", sort);
            //asc - хронологический
            //desc - антихронологический
        */
        if (offset > 0)
            params.put("offset", offset);
        if (count > 0)
            params.put("count", count);
        params.put("preview_length", "0");
        params.put("need_likes", "1");
        if (reverse_order)
            params.put("sort", "desc");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        CommentList commnets = new CommentList();
        commnets.count = response.getInt("count");
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i)
            commnets.comments.add(VKComment.parse((JSONObject) array.get(i)));
        return commnets;
    }


    //deprecated, use http://vk.com/dev/likes.add instead
    @Deprecated
    public Long wallAddLike(Long owner_id, Long post_id, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        return addLike(owner_id, post_id, "post", null, captcha_key, captcha_sid);
    }

    //deprecated, use http://vk.com/dev/likes.delete instead
    @Deprecated
    public Long wallDeleteLike(Long owner_id, Long post_id) throws IOException, JSONException, KException {
        return deleteLike(owner_id, "post", post_id, null, null);
    }

    //http://vk.com/dev/likes.add
    public Long addLike(Long owner_id, Long item_id, String type, String access_key, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("likes.add");
        params.put("owner_id", owner_id);
        params.put("item_id", item_id);
        params.put("type", type);
        params.put("access_key", access_key);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        long likes = response.optLong("likes", -1);
        return likes;
    }

    //http://vk.com/dev/likes.delete
    public Long deleteLike(Long owner_id, String type, Long item_id, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("likes.delete");
        params.put("owner_id", owner_id);
        params.put("type", type);
        params.put("item_id", item_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optLong("likes", -1);
    }

    //http://vk.com/dev/photos.getById
    public ArrayList<VKPhoto> getPhotosById(String photos, Integer extended, Integer photo_sizes) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getById");
        params.put("photos", photos);
        params.put("extended", extended);
        params.put("photo_sizes", photo_sizes);
        JSONObject root = sendRequest(params);
        JSONArray response = root.optJSONArray("response");
        if (response == null)
            return new ArrayList<VKPhoto>();
        ArrayList<VKPhoto> photos1 = parsePhotos(response);
        return photos1;
    }

    public VKPhoto getPhotoCountsByIdWithExecute(String photo, boolean get_user_id) throws IOException, JSONException, KException {
        String b = (get_user_id) ? ",\"user_id\":p@.user_id" : "";
        String code = "var p=API.photos.getById({\"photos\":\"" + photo + "\",\"extended\":1}); return {\"pid\":p@.id,\"likes\":p@.likes,\"comments\":p@.comments,\"can_comment\":p@.can_comment,\"tags\":p@.tags" + b + "};";
        VKParams params = new VKParams("execute");
        params.put("code", code);
        JSONObject root = sendRequest(params);
        JSONObject array = root.optJSONObject("response");
        if (array == null)
            return null;
        return VKPhoto.parseCounts(array);
    }

    //http://vk.com/dev/groups.get
    public ArrayList<VKGroup> getGroups(Integer user_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.get");
        params.put("extended", "1");
        params.put("fields", "members_count");
        params.put("user_id", user_id);
        JSONObject root = sendRequest(params);
        ArrayList<VKGroup> groups = new ArrayList<VKGroup>();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        //if there are no groups "response" will not be array
        if (array == null)
            return groups;
        groups = VKGroup.parseGroups(array);
        return groups;
    }

    //http://vk.com/dev/wall.delete
    public Boolean deleteWallPost(Long post_id, long wall_owner_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.delete");
        params.put("owner_id", wall_owner_id);
        params.put("post_id", post_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/wall.deleteComment
    public Boolean deleteWallComment(Long wall_owner_id, long comment_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.deleteComment");
        params.put("owner_id", wall_owner_id);
        params.put("comment_id", comment_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/notes.deleteComment
    public Boolean deleteNoteComment(Long note_owner_id, long comment_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notes.deleteComment");
        params.put("owner_id", note_owner_id);
        params.put("comment_id", comment_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/video.deleteComment
    public Boolean deleteVideoComment(Long video_owner_id, long comment_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.deleteComment");
        params.put("owner_id", video_owner_id);
        params.put("comment_id", comment_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/photos.deleteComment
    public Boolean deletePhotoComment(long photo_id, Long photo_owner_id, long comment_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.deleteComment");
        params.put("owner_id", photo_owner_id);
        params.put("comment_id", comment_id);
        params.put("photo_id", photo_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/video.search
    public ArrayList<VKVideo> searchVideo(String q, String sort, String hd, Long count, Long offset, Integer adult, String filters) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.search");
        params.put("q", q);
        params.put("sort", sort);
        params.put("hd", hd);
        params.put("count", count);
        params.put("offset", offset);
        params.put("adult", adult);     //safe search: 1 - disabled, 0 - enabled
        params.put("filters", filters); //mp4, youtube, vimeo, short, long
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKVideo> videoss = new ArrayList<VKVideo>();
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                JSONObject o = (JSONObject) array.get(i);
                VKVideo video = VKVideo.parse(o);
                videoss.add(video);
            }
        }
        return videoss;
    }

    //http://vk.com/dev/users.search
    public ArrayList<VKFullUser> searchUser(String q, String fields, Long count, Long offset, Integer sort,
                                        Integer city, Integer country, String hometown, Integer university_country, Integer university, Integer university_year,
                                        Integer sex, Integer status, Integer age_from, Integer age_to, Integer birth_day, Integer birth_month, Integer birth_year,
                                        Integer online, Integer has_photo, Integer school_country, Integer school_city, Integer school, Integer school_year,
                                        String religion, String interests, String company, String position, Long gid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("users.search");
        params.put("q", q);
        params.put("count", count);
        params.put("offset", offset);
        params.put("fields", fields);
        if (sort != null && sort > 0)
            params.put("sort", sort);
        if (city != null && city > 0)
            params.put("city", city);
        if (country != null && country > 0)
            params.put("country", country);
        if (hometown != null && hometown.length() > 0)
            params.put("hometown", hometown);
        if (university_country != null && university_country > 0)
            params.put("university_country", university_country);
        if (university != null && university > 0)
            params.put("university", university);
        if (university_year != null && university_year > 0)
            params.put("university_year", university_year);
        if (sex != null && sex > 0)
            params.put("sex", sex);
        if (status != null && status > 0)
            params.put("status", status);
        if (age_from != null && age_from > 0)
            params.put("age_from", age_from);
        if (age_to != null && age_to > 0)
            params.put("age_to", age_to);
        if (birth_day != null && birth_day > 0)
            params.put("birth_day", birth_day);
        if (birth_month != null && birth_month > 0)
            params.put("birth_month", birth_month);
        if (birth_year != null && birth_year > 0)
            params.put("birth_year", birth_year);
        if (online != null && online > 0)
            params.put("online", online);
        if (has_photo != null && has_photo > 0)
            params.put("has_photo", has_photo);
        if (school_country != null && school_country > 0)
            params.put("school_country", school_country);
        if (school_city != null && school_city > 0)
            params.put("school_city", school_city);
        if (school != null && school > 0)
            params.put("school", school);
        if (school_year != null && school_year > 0)
            params.put("school_year", school_year);
        if (religion != null && religion.length() > 0)
            params.put("religion", religion);
        if (interests != null && interests.length() > 0)
            params.put("interests", interests);
        if (company != null && company.length() > 0)
            params.put("company", company);
        if (position != null && position.length() > 0)
            params.put("position", position);
        if (gid != null && gid > 0)
            params.put("group_id", gid);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKFullUser.parseUsers(array);
    }

    public ArrayList<VKFullUser> searchUserExtended(String q, String fields, Long count, Long offset, Integer sort,
                                                Integer city, Integer country, String hometown, Integer university_country, Integer university, Integer university_year,
                                                Integer sex, Integer status, Integer age_from, Integer age_to, Integer birth_day, Integer birth_month, Integer birth_year,
                                                Integer online, Integer has_photo, Integer school_country, Integer school_city, Integer school, Integer school_year,
                                                String religion, String interests, String company, String position, Long gid) throws IOException, JSONException, KException {
        String uids = Utils.parseProfileId(q);
        if (uids != null && uids.length() > 0 && (offset == null || offset == 0)) {
            Log.i(TAG, "Search with uid = " + uids);
            String code = "var a=API.users.search({\"q\":\"" + q + "\",\"count\":" + count + ",\"count\":" + offset + ",\"fields\":\"" + fields + "\"});";
            code += "var b=API.users.get({\"user_ids\":" + uids + ",\"fields\":\"" + fields + "\"});";
            code += "return b+a.items;";
            VKParams params = new VKParams("execute");
            params.put("code", code);
            JSONObject root = sendRequest(params);
            JSONArray array = root.optJSONArray("response");
            return VKFullUser.parseUsers(array);
        } else
            return searchUser(q, fields, count, offset, sort,
                    city, country, hometown, university_country, university, university_year,
                    sex, status, age_from, age_to, birth_day, birth_month, birth_year,
                    online, has_photo, school_country, school_city, school, school_year,
                    religion, interests, company, position, gid);
    }

    //http://vk.com/dev/video.delete
    public String deleteVideo(Long vid, Long oid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.delete");
        params.put("video_id", vid);
        params.put("owner_id", oid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/video.add
    public String addVideo(Long vid, Long oid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.add");
        params.put("video_id", vid);
        params.put("owner_id", oid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/notes.add
    public long createNote(String title, String text) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notes.add");
        params.put("title", title);
        params.put("text", text);
        params.put("privacy", "0");
        params.put("comment_privacy", "0");
        JSONObject root = sendRequest(params, true);
        long note_id = root.getLong("response");
        return note_id;
    }


    //http://vk.com/dev/account.setOnline
    public void setOnline(String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.setOnline");
        addCaptchaParams(captcha_key, captcha_sid, params);
        sendRequest(params);
    }

    //http://vk.com/dev/friends.add
    public long addFriend(Long uid, String text, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.add");
        params.put("user_id", uid);
        params.put("text", text);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    //http://vk.com/dev/friends.delete
    public long deleteFriend(Long uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.delete");
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    //http://vk.com/dev/friends.getRequests
    public ArrayList<Object[]> getRequestsFriends(Integer out) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.getRequests");
        params.put("need_messages", "1");
        params.put("out", out);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<Object[]> users = new ArrayList<Object[]>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) {
                    Long id = item.optLong("user_id", -1);
                    if (id != -1) {
                        Object[] u = new Object[2];
                        u[0] = id;
                        u[1] = item.optString("message");
                        users.add(u);
                    }
                }
            }
        }
        return users;
    }

    //http://vk.com/dev/users.getSubscriptions
    public ArrayList<Long> getSubscriptions(Long uid, int offset, int count, Integer extended) throws IOException, JSONException, KException {
        VKParams params = new VKParams("users.getSubscriptions");
        params.put("user_id", uid);
        //params.put("extended", extended); //TODO
        if (offset > 0)
            params.put("offset", offset);
        if (count > 0)
            params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        JSONObject jusers = response.optJSONObject("users");
        JSONArray array = jusers.optJSONArray("items");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; ++i) {
                Long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    //http://vk.com/dev/users.getFollowers
    public ArrayList<VKFullUser> getFollowers(Long uid, int offset, int count, String fields, String name_case) throws IOException, JSONException, KException {
        VKParams params = new VKParams("users.getFollowers");
        params.put("user_id", uid);
        if (offset > 0)
            params.put("offset", offset);
        if (count > 0)
            params.put("count", count);

        // if this method is called without fields it will return just ids in wrong format
        if (fields == null)
            fields = "first_name,last_name, photo_100, online";
        params.put("fields", fields);
        params.put("name_case", name_case);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKFullUser.parseUsers(array);
    }

    //http://vk.com/dev/messages.deleteDialog
    // TODO: Обратите внимание, что на метод наложено ограничение, за один вызов нельзя удалить больше 10000 сообщений,
    // TODO: поэтому если сообщений в переписке больше — метод нужно вызывать несколько раз.
    public int deleteMessageDialog(Long uid, Long chatId) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.deleteDialog");
        if (chatId != 0) {
            params.put("chat_id", chatId);
        } else {
            params.put("user_id", uid);
        }
        JSONObject root = sendRequest(params);
        return root.getInt("response");
    }

    //http://vk.com/dev/execute
    public void execute(String code) throws IOException, JSONException, KException {
        VKParams params = new VKParams("execute");
        params.put("code", code);
        sendRequest(params);
    }

    //http://vk.com/dev/photos.delete
    public boolean deletePhoto(Long owner_id, Long photo_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.delete");
        params.put("owner_id", owner_id);
        params.put("photo_id", photo_id);
        JSONObject root = sendRequest(params);
        long response = root.optLong("response", -1);
        return response == 1;
    }

    //http://vk.com/dev/polls.getById
    public VKPoll getPollById(long poll_id, long owner_id, long topic_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("polls.getById");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        if (topic_id != 0)
            params.put("board", topic_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return VKPoll.parse(response);
    }

    //http://vk.com/dev/polls.addVote
    public int addPollVote(long poll_id, long answer_id, long owner_id, long topic_id, String captcha_key, String captcha_sid) throws JSONException, IOException, KException {
        VKParams params = new VKParams("polls.addVote");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        if (topic_id != 0)
            params.put("board", topic_id);
        params.put("answer_id", answer_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return root.getInt("response");
    }

    //http://vk.com/dev/polls.deleteVote
    public int deletePollVote(long poll_id, long answer_id, long owner_id, long topic_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("polls.deleteVote");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        if (topic_id != 0)
            params.put("board", topic_id);
        params.put("answer_id", answer_id);
        JSONObject root = sendRequest(params);
        return root.getInt("response");
    }

    //http://vk.com/dev/polls.getVoters
    public ArrayList<VKFullUser> getPollVoters(long poll_id, long owner_id, Collection<Long> answer_ids, Long count, Long offset, String fields, long topic_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("polls.getVoters");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        if (topic_id != 0)
            params.put("board", topic_id);
        params.put("answer_ids", arrayToString(answer_ids));
        params.put("count", count);
        params.put("offset", offset);
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray response = root.optJSONArray("response");//массив ответов
        JSONObject object = (JSONObject) response.get(0);
        JSONObject array2 = object.optJSONObject("users");
        JSONArray array = array2.optJSONArray("items");
        //TODO for others answer_ids
        return VKFullUser.parseUsers(array);
    }

    //http://vk.com/dev/friends.getLists
    public ArrayList<FriendsList> getFriendLists() throws JSONException, IOException, KException {
        VKParams params = new VKParams("friends.getLists");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");

        ArrayList<FriendsList> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            FriendsList list = FriendsList.parse(array.optJSONObject(i));
            result.add(list);
        }
        return result;
    }

    //http://vk.com/dev/video.save
    public String saveVideo(String name, String description, Long gid, int privacy_view, int privacy_comment) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.save");
        params.put("name", name);
        params.put("description", description);
        params.put("group_id", gid);
        if (privacy_view > 0)
            params.put("privacy_view", privacy_view);
        if (privacy_comment > 0)
            params.put("privacy_comment", privacy_comment);
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/photos.deleteAlbum
    public String deleteAlbum(Long aid, Long gid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.deleteAlbum");
        params.put("album_id", aid);
        params.put("group_id", gid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/photos.getTags
    public ArrayList<PhotoTag> getPhotoTagsById(Long pid, Long owner_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.getTags");
        params.put("owner_id", owner_id);
        params.put("photo_id", pid);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<PhotoTag>();
        ArrayList<PhotoTag> photo_tags = parsePhotoTags(array, pid, owner_id);
        return photo_tags;
    }

    private ArrayList<PhotoTag> parsePhotoTags(JSONArray array, Long pid, Long owner_id) throws JSONException {
        ArrayList<PhotoTag> photo_tags = new ArrayList<PhotoTag>();
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            PhotoTag p = PhotoTag.parse(o);
            photo_tags.add(p);
            if (pid != null)
                p.pid = pid;
            if (owner_id != null)
                p.owner_id = owner_id;
        }
        return photo_tags;
    }

    //http://vk.com/dev/photos.putTag
    public String putPhotoTag(PhotoTag ptag, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        if (ptag == null)
            return null;
        VKParams params = new VKParams("photos.putTag");
        params.put("owner_id", ptag.owner_id);
        params.put("photo_id", ptag.pid);
        params.put("user_id", ptag.uid);
        params.putDouble("x", ptag.x);
        params.putDouble("x2", ptag.x2);
        params.putDouble("y", ptag.y);
        params.putDouble("y2", ptag.y2);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    /**
     * topics region **
     */
    //http://vk.com/dev/board.getTopics
    public ArrayList<GroupTopic> getGroupTopics(long gid, Integer order, int extended, int count, int offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("board.getTopics");
        params.put("group_id", gid);
        params.put("order", order);
        if (extended == 1)
            params.put("extended", "1"); //for profiles
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        JSONObject root = sendRequest(params);
        ArrayList<GroupTopic> result = new ArrayList<GroupTopic>();
        JSONObject response = root.optJSONObject("response");
        if (response != null) {
            JSONArray topics = response.optJSONArray("items");
            if (topics != null) {
                for (int i = 0; i < topics.length(); ++i) {
                    JSONObject o = topics.getJSONObject(i);
                    GroupTopic gt = GroupTopic.parse(o);
                    gt.gid = gid;
                    result.add(gt);
                }
            }
        }
        return result;
    }

    //http://vk.com/dev/board.getComments
    public CommentList getGroupTopicComments(long gid, long tid, int photo_sizes, int extended, int count, int offset, boolean reverse_order) throws IOException, JSONException, KException {
        VKParams params = new VKParams("board.getComments");
        params.put("group_id", gid);
        params.put("topic_id", tid);
        if (photo_sizes == 1)
            params.put("photo_sizes", "1");
        if (extended == 1)
            params.put("extended", "1");
        if (count > 0)
            params.put("count", count);
        if (offset > 0)
            params.put("offset", offset);
        if (reverse_order)
            params.put("sort", "desc");
        params.put("need_likes", "1");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        CommentList result = new CommentList();
        if (response != null) {
            JSONArray comments = response.optJSONArray("items");
            int category_count = comments.length();
            result.count = response.getInt("count");
            for (int i = 0; i < category_count; ++i) {
                JSONObject o = comments.getJSONObject(i);
                VKComment c = VKComment.parseTopicComment(o);
                result.comments.add(c);
            }

            //topic poll parsed separately
            if (offset == 0) {
                JSONObject poll_json = response.optJSONObject("poll");
                if (poll_json != null) {
                    VKPoll poll = VKPoll.parse(poll_json);
                    poll.topic_id = tid;
                    VKAttachment attachment = new VKAttachment();
                    attachment.poll = poll;
                    attachment.type = "poll";
                    if (result.comments.size() > 0)
                        result.comments.get(0).attachments.add(attachment);
                }
            }
        }
        return result;
    }

    //http://vk.com/dev/board.addComment
    public long createGroupTopicComment(long gid, long tid, String text, Collection<String> attachments, boolean from_group, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("board.addComment");
        params.put("group_id", gid);
        params.put("topic_id", tid);
        params.put("text", text);
        params.put("attachments", arrayToString(attachments));
        if (from_group)
            params.put("from_group", "1");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        long message_id = root.optLong("response");
        return message_id;
    }

    //http://vk.com/dev/board.editComment
    public boolean editGroupTopicComment(long cid, long gid, long tid, String text, Collection<String> attachments, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("board.editComment");
        params.put("comment_id", cid);
        params.put("group_id", gid);
        params.put("topic_id", tid);
        params.put("text", text);
        params.put("attachments", arrayToString(attachments));
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/board.deleteComment
    public Boolean deleteGroupTopicComment(long gid, long tid, long cid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("board.deleteComment");
        params.put("group_id", gid);
        params.put("topic_id", tid);
        params.put("comment_id", cid);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/board.addTopic
    public long createGroupTopic(long gid, String title, String text, boolean from_group, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("board.addTopic");
        params.put("group_id", gid);
        params.put("title", title);
        params.put("text", text);
        if (from_group)
            params.put("from_group", "1");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        long topic_id = root.optLong("response");
        return topic_id;
    }

    //http://vk.com/dev/board.deleteTopic
    public Boolean deleteGroupTopic(long gid, long tid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("board.deleteTopic");
        params.put("group_id", gid);
        params.put("topic_id", tid);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    /**
     * end topics region **
     */

    //http://vk.com/dev/groups.getById
    public ArrayList<VKGroup> getGroupsById(Collection<Long> uids, String domain, String fields) throws IOException, JSONException, KException {
        if (uids == null && domain == null)
            return null;
        if (uids.isEmpty() && domain == null)
            return null;
        VKParams params = new VKParams("groups.getById");
        String str_uids;
        if (uids != null && uids.size() > 0)
            str_uids = arrayToString(uids);
        else
            str_uids = domain;
        params.put("group_ids", str_uids);
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKGroup.parseGroups(array);
    }

    //http://vk.com/dev/groups.join
    public String joinGroup(long gid, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.join");
        params.put("group_id", gid);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/groups.leave
    public String leaveGroup(long gid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.leave");
        params.put("group_id", gid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    // http://vk.com/dev/groups.isMember
    public Boolean isGroupMember(long group_id, long user_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("groups.isMember");
        params.put("group_id", group_id);
        params.put("user_id", user_id);
        JSONObject root = sendRequest(params);
        Long response = root.optLong("response");

        return response == 1;
    }

    //http://vk.com/dev/groups.search
    public ArrayList<VKGroup> searchGroup(String q, Long count, Long offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.search");
        params.put("q", q);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKGroup> groups = new ArrayList<VKGroup>();
        //if there are no groups "response" will not be array
        if (array == null)
            return groups;
        groups = VKGroup.parseGroups(array);
        return groups;
    }

    //http://vk.com/dev/account.registerDevice
    public String registerDevice(String token, String device_model, String system_version, Integer no_text, String subscribe)
            throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.registerDevice");
        params.put("token", token);
        params.put("device_model", device_model);
        params.put("system_version", system_version);
        params.put("no_text", no_text);
        params.put("subscribe", subscribe);
        //params.put("gcm", 1);
        JSONObject root = sendRequest(params);
        return root.getString("response");
    }

    //http://vk.com/dev/account.unregisterDevice
    public String unregisterDevice(String token) throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.unregisterDevice");
        params.put("token", token);
        JSONObject root = sendRequest(params);
        return root.getString("response");
    }

    //http://vk.com/dev/notifications.get
    public VKNotifications getNotifications(String filters, Long start_time, Long end_time, Integer offset, Integer count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("notifications.get");
        params.put("filters", filters);
        params.put("start_time", start_time);
        params.put("end_time", end_time);
        params.put("offset", offset);
        params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return VKNotifications.parse(response);
    }

    //http://vk.com/dev/messages.getById
    public ArrayList<VKMessage> getMessagesById(ArrayList<Integer> message_ids) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.getById");
        params.put("message_ids", arrayToString(message_ids));
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return parseMessages(array);
    }

    //http://vk.com/dev/account.getCounters
    public Counters getCounters(String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.getCounters");
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return Counters.parse(response);
    }

    /**
     * faves **
     */
    //http://vk.com/dev/fave.getUsers
    public ArrayList<VKFullUser> getFaveUsers(String fields, Integer count, Integer offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("fave.getUsers");
        params.put("fields", fields);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        ArrayList<VKFullUser> users = new ArrayList<VKFullUser>();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        if (array == null)
            return users;
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKFullUser u = VKFullUser.parseFromFave(o);
            users.add(u);
        }
        return users;
    }

    //http://vk.com/dev/fave.getPhotos
    public ArrayList<VKPhoto> getFavePhotos(Integer count, Integer offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("fave.getPhotos");
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        if (array == null)
            return new ArrayList<VKPhoto>();
        ArrayList<VKPhoto> photos = parsePhotos(array);
        return photos;
    }

    //http://vk.com/dev/fave.getVideos
    public ArrayList<VKVideo> getFaveVideos(Integer count, Integer offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("fave.getVideos");
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<VKVideo> videos = new ArrayList<VKVideo>();
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                JSONObject o = (JSONObject) array.get(i);
                VKVideo video = VKVideo.parse(o);
                videos.add(video);
            }
        }
        return videos;
    }

    //http://vk.com/dev/fave.getPosts
    public ArrayList<VKWallMessage> getFavePosts(Integer count, Integer offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("fave.getPosts");
        //params.put("extended", extended);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        //JSONArray array = response.optJSONArray("wall");
        //JSONArray profiles_array = response.optJSONArray("profiles");
        //JSONArray groups_array = response.optJSONArray("groups");
        ArrayList<VKWallMessage> wmessages = new ArrayList<VKWallMessage>();
        if (array == null)
            return wmessages;
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKWallMessage wm = VKWallMessage.parse(o);
            wmessages.add(wm);
        }
        return wmessages;
    }

    //http://vk.com/dev/fave.getLinks
    public ArrayList<VKLink> getFaveLinks(Integer count, Integer offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("fave.getLinks");
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        ArrayList<VKLink> groups = new ArrayList<VKLink>();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        if (array == null)
            return groups;
        for (int i = 0; i < array.length(); i++) {
            JSONObject jlink = (JSONObject) array.get(i);
            VKLink link = VKLink.parse(jlink);
            groups.add(link);
        }

        return groups;
    }
    /*** end faves  ***/

    /**
     * chat methods **
     */
    //http://vk.com/dev/messages.createChat
    public Long сreateChat(ArrayList<Integer> uids, String title) throws IOException, JSONException, KException {
        if (uids == null || uids.size() == 0)
            return null;
        VKParams params = new VKParams("messages.createChat");
        String str_uids = String.valueOf(uids.get(0));
        for (int i = 1; i < uids.size(); i++)
            str_uids += "," + String.valueOf(uids.get(i));
        params.put("user_ids", str_uids);
        params.put("title", title);
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    //http://vk.com/dev/messages.editChat
    public Integer editChat(long chat_id, String title) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.editChat");
        params.put("chat_id", chat_id);
        params.put("title", title);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/messages.getChatUsers
    public ArrayList<VKFullUser> getChatUsers(long chat_id, String fields) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.getChatUsers");
        params.put("chat_id", chat_id);
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKFullUser.parseUsers(array);
    }

    // https://vk.com/dev/messages.getChat
    public VKChat getChat(long chat_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("messages.getChat");
        params.put("chat_id", chat_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return VKChat.parse(response);
    }

    //http://vk.com/dev/messages.addChatUser
    public Integer addUserToChat(long chat_id, long uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.addChatUser");
        params.put("chat_id", chat_id);
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/messages.removeChatUser
    public Integer removeUserFromChat(long chat_id, long uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.removeChatUser");
        params.put("chat_id", chat_id);
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    /**
     * end chat methods **
     */

    //http://vk.com/dev/friends.getSuggestions
    public ArrayList<VKFullUser> getSuggestions(String filter, String fields) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.getSuggestions");
        params.put("filter", filter);   //mutual, contacts, mutual_contacts
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKFullUser.parseUsers(array);
    }

    //http://vk.com/dev/account.importContacts
    @Deprecated
    public Integer importContacts(Collection<String> contacts) throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.importContacts");
        params.put("contacts", arrayToString(contacts));
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/friends.getByPhones
    public ArrayList<VKFullUser> getFriendsByPhones(ArrayList<String> phones, String fields) throws IOException, JSONException, KException {
        VKParams params = new VKParams("friends.getByPhones");
        params.put("phones", arrayToString(phones));
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKFullUser.parseUsersForGetByPhones(array);
    }

    /**
     * methods for messages search **
     */
    //http://vk.com/dev/messages.search
    public ArrayList<VKMessage> searchMessages(String q, int offset, int count, Integer preview_length) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.search");
        params.put("q", q);
        params.put("count", count);
        params.put("offset", offset);
        params.put("preview_length", preview_length);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKMessage.parseArray(array);
    }

    //http://vk.com/dev/messages.searchDialogs
    public ArrayList<SearchDialogItem> searchDialogs(String q, String fields, Integer limit) throws IOException, JSONException, KException {
        VKParams params = new VKParams("messages.searchDialogs");
        params.put("q", q);
        params.put("fields", fields);
        params.put("limit", limit);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKMessage.parseSearchedDialogs(array);
    }

    //http://vk.com/dev/messages.getLastActivity
    public VKFullUser.LastActivity getLastActivity(long user_id) throws JSONException, IOException, KException {
        VKParams params = new VKParams("messages.getLastActivity");
        params.put("user_id", user_id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return VKFullUser.LastActivity.parse(response);
    }

    //http://vk.com/dev/groups.getMembers
    public ArrayList<Long> getGroupsMembers(long gid, Integer count, Integer offset, String sort) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.getMembers");
        params.put("group_id", gid);
        params.put("count", count);
        params.put("offset", offset);
        params.put("sort", sort); //id_asc, id_desc, time_asc, time_desc
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<Long> users = new ArrayList<Long>();
        if (array != null) {
            int category_count = array.length();
            for (int i = 0; i < category_count; i++) {
                Long id = array.optLong(i, -1);
                if (id != -1)
                    users.add(id);
            }
        }
        return users;
    }

    public ArrayList<VKFullUser> getGroupsMembersWithExecute(long gid, Integer count, Integer offset, String sort, String fields) throws IOException, JSONException, KException {
        //String code = "return API.users.get({\"user_ids\":API.groups.getMembers({\"gid\":" + String.valueOf(gid) + ",\"count\":" + String.valueOf(count) + ",\"offset\":" + String.valueOf(offset) + ",\"sort\":\"id_asc\"}),\"fields\":\"" + fields + "\"});";
        String code = "var members=API.groups.getMembers({\"gid\":" + gid + "}); var u=members[1]; return API.users.get({\"user_ids\":u,\"fields\":\"" + fields + "\"});";
        VKParams params = new VKParams("execute");
        params.put("code", code);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return VKFullUser.parseUsers(array);
    }

    //http://vk.com/dev/utils.getServerTime
    public long getServerTime() throws IOException, JSONException, KException {
        VKParams params = new VKParams("utils.getServerTime");
        JSONObject root = sendRequest(params);
        return root.getLong("response");
    }

    //http://vk.com/dev/audio.getAlbums
    public ArrayList<AudioAlbum> getAudioAlbums(Long owner_id, Integer offset, Integer count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.getAlbums");
        params.put("owner_id", owner_id);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<AudioAlbum> albums = AudioAlbum.parseAlbums(array);
        return albums;
    }

    //http://vk.com/dev/audio.getRecommendations
    public ArrayList<VKAudio> getAudioRecommendations() throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.getRecommendations");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return parseAudioList(array);
    }

    //http://vk.com/dev/audio.getPopular
    public ArrayList<VKAudio> getAudioPopular() throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.getPopular");
        //params.put("only_eng", only_eng);
        //params.put("genre_id", genre_id);
        //params.put("count", count);
        //params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array);
    }

    //http://vk.com/dev/video.getAlbums
    public ArrayList<AudioAlbum> getVideoAlbums(long owner_id, Integer offset, Integer count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("video.getAlbums");
        params.put("owner_id", owner_id);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        ArrayList<AudioAlbum> albums = AudioAlbum.parseAlbums(array);
        return albums;
    }


    //http://vk.com/dev/wall.edit
    public int editWallPost(long owner_id, long post_id, String text, Collection<String> attachments, String lat, String lon, long place_id, Long publish_date, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.edit");
        params.put("owner_id", owner_id);
        params.put("post_id", post_id);
        params.put("message", text);
        params.put("attachments", arrayToString(attachments));
        params.put("lat", lat);
        params.put("long", lon);
        params.put("place_id", place_id);
        params.put("publish_date", publish_date);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        return root.optInt("response");
    }

    //http://vk.com/dev/photos.edit
    public Integer photoEdit(Long owner_id, long pid, String caption) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.edit");
        params.put("owner_id", owner_id);
        params.put("photo_id", pid);
        params.put("caption", caption);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/docs.get
    public ArrayList<VKDocument> getDocs(Integer owner_id, Integer count, Long offset) throws IOException, JSONException, KException {
        VKParams params = new VKParams("docs.get");
        params.put("owner_id", owner_id);
        params.put("count", count);
        params.put("offset", offset);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKDocument.parseDocs(array);
    }

    //http://vk.com/dev/docs.getUploadServer
    public String getDocsUploadServer() throws IOException, JSONException, KException {
        VKParams params = new VKParams("docs.getUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/docs.save
    public VKDocument saveDoc(String file) throws IOException, JSONException, KException {
        VKParams params = new VKParams("docs.save");
        params.put("file", file);
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        ArrayList<VKDocument> docs = VKDocument.parseDocs(array);
        return docs.get(0);
    }

    //http://vk.com/dev/docs.delete
    public Boolean deleteDoc(Long doc_id, long owner_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("docs.delete");
        params.put("owner_id", owner_id);
        params.put("doc_id", doc_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/notifications.markAsViewed
    public Boolean markNotificationsAsViewed() throws IOException, JSONException, KException {
        VKParams params = new VKParams("notifications.markAsViewed");
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/newsfeed.getBanned
    public BannArg getBanned(boolean is_extended, String fields) throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.getBanned");
        if (is_extended)
            params.put("extended", "1");
        params.put("fields", fields);
        JSONObject root = sendRequest(params);
        JSONObject object = root.optJSONObject("response");
        return BannArg.parse(object, is_extended);
    }

    //http://vk.com/dev/newsfeed.addBan
    public Boolean addBan(Collection<Long> uids, Collection<Long> gids) throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.addBan");
        if (uids != null && uids.size() > 0)
            params.put("uids", arrayToString(uids));
        if (gids != null && gids.size() > 0)
            params.put("gids", arrayToString(gids));
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/newsfeed.deleteBan
    public Boolean deleteBan(Collection<Long> uids, Collection<Long> gids) throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.deleteBan");
        if (uids != null && uids.size() > 0)
            params.put("uids", arrayToString(uids));
        if (gids != null && gids.size() > 0)
            params.put("gids", arrayToString(gids));
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/audio.getBroadcast
    //gets status of broadcasting user current audio to his page
    @Deprecated
    public boolean audioGetBroadcast() throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.getBroadcast");
        // метод устаревший, можно передавать старую версию 4.98 если перестанет работать. Замены ему пока нет http://vk.com/bugs?act=show&id=4717174_23
        // TODO: Теперь вместо него следует использовать status.get
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optInt("enabled") == 1;
    }

    //http://vk.com/dev/audio.setBroadcast
    public boolean audioSetBroadcast(String audio, String target_ids) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.setBroadcast");
        params.put("audio", audio);
        params.put("target_ids", target_ids);
        sendRequest(params);
        //В случае успешного выполнения возвращает массив идентификаторов сообществ и пользователя, которым был установлен или удален аудиостатус.
        //response: [1661530]
        //нет необходимости парсить пока
        return true;
    }

    //http://vk.com/dev/audio.addAlbum
    public Long addAudioAlbum(String title, Long gid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.addAlbum");
        params.put("title", title);
        params.put("group_id", gid);
        JSONObject root = sendRequest(params);
        JSONObject obj = root.getJSONObject("response");
        return obj.optLong("album_id");
    }

    //http://vk.com/dev/audio.editAlbum
    public Integer editAudioAlbum(String title, long album_id, Long gid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.editAlbum");
        params.put("title", title);
        params.put("album_id", album_id);
        params.put("group_id", gid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/audio.deleteAlbum
    public Integer deleteAudioAlbum(long album_id, Long gid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.deleteAlbum");
        params.put("album_id", album_id);
        params.put("group_id", gid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/audio.moveToAlbum
    public Integer moveToAudioAlbum(Collection<Long> aids, long album_id, Long gid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.moveToAlbum");
        params.put("audio_ids", arrayToString(aids));
        params.put("album_ids", arrayToString(aids));//album_ids instead audio_ids - Баг в API
        params.put("album_id", album_id);
        params.put("group_id", gid);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/wall.getById
    public ArrayList<VKWallMessage> getWallMessageById(ArrayList<String> posts) throws IOException, JSONException, KException {
        VKParams params = new VKParams("wall.getById");
        params.put("posts", arrayToString(posts));
        JSONObject root = sendRequest(params);
        JSONArray array = root.getJSONArray("response");
        ArrayList<VKWallMessage> wmessages = new ArrayList<VKWallMessage>();
        int category_count = array.length();
        for (int i = 0; i < category_count; ++i) {
            JSONObject o = (JSONObject) array.get(i);
            VKWallMessage wm = VKWallMessage.parse(o);
            wmessages.add(wm);
        }
        return wmessages;
    }

    //http://vk.com/dev/newsfeed.unsubscribe
    public Integer unsubscribeMewsfeed(String type, Long owner_id, Long item_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.unsubscribe");
        params.put("type", type);
        params.put("owner_id", owner_id);
        params.put("item_id", item_id);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }

    //http://vk.com/dev/account.getBanned
    public ArrayList<VKFullUser> getBlackList(Long offset, Long count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.getBanned");
        String fields = "first_name,last_name,photo_100,online";
        params.put("fields", fields);
        params.put("offset", offset);
        params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return VKFullUser.parseUsers(array);
    }

    //http://vk.com/dev/account.banUser
    public Boolean addToBlackList(long uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.banUser");
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/account.unbanUser
    public Boolean deleteFromBlackList(long uid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("account.unbanUser");
        params.put("user_id", uid);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/docs.add
    public Long addDoc(long owner_id, long document_id, String access_key) throws IOException, JSONException, KException {
        VKParams params = new VKParams("docs.add");
        params.put("doc_id", document_id);
        params.put("owner_id", owner_id);
        params.put("access_key", access_key);
        JSONObject root = sendRequest(params);
        //returns new document_id
        return root.optLong("response");
    }

    //http://vk.com/dev/newsfeed.search
    public Newsfeed searchNews(String q, String start_id, int extended, Long start_time, Long end_time, long count, Long offset, double latitude, double longitude, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("newsfeed.search");
        params.put("q", q);
        params.put("start_id", start_id);
        params.put("extended", extended);
        params.put("start_time", start_time);
        params.put("end_time", end_time);
        if (count != 0)
            params.put("count", count);
        params.put("offset", offset);
        if (latitude != 0)
            params.putDouble("latitude", latitude);
        if (longitude != 0)
            params.putDouble("longitude", longitude);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        return Newsfeed.parseFromSearch(root);
    }

    //http://vk.com/dev/groups.getBanned
    public ArrayList<GroupBanItem> getGroupBannedUsers(long group_id, Long offset, Long count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.getBanned");
        params.put("group_id", group_id);
        params.put("offset", offset);
        params.put("count", count);
        //not documented
        params.put("fields", "photo_100");
        //
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        return GroupBanItem.parseAll(array);
    }

    //http://vk.com/dev/groups.banUser
    public Boolean addGroupBanUser(long group_id, long user_id, Long end_date, Integer reason, String comment, boolean comment_visible) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.banUser");
        params.put("group_id", group_id);
        params.put("user_id", user_id);
        params.put("end_date", end_date);
        params.put("reason", reason);
        params.put("comment", comment);
        if (comment_visible)
            params.put("comment_visible", "1");
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/groups.unbanUser
    public Boolean deleteGroupBanUser(long group_id, long user_id) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.unbanUser");
        params.put("group_id", group_id);
        params.put("user_id", user_id);
        JSONObject root = sendRequest(params);
        int response = root.optInt("response");
        return response == 1;
    }

    //http://vk.com/dev/photos.copy
    public Long copyPhoto(long owner_id, long photo_id, String access_key) throws IOException, JSONException, KException {
        VKParams params = new VKParams("photos.copy");
        params.put("owner_id", owner_id);
        params.put("photo_id", photo_id);
        params.put("access_key", access_key);
        JSONObject root = sendRequest(params);
        Long response = root.optLong("response");
        return response;
    }

    //http://vk.com/dev/account.setOffline
    public Long setOffline() {
        try {
            VKParams params = new VKParams("account.setOffline");
            JSONObject root = sendRequest(params);
            return root.optLong("response");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    //http://vk.com/dev/groups.getInvites
    public ArrayList<VKGroup> getGroupsInvites(Long offset, Long count) throws IOException, JSONException, KException {
        VKParams params = new VKParams("groups.getInvites");
        params.put("offset", offset);
        params.put("count", count);
        JSONObject root = sendRequest(params);
        ArrayList<VKGroup> groups = new ArrayList<VKGroup>();
        JSONObject response = root.optJSONObject("response");
        JSONArray array = response.optJSONArray("items");
        //if there are no groups "response" will not be array
        if (array == null)
            return groups;
        groups = VKGroup.parseGroups(array);
        return groups;
    }

    //http://vk.com/dev/audio.edit 
    public Long editAudio(long owner_id, long audio_id, String artist, String title, String text, Integer genre_id, Integer no_search, String captcha_key, String captcha_sid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("audio.edit");
        params.put("owner_id", owner_id);
        params.put("audio_id", audio_id);
        params.put("artist", artist);
        params.put("title", title);
        params.put("text", text);
        params.put("genre_id", genre_id);
        params.put("no_search", no_search);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params, true);
        Long lyrics_id = root.optLong("response");
        return lyrics_id;
    }

    // http://vk.com/dev/stats.trackVisitor
    public Long trackStatsVisitor() throws JSONException, IOException, KException {
        VKParams params = new VKParams("stats.trackVisitor");
        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    // https://vk.com/dev/storage.set
    public Long setStorage(String key, String value) throws JSONException, IOException, KException {
        VKParams params = new VKParams("storage.set");
        params.put("key", key);
        params.put("value", value);
        params.put("global", "1");

        JSONObject root = sendRequest(params);
        return root.optLong("response");
    }

    // http://vk.com/dev/storage.get
    public String getStorage(String key) throws JSONException, IOException, KException {
        VKParams params = new VKParams("storage.get");
        params.put("key", key);
        params.put("global", "1");

        JSONObject root = sendRequest(params); // отправляем запрос, и парсим полученный обьект
        return root.optString("response");
    }

    // http://vk.com/dev/gifts.get
    public ArrayList<VKGift> getGifts(long user_id, int count, int offset) throws JSONException, IOException, KException {
        VKParams params = new VKParams("gifts.get");
        params.put("user_id", user_id);
        params.put("count", count);
        params.put("offset", offset);

        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return VKGift.parseGifts(response.optJSONArray("items"));
    }

    // FIXME: 20.11.15
    /**
     *
     * Отправка подарка
     * TODO: Данный метод был удален из документации, однако он работает
     * @param user_ids IDs пользователей, которым отправляются подарки
     * @param message текст сообщения
     * @param isPrivate - флаг, отвечающий за тип подарка: публичный\приватный
     * @param gift_id подарка, можно его достать из m.vk.com версии.  ID подарка будет написан в адресной строке
     * @param guid уникальный индентификатор, чтобы предотвратить повторную отправку. - любое число
     *
     * В случае недостатка голосов - вернет ошибку "Not enough money on owner's balance"
     */
    // https://vk.com/dev/gifts.send
    public void sendGift(ArrayList<Long> user_ids, String message, boolean isPrivate, int gift_id, int guid) throws IOException, JSONException, KException {
        VKParams params = new VKParams("gifts.send");
        params.put("user_ids", arrayToString(user_ids));
        params.put("gift_id", gift_id);
        params.put("private", isPrivate);
        params.put("message", message);
        params.put("guid", guid);
        sendRequest(params);
    }

    // http://vk.com/dev/utils.resolveScreenName
    public VKResolveScreenName utilsResolveScreenName(String screen_name) throws IOException, JSONException, KException {
        VKParams params = new VKParams("utils.resolveScreenName");
        params.put("screen_name", screen_name);

        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");

        return VKResolveScreenName.parse(response);
    }

    /**
     * Прямая авторизация с помощью Офф клиента вк для Андроид
     *
     * @param login
     * @param password
     * @return JSONObject
     */
    // http://vk.com/dev/auth_direct
    public JSONObject directauthorizationWithOff(String login, String password) throws IOException, JSONException {
        String client_secret = "hHbZxrka2uZ6jB1inYsH";
        String client_id = "2274003";

        String url = "https://oauth.vk.com/token?grant_type=password" +
                "&client_id=" + client_id +
                "&client_secret=" + client_secret +
                "&username=" + login +
                "&password=" + password +
                "&scope=" + Auth.getSettings() +
                "&v=" + API_VERSION;

        return new JSONObject(Api.sendRequestInternal(url, "", true));
    }




}
