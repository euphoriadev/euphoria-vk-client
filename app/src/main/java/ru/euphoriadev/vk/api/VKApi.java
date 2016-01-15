package ru.euphoriadev.vk.api;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeMap;

import ru.euphoriadev.vk.http.DefaultHttpClient;
import ru.euphoriadev.vk.http.HttpBaseRequest;
import ru.euphoriadev.vk.http.HttpGetRequest;
import ru.euphoriadev.vk.http.HttpPostRequest;
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

    /**
     * Private Constructor, Нou don't have use it
     *
     * @see #init(VKAccount)
     */
    private VKApi(VKAccount account) {
        this.mAccount = account;
    }


    public static class VKAccount {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String USER_ID = "user_id";
        public static final String EMAIL = "email";
        public static final String API_ID = "api_id";

        /**
         * String token for use in request parameters
         */
        public String accessToken = null;

        /**
         * String current email of user
         */
        public String email = null;

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
        private VKRequest request;

        public VKUsers get() {
            request = new VKRequest("users.get", new VKParams());
            return this;
        }

        public VKUsers userIds(ArrayList<Integer> ids) {
            request.params.put("user_ids", VKUtil.arrayToString(ids));
            return this;
        }

        public String execute() {
            return request.execute();
        }
    }

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
        public boolean useSystemLanguage;

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

            return new DefaultHttpClient().execute(request).toString();
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
