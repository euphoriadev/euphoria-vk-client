package ru.euphoriadev.vk.api;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeMap;

import ru.euphoriadev.vk.http.DefaultHttpClient;
import ru.euphoriadev.vk.http.HttpBaseRequest;
import ru.euphoriadev.vk.http.HttpClient;
import ru.euphoriadev.vk.http.HttpGetRequest;
import ru.euphoriadev.vk.http.HttpPostRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.PrefManager;

/**
 * Created by Igor on 15.01.16.
 * <p/>
 * Simple VK Library for execute request
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

    public static VKUsers users() {
        return new VKUsers();
    }

    public static VKMessages messages() {
        return new VKMessages();
    }

    /**
     * Private Constructor, Нou don't have use it
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
     *
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
     *
     * http://vk.com/dev/messages
     */
    public static class VKMessages {

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
            this.request.params.put("user_ids", VKUtil.arrayToString(uids));
            return this;
        }

        public VKMethodSetter userId(int userId) {
            Arrays.asList(new int[]{userId});
            return this;
        }

        /**
         * Profile fields
         */
        public VKMethodSetter fields(String fields) {
            this.request.params.put("fields", fields);
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
            this.request.params.put("name_case", nameCase);
            return this;
        }


        /** Setters for users.search. NOT FULL */

        /**
         * Search query string (e.g., Vasya Babich).
         */
        public VKMethodSetter q(String q) {
            this.request.params.put("q", q);
            return this;
        }

        /**
         * Sort order:
         * 1 — by date registered
         * 0 — by rating
         */
        public VKMethodSetter sort(int sortOrder) {
            this.request.params.put("sort", sortOrder);
            return this;
        }

        /**
         * Offset needed to return a specific subset of users
         */
        public VKMethodSetter offset(int offset) {
            this.request.params.put("offset", offset);
            return this;
        }

        /**
         * Number of users to return. Max value 1 000
         */
        public VKMethodSetter count(int count) {
            this.request.params.put("count", count);
            return this;
        }

        /**
         * City ID
         */
        public VKMethodSetter city(int cityId) {
            this.request.params.put("city", cityId);
            return this;
        }

        /**
         * Country ID
         */
        public VKMethodSetter country(int countryId) {
            this.request.params.put("country", countryId);
            return this;
        }

        /**
         * City name in a string
         */
        public VKMethodSetter hometown(int hometown) {
            this.request.params.put("hometown", hometown);
            return this;
        }

        /**
         * ID of the country where the user graduated
         */
        public VKMethodSetter universityCountry(int countryId) {
            this.request.params.put("university_country", countryId);
            return this;
        }

        /**
         * ID of the institution of higher education
         */
        public VKMethodSetter university(int universityId) {
            this.request.params.put("university", universityId);
            return this;
        }

        /**
         * Year of graduation from an institution of higher education
         */
        public VKMethodSetter universityYear(int year) {
            this.request.params.put("university_year", year);
            return this;
        }

        /**
         * Sex of user
         * 1 — female
         * 2 — male
         * 0 — any (default
         */
        public VKMethodSetter sex(int sex) {
            this.request.params.put("sex", sex);
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
            this.request.params.put("status", status);
            return this;
        }

        /**
         * Minimum age
         */
        public VKMethodSetter ageFrom(int minAge) {
            this.request.params.put("age_from", minAge);
            return this;
        }

        /**
         * Maximum age
         */
        public VKMethodSetter ageTo(int maxAge) {
            this.request.params.put("age_to", maxAge);
            return this;
        }

        /**
         * Day of birth
         */
        public VKMethodSetter birthDay(int day) {
            this.request.params.put("birth_day", day);
            return this;
        }

        /**
         * Month of birth
         */
        public VKMethodSetter birthMonth(int month) {
            this.request.params.put("birth_month", month);
            return this;
        }

        /**
         * Year of birth
         */
        public VKMethodSetter birthYear(int year) {
            this.request.params.put("birth_year", year);
            return this;
        }

        /**
         * Online status
         *
         * true — online only
         * false — all users
         */
        public VKMethodSetter online(boolean online) {
            this.request.params.put("online", online);
            return this;
        }

        /**
         * Has photo
         *
         * 1 — with photo only
         * 0 — all users
         */
        public VKMethodSetter hasPhoto(boolean hasPhoto) {
            this.request.params.put("has_photo", hasPhoto);
            return this;
        }


        /** Setters for users.getSubscriptions */

        /**
         * false — to return separate lists of users and communities (default)
         * true — to return a combined list of users and communities
         */
        public VKMethodSetter extended(boolean extended) {
            this.request.params.put("extended", extended);
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
            this.request.params.put("type", type);
            return this;
        }

        /**
         * Comment describing the complaint
         */
        public VKMethodSetter comment(String comment) {
            this.request.params.put("comment", comment);
            return this;
        }


        /**
         * Setters for users.report users.getNearby
         */

        /**
         * Geographic latitude of the place a user is located,
         * in degrees (from -90 to 90)
         */
        public VKMethodSetter latitude(float latitude) {
            this.request.params.put("latitude", latitude);
            return this;
        }

        /**
         * Geographic longitude of the place a user is located,
         * in degrees (from -90 to 90)
         */
        public VKMethodSetter longitude(float latitude) {
            this.request.params.put("latitude", latitude);
            return this;
        }

        /**
         * Current location accuracy in meters
         */
        public VKMethodSetter accuracy(int accuracy) {
            this.request.params.put("accuracy", accuracy);
            return this;
        }

        /**
         * Time when a user disappears from location search results, in seconds
         * Default 7 200
         */
        public VKMethodSetter timeout(int timeout) {
            this.request.params.put("timeout", timeout);
            return this;
        }

        /**
         * Search zone radius type (1 to 4)
         * <p/>
         * 1 – 300 m;
         * 2 – 2400 m;
         * 3 – 18 km;
         * 4 – 150 km.
         *
         * By default 1
         */
        public VKMethodSetter radius(int radius) {
            this.request.params.put("radius", radius);
            return this;
        }



        /**
         * Execute request and convert to {@link String}
         */
        public String execute() {
            return this.request.execute();
        }

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

        public String execute() {
            String url = getSignedUrl();
            HttpBaseRequest request;

            if (isPost) {
                request = new HttpPostRequest(url, params.toString());
            } else {
                request = new HttpGetRequest(url);
            }

            HttpResponse response = getInstance().mClient.execute(request);
            if (response != null) {
                return response.toString();
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
            if (items == null) {
                return null;
            }

            StringBuilder buffer = new StringBuilder(32);
            for (Object item : items) {
                buffer.append(item);
                buffer.append(',');
            }
            return buffer.toString();
        }
    }

    /**
     * Task for Async execute
     *
     * @see AsyncTask
     * @see VKOnResponseListener
     */
    public static class VKAsyncRequestTask extends AsyncTask<VKRequest, Void, String> {
        private VKOnResponseListener listener;

        public VKAsyncRequestTask(VKOnResponseListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(VKRequest... params) {
            return params[0].execute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (listener != null && result != null) {
                try {
                    listener.onResponse(new JSONObject(result));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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
    }


}
