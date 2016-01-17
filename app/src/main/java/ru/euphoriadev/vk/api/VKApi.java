package ru.euphoriadev.vk.api;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeMap;

import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.http.DefaultHttpClient;
import ru.euphoriadev.vk.http.HttpBaseRequest;
import ru.euphoriadev.vk.http.HttpClient;
import ru.euphoriadev.vk.http.HttpGetRequest;
import ru.euphoriadev.vk.http.HttpPostRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.PrefManager;

/**
 * Created by Igor on 15.01.16.
 * <p/>
 * Simple VK Library for execute request
 * <p/>
 *
 * Example to init api and execute users.get request:
 * <pre>
 *      VKApi.init(VKApi.VKAccount.from(account));
 *      String response = VKApi.users()
 *               .get()
 *               .userId(1)
 *               .fields("photo_50, nickname")
 *               .execute();
 *
 *      // return users in json object
 * </pre>
 */

public class VKApi {
    public static final String TAG = "Euphoria.VKApi";
    public static final String BASE_URL = "https://api.vk.com/method/";
    public static final String API_VERSION = "5.14";

    protected static volatile VKApi sInstante;

    private VKAccount mAccount;
    private HttpClient mClient;

    /**
     * Init VKApi to send requests
     *
     * @param account the vk account,
     *                on behalf of which to send requests to server VK
     */
    public static VKApi init(VKAccount account) {
        if (sInstante == null) {
            sInstante = new VKApi(account);
        }
        return sInstante;
    }

    /**
     * Get initialized VKApi
     *
     * @return vk api
     */
    public static VKApi getInstance() {
        if (sInstante == null) {
            throw new IllegalArgumentException("You must to initialize VKApi");
        }
        return sInstante;
    }

    public VKAccount getAccount() {
        return mAccount;
    }

    /**
     * Methods for users
     */
    public static VKUsers users() {
        return new VKUsers();
    }

    /**
     * Methods for messages
     */
    public static VKMessages messages() {
        return new VKMessages();
    }

    /**
     * Create custom method setter with method name
     *
     * @param methodName the name of method e.g. docs.get, audio.getById
     */
    public static VKMethodSetter createCustom(String methodName) {
        return new VKMethodSetter(new VKRequest(methodName, new VKParams()));
    }

    /**
     * Private constructor, Нou don't have use it
     *
     * @see #init(VKAccount)
     */
    private VKApi(VKAccount account) {
        this.mAccount = account;
        this.mClient = new DefaultHttpClient();
    }

    /**
     * Account it store the necessary data to run the query on behalf of user
     * such as the token, email, api id and user id
     */
    public static class VKAccount {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String USER_ID = "user_id";
        public static final String EMAIL = "email";
        public static final String API_ID = "api_id";

        /**
         * String token for use in request parameters
         */
        public String accessToken;

        /**
         * String current email of user
         */
        public String email;

        /**
         * Current user id for this token
         */
        public long userId;

        /**
         * Api id of standalone vk app
         */
        public int apiId;

        /**
         * Create new vk account
         *
         * @param accessToken the access token of current user
         * @param email       the email of current user (not necessarily)
         * @param userId      the user id of current user
         */
        public VKAccount(String accessToken, @Nullable String email, int userId, int apiId) {
            this.accessToken = accessToken;
            this.email = email;
            this.userId = userId;
            this.apiId = apiId;
        }

        /**
         * Empty Constructor
         */
        public VKAccount() {

        }

        /**
         * Create new VKAccount from {@link Account}, this is to support
         *
         * @param account the old account to get properties (access_token, userId)
         * @return new VKAccount with properties {@link Account}
         */
        public static VKAccount from(Account account) {
            return new VKAccount(account.access_token, null, (int) account.user_id, Integer.parseInt(Account.API_ID));
        }

        /**
         * Save account properties into shared preferences
         *
         * @return true if save is successful
         */
        public boolean save() {
            try {
                PrefManager.putString(ACCESS_TOKEN, accessToken);
                PrefManager.putString(EMAIL, email);
                PrefManager.putLong(USER_ID, userId);
                PrefManager.putLong(API_ID, userId);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * Restores account properties from SD
         *
         * @return this account
         */
        public VKAccount restore() {
            this.accessToken = PrefManager.getString(ACCESS_TOKEN);
            this.email = PrefManager.getString(EMAIL);
            this.userId = PrefManager.getLong(USER_ID);
            this.apiId = PrefManager.getInt(API_ID);
            return this;
        }

        public void clear() {
            PrefManager.remove(ACCESS_TOKEN);
            PrefManager.remove(EMAIL);
            PrefManager.remove(USER_ID);
        }
    }

    /**
     * Api methods for users
     * <p/>
     * http://vk.com/dev/users
     */
    public static class VKUsers {

        /**
         * http://vk.com/dev/users.get
         */
        public VKMethodSetter get() {
            return new VKMethodSetter(new VKRequest("users.get", new VKParams()));
        }

        /**
         * http://vk.com/dev/users.search
         */
        public VKMethodSetter search() {
            return new VKMethodSetter(new VKRequest("users.search", new VKParams()));
        }

        /**
         * http://vk.com/dev/users.isAppUser
         */
        public VKMethodSetter isAppUser() {
            return new VKMethodSetter(new VKRequest(("users.isAppUser"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.getSubscriptions
         */
        public VKMethodSetter getSubscriptions() {
            return new VKMethodSetter(new VKRequest(("users.getSubscriptions"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.getFollowers
         */
        public VKMethodSetter getFollowers() {
            return new VKMethodSetter(new VKRequest(("users.getFollowers"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.report
         */
        public VKMethodSetter report() {
            return new VKMethodSetter(new VKRequest(("users.report"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.getNearby
         */
        public VKMethodSetter getNearby() {
            return new VKMethodSetter(new VKRequest(("users.getNearby"), new VKParams()));
        }

    }

    /**
     * Api methods for messages
     * <p/>
     * http://vk.com/dev/messages
     */
    public static class VKMessages {

        /**
         * http://vk.com/dev/messages.getDialogs
         */
        public VKMethodSetter getDialogs() {
            return new VKMethodSetter(new VKRequest(("messages.getDialogs"), new VKParams()));
        }
    }

    /**
     * Method setter for {@link VKRequest}
     */
    public static class VKMethodSetter {
        private VKRequest request;

        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKMethodSetter(VKRequest request) {
            this.request = request;
        }

        /** Setters for users.get */

        /**
         * User IDs or screen names (screen_name). By default, current user ID.
         */
        public VKMethodSetter userIds(Collection<Integer> uids) {
            this.request.params.put(VKConst.USER_IDS, VKUtil.arrayToString(uids));
            return this;
        }

        public VKMethodSetter userId(int userId) {
            Arrays.asList(new int[] { userId} );
            return this;
        }

        /**
         * Profile fields
         */
        public VKMethodSetter fields(String fields) {
            this.request.params.put(VKConst.FIELDS, fields);
            return this;
        }

        /**
         * Case for declension of user name and surname:
         * nom — nominative (default)
         * gen — genitive
         * dat — dative
         * acc — accusative
         * ins — instrumental
         * abl — prepositional
         */
        public VKMethodSetter nameCase(String nameCase) {
            this.request.params.put(VKConst.NAME_CASE, nameCase);
            return this;
        }


        /** Setters for users.search. NOT FULL */

        /**
         * Search query string (e.g., Vasya Babich).
         */
        public VKMethodSetter q(String q) {
            this.request.params.put(VKConst.Q, q);
            return this;
        }

        /**
         * Sort order:
         * 1 — by date registered
         * 0 — by rating
         */
        public VKMethodSetter sort(int sortOrder) {
            this.request.params.put(VKConst.SORT, sortOrder);
            return this;
        }

        /**
         * Offset needed to return a specific subset of users
         */
        public VKMethodSetter offset(int offset) {
            this.request.params.put(VKConst.OFFSET, offset);
            return this;
        }

        /**
         * Number of users to return. Max value 1 000
         */
        public VKMethodSetter count(int count) {
            this.request.params.put(VKConst.COUNT, count);
            return this;
        }

        /**
         * City ID
         */
        public VKMethodSetter city(int cityId) {
            this.request.params.put(VKConst.CITY, cityId);
            return this;
        }

        /**
         * Country ID
         */
        public VKMethodSetter country(int countryId) {
            this.request.params.put(VKConst.COUNTRY, countryId);
            return this;
        }

        /**
         * City name in a string
         */
        public VKMethodSetter hometown(int hometown) {
            this.request.params.put(VKConst.HOMETOWN, hometown);
            return this;
        }

        /**
         * ID of the country where the user graduated
         */
        public VKMethodSetter universityCountry(int countryId) {
            this.request.params.put(VKConst.UNIVERSITY_COUNTRY, countryId);
            return this;
        }

        /**
         * ID of the institution of higher education
         */
        public VKMethodSetter university(int universityId) {
            this.request.params.put(VKConst.UNIVERSITY, universityId);
            return this;
        }

        /**
         * Year of graduation from an institution of higher education
         */
        public VKMethodSetter universityYear(int year) {
            this.request.params.put(VKConst.UNIVERSITY_YEAR, year);
            return this;
        }

        /**
         * Sex of user
         * 1 — female
         * 2 — male
         * 0 — any (default
         */
        public VKMethodSetter sex(int sex) {
            this.request.params.put(VKConst.SEX, sex);
            return this;
        }

        /**
         * Relationship status:
         * 1 — Not married
         * 2 — In a relationship
         * 3 — Engaged
         * 4 — Married
         * 5 — It's complicated
         * 6 — Actively searching
         * 7 — In love
         */
        public VKMethodSetter status(int status) {
            this.request.params.put(VKConst.STATUS, status);
            return this;
        }

        /**
         * Minimum age
         */
        public VKMethodSetter ageFrom(int minAge) {
            this.request.params.put(VKConst.AGE_FROM, minAge);
            return this;
        }

        /**
         * Maximum age
         */
        public VKMethodSetter ageTo(int maxAge) {
            this.request.params.put(VKConst.AGE_TO, maxAge);
            return this;
        }

        /**
         * Day of birth
         */
        public VKMethodSetter birthDay(int day) {
            this.request.params.put(VKConst.BIRTH_DAY, day);
            return this;
        }

        /**
         * Month of birth
         */
        public VKMethodSetter birthMonth(int month) {
            this.request.params.put(VKConst.BIRTH_MONTH, month);
            return this;
        }

        /**
         * Year of birth
         */
        public VKMethodSetter birthYear(int year) {
            this.request.params.put(VKConst.BIRTH_YEAR, year);
            return this;
        }

        /**
         * Online status
         * <p/>
         * true — online only
         * false — all users
         */
        public VKMethodSetter online(boolean online) {
            this.request.params.put(VKConst.ONLINE, online);
            return this;
        }

        /**
         * Has photo
         * <p/>
         * 1 — with photo only
         * 0 — all users
         */
        public VKMethodSetter hasPhoto(boolean hasPhoto) {
            this.request.params.put(VKConst.HAS_PHOTO, hasPhoto);
            return this;
        }


        /** Setters for users.getSubscriptions */

        /**
         * false — to return separate lists of users and communities (default)
         * true — to return a combined list of users and communities
         */
        public VKMethodSetter extended(boolean extended) {
            this.request.params.put(VKConst.EXTENDED, extended);
            return this;
        }

        /** Setters for users.report */

        /**
         * Type of complaint:
         * porn – pornography
         * spam – spamming
         * insult – abusive behavior
         * advertisment – disruptive advertisements
         */
        public VKMethodSetter type(String type) {
            this.request.params.put(VKConst.TYPE, type);
            return this;
        }

        /**
         * Comment describing the complaint
         */
        public VKMethodSetter comment(String comment) {
            this.request.params.put(VKConst.COMMENT, comment);
            return this;
        }


        /** Setters for users.getNearby */

        /**
         * Geographic latitude of the place a user is located,
         * in degrees (from -90 to 90)
         */
        public VKMethodSetter latitude(float latitude) {
            this.request.params.put(VKConst.LATITUDE, latitude);
            return this;
        }

        /**
         * Geographic longitude of the place a user is located,
         * in degrees (from -90 to 90)
         */
        public VKMethodSetter longitude(float longitude) {
            this.request.params.put(VKConst.LONGITUDE, longitude);
            return this;
        }

        /**
         * Current location accuracy in meters
         */
        public VKMethodSetter accuracy(int accuracy) {
            this.request.params.put(VKConst.ACCURACY, accuracy);
            return this;
        }

        /**
         * Time when a user disappears from location search results, in seconds
         * Default 7 200
         */
        public VKMethodSetter timeout(int timeout) {
            this.request.params.put(VKConst.TIMEOUT, timeout);
            return this;
        }

        /**
         * Search zone radius type (1 to 4)
         * <p/>
         * 1 – 300 m;
         * 2 – 2400 m;
         * 3 – 18 km;
         * 4 – 150 km.
         * <p/>
         * By default 1
         */
        public VKMethodSetter radius(int radius) {
            this.request.params.put(VKConst.RADIUS, radius);
            return this;
        }


        /** Setters for messages.getDialogs */

        /**
         * true - to return only conversations which have unread messages
         * false - returns all messages
         * <p/>
         * By default is false
         */
        public VKMethodSetter unread(boolean unread) {
            this.request.params.put(VKConst.UNREAD, unread);
            return this;
        }

        /**
         * Execute request and convert to {@link JSONObject}
         */
        public JSONObject execute() {
            return this.request.execute();
        }

        /**
         * ASYNC (in new Thread) Execute request and convert to {@link String}
         *
         * @param listener callback for a successful.
         *                 Called in main (UI) thread
         */
        public void execute(VKOnResponseListener listener) {
            new VKAsyncRequestTask(listener).execute(request);
        }
    }

    /**
     * Class for execution and configuration API-requests
     */
    public static class VKRequest {

        /**
         * Selected method name
         */
        public String methodName;

        /**
         * Http method, true = POST, false = GET
         */
        public boolean isPost;

        /**
         * Sets current system language as default for API data
         */
        public boolean useSystemLanguage = true;

        /**
         * Specify listener for current request
         */
        @Nullable
        public VKOnResponseListener responseListener;

        /**
         * list of http parameters
         */
        public VKParams params;

        /**
         * Creates new request with parameters. See documentation for methods here https://vk.com/dev/methods
         *
         * @param methodName API-method name, e.g. audio.get
         */
        public VKRequest(String methodName) {
            this(methodName, null);
        }

        /**
         * Creates new request with parameters. See documentation for methods here https://vk.com/dev/methods
         *
         * @param methodName API-method name, e.g. audio.get
         * @param parameters method parameters
         */
        public VKRequest(String methodName, VKParams parameters) {
            this(methodName, parameters, false, null);
        }

        /**
         * Creates new request with parameters. See documentation for methods here https://vk.com/dev/methods
         *
         * @param methodName API-method name, e.g. audio.get
         * @param parameters method parameters
         */
        public VKRequest(String methodName, VKParams parameters, boolean isPost, VKOnResponseListener listener) {
            this.methodName = methodName;
            this.params = parameters;
            this.responseListener = listener;
            this.useSystemLanguage = true;
            this.isPost = isPost;
        }

        private String getSignedUrl() {
            if (!params.containsKey("access_token")) {
                params.put("access_token", VKApi.getInstance().getAccount().accessToken);
            }
            if (!params.containsKey("v")) {
                params.put("v", API_VERSION);
            }
            if (!params.containsKey("lang") && useSystemLanguage) {
                params.put("lang", Locale.getDefault().getLanguage());
            }

            String args = "";
            if (!isPost) args = params.toString();

            return BASE_URL + methodName + "?" + args;
        }

        public JSONObject execute() {
            String url = getSignedUrl();
            HttpBaseRequest request;

            if (isPost) {
                request = new HttpPostRequest(url, params.toString());
            } else {
                request = new HttpGetRequest(url);
            }

            HttpResponse response = getInstance().mClient.execute(request);
            if (response != null) {
                try {
                    return new JSONObject(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    /**
     * Class for operate with parameters
     */
    public static class VKParams extends TreeMap<String, String> {

        public void put(String paramName, int paramValue) {
            put(paramName, String.valueOf(paramValue));
        }

        public void put(String paramName, long paramValue) {
            put(paramName, String.valueOf(paramValue));
        }

        public void put(String paramName, boolean paramValue) {
            put(paramName, paramValue ? "1" : "0");
        }

        public void put(String paramName, float paramValue) {
            put(paramName, String.valueOf(paramValue));
        }

        public void put(String paramName, double paramValue) {
            put(paramName, String.valueOf(paramValue));
        }

        /**
         * Convert params to {@link String} with use {@link URLEncoder}
         */
        @Override
        public String toString() {
            StringBuilder params = new StringBuilder();
            try {
                for (Entry<String, String> entry : entrySet()) {
                    if (params.length() != 0)
                        params.append("&");

                    params.append(entry.getKey())
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return params.toString();
        }
    }

    /**
     * VK Utils
     */
    public static class VKUtil {

        /**
         * Convert items to {@link String}
         */
        static <T> String arrayToString(Collection<T> items) {
            if (isEmpty(items)) {
                return null;
            }

            StringBuilder buffer = new StringBuilder(32);
            for (Object item : items) {
                buffer.append(item);
                buffer.append(',');
            }
            return buffer.toString();
        }

        /**
         * Returns true if the collection is null or empty
         *
         * @param list the collection to be examined
         * @return true of list is null or empty
         */
        static boolean isEmpty(Collection list) {
            return list == null || list.isEmpty();
        }

        public static void checkErrors(String url, JSONObject source) throws VKException {
            if (source == null) {
                return;
            }

            if (source.has("error")) {
                JSONObject errorJson = source.optJSONObject("error");
                String errorMessage = errorJson.optString("error_msg");
                int errorCode = errorJson.optInt("error_cde");

                VKException exception = new VKException(url, errorMessage, errorCode);
                if (errorCode == 14) {
                    exception.captchaSid = errorJson.optString("captcha_sid");
                    exception.captchaImg = errorJson.optString("captcha_img");
                }
                if (errorCode == 17) {
                    exception.redirectUri = errorJson.optString("redirect_uri");
                }

                throw exception;
            }
            // probably, if use execute method, http://vk.com/dev/execute
            if (source.has("execute_errors")) {
                JSONArray errorsArray = source.optJSONArray("execute_errors");
                if (errorsArray.length() <= 0) {
                    return;
                }

                // only first error is processed if there are multiple
                JSONObject errorJson = errorsArray.optJSONObject(0);
                String errorMessage = errorJson.optString("error_msg");
                int errorCode = errorJson.optInt("error_code");

                VKException exception = new VKException(url, errorMessage, errorCode);
                if (errorCode == 14) {
                    exception.captchaSid = errorJson.optString("captcha_sid");
                    exception.captchaImg = errorJson.optString("captcha_img");
                }
                if (errorCode == 17) {
                    exception.redirectUri = errorJson.optString("redirect_uri");
                }
                throw exception;
            }
        }

    }

    /**
     * Converter json object to VK object. e.g {@link VKUser}, {@link VKMessages}
     */
    public static class VKJsonParser {

        public static Collection<VKUser> parseUsers(JSONArray jsonUsers) {
            return VKUser.parseUsers(jsonUsers);
        }

    }

    /**
     * Constants for api. List is not full
     */
    public class VKConst {
        /** Commons */
        public static final String USER_ID = "user_id";
        public static final String USER_IDS = "user_ids";
        public static final String OWNER_ID = "owner_id";
        public static final String FIELDS = "fields";
        public static final String SORT = "sort";
        public static final String OFFSET = "offset";
        public static final String COUNT = "count";

        /** Auth */
        public static final String VERSION = "v";
        public static final String HTTPS = "https";
        public static final String LANG = "lang";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String SIG = "sig";

        /** Get users */
        public static final String NAME_CASE = "name_case";

        /** Messages */
        public static final String OUT = "out";
        public static final String TIME_OFFSET = "time_offset";
        public static final String FILTERS = "filters";
        public static final String LAST_MESSAGE_ID = "last_message_id";
        public static final String START_MESSAGE_ID = "start_message_id";
        public static final String PREVIEW_LENGTH = "preview_length";
        public static final String UNREAD = "unread";

        /** Get subscriptions */
        public static final String EXTENDED = "extended";

        /** Report users */
        public static final String TYPE = "type";
        public static final String COMMENT = "comment";

        /** Get nearby users */
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ACCURACY = "accuracy";
        public static final String TIMEOUT = "timeout";
        public static final String RADIUS = "timeout";

        /** Search */
        public static final String Q = "q";
        public static final String CITY = "city";
        public static final String COUNTRY = "country";
        public static final String HOMETOWN = "hometown";
        public static final String UNIVERSITY_COUNTRY = "university_country";
        public static final String UNIVERSITY = "university";
        public static final String UNIVERSITY_YEAR = "university_year";
        public static final String SEX = "sex";
        public static final String STATUS = "status";
        public static final String AGE_FROM = "age_from";
        public static final String AGE_TO = "age_to";
        public static final String BIRTH_DAY = "birth_day";
        public static final String BIRTH_MONTH = "birth_month";
        public static final String BIRTH_YEAR = "birth_year";
        public static final String ONLINE = "online";
        public static final String HAS_PHOTO = "has_photo";
        public static final String SCHOOL_COUNTRY = "school_country";
        public static final String SCHOOL_CITY = "school_city";
        public static final String SCHOOL = "school";
        public static final String SCHOOL_YEAR = "school_year";
        public static final String RELIGION = "religion";
        public static final String INTERESTS = "interests";
        public static final String COMPANY = "company";
        public static final String POSITION = "position";
        public static final String GROUP_ID = "group_id";

        public static final String FRIENDS_ONLY = "friends_only";
        public static final String FROM_GROUP = "from_group";
        public static final String MESSAGE = "message";
        public static final String ATTACHMENTS = "attachments";
        public static final String SERVICES = "services";
        public static final String SIGNED = "signed";
        public static final String PUBLISH_DATE = "publish_date";
        public static final String LAT = "lat";
        public static final String LONG = "long";
        public static final String PLACE_ID = "place_id";
        public static final String POST_ID = "post_id";
        public static final String POSTS = "posts";

        /** Errors */
        public static final String ERROR_CODE = "error_code";
        public static final String ERROR_MSG = "error_msg";
        public static final String REQUEST_PARAMS = "request_params";

        /** Captcha */
        public static final String CAPTCHA_IMG = "captcha_img";
        public static final String CAPTCHA_SID = "captcha_sid";
        public static final String CAPTCHA_KEY = "captcha_key";
        public static final String REDIRECT_URI = "redirect_uri";

        /** Photos */
        public static final String PHOTO = "photo";
        public static final String PHOTOS = "photos";
        public static final String ALBUM_ID = "album_id";
        public static final String PHOTO_IDS = "photo_ids";
        public static final String PHOTO_SIZES = "photo_sizes";
        public static final String REV = "rev";
        public static final String FEED_TYPE = "feed_type";
        public static final String FEED = "feed";

        /** Videos */
        public static final String ADULT = "adult";

    }

    /**
     * Task for Async execute
     *
     * @see AsyncTask
     * @see VKOnResponseListener
     */
    public static class VKAsyncRequestTask extends AsyncTask<VKRequest, Void, JSONObject> {
        private VKOnResponseListener listener;

        public VKAsyncRequestTask(VKOnResponseListener listener) {
            this.listener = listener;
        }

        @Override
        protected JSONObject doInBackground(final VKRequest... params) {
            final JSONObject response = params[0].execute();
            checkError(params[0], response);
            return response;
        }

        private void checkError(final VKRequest request, final JSONObject json) {
            AndroidUtils.runOnUi(new Runnable() {
                @Override
                public void run() {
                    try {
                        VKUtil.checkErrors(request.getSignedUrl(), json);
                    } catch (VKException e) {
                        e.printStackTrace();

                        if (listener != null) {
                            listener.onError(e);
                        }
                    }
                }
            });
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (listener != null && result != null) {
                listener.onResponse(result);
            }
        }
    }

    /**
     * Thrown when server vk could not handle the request
     * see website to get description of error codes: http://vk.com/dev/errors
     */
    public static class VKException extends Exception {
        public String url;
        public String errorMessage;
        public int errorCode;

        /**
         * Captcha ID,
         * see http://vk.com/dev/captcha_error */
        public String captchaSid;
        /**
         * Link to image, you want to show the user,
         * that he typed text from this image
         *
         * see http://vk.com/dev/captcha_error
         */
        public String captchaImg;

        /**
         * In some cases, Vkontakte requires passing a validation procedure of the user,
         * resulting in since version 5.0 API
         * (for older versions will be prompted captcha_error)
         * any request to API the following error is returned
         *
         * see http://vk.com/dev/need_validation
         */
        public String redirectUri;

        public VKException(String url, String errorMessage, int errorCode) {
            super(errorMessage);
            this.url = url;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }
    }

    /**
     * See website http://vk.com/dev/errors
     */
    public static class VKErrorCodes {
        private VKErrorCodes() {
            // empty
        }

        public static final int UNKNOWN_ERROR = 1;
        public static final int APP_OFF = 2;
        public static final int UNKNOWN_METHOD = 3;
        public static final int INVALID_SIGNATURE = 4;
        public static final int USER_AUTHORIZATION_FAILED = 5;
        public static final int TOO_MANY_REQUESTS_PER_SECOND = 6;
        public static final int NO_RIGHTS = 7;
        public static final int BAD_REQUEST = 8;
        public static final int TOO_MANY_SIMILAR_ACTIONS = 9;
        public static final int INTERNAL_SERVER_ERROR = 10;
        public static final int CAPTCHA_NEEDED = 14;
        public static final int ACCESS_DENIED = 15;
        public static final int REQUIRES_REQUESTS_OVER_HTTPS = 16;
        public static final int VALIDATION_REQUIRED = 17;
        public static final int ACTION_PROHIBITED = 20;
        public static final int ACTION_ALLOWED_ONLY_FOR_STANDALONE = 21;
        public static final int METHOD_OFF = 23;
        public static final int CONFIRMATION_REQUIRED = 24;
        public static final int PARAMETER_IS_NOT_SPECIFIED = 100;
        public static final int INCORRECT_APP_ID = 101;
        public static final int INCORRECT_USER_ID = 113;
        public static final int INCORRECT_TIMESTAMP = 150;
        public static final int ACCESS_TO_ALBUM_DENIED = 200;
        public static final int ACCESS_TO_AUDIO_DENIED = 201;
        public static final int ACCESS_TO_GROUP_DENIED = 203;
        public static final int ALBUM_IS_FULL = 300;
        public static final int ACTION_DENIED = 500;

    }

    /**
     * Scope constants used for authorization, see http://vk.com/dev/permissions
     */
    public static class VKScope {
        public static final String NOTIFY = "notify";
        public static final String FRIENDS = "friends";
        public static final String PHOTOS = "photos";
        public static final String AUDIO = "audio";
        public static final String VIDEO = "video";
        public static final String DOCS = "docs";
        public static final String NOTES = "notes";
        public static final String PAGES = "pages";
        public static final String STATUS = "status";
        public static final String WALL = "wall";
        public static final String GROUPS = "groups";
        public static final String MESSAGES = "messages";
        public static final String NOTIFICATIONS = "notifications";
        public static final String STATS = "stats";
        public static final String ADS = "ads";
        public static final String OFFLINE = "offline";
        public static final String EMAIL = "email";
        public static final String NOHTTPS = "nohttps";
        public static final String DIRECT = "direct";

        /**
         * Converts integer value of permissions into {@link ArrayList} of constants
         *
         * @param permissionsValue integer permissions value
         * @return ArrayList contains string constants of permissions (scope)
         */
        public static ArrayList<String> parseVkPermissionsFromInteger(int permissionsValue) {
            ArrayList<String> res = new ArrayList<String>();
            if ((permissionsValue & 1) > 0) res.add(NOTIFY);
            if ((permissionsValue & 2) > 0) res.add(FRIENDS);
            if ((permissionsValue & 4) > 0) res.add(PHOTOS);
            if ((permissionsValue & 8) > 0) res.add(AUDIO);
            if ((permissionsValue & 16) > 0) res.add(VIDEO);
            if ((permissionsValue & 128) > 0) res.add(PAGES);
            if ((permissionsValue & 1024) > 0) res.add(STATUS);
            if ((permissionsValue & 2048) > 0) res.add(NOTES);
            if ((permissionsValue & 4096) > 0) res.add(MESSAGES);
            if ((permissionsValue & 8192) > 0) res.add(WALL);
            if ((permissionsValue & 32768) > 0) res.add(ADS);
            if ((permissionsValue & 65536) > 0) res.add(OFFLINE);
            if ((permissionsValue & 131072) > 0) res.add(DOCS);
            if ((permissionsValue & 262144) > 0) res.add(GROUPS);
            if ((permissionsValue & 524288) > 0) res.add(NOTIFICATIONS);
            if ((permissionsValue & 1048576) > 0) res.add(STATS);
            if ((permissionsValue & 4194304) > 0) res.add(EMAIL);
            return res;
        }

        /**
         * Gets permissions as String
         */
        public static String getAllPermissions() {
            return "notify, friends, photos, audio, video, docs, notes, pages, status, wall, groups, messages, notifications, email, offline";
        }
    }

    /**
     * Callback for Async execute
     */
    public interface VKOnResponseListener {
        /**
         * Called when successfully receiving the response from WEB
         *
         * @param responseJson json object from response
         */
        void onResponse(JSONObject responseJson);

        /**
         * Called when an error occurs on the server side
         * Visit website to get description of error codes: http://vk.com/dev/errors
         */
        void onError(VKException exception);
    }

}
