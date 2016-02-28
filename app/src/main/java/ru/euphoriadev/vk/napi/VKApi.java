package ru.euphoriadev.vk.napi;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.euphoriadev.vk.BuildConfig;
import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKPhoto;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.http.AsyncHttpClient;
import ru.euphoriadev.vk.http.HttpEntity;
import ru.euphoriadev.vk.http.HttpParams;
import ru.euphoriadev.vk.http.HttpRequest;
import ru.euphoriadev.vk.http.HttpResponse;
import ru.euphoriadev.vk.http.HttpResponseCodeException;
import ru.euphoriadev.vk.interfaces.RunnableToast;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.common.AppLoader;
import ru.euphoriadev.vk.util.ArrayUtil;
import ru.euphoriadev.vk.common.PrefManager;

/**
 * Created by Igor on 15.01.16.
 * <p/>
 * Simple new VK Library for execute request.
 * All classes are placed into this, for convenience and compactness.
 * <p/>
 * This library is a "mixture" of "VK-SDK" and "VK-Android-SDK by Thest1" library.
 * For more information about VK API - please, visit the official documentation
 * https://vk.com/dev/main.
 * <p/>
 * However, some methods and docs are hidden from users, but! this is not a problem,
 * because their list can be obtained by following this link:
 * https://vkapi.zf-projects.ru/methods-list
 * <p/>
 * Example to init api and execute users.get request:
 * <pre>
 *    VKApi.init(VKApi.VKUserConfig.from(account));
 *    JSONObject response = VKApi.users()
 *             .get()
 *             .userId(1)
 *             .fields("photo_50, nickname")
 *             .execute();
 *
 *    // return users in json object
 * </pre>
 * <p/>
 * You can also login via the official standalone app. e.g. to send gifts:
 * <pre>
 *    VKApi.authorization("Vasya Pupkin", "Pass228", new VKApi.VKOnAuthorizationListener() {
 *         @Override
 *         public void onSuccess(VKApi.VKUserConfig newConfig) {
 *             // authorization in successfully
 *             Log.w("Auth", "Auth success! user id: " + newConfig.userId);
 *         }
 *
 *         @Override
 *         public void onError(VKApi.VKException e) {
 *             // authorization is failed
 *             Log.w("Auth", "Auth failed: " + e.errorMessage);
 *         }
 *     });
 *
 * </pre>
 */

public class VKApi {
    public static final String TAG = "Euphoria.VKApi";
    public static final String BASE_URL = "https://api.vk.com/method/";
    public static final String API_VERSION = "5.44";
    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static final int OFFSET_PEER_ID = 2_000_000_000;
    public static final VKOnResponseListener DEFAULT_RESPONSE_LISTENER = new VKOnResponseListener() {
        @Override
        public void onResponse(VKRequest request, JSONObject responseJson) {
//            Log.i(TAG, responseJson.toString());
        }

        @Override
        public void onError(VKException exception) {
            exception.printStackTrace();
            AndroidUtils.post(new RunnableToast(AppLoader.appContext, exception.getMessage(), true));
        }

    };

    /**
     * For execute requests on background
     */
    private static final ExecutorService VK_SINGLE_EXECUTOR = Executors.newSingleThreadExecutor();
    private static volatile VKApi instance;

    private VKUserConfig mUserConfig;
    private AsyncHttpClient mClient;

    /**
     * Private constructor, Нou don't have use it
     *
     * @see #init(VKUserConfig)
     */
    private VKApi(VKUserConfig account) {
        this.mUserConfig = account;
        this.mClient = new AsyncHttpClient(null, 1);
    }

    /**
     * Init VKApi to send requests
     *
     * @param account the vk account,
     *                on behalf of which to send requests to server VK
     */
    public static synchronized VKApi init(VKUserConfig account) {
        if (instance == null) {
            instance = new VKApi(account);
        }
        return instance;
    }

    /**
     * Get initialized VKApi
     */
    public static VKApi getInstance() {
        if (instance == null) {
            throw new IllegalArgumentException("You must to initialize VKApi");
        }
        return instance;
    }

    /**
     * Return current account from api
     */
    public static VKUserConfig getUserConfig() {
        return getInstance().mUserConfig;
    }

    /**
     * Set the config for execute request with user access token and id
     *
     * @param newConfig the new config to set
     */
    public static void setUserConfig(VKUserConfig newConfig) {
        getInstance().mUserConfig = newConfig;
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
     * Methods for friends
     */
    public static VKFriends friends() {
        return new VKFriends();
    }

    /**
     * Methods for photos
     */
    public static VKPhotos photos() {
        return new VKPhotos();
    }

    /**
     * Methods for account
     */
    public static VKAccount account() {
        return new VKAccount();
    }

    /**
     * Execute request
     * A universal method for calling a sequence of other methods,
     * while saving and filtering interim results
     * <p/>
     * TODO: Inside the code may contain no more than 25 references to API methods
     * Read more: http://vk.com/dev/execute
     *
     * @param code the code algorithm in VKScript
     */
    public static JSONObject execute(String code) {
        VKRequest request = new VKRequest("execute");
        request.params.put(VKConst.CODE, code);
        try {
            return request.execute();
        } catch (HttpResponseCodeException e) {
            if (DEBUG) e.printStackTrace();
        }
        return null;
    }

    /**
     * Execute request ASYNC
     *
     * @param code     the code algorithm in VKScript
     * @param listener callback for a successful.
     *                 Called in main (UI) thread
     */
    public static void execute(String code, VKOnResponseListener listener) {
        VKRequest request = new VKRequest("execute");
        request.params.put(VKConst.CODE, code);
        new VKAsyncRequestTask(listener).executeOnExecutor(VK_SINGLE_EXECUTOR, request);
    }

    /**
     * Returns message uploader for upload photos in dialog
     */
    public static VKMessageUploader getMessageUploader() {
        return new VKMessageUploader();
    }

    /**
     * Returns document uploader for upload files
     */
    public static VKDocUploader getDocUploader() {
        return new VKDocUploader();
    }

    /**
     * Direct authorization with Official client vk id.
     * Read more: http://vk.com/dev/auth_direct
     */
    public static void authorization(String login, String password, final VKOnAuthorizationListener listener) {
        final String client_secret = "hHbZxrka2uZ6jB1inYsH";
        final int client_id = VKIdentifiers.ANDROID_OFFICIAL;

        final String url = "https://oauth.vk.com/token";

        final HttpParams params = new HttpParams();
        params.addParam("grant_type", "password");
        params.addParam("client_id", client_id);
        params.addParam("client_secret", client_secret);
        params.addParam("username", login);
        params.addParam("password", password);
        params.addParam("scope", VKScope.getAllPermissions());
        params.addParam("v", API_VERSION);

        HttpRequest request = new HttpRequest(url, "GET", params);
        getInstance().mClient.execute(request, new HttpRequest.OnResponseListener() {
            @Override
            public void onResponse(AsyncHttpClient client, HttpResponse response) {
                JSONObject json = response.getContentAsJson();
                try {
                    VKUtil.checkErrors(url, json);

                    if (listener != null) {
                        listener.onSuccess(VKUserConfig.from(json, client_id));
                    }
                } catch (VKException e) {
                    if (DEBUG) e.printStackTrace();
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }

            @Override
            public void onError(AsyncHttpClient client, HttpResponseCodeException exception) {
                if (listener != null) {
                    listener.onError(VKException.from(url, VKErrorCodes.USER_AUTHORIZATION_FAILED, exception));
                }
            }
        });

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
     * Release resources, close HttpClient
     */
    public synchronized static void close() {
        if (instance == null) {
            return;
        }
        getInstance().mClient.close();
    }

    /**
     * Account config it store the necessary data to run the query on behalf of user
     * such as the token, email, api id and user id
     */
    public static class VKUserConfig {
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
        public int userId;

        /**
         * Api id of standalone vk app, used only by authorization
         */
        public int apiId;

        /**
         * Create new vk account
         *
         * @param accessToken the access token of current user
         * @param email       the email of current user (not necessarily)
         * @param userId      the user id of current user
         */
        public VKUserConfig(String accessToken, @Nullable String email, int userId, int apiId) {
            this.accessToken = accessToken;
            this.email = email;
            this.userId = userId;
            this.apiId = apiId;
        }

        /**
         * Empty Constructor
         */
        public VKUserConfig() {
        }

        /**
         * Create new vk account and restore properties from file on SD Card
         */
        public VKUserConfig(File file) {
            this.restore(file);
        }

        /**
         * Create new VKUserConfig from {@link Account}, this is to support
         *
         * @param account the old account to get properties (access_token, userId)
         * @return new VKUserConfig with properties from {@link Account}
         */
        public static VKUserConfig from(Account account) {
            return new VKUserConfig(account.access_token, null, (int) account.user_id, Integer.parseInt(Account.API_ID));
        }

        /**
         * Creates new VKUserConfig {@link JSONObject} source
         *
         * @param source the source to get properties
         * @param apiId  the id of standalone app
         */
        public static VKUserConfig from(JSONObject source, int apiId) {
            if (VKUtil.isEmpty(source)) {
                return null;
            }
            VKUserConfig config = new VKUserConfig();
            config.accessToken = source.optString(ACCESS_TOKEN);
            config.userId = source.optInt(USER_ID);
            config.email = source.optString(EMAIL);
            config.apiId = apiId;
            return config;
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
                if (DEBUG) e.printStackTrace();
                return false;
            }
        }

        /**
         * Save account properties into file
         *
         * @return true if save is successful
         */
        public boolean save(File file) {
            JSONObject json = new JSONObject();
            try {
                json.putOpt(ACCESS_TOKEN, accessToken);
                json.putOpt(USER_ID, userId);
                json.putOpt(API_ID, apiId);
                json.putOpt(EMAIL, email);

                FileUtils.write(file, json.toString());
            } catch (JSONException | IOException e) {
                if (DEBUG) e.printStackTrace();
                return false;
            }
            return true;
        }

        /**
         * Restores account properties from SharedPreferences
         *
         * @return this account
         */
        public VKUserConfig restore() {
            this.accessToken = PrefManager.getString(ACCESS_TOKEN);
            this.userId = (int) PrefManager.getLong(USER_ID);
            this.apiId = PrefManager.getInt(API_ID);
            this.email = PrefManager.getString(EMAIL);
            return this;
        }

        /**
         * Restores account properties from SD
         *
         * @return this account
         */
        public VKUserConfig restore(File file) {
            try {
                String readText = FileUtils.readFileToString(file);
                JSONObject json = new JSONObject(readText);

                this.accessToken = json.optString(ACCESS_TOKEN);
                this.email = json.optString(EMAIL);
                this.userId = json.optInt(USER_ID);
                this.apiId = json.optInt(EMAIL);

            } catch (IOException | JSONException e) {
                if (DEBUG) e.printStackTrace();
            }
            return this;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(64);
            result.append("UserConfig {\n");
            result.append(USER_ID).append(" ").append(userId).append(", ");
            result.append(ACCESS_TOKEN).append(" ").append(accessToken).append(", ");
            result.append(API_ID).append(" ").append(apiId).append(", ");
            if (!TextUtils.isEmpty(email)) {
                result.append(EMAIL).append(" ").append(email).append(", ");
            }
            result.append("\n}");

            return result.toString();
        }

        /**
         * Remove account properties from SharedPreferences
         */
        public void clear() {
            PrefManager.remove(ACCESS_TOKEN);
            PrefManager.remove(USER_ID);
            PrefManager.remove(EMAIL);
            PrefManager.remove(API_ID);
        }
    }

    /**
     * Api methods for users
     * <p/>
     * see http://vk.com/dev/users
     */
    public static class VKUsers {

        /**
         * Returns detailed information on users
         * <p/>
         * NOTE: This is an open method; it does not require an access_token
         * http://vk.com/dev/users.get
         *
         * @see VKMessageMethodSetter#userIds(int...)
         * @see VKMessageMethodSetter#fields(String)
         * @see VKMessageMethodSetter#nameCase(String)
         */
        public VKUserMethodSetter get() {
            return new VKUserMethodSetter(new VKRequest("users.get", new VKParams()));
        }

        /**
         * Returns a list of users matching the search criteria
         * <p/>
         * NOTE: This method doesn't require any specific rights.
         * http://vk.com/dev/users.search
         *
         * @see VKUserSearchMethodSetter#q(String)
         * @see VKUserSearchMethodSetter#sort(int)
         * @see VKUserSearchMethodSetter#offset(int)
         * @see VKUserSearchMethodSetter#count(int)
         * @see VKMessageMethodSetter#fields(String)
         * @see VKUserSearchMethodSetter#city(int)
         * @see VKUserSearchMethodSetter#country(int)
         * @see VKUserSearchMethodSetter#hometown(int)
         * @see VKUserSearchMethodSetter#university(int)
         * @see VKUserSearchMethodSetter#universityCountry(int)
         * @see VKUserSearchMethodSetter#universityYear(int)
         * @see VKUserSearchMethodSetter#sex(int)
         * @see VKUserSearchMethodSetter#status(int)
         * @see VKUserSearchMethodSetter#ageFrom(int)
         * @see VKUserSearchMethodSetter#ageTo(int)
         * @see VKUserSearchMethodSetter#birthDay(int)
         * @see VKUserSearchMethodSetter#birthMonth(int)
         * @see VKUserSearchMethodSetter#birthYear(int)
         * @see VKUserSearchMethodSetter#online(boolean)
         * @see VKUserSearchMethodSetter#hasPhoto(boolean)
         */
        public VKUserSearchMethodSetter search() {
            return new VKUserSearchMethodSetter(new VKRequest("users.search", new VKParams()));
        }

        /**
         * Returns information whether a user installed the application
         * <p/>
         * NOTE: This method doesn't require any specific rights
         * http://vk.com/dev/users.isAppUser
         *
         * @see VKMethodSetter#userId(int)
         */
        public VKUserMethodSetter isAppUser() {
            return new VKUserMethodSetter(new VKRequest(("users.isAppUser"), new VKParams()));
        }

        /**
         * Returns a list of IDs of users and communities followed by the user
         * <p/>
         * NOTE: This is an open method; it does not require an access_token.
         * http://vk.com/dev/users.getSubscriptions
         *
         * @see VKUserMethodSetter#userId(int)
         * @see VKUserMethodSetter#offset(int)
         * @see VKUserMethodSetter#count(int)
         * @see VKUserMethodSetter#fields(String)
         * @see VKUserMethodSetter#extended(boolean)
         */
        public VKUserMethodSetter getSubscriptions() {
            return new VKUserMethodSetter(new VKRequest(("users.getSubscriptions"), new VKParams()));
        }

        /**
         * Returns a list of IDs of followers of the user in question,
         * sorted by date added, most recent first.
         * <p/>
         * NOTE: This is an open method; it does not require an access_token
         * http://vk.com/dev/users.getFollowers
         *
         * @see VKMethodSetter#userId(int)
         * @see VKMethodSetter#offset(int)
         * @see VKMethodSetter#count(int)
         * @see VKMethodSetter#fields(String)
         * @see VKMethodSetter#nameCase(String)
         */
        public VKUserMethodSetter getFollowers() {
            return new VKUserMethodSetter(new VKRequest(("users.getFollowers"), new VKParams()));
        }

        /**
         * Reports (submits a complain about) a user.
         * <p/>
         * http://vk.com/dev/users.report
         *
         * @see VKMethodSetter#userId(int)
         * @see VKUserMethodSetter#type(String)
         * @see VKUserMethodSetter#comment(String)
         */
        public VKUserMethodSetter report() {
            return new VKUserMethodSetter(new VKRequest(("users.report"), new VKParams()));
        }

        /**
         * The index of the current location of the user,
         * and returns a list of users that are near
         * <p/>
         * http://vk.com/dev/users.getNearby
         *
         * @see VKUserMethodSetter#latitude(float)
         * @see VKUserMethodSetter#longitude(float)
         * @see VKUserMethodSetter#accuracy(int)
         * @see VKUserMethodSetter#timeout(int)
         * @see VKUserMethodSetter#radius(int)
         * @see VKMethodSetter#fields(String)
         * @see VKMethodSetter#nameCase(String)
         */
        public VKUserMethodSetter getNearby() {
            return new VKUserMethodSetter(new VKRequest(("users.getNearby"), new VKParams()));
        }

    }

    /**
     * Api methods for messages
     * <p/>
     * http://vk.com/dev/messages
     */
    public static class VKMessages {

        /**
         * Returns a list of the current user's incoming or outgoing private messages
         * <p/>
         * http://vk.com/dev/messages.get
         *
         * @see VKMessageMethodSetter#out(boolean)
         * @see VKMethodSetter#offset(int)
         * @see VKMethodSetter#count(int)
         * @see VKMessageMethodSetter#timeOffset(int)
         * @see VKMessageMethodSetter#filters(int)
         * @see VKMessageMethodSetter#previewLength(int)
         * @see VKMessageMethodSetter#lastMessageId(int)
         */
        public VKMessageMethodSetter get() {
            return new VKMessageMethodSetter(new VKRequest(("messages.get"), new VKParams()));
        }

        /**
         * Returns the list of dialogs of the current user
         * <p/>
         * http://vk.com/dev/messages.getDialogs
         *
         * @see VKMethodSetter#offset(int)
         * @see VKMethodSetter#count(int)
         * @see VKMessageMethodSetter#startMessageId(int)
         * @see VKMessageMethodSetter#previewLength(int)
         * @see VKMessageMethodSetter#unread(boolean)
         */
        public VKMessageMethodSetter getDialogs() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getDialogs"), new VKParams()));
        }

        /**
         * Returns messages by their IDs
         * <p/>
         * http://vk.com/dev/messages.getById
         *
         * @see VKMessageMethodSetter#messageIds(int...)
         * @see VKMessageMethodSetter#previewLength(int)
         */
        public VKMessageMethodSetter getById() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getById"), new VKParams()));
        }

        /**
         * Returns a list of the current user's private messages,
         * that match search criteria
         * <p/>
         * http://vk.com/dev/messages.search
         */
        public VKMessageMethodSetter search() {
            return new VKMessageMethodSetter(new VKRequest(("messages.search"), new VKParams()));
        }

        /**
         * Returns a list of the current user's private messages,
         * that match search criteria
         * <p/>
         * http://vk.com/dev/messages.getHistory
         */
        public VKMessageMethodSetter getHistory() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getHistory"), new VKParams()));
        }

        /**
         * Returns media files from the dialog or group chat
         * <p/>
         * Result:
         * Returns a list of photo, video, audio or doc objects depending
         * on media_type parameter value
         * and additional next_from field containing new offset value
         * <p/>
         * http://vk.com/dev/messages.getHistoryAttachments
         */
        public VKMessageMethodSetter getHistoryAttachments() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getHistoryAttachments"), new VKParams()));
        }

        /**
         * Sends a message
         * <p/>
         * http://vk.com/dev/messages.send
         */
        public VKMessageMethodSetter send() {
            return new VKMessageMethodSetter(new VKRequest(("messages.send"), new VKParams()));
        }

        /**
         * Sends a sticker
         * <p/>
         * Result:
         * After successful execution, returns the sent message ID (mid).
         * <p/>
         * Error codes:
         * 900	Cannot send sticker to user from blacklist
         * <p/>
         * http://vk.com/dev/messages.sendSticker
         */
        public VKMessageMethodSetter sendSticker() {
            return new VKMessageMethodSetter(new VKRequest(("messages.sendSticker"), new VKParams()));
        }

        /**
         * Deletes one or more messages
         * <p/>
         * http://vk.com/dev/messages.delete
         */
        public VKMessageMethodSetter delete() {
            return new VKMessageMethodSetter(new VKRequest(("messages.delete"), new VKParams()));
        }

        /**
         * Deletes all private messages in a conversation
         * NOTE: If the number of messages exceeds the maximum,
         * the method shall be called several times
         * <p/>
         * http://vk.com/dev/messages.deleteDialog
         */
        public VKMessageMethodSetter deleteDialog() {
            return new VKMessageMethodSetter(new VKRequest(("messages.deleteDialog"), new VKParams()));
        }

        /**
         * Restores a deleted message
         * <p/>
         * http://vk.com/dev/messages.restore
         */
        public VKMessageMethodSetter restore() {
            return new VKMessageMethodSetter(new VKRequest(("messages.restore"), new VKParams()));
        }

        /**
         * Marks messages as read
         * <p/>
         * http://vk.com/dev/messages.markAsRead
         */
        public VKMessageMethodSetter markAsRead() {
            return new VKMessageMethodSetter(new VKRequest(("messages.markAsRead"), new VKParams()));
        }

        /**
         * Marks messages as new (unread)
         * (This method is deprecated and may be disabled soon, please avoid using it)
         * <p/>
         * http://vk.com/dev/messages.markAsNew
         */
        @Deprecated
        public VKMessageMethodSetter markAsNew() {
            return new VKMessageMethodSetter(new VKRequest(("messages.markAsNew"), new VKParams()));
        }

        /**
         * Marks and unmarks messages as important (starred)
         * <p/>
         * http://vk.com/dev/messages.markAsImportant
         */
        public VKMessageMethodSetter markAsImportant() {
            return new VKMessageMethodSetter(new VKRequest(("messages.markAsImportant"), new VKParams()));
        }

        /**
         * Returns data required for connection to a Long Poll server.
         * With Long Poll connection,
         * you can immediately know about incoming messages and other events.
         * <p/>
         * Result:
         * Returns an object with key, server, ts fields.
         * With such data you can connect to an instant message server
         * to immediately receive incoming messages and other events
         * <p/>
         * http://vk.com/dev/messages.getLongPollServer
         */
        public VKMessageMethodSetter getLongPollServer() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getLongPollServer"), new VKParams()));
        }

        /**
         * Returns updates in user's private messages.
         * To speed up handling of private messages,
         * it can be useful to cache previously loaded messages on
         * a user's mobile device/desktop, to prevent re-receipt at each call.
         * With this method, you can synchronize a local copy of
         * the message list with the actual version.
         * <p/>
         * Result:
         * Returns an object that contains the following fields:
         * 1 — history:     An array similar to updates field returned
         * from the Long Poll server,
         * with these exceptions:
         * - For events with code 4 (addition of a new message),
         * there are no fields except the first three.
         * - There are no events with codes 8, 9 (friend goes online/offline)
         * or with codes 61, 62 (typing during conversation/chat).
         * <p/>
         * 2 — messages:    An array of private message objects that were found
         * among events with code 4 (addition of a new message)
         * from the history field.
         * Each object of message contains a set of fields described here.
         * The first array element is the total number of messages
         * <p/>
         * http://vk.com/dev/messages.getLongPollHistory
         */
        public VKMessageMethodSetter getLongPollHistory() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getLongPollHistory"), new VKParams()));
        }

        /**
         * Returns information about a chat
         * <p/>
         * Returns a list of chat objects.
         * If the fields parameter is set,
         * the users field contains a list of user objects with
         * an additional invited_by field containing the ID of the user who
         * invited the current user to chat.
         * <p/>
         * http://vk.com/dev/messages.getChat
         */
        public VKMessageMethodSetter getChat() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getChat"), new VKParams()));
        }

        /**
         * Creates a chat with several participants
         * <p/>
         * Returns the ID of the created chat (chat_id).
         * <p/>
         * Errors:
         * 9	Flood control
         * http://vk.com/dev/messages.createChat
         */
        public VKMessageMethodSetter createChat() {
            return new VKMessageMethodSetter(new VKRequest(("messages.createChat"), new VKParams()));
        }

        /**
         * Edits the title of a chat
         * <p/>
         * Result:
         * Returns 1
         * <p/>
         * http://vk.com/dev/messages.editChat
         */
        public VKMessageMethodSetter editChat() {
            return new VKMessageMethodSetter(new VKRequest(("messages.editChat"), new VKParams()));
        }

        /**
         * Returns a list of IDs of users participating in a chat
         * <p/>
         * Result:
         * Returns a list of IDs of chat participants.
         * <p/>
         * If fields is set, the user fields contains a list of user objects
         * with an additional invited_by field containing the ID
         * of the user who invited the current user to chat.
         * <p/>
         * http://vk.com/dev/messages.getChatUsers
         */
        public VKMessageMethodSetter getChatUsers() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getChatUsers"), new VKParams()));
        }

        /**
         * Changes the status of a user as typing in a conversation
         * <p/>
         * Result:
         * Returns 1.
         * "User N is typing..." is shown for 10 seconds
         * after the method is called, or until the message is sent.
         * <p/>
         * http://vk.com/dev/messages.setActivity
         */
        public VKMessageMethodSetter setActivity() {
            return new VKMessageMethodSetter(new VKRequest(("messages.setActivity"), new VKParams())).type(true);
        }


    }

    /**
     * Api methods for photos
     * <p/>
     * http://vk.com/dev/photos
     */
    public static class VKPhotos {

        /**
         * Returns the server address for photo upload in a private message for a user.
         * When uploaded successfully, the photo can be saved using
         * the photos.saveMessagesPhoto method.
         * <p/>
         * Result:
         * Returns an object with an upload_url, aid and mid fields.
         * <p/>
         * http://vk.com/dev/photos.getMessagesUploadServer
         */
        public VKMethodSetter getMessagesUploadServer() {
            return new VKMethodSetter(new VKRequest("photos.getMessagesUploadServer"));
        }

        /**
         * Returns the server address for photo upload onto a user's wall.
         * When uploaded successfully, the photo can be saved using
         * the photos.saveWallPhoto method
         * <p/>
         * Result:
         * Returns an object with an upload_url field
         * <p/>
         * http://vk.com/dev/photos.getWallUploadServer
         */
        public VKMethodSetter getWallUploadServer() {
            return new VKMethodSetter(new VKRequest("photos.getWallUploadServer"));
        }

        /**
         * Saves a photo after being successfully uploaded.
         * URL obtained with photos.getMessagesUploadServer method
         *
         * @param server the server in integer returned when photo upload
         * @param photo  parameter returned when photo upload to the server
         */
        public VKMethodSetter saveMessagesPhoto(int server, String photo, String hash) {
            VKRequest request = new VKRequest("photos.saveMessagesPhoto");
            request.params.put(VKConst.SERVER, server);
            request.params.put(VKConst.PHOTO, photo);
            request.params.put(VKConst.HASH, hash);

            return new VKMethodSetter(request);
        }
    }

    /**
     * Api methods for friends
     * <p/>
     * http://vk.com/dev/friends
     */
    public static class VKFriends {

        /**
         * Returns a list of user IDs or detailed information about a user's friends
         * <p/>
         * http://vk.com/dev/friends.get
         */
        public VKFriendsMethodSetter get() {
            return new VKFriendsMethodSetter(new VKRequest("friends.get"));
        }

        /**
         * Returns a list of user IDs of a user's friends who are online
         * <p/>
         * http://vk.com/dev/friends.getOnline
         */
        public VKFriendsMethodSetter getOnline() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getOnline"));
        }

        /**
         * Returns a list of user IDs of the mutual friends of two users
         * <p/>
         * http://vk.com/dev/friends.getMutual
         */
        public VKFriendsMethodSetter getMutual() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getMutual"));
        }

        /**
         * Returns a list of user IDs of the current user's recently added friends
         * <p/>
         * http://vk.com/dev/friends.getRecent
         */
        public VKFriendsMethodSetter getRecent() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getRecent"));
        }

        /**
         * Returns information about the current user's incoming and outgoing friend requests
         * <p/>
         * http://vk.com/dev/friends.getRequests
         */
        public VKFriendsMethodSetter getRequests() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getRequests"));
        }

        /**
         * Approves or creates a friend request.
         * If the selected user ID is in the friend request list obtained
         * using the friends.getRequests method,
         * this method approves the friend request and adds
         * the selected user to the current user's friend list.
         * Otherwise, this method creates a friend request from the current user to
         * the selected user.
         * <p/>
         * Returns one of the following values:
         * 1 — Friend request sent.
         * 2 — Friend request from the user approved.
         * 4 — Request resending.
         * <p/>
         * Errors:
         * 174	Cannot add user himself as friend
         * 175	Cannot add this user to friends as they have put you on their blacklist
         * 176  Cannot add this user to friends as you put him on blacklist
         * <p/>
         * http://vk.com/dev/friends.add
         */
        public VKFriendsMethodSetter add() {
            return new VKFriendsMethodSetter(new VKRequest("friends.add"));
        }

        /**
         * Edits the friend lists of the selected user
         * <p/>
         * Result:
         * Returns 1.
         * <p/>
         * http://vk.com/dev/friends.edit
         */
        public VKFriendsMethodSetter edit() {
            return new VKFriendsMethodSetter(new VKRequest("friends.edit"));
        }

        /**
         * Declines a friend request or deletes a user from the current user's friend list.
         * If the selected user ID is in the friend request list obtained
         * using the friends.getRequests method,
         * this method declines the friend request.
         * Otherwise, this method deletes the specified user from the friend list
         * of the current user obtained using the friends.get method.
         * <p/>
         * Returns one of the following values:
         * 1 — User deleted from the current user's friend list.
         * 2 — Friend request from the user declined.
         * 3 — Friend request suggestion for the user deleted.
         * <p/>
         * Starting from version 5.28 returns object with fields:
         * success —             managed successfully remove other
         * friend_deleted —      has been removed each
         * out_request_deleted — cancelled outgoing request
         * in_request_deleted —  rejected the incoming request
         * suggestion_deleted —  rejected the recommendation of a friend
         * <p/>
         * http://vk.com/dev/friends.delete
         */
        public VKFriendsMethodSetter delete() {
            return new VKFriendsMethodSetter(new VKRequest("friends.delete"));
        }

        /**
         * Returns a list of the current user's friend lists
         * <p/>
         * Returns an array of list objects, each containing the following fields:
         * lid —  Friend list ID.
         * name — Friend list name.
         * <p/>
         * http://vk.com/dev/friends.getLists
         */
        public VKFriendsMethodSetter getLists() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getLists"));
        }

        /**
         * Creates a new friend list for the current user
         * <p/>
         * Returns the ID (lid) of the friend list that was created
         * <p/>
         * http://vk.com/dev/friends.addList
         */
        public VKFriendsMethodSetter addList() {
            return new VKFriendsMethodSetter(new VKRequest("friends.addList"));
        }

        /**
         * Edits a friend list of the current user.
         * <p/>
         * Returns 1.
         * <p/>
         * http://vk.com/dev/friends.editList
         */
        public VKFriendsMethodSetter editList() {
            return new VKFriendsMethodSetter(new VKRequest("friends.editList"));
        }

        /**
         * Deletes a friend list of the current user.
         * <p/>
         * Returns 1.
         * <p/>
         * http://vk.com/dev/friends.deleteList
         */
        public VKFriendsMethodSetter deleteList() {
            return new VKFriendsMethodSetter(new VKRequest("friends.deleteList"));
        }

        /**
         * Returns a list of IDs of the current user's friends
         * who installed the application.
         * <p/>
         * http://vk.com/dev/friends.getAppUsers
         */
        public VKFriendsMethodSetter getAppUsers() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getAppUsers"));
        }

        /**
         * Returns a list of the current user's friends whose phone numbers,
         * validated or specified in a profile, are in a given list.
         * This method can be used only if the current user's mobile phone number
         * is validated. To check the validation,
         * use the users.get method with user_ids=API_USER and
         * fields=has_mobile parameters where API_USER is equal
         * to the current user ID.
         * <p/>
         * Result:
         * For users whose validated phone numbers are in the list,
         * returns information as an array of user objects,
         * each containing a set of fields defined by the fields parameter.
         * The uid, first_name, last_name, and phone fields are always returned,
         * regardless of the selected fields.
         * The phone field of each object contains a phone number from
         * the list of phone numbers intended for search.
         * <p/>
         * http://vk.com/dev/friends.getByPhones
         */
        public VKFriendsMethodSetter getByPhones() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getByPhones"));
        }

        /**
         * Marks all incoming friend requests as viewed
         * <p/>
         * Result:
         * Returns 1
         * <p/>
         * http://vk.com/dev/friends.deleteAllRequests
         */
        public VKFriendsMethodSetter deleteAllRequests() {
            return new VKFriendsMethodSetter(new VKRequest("friends.deleteAllRequests"));
        }

        /**
         * Returns a list of profiles of users whom the current user may know.
         * For the method to return enough suggestions,
         * method account.importContacts will be called first.
         * <p/>
         * Result:
         * Returns an array of user objects,
         * each containing a set of fields defined by the fields parameter.
         * The uid, first_name, last_name, lists,
         * and online fields are always returned, regardless of the selected fields
         * <p/>
         * http://vk.com/dev/friends.getSuggestions
         */
        public VKFriendsMethodSetter getSuggestions() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getSuggestions"));
        }

    }

    /**
     * /**
     * Api methods for account
     * <p/>
     * http://vk.com/dev/account
     */
    public static class VKAccount {


        /**
         * Returns non-null values of user counters
         * NOTE: This method doesn't require any specific rights.
         */
        public VKMethodSetter getCounters() {
            return new VKMethodSetter(new VKRequest("account.getCounters"))
                    .fields("friends, messages, photos, videos, notes, gifts, events, groups, notifications, sdk, app_requests");
        }

        /**
         * Marks the current user as online for 15 minutes
         */
        public VKMethodSetter setOnline() {
            return new VKMethodSetter(new VKRequest("account.setOnline"));
        }

        /**
         * Marks a current user as Offline
         */
        public VKMethodSetter setOffline() {
            return new VKMethodSetter(new VKRequest("account.setOffline"));
        }

        /**
         * Gets settings of the current user in this application
         */
        public VKMethodSetter getAppPermissions() {
            return new VKMethodSetter(new VKRequest("account.getAppPermissions"));
        }


    }

    /**
     * Empty setter, does not contain methods
     */
    public static class VKEmptyMethodSetter {
        protected VKRequest request;

        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKEmptyMethodSetter(VKRequest request) {
            this.request = request;
        }
    }

    /**
     * Common setters, e.g. fields, user_ids, offset
     */
    public static class VKMethodSetter extends VKEmptyMethodSetter {
        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKMethodSetter(VKRequest request) {
            super(request);
        }

        /**
         * User IDs or screen names (screen_name). By default, current user ID.
         */
        public VKMethodSetter userIds(Collection<Integer> ids) {
            this.request.params.put(VKConst.USER_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * User IDs or screen names (screen_name). By default, current user ID.
         * e.g "1, 2, 10"
         */
        public VKMethodSetter userIds(String ids) {
            this.request.params.put(VKConst.USER_IDS, ids);
            return this;
        }

        /**
         * User ID, By default, current user ID
         */
        public VKMethodSetter userIds(int... ids) {
            this.request.params.put(VKConst.USER_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * User ID, By default, current user ID
         */
        public VKMethodSetter userIds(int userId) {
            userIds(Collections.singletonList(userId));
            return this;
        }

        /**
         * User ID. By default, the current user ID
         */
        public VKMethodSetter userId(int userId) {
            this.request.params.put(VKConst.USER_ID, userId);
            return this;
        }

        /**
         * ID of the user or community, e.g. audios.get
         */
        public VKMethodSetter ownerId(int ownerId) {
            this.request.params.put(VKConst.OWNER_ID, ownerId);
            return this;
        }

        /**
         * ID of community, e.g. photos.getWallUploadServer
         */
        public VKMethodSetter groupId(int groupId) {
            this.request.params.put(VKConst.GROUP_ID, groupId);
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
         * Number of users/messages/audios... to return
         * NOTE: even when using the offset parameter for information available
         * only the first 1000 results!
         */
        public VKMethodSetter count(int count) {
            this.request.params.put(VKConst.COUNT, count);
            return this;
        }

        /**
         * Sort order
         */
        public VKMethodSetter sort(int sortOrder) {
            this.request.params.put(VKConst.SORT, sortOrder);
            return this;
        }

        /**
         * Offset needed to return a specific subset
         */
        public VKMethodSetter offset(int offset) {
            this.request.params.put(VKConst.OFFSET, offset);
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

        /**
         * Captcha Sid, specifies for Captcha needed error
         */
        public VKMethodSetter captchaSid(String captchaSid) {
            this.request.params.put(VKConst.CAPTCHA_SID, captchaSid);
            return this;
        }

        /**
         * Captcha key, specifies for Captcha needed error
         */
        public VKMethodSetter captchaKey(String captchaKey) {
            this.request.params.put(VKConst.CAPTCHA_KEY, captchaKey);
            return this;
        }

        /**
         * Need for {@link VKResponseHandler}
         */
        public VKMethodSetter asModel(Object vkModel) {
            this.request.model = vkModel;
            return this;
        }

        /**
         * Uses a separate config for this request (Access Token)
         */
        public VKMethodSetter withConfig(VKUserConfig config) {
            this.request.params.put(VKConst.ACCESS_TOKEN, config.accessToken);
            return this;
        }

        /**
         * Executes request and convert to {@link JSONObject}
         */
        public JSONObject execute() throws HttpResponseCodeException {
            return this.request.execute();
        }

        /**
         * ASYNC (in new Thread) Executes request and convert to {@link String}
         *
         * @param listener callback for a successful.
         *                 Called in main (UI) thread
         */
        public void execute(VKOnResponseListener listener) {
            final VKAsyncRequestTask task = new VKAsyncRequestTask(listener);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(VK_SINGLE_EXECUTOR, request);
            } else {
                task.execute(request);
            }
        }

    }

    /**
     * Method setter for users.search
     */
    public static class VKUserMethodSetter extends VKMethodSetter {

        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKUserMethodSetter(VKRequest request) {
            super(request);
        }

        /** Setters for users.getSubscriptions */

        /**
         * false — to return separate lists of users and communities (default)
         * true — to return a combined list of users and communities
         */
        public VKUserMethodSetter extended(boolean extended) {
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
        public VKUserMethodSetter type(String type) {
            this.request.params.put(VKConst.TYPE, type);
            return this;
        }

        /**
         * Comment describing the complaint
         */
        public VKUserMethodSetter comment(String comment) {
            this.request.params.put(VKConst.COMMENT, comment);
            return this;
        }


        /** Setters for users.getNearby */

        /**
         * Geographic latitude of the place a user is located,
         * in degrees (from -90 to 90)
         */
        public VKUserMethodSetter latitude(float latitude) {
            this.request.params.put(VKConst.LATITUDE, latitude);
            return this;
        }

        /**
         * Geographic longitude of the place a user is located,
         * in degrees (from -90 to 90)
         */
        public VKUserMethodSetter longitude(float longitude) {
            this.request.params.put(VKConst.LONGITUDE, longitude);
            return this;
        }

        /**
         * Current location accuracy in meters
         */
        public VKUserMethodSetter accuracy(int accuracy) {
            this.request.params.put(VKConst.ACCURACY, accuracy);
            return this;
        }

        /**
         * Time when a user disappears from location search results, in seconds
         * Default 7 200
         */
        public VKUserMethodSetter timeout(int timeout) {
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
        public VKUserMethodSetter radius(int radius) {
            this.request.params.put(VKConst.RADIUS, radius);
            return this;
        }

    }

    /**
     * Method setter for {@link VKRequest}
     */
    public static class VKUserSearchMethodSetter extends VKUserMethodSetter {
        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKUserSearchMethodSetter(VKRequest request) {
            super(request);
        }

        /** Setters for users.search. NOT FULL */

        /**
         * Search query string (e.g., Vasya Babich).
         */
        public VKUserSearchMethodSetter q(String q) {
            this.request.params.put(VKConst.Q, q);
            return this;
        }

        /**
         * Sort order:
         * 1 — by date registered
         * 0 — by rating
         */
        public VKUserSearchMethodSetter sort(int sortOrder) {
            this.request.params.put(VKConst.SORT, sortOrder);
            return this;
        }

        /**
         * City ID
         */
        public VKUserSearchMethodSetter city(int cityId) {
            this.request.params.put(VKConst.CITY, cityId);
            return this;
        }

        /**
         * Country ID
         */
        public VKUserSearchMethodSetter country(int countryId) {
            this.request.params.put(VKConst.COUNTRY, countryId);
            return this;
        }

        /**
         * City name in a string
         */
        public VKUserSearchMethodSetter hometown(int hometown) {
            this.request.params.put(VKConst.HOMETOWN, hometown);
            return this;
        }

        /**
         * ID of the country where the user graduated
         */
        public VKUserSearchMethodSetter universityCountry(int countryId) {
            this.request.params.put(VKConst.UNIVERSITY_COUNTRY, countryId);
            return this;
        }

        /**
         * ID of the institution of higher education
         */
        public VKUserSearchMethodSetter university(int universityId) {
            this.request.params.put(VKConst.UNIVERSITY, universityId);
            return this;
        }

        /**
         * Year of graduation from an institution of higher education
         */
        public VKUserSearchMethodSetter universityYear(int year) {
            this.request.params.put(VKConst.UNIVERSITY_YEAR, year);
            return this;
        }

        /**
         * Sex of user
         * 1 — female
         * 2 — male
         * 0 — any (default
         */
        public VKUserSearchMethodSetter sex(int sex) {
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
        public VKUserSearchMethodSetter status(int status) {
            this.request.params.put(VKConst.STATUS, status);
            return this;
        }

        /**
         * Minimum age
         */
        public VKUserSearchMethodSetter ageFrom(int minAge) {
            this.request.params.put(VKConst.AGE_FROM, minAge);
            return this;
        }

        /**
         * Maximum age
         */
        public VKUserSearchMethodSetter ageTo(int maxAge) {
            this.request.params.put(VKConst.AGE_TO, maxAge);
            return this;
        }

        /**
         * Day of birth
         */
        public VKUserSearchMethodSetter birthDay(int day) {
            this.request.params.put(VKConst.BIRTH_DAY, day);
            return this;
        }

        /**
         * Month of birth
         */
        public VKUserSearchMethodSetter birthMonth(int month) {
            this.request.params.put(VKConst.BIRTH_MONTH, month);
            return this;
        }

        /**
         * Year of birth
         */
        public VKUserSearchMethodSetter birthYear(int year) {
            this.request.params.put(VKConst.BIRTH_YEAR, year);
            return this;
        }

        /**
         * Online status
         * <p/>
         * true — online only
         * false — all users
         */
        public VKUserSearchMethodSetter online(boolean online) {
            this.request.params.put(VKConst.ONLINE, online);
            return this;
        }

        /**
         * Has photo
         * <p/>
         * 1 — with photo only
         * 0 — all users
         */
        public VKUserSearchMethodSetter hasPhoto(boolean hasPhoto) {
            this.request.params.put(VKConst.HAS_PHOTO, hasPhoto);
            return this;
        }

        // to be continued...
    }

    /**
     * Method setter for messages
     */
    public static class VKMessageMethodSetter extends VKMethodSetter {

        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKMessageMethodSetter(VKRequest request) {
            super(request);
        }

        /** Setters for messages.get */

        /**
         * false — to return incoming messages (default)
         * true — to return outgoing messages
         */
        public VKMessageMethodSetter out(boolean out) {
            this.request.params.put(VKConst.COUNT, out);
            return this;
        }

        /**
         * Maximum time since a message was sent, in seconds.
         * To return messages without a time limitation, set as 0.
         */
        public VKMessageMethodSetter timeOffset(int timeOffset) {
            this.request.params.put(VKConst.TIME_OFFSET, timeOffset);
            return this;
        }

        /**
         * Filter to apply:
         * 1 — unread only
         * 2 — not from the chat
         * 4 — messages from friends
         * 4 — messages from friends
         * 8 - important messages
         * <p/>
         * If the 4 flag is set, the 1 and 2 flags are not considered
         */
        public VKMessageMethodSetter filters(int filters) {
            this.request.params.put(VKConst.FILTERS, filters);
            return this;
        }

        /**
         * Number of characters after which to truncate a previewed message.
         * To preview the full message, specify 0.
         */
        public VKMessageMethodSetter previewLength(int previewLength) {
            this.request.params.put(VKConst.PREVIEW_LENGTH, previewLength);
            return this;
        }

        /**
         * ID of the message received before the message,
         * that will be returned last (provided that no more than count messages
         * were received before it; otherwise offset parameter shall be used).
         */
        public VKMessageMethodSetter lastMessageId(int lastMessageId) {
            this.request.params.put(VKConst.LAST_MESSAGE_ID, lastMessageId);
            return this;
        }


        /** Setters for messages.getDialogs */

        /**
         * true - to return only conversations which have unread messages
         * false - returns all messages
         * <p/>
         * By default is false
         */
        public VKMessageMethodSetter unread(boolean unread) {
            this.request.params.put(VKConst.UNREAD, unread);
            return this;
        }

        /** Setters for messages.getById */

        /**
         * Message IDs
         */
        public VKMessageMethodSetter messageIds(ArrayList<Integer> ids) {
            this.request.params.put(VKConst.MESSAGE_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * Message IDs
         */
        public VKMessageMethodSetter messageIds(int... ids) {
            this.request.params.put(VKConst.MESSAGE_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /** Setters for messages.search */

        /**
         * Search query string
         */
        public VKMessageMethodSetter q(String query) {
            this.request.params.put(VKConst.Q, query);
            return this;
        }

        /** Setters for messages.getHistory */

        /**
         * if the value is > 0, then this message ID,
         * starting from which history of correspondence,
         * if the passed value is -1, then value
         * of offset is added number of unread messages at the end of the dialog).
         */
        public VKMessageMethodSetter startMessageId(int startMessageId) {
            this.request.params.put(VKConst.START_MESSAGE_ID, startMessageId);
            return this;
        }

        /**
         * Destination ID
         * <p/>
         * For group chat: 2000000000 + ID of conversation.
         * For community: -community ID
         * <p/>
         * TODO: accessible for versions from 5.38
         */
        public VKMessageMethodSetter peerId(int peerId) {
            this.request.params.put(VKConst.PEER_ID, peerId);
            return this;
        }

        /**
         * Sort order:
         * true —  return messages in chronological order (reverse).
         * false — return messages in reverse chronological order (default)
         */
        public VKMessageMethodSetter rev(boolean rev) {
            this.request.params.put(VKConst.REV, rev);
            return this;
        }


        /** Setters for messages.send */

        /**
         * User's short address (for example, durov)
         */
        public VKMessageMethodSetter domain(String domain) {
            this.request.params.put(VKConst.DOMAIN, domain);
            return this;
        }

        /**
         * ID of conversation the message will relate to
         */
        public VKMessageMethodSetter chatId(int chatId) {
            this.request.params.put(VKConst.CHAT_ID, chatId);
            return this;
        }

        /**
         * (Required if attachments is not set.) Text of the message
         */
        public VKMessageMethodSetter message(String message) {
            this.request.params.put(VKConst.MESSAGE, message);
            return this;
        }

        /**
         * Unique ID used to prevent re-sending of the same message
         * (Not necessarily)
         */
        public VKMessageMethodSetter guid(int guid) {
            this.request.params.put(VKConst.GUID, guid);
            return this;
        }

        /**
         * Geographical latitude of a check-in, in degrees (from -90 to 90).
         */
        public VKMessageMethodSetter lat(double lat) {
            this.request.params.put(VKConst.LAT, lat);
            return this;
        }

        /**
         * Geographical longitude of a check-in, in degrees (from -180 to 180).
         */
        public VKMessageMethodSetter longitude(long longitude) {
            this.request.params.put(VKConst.LONG, longitude);
            return this;
        }

        /**
         * List of objects attached to the message, separated by commas
         */
        public final VKMessageMethodSetter attachment(Collection<String> attachments) {
            this.request.params.put(VKConst.ATTACHMENT, VKUtil.arrayToString(attachments));
            return this;
        }


        /**
         * List of objects attached to the message, separated by commas
         */
        public final VKMessageMethodSetter attachment(String... attachments) {
            this.request.params.put(VKConst.ATTACHMENT, VKUtil.arrayToString(attachments));
            return this;
        }

        /**
         * List of objects attached to the message, separated by commas
         */
        public final VKMessageMethodSetter forwardMessages(Collection<String> ids) {
            this.request.params.put(VKConst.FORWARD_MESSAGES, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * (Required if message is not set.)
         * List of objects attached to the message, separated by commas
         */
        public final VKMessageMethodSetter forwardMessages(int... ids) {
            this.request.params.put(VKConst.FORWARD_MESSAGES, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * Sticker ID
         */
        public final VKMessageMethodSetter stickerId(int stickerId) {
            this.request.params.put(VKConst.STICKER_ID, stickerId);
            return this;
        }

        /** Setters for messages.restore */

        /**
         * ID of a previously-deleted message to restore
         */
        public final VKMessageMethodSetter messageId(int messageId) {
            this.request.params.put(VKConst.MESSAGE_ID, messageId);
            return this;
        }


        /** Setters for messages.markAsImportant */

        /**
         * Sets flag to important message:
         * false — to remove the star
         * true — to add a star (mark as important)
         */
        public final VKMessageMethodSetter important(boolean important) {
            this.request.params.put(VKConst.IMPORTANT, important);
            return this;
        }

        /** Setters for messages.getLongPollServer */

        /**
         * True, to use SSL
         */
        public final VKMessageMethodSetter useSsl(boolean useSsl) {
            this.request.params.put(VKConst.USE_SSL, useSsl);
            return this;
        }

        /**
         * True, to return the pts field,
         * needed for the messages.getLongPollHistory method
         */
        public final VKMessageMethodSetter needPts(boolean needPts) {
            this.request.params.put(VKConst.NEED_PTS, needPts);
            return this;
        }


        /** Setters for messages.getLongPollHistory */

        /**
         * Last value of the ts parameter returned from the Long Poll server
         * or by using messages.getLongPollServer method.
         */
        public final VKMessageMethodSetter ts(int ts) {
            this.request.params.put(VKConst.TS, ts);
            return this;
        }

        /**
         * Last value of the parameter new_pts received from Long Poll server
         * used to receive actions, which are kept always
         */
        public final VKMessageMethodSetter pts(int pts) {
            this.request.params.put(VKConst.PTS, pts);
            return this;
        }

        /**
         * Number of messages that need to return
         */
        public final VKMessageMethodSetter msgsLimit(int limit) {
            this.request.params.put(VKConst.MSGS_LIMIT, limit);
            return this;
        }

        /**
         * True - returns history only from those users who are currently online
         */
        public final VKMessageMethodSetter onlines(boolean onlines) {
            this.request.params.put(VKConst.ONLINES, onlines);
            return this;
        }

        /**
         * Maximum ID of the message among existing ones in the local copy.
         * Both messages received with API methods
         * (for example, messages.getDialogs, messages.getHistory),
         * and data received from a Long Poll server (events with code 4)
         * are taken into account
         */
        public final VKMessageMethodSetter maxMsgId(int id) {
            this.request.params.put(VKConst.MAX_MSG_ID, id);
            return this;
        }


        /** Setters for messages.messages.getChat */

        /**
         * Chat IDs
         */
        public final VKMessageMethodSetter chatIds(int... ids) {
            this.request.params.put(VKConst.MAX_MSG_ID, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * Chat IDs
         */
        public final VKMessageMethodSetter chatIds(Collection<Integer> ids) {
            this.request.params.put(VKConst.MAX_MSG_ID, VKUtil.arrayToString(ids));
            return this;
        }


        /** Setters for messages.messages.createChat */

        /**
         * Chat title
         */
        public final VKMessageMethodSetter title(String title) {
            this.request.params.put(VKConst.TITLE, title);
            return this;
        }


        /** Setters for messages.messages.messages.setActivity */

        /**
         * typing — user has started to type
         */
        public final VKMessageMethodSetter type(boolean typing) {
            this.request.params.put(VKConst.TYPE, typing ? "typing" : null);
            return this;
        }


        /** Setters for messages.messages.messages.getHistoryAttachments */

        /**
         * Type of media files to return:
         * - photo;
         * - video;
         * - audio;
         * - doc;
         * - link.
         * Use {@link VKConst}
         */
        public final VKMessageMethodSetter mediaType(String type) {
            this.request.params.put(VKConst.MEDIA_TYPE, type);
            return this;
        }

        /**
         * true — to return photo sizes in
         * a special format (https://vk.com/dev/photo_sizes)
         */
        public final VKMessageMethodSetter photoSizes(boolean photoSizes) {
            this.request.params.put(VKConst.PHOTO_SIZES, photoSizes);
            return this;
        }


    }

    /**
     * Method setter for friends
     */
    public static class VKFriendsMethodSetter extends VKMethodSetter {
        public static final String SORT_ORDER_HINTS = "hints";
        public static final String SORT_ORDER_RAMDOM = "random";
        public static final String SORT_ORDER_MOBILE = "mobile";
        public static final String SORT_ORDER_NAME = "name";

        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKFriendsMethodSetter(VKRequest request) {
            super(request);
        }

        /** Setters for friends.get */

        /**
         * Sort order:
         * name —   by name (enabled only if the fields parameter is used)
         * random — by random, returns friends in random order
         * hints —  by rating, similar to how friends are sorted in My friends section
         * mobile — by mobile, above those of friends who have installed the mobile app
         */
        public VKFriendsMethodSetter sort(String sortOrder) {
            request.params.put(VKConst.SORT, sortOrder);
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
        public VKFriendsMethodSetter nameCase(String nameCase) {
            request.params.put(VKConst.NAME_CASE, nameCase);
            return this;
        }

        /** Setters for friends.getOnline */

        /**
         * Friend list ID. If this parameter is not set,
         * information about all online friends is returned
         */
        public VKFriendsMethodSetter listId(int listId) {
            request.params.put(VKConst.LIST_ID, listId);
            return this;
        }

        /**
         * Online mobile flag
         * true — to return an additional online_mobile field
         * false — (default)
         */
        public VKFriendsMethodSetter onlineMobile(boolean onlineMobile) {
            request.params.put(VKConst.ONLINE_MOBUILE, onlineMobile);
            return this;
        }


        /** Setters for friends.getMutual */

        /**
         * ID of the user whose friends will be checked against the friends
         * of the user specified in target_uid
         */
        public VKFriendsMethodSetter sourceUid(int sourceUid) {
            request.params.put(VKConst.SOURCE_UID, sourceUid);
            return this;
        }

        /**
         * ID of the user whose friends will be checked against the friends
         * of the user specified in source_uid
         */
        public VKFriendsMethodSetter targetUid(int targetUid) {
            request.params.put(VKConst.TARGET_UID, targetUid);
            return this;
        }

        /**
         * A list of user IDs to which you want to search for mutual friends
         */
        public VKFriendsMethodSetter targetUids(int... uids) {
            request.params.put(VKConst.TARGET_UIDS, VKUtil.arrayToString(uids));
            return this;
        }

        /**
         * A list of user IDs to which you want to search for mutual friends
         */
        public VKFriendsMethodSetter targetUids(int uid) {
            targetUids(Collections.singletonList(uid));
            return this;
        }

        /**
         * A list of user IDs to which you want to search for mutual friends
         */
        public VKFriendsMethodSetter targetUids(Collection<Integer> uids) {
            request.params.put(VKConst.TARGET_UIDS, VKUtil.arrayToString(uids));
            return this;
        }


        /** Setters for friends.getRequests */

        /**
         * true — to return response messages from users who have sent a friend request or,
         * if suggested is set to true, to return a list of suggested friends
         */
        public VKFriendsMethodSetter extended(boolean extended) {
            request.params.put(VKConst.EXTENDED, extended);
            return this;
        }

        /**
         * true — to return a list of mutual friends (up to 20), if any
         */
        public VKFriendsMethodSetter needMutual(boolean needMutual) {
            request.params.put(VKConst.NEED_MUTUAL, needMutual);
            return this;
        }

        /**
         * false - do not return viewed requests, true — return viewed requests.
         * (If out = 1, need_viewed is ignored).
         */
        public VKFriendsMethodSetter needViewed(boolean needViewed) {
            request.params.put(VKConst.NEED_VIEWED, needViewed);
            return this;
        }

        /**
         * true — to return a list of suggested friends
         * false — to return friend requests (default)
         */
        public VKFriendsMethodSetter suggested(boolean suggested) {
            request.params.put(VKConst.SUGGESTED, suggested);
            return this;
        }


        /** Setters for friends.add */

        /**
         * Text of the message (up to 500 characters) for the friend request, if any
         */
        public VKFriendsMethodSetter text(String text) {
            request.params.put(VKConst.TEXT, text);
            return this;
        }


        /** Setters for friends.getRequests */

        /**
         * IDs of the friend lists to which to add the user
         */
        public VKFriendsMethodSetter listIds(int... ids) {
            request.params.put(VKConst.LIST_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * IDs of the friend lists to which to add the user
         */
        public VKFriendsMethodSetter listIds(Collection<Integer> ids) {
            request.params.put(VKConst.LIST_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * IDs of the friend lists to which to add the user
         */
        public VKFriendsMethodSetter listIds(int id) {
            request.params.put(VKConst.LIST_IDS, VKUtil.arrayToString(new int[]{id}));
            return this;
        }


        /** Setters for friends.getLists */

        /**
         * Return whether the system marks list of public user's friends
         */
        public VKFriendsMethodSetter returnSystem(boolean returnSystem) {
            request.params.put(VKConst.RETURN_SYSTEM, returnSystem);
            return this;
        }


        /** Setters for friends.addList */

        /**
         * Name of the friend list
         */
        public VKFriendsMethodSetter name(String name) {
            request.params.put(VKConst.NAME, name);
            return this;
        }


        /** Setters for friends.editList */

        /**
         * (Applies if user_ids parameter is not set.)
         * User IDs to add to the friend list.
         */
        public VKFriendsMethodSetter addUserIds(int... ids) {
            request.params.put(VKConst.ADD_USER_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * (Applies if user_ids parameter is not set.)
         * User IDs to add to the friend list.
         */
        public VKFriendsMethodSetter addUserIds(Collection<Integer> ids) {
            request.params.put(VKConst.ADD_USER_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * (Applies if user_ids parameter is not set.)
         * User IDs to add to the friend list.
         */
        public VKFriendsMethodSetter addUserIds(int id) {
            request.params.put(VKConst.ADD_USER_IDS, VKUtil.arrayToString(new int[]{id}));
            return this;
        }

        /**
         * (Applies if user_ids parameter is not set.)
         * User IDs to delete from the friend list.
         */
        public VKFriendsMethodSetter deleteUserIds(int... ids) {
            request.params.put(VKConst.DELETE_USER_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * (Applies if user_ids parameter is not set.)
         * User IDs to delete from the friend list.
         */
        public VKFriendsMethodSetter deleteUserIds(Collection<Integer> ids) {
            request.params.put(VKConst.DELETE_USER_IDS, VKUtil.arrayToString(ids));
            return this;
        }

        /**
         * (Applies if user_ids parameter is not set.)
         * User IDs to delete from the friend list.
         */
        public VKFriendsMethodSetter deleteUserIds(int id) {
            request.params.put(VKConst.DELETE_USER_IDS, VKUtil.arrayToString(new int[]{id}));
            return this;
        }


        /** Setters for friends.getByPhones */

        /**
         * List of phone numbers in MSISDN format (maximum 1000).
         * <p/>
         * Example:
         * +79219876543,+79111234567
         */
        public VKFriendsMethodSetter phones(String... phones) {
            request.params.put(VKConst.PHONES, VKUtil.arrayToString(phones));
            return this;
        }

        /**
         * List of phone numbers in MSISDN format (maximum 1000).
         * <p/>
         * Example:
         * +79219876543,+79111234567
         */
        public VKFriendsMethodSetter phones(Collection<String> phones) {
            request.params.put(VKConst.PHONES, VKUtil.arrayToString(phones));
            return this;
        }


        /** Setters for friends.getSuggestions */

        /**
         * Types of potential friends to return:
         * mutual — users with many mutual friends
         * contacts — users found with the account.importContacts method
         * mutual_contacts — users who imported the same contacts
         * as the current user with the account.importContacts method
         */
        public VKFriendsMethodSetter filter(String filter) {
            request.params.put(VKConst.FILTER, filter);
            return this;
        }
    }

    /**
     * Class for execution and configuration API-requests
     */
    public static class VKRequest {

        public Object model;
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
            this(methodName, new VKParams());
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
            if (!params.containsKey(VKConst.ACCESS_TOKEN)) {
                params.put(VKConst.ACCESS_TOKEN, VKApi.getUserConfig().accessToken);
            }
            if (!params.containsKey(VKConst.VERSION)) {
                params.put(VKConst.VERSION, API_VERSION);
            }
            if (!params.containsKey(VKConst.LANG) && useSystemLanguage) {
                params.put(VKConst.LANG, Locale.getDefault().getLanguage());
            }

            String args = "";
            if (!isPost) args = params.toString();

            return BASE_URL + methodName + "?" + args;
        }

        /**
         * Execute request and convert to {@link JSONObject}
         */
        public JSONObject execute() throws HttpResponseCodeException {
            String url = getSignedUrl();
            HttpRequest request;

            request = new HttpRequest(url, isPost ? "POST" : "GET", null);

            HttpResponse response = getInstance().mClient.execute(request);
            try {
                return new JSONObject(response.toString());
            } catch (JSONException e) {
                if (DEBUG) e.printStackTrace();
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
        public static <T> String arrayToString(Collection<T> items) {
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

        @SafeVarargs
        public static <T> String arrayToString(T... array) {
            if (array.length == 0) {
                return null;
            }

            StringBuilder buffer = new StringBuilder(32);
            for (Object item : array) {
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
        public static boolean isEmpty(Collection list) {
            return ArrayUtil.isEmpty(list);
        }

        /**
         * Returns true, if array is null or empty
         *
         * @param array the json array to check
         */
        public static boolean isEmpty(JSONArray array) {
            return array == null || array.length() <= 0;
        }

        /**
         * Returns true, if source is null or empty
         *
         * @param source the json object to check
         */
        public static boolean isEmpty(JSONObject source) {
            return source == null || source.length() <= 0;
        }

        public static void checkErrors(String url, JSONObject source) throws VKException {
            if (isEmpty(source)) {
                return;
            }

            if (source.has("error")) {
                JSONObject errorJson = source.optJSONObject("error");
                String errorMessage = errorJson.optString("error_msg");
                int errorCode = errorJson.optInt("error_cde");

                VKException exception = new VKException(url, errorMessage, errorCode);
                // Captcha needed
                if (errorCode == 14) {
                    exception.captchaSid = errorJson.optString("captcha_sid");
                    exception.captchaImg = errorJson.optString("captcha_img");
                }
                // Validation required
                if (errorCode == 17) {
                    exception.redirectUri = errorJson.optString("redirect_uri");
                }

                throw exception;
            }
            // if use execute method, http://vk.com/dev/execute
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
                // Captcha needed
                if (errorCode == 14) {
                    exception.captchaSid = errorJson.optString("captcha_sid");
                    exception.captchaImg = errorJson.optString("captcha_img");
                }
                // Validation required
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

        /**
         * Deprecated, use {@link VKJsonParser#toJson(Object)}i
         */
        @Deprecated
        public static Collection<VKUser> parseUsers(JSONArray items) {
            return VKUser.parseUsers(items);
        }

        /**
         * Deprecated, use {@link VKJsonParser#toJson(Object)}i
         */
        @Deprecated
        public static VKUser parseUsers(JSONObject response) {
            return VKUser.parse(response);
        }

        /**
         * Deprecated, use {@link VKJsonParser#toJson(Object)}i
         */
        @Deprecated
        public static Collection<VKMessage> parseMessages(JSONArray items) {
            return VKMessage.parseArray(items);
        }

        /**
         * Deprecated, use {@link VKJsonParser#toJson(Object)}i
         */
        @Deprecated
        public static VKMessage parseMessage(JSONObject response) {
            return VKMessage.parse(response);
        }

        /**
         * Parse boolean from server response.
         *
         * @param from server response like this format: {@code response: 1}
         */
        public static boolean parseBoolean(JSONObject from) {
            return from.optInt("response", 0) == 1;
        }

        /**
         * Parse boolean from JSONObject with given name.
         *
         * @param from server response like this format: {@code field: 1}
         * @param name name of field to read
         */
        public static boolean parseBoolean(JSONObject from, String name) {
            return from != null && from.optInt(name, 0) == 1;
        }

        /**
         * Parse int from JSONObject with given name.
         *
         * @param from server response like this format: {@code field: 34}
         * @param name name of field to read
         */
        public static int parseInt(JSONObject from, String name) {
            if (VKUtil.isEmpty(from)) return 0;
            return from.optInt(name, 0);
        }

        /**
         * Parse int from server response.
         *
         * @param from server response like this format: {@code response: 34}
         */
        public static int parseInt(JSONObject from) {
            if (VKUtil.isEmpty(from)) return 0;
            return from.optInt("response");
        }

        /**
         * Parse long from JSONObject with given name.
         *
         * @param from server response like this format: {@code field: 34}
         * @param name name of field to read
         */
        public static long parseLong(JSONObject from, String name) {
            if (VKUtil.isEmpty(from)) return 0;
            return from.optLong(name, 0);
        }

        /**
         * Parse int array from JSONObject with given name.
         *
         * @param from int JSON array like this one {@code {11, 34, 42}}
         */
        public static int[] parseIntArray(JSONArray from) {
            int[] result = new int[from.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = from.optInt(i);
            }
            return result;
        }

        /**
         * Convert object to JSON, uses {@link java.lang.reflect.Field}.
         * The method is quite long,
         * because reflection too much performs access checks.
         *
         * @param object the object for which Json representation
         * @return {@link JSONObject} from {@link Object}
         */
        public static JSONObject toJson(Object object) {
            Field[] fields = object.getClass().getFields();
            JSONObject jsonObject = new JSONObject();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                try {
                    jsonObject.put(field.getName(), field.get(object));
                } catch (JSONException | IllegalAccessException e) {
                    if (DEBUG) e.printStackTrace();
                }
            }
            return jsonObject;
        }

        /**
         * Parse object from JSON with Reflection.
         * However, not everything is so simple,
         * you need to follow the rules for proper parsing:
         * <p/>
         * 1. All fields must be public.
         * 2. The field names must be fully equal to key name from json.
         * 3. Not necessarily, but it is better to use
         * primitive types (int, char) instead of wrappers (Integer, Long).
         * <p/>
         * NOTE: So far, this is an experimental feature, may not function properly
         *
         * @param source the json to parse value on object
         * @param aClass the class of result object
         * @param <E>    the class type of object for result
         * @return a new Object, with filled fields from json
         */
        @SuppressWarnings("unchecked")
        public static <E> E fromJson(JSONObject source, Class<E> aClass) {
            if (source == null || source.length() == 0) {
                return null;
            }
            JSONArray jsonItems = null;
            if (source.has("response")) {
                Object json = source.opt(VKConst.RESPONSE);
                if (json instanceof JSONObject) {
                    source = (JSONObject) json;
                } else if (json instanceof JSONArray) {
                    jsonItems = (JSONArray) json;
                }
            }

            if (jsonItems == null) {
                jsonItems = source.optJSONArray(VKConst.ITEMS);
            }

            // if value in "items" array
            if (jsonItems != null && jsonItems.length() > 0) {
                JSONObject item = jsonItems.optJSONObject(0);
                return fromJson(item, aClass);
            }

            // if format: {@code response: 1}
            if (aClass == Integer.class || aClass == Long.class) {
                return (E) Integer.valueOf(VKJsonParser.parseInt(source));
            } else if (aClass == Boolean.class) {
                return (E) Boolean.valueOf(VKJsonParser.parseBoolean(source));
            }

            // if a complex type class, e.g. VKUser, VKMessage,
            try {
                Object object = aClass.getConstructor().newInstance();
                Field[] fields = aClass.getFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    field.setAccessible(true);

                    String key = field.getName();
                    Object value = source.opt(key);
                    if (value == null) {
                        continue;
                    }

                    Class<?> type = field.getType();
                    if (type.isPrimitive()) {
                        if (type == int.class) {
                            field.setInt(object, Integer.parseInt(value.toString()));
                        } else if (type == long.class) {
                            field.setLong(object, Long.parseLong(value.toString()));
                        } else if (type == float.class) {
                            field.setFloat(object, Float.parseFloat(value.toString()));
                        } else if (type == double.class) {
                            field.setDouble(object, Double.parseDouble(value.toString()));
                        }
                    } else {
                        if (type.isArray() && value instanceof JSONArray) {
                            JSONArray array = (JSONArray) value;
                            Object arrayFromJson = parseArray(type.getComponentType(), array);

                            field.set(object, arrayFromJson);
                        }
                        if (type == String.class) {
                            field.set(object, value);
                        }
                    }
                }
                return (E) object;
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
            return null;
        }

        /**
         * Create object array and parse from {@link JSONObject}.
         * Supports parsing of primitive types (int, long, double)
         *
         * @param componentType the component type of array item
         * @param jsonArray     the json array to parse
         * @return instance of array, to set to array field in class
         * @see Field#getType()
         * @see Class#isArray()
         * @see Class#getComponentType()
         */
        public static Object parseArray(Class<?> componentType, JSONArray jsonArray) {
            Object instanceArray = Array.newInstance(componentType, jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                Object value = jsonArray.opt(i);
                if (value == null) {
                    continue;
                }
                if (componentType.isPrimitive()) {
                    if (componentType == int.class) {
                        Array.setInt(instanceArray, i, jsonArray.optInt(i));
                    } else if (componentType == long.class) {
                        Array.setLong(instanceArray, i, jsonArray.optLong(i));
                    } else if (componentType == float.class) {
                        Array.setFloat(instanceArray, i, Float.parseFloat(value.toString()));
                    } else if (componentType == double.class) {
                        Array.setDouble(instanceArray, i, jsonArray.optDouble(i));
                    }
                } else {
                    Array.set(instanceArray, i, value);
                }
            }
            return instanceArray;
        }

        /**
         * Returns root JSONObject from server response
         *
         * @param source standard VK server response
         */
        public static JSONObject responseJson(JSONObject source) {
            return source.optJSONObject(VKConst.RESPONSE);
        }

        /**
         * Returns root JSONArray from server response
         *
         * @param source standard VK server response
         */
        public static JSONArray responseArray(JSONObject source) {
            if (source.has(VKConst.RESPONSE)) {
                Object json = source.opt(VKConst.RESPONSE);
                if (json instanceof JSONObject) {
                    // response is object: {response: {...}}
                    source = (JSONObject) json;
                } else if (json instanceof JSONArray) {
                    // response is array: {response: [...]}
                    return ((JSONArray) json);
                }
            }

            // items in response: {response: {...
            // items: [...]
            // }}
            return source.optJSONArray(VKConst.ITEMS);
        }

        public static <T> T newInstance(Class<T> c) {
            try {
                @SuppressWarnings("unchecked")
                Constructor<T> constructor = c.getConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
            return null;
        }

    }

    /**
     * Task for Async execute
     *
     * @see AsyncTask
     * @see VKOnResponseListener
     */
    public static class VKAsyncRequestTask extends AsyncTask<VKRequest, Void, JSONObject> {
        private VKOnResponseListener listener;
        private VKRequest request;

        public VKAsyncRequestTask(VKOnResponseListener listener) {
            this.listener = listener;
        }

        @Override
        protected JSONObject doInBackground(final VKRequest... params) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            final JSONObject response;
            final VKRequest request = params[0];
            this.request = request;
            try {
                response = request.execute();
                checkError(request, response);
                return response;
            } catch (final HttpResponseCodeException e) {
                if (DEBUG) e.printStackTrace();

                if (listener != null) {
                    AndroidUtils.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            if (e instanceof VKException) {
                                listener.onError((VKException) e);
                            } else {
                                listener.onError(new VKException(request.getSignedUrl(), e.responseMessage, e.responseCode));
                            }
                        }
                    });
                }
            }
            return null;
        }

        /**
         * Check errors for vk
         */
        private void checkError(final VKRequest request, final JSONObject json) throws VKException {
            VKUtil.checkErrors(request.getSignedUrl(), json);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (listener != null && result != null) {
                listener.onResponse(request, result);
            }
        }
    }

    /**
     * Thrown when server vk could not handle the request
     * see website to get description of error codes: http://vk.com/dev/errors
     * <p/>
     * Check {@link VKErrorCodes} to get descriptions of error codes
     */
    public static class VKException extends HttpResponseCodeException {
        public String url;
        public String errorMessage;
        public int errorCode;

        /**
         * Captcha ID,
         * see http://vk.com/dev/captcha_error
         */
        public String captchaSid;
        /**
         * Link to image, you want to show the user,
         * that he typed text from this image
         * <p/>
         * see http://vk.com/dev/captcha_error
         */
        public String captchaImg;

        /**
         * In some cases, VK requires passing a validation procedure of the user,
         * resulting in since version 5.0 API
         * (for older versions will be prompted captcha_error)
         * any request to API the following error is returned
         * <p/>
         * see http://vk.com/dev/need_validation
         */
        public String redirectUri;

        /**
         * Constructs a new {@code VKException}
         *
         * @param url          the url of executed request
         * @param errorMessage the detail error message for this exception
         * @param errorCode    the error code
         */
        public VKException(String url, String errorMessage, int errorCode) {
            super(errorMessage, errorCode);
            this.url = url;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }

        public static VKException from(String url, int errorCode, Exception e) {
            if (e instanceof VKException) {
                return (VKException) e;
            }

            if (e == null || TextUtils.isEmpty(url)) {
                return null;
            }

            return new VKException(url, e.getMessage(), errorCode);
        }
    }

    /**
     * Numeric status codes of errors
     * See website http://vk.com/dev/errors
     */
    public static class VKErrorCodes {
        /**
         * Unknown error occurred
         */
        public static final int UNKNOWN_ERROR = 1;
        /**
         * Application is disabled. Enable your application or use test mode
         */
        public static final int APP_OFF = 2;
        /**
         * Unknown method passed
         */
        public static final int UNKNOWN_METHOD = 3;
        /**
         * Incorrect signature
         */
        public static final int INVALID_SIGNATURE = 4;
        /**
         * User authorization failed
         */
        public static final int USER_AUTHORIZATION_FAILED = 5;
        /**
         * Too many requests per second
         */
        public static final int TOO_MANY_REQUESTS_PER_SECOND = 6;
        /**
         * Permission to perform this action is denied
         */
        public static final int NO_RIGHTS = 7;
        /**
         * Invalid request
         */
        public static final int BAD_REQUEST = 8;
        /**
         * Flood control
         */
        public static final int TOO_MANY_SIMILAR_ACTIONS = 9;
        /**
         * Internal server error
         */
        public static final int INTERNAL_SERVER_ERROR = 10;
        /**
         * In test mode application should be disabled or user should be authorized
         */
        public static final int IN_TEST_MODE = 11;
        /**
         * For execute method. Unable to compile code
         */
        public static final int EXEXUTE_CODE_COMPILE_ERROR = 12;
        /**
         * For execute method. Runtime error occurred during code invocation
         */
        public static final int EXECUTE_CODE_RUNTIME_ERROR = 13;
        /**
         * Captcha needed
         */
        public static final int CAPTCHA_NEEDED = 14;
        /**
         * Access denied
         */
        public static final int ACCESS_DENIED = 15;
        /**
         * HTTP authorization failed
         */
        public static final int REQUIRES_REQUESTS_OVER_HTTPS = 16;
        /**
         * Validation required
         */
        public static final int VALIDATION_REQUIRED = 17;
        /**
         * Permission to perform this action is denied
         * for non-standalone applications
         */
        public static final int ACTION_PROHIBITED = 20;
        /**
         * Permission to perform this action
         * is allowed only for Standalone and OpenAPI applications
         */
        public static final int ACTION_ALLOWED_ONLY_FOR_STANDALONE = 21;
        /**
         * This method was disabled
         */
        public static final int METHOD_OFF = 23;
        /**
         * Confirmation required
         */
        public static final int CONFIRMATION_REQUIRED = 24;
        /**
         * One of the parameters specified was missing or invalid
         */
        public static final int PARAMETER_IS_NOT_SPECIFIED = 100;
        /**
         * Invalid application API ID
         */
        public static final int INCORRECT_APP_ID = 101;
        /**
         * Invalid user id
         */
        public static final int INCORRECT_USER_ID = 113;
        /**
         * Invalid timestamp
         */
        public static final int INCORRECT_TIMESTAMP = 150;
        /**
         * Access to album denied
         */
        public static final int ACCESS_TO_ALBUM_DENIED = 200;
        /**
         * Access to audio denied
         */
        public static final int ACCESS_TO_AUDIO_DENIED = 201;
        /**
         * Access to group denied
         */
        public static final int ACCESS_TO_GROUP_DENIED = 203;
        /**
         * This album is full
         */
        public static final int ALBUM_IS_FULL = 300;
        /**
         * Permission denied.
         * You must enable votes processing in application settings
         */
        public static final int ACTION_DENIED = 500;
        /**
         * Permission denied.
         * You have no access to operations specified with given object(s)
         */
        public static final int PERMISSION_DENIED = 600;
        /**
         * Message errors
         */
        public static final int CANNOT_SEND_MESSAGE_BLACK_LIST = 900;
        public static final int CANNOT_SEND_MESSAGE_GROUP = 901;
        private VKErrorCodes() {
            // empty
        }

    }

    /**
     * IDs of messaging apps
     */
    public static class VKIdentifiers {
        /**
         * Official clients
         */
        public static final int ANDROID_OFFICIAL = 2274003;
        public static final int IPHONE_OFFICIAL = 3140623;
        public static final int IPAD_OFFICIAL = 3682744;
        public static final int WP_OFFICIAL = 3502557;
        public static final int WINDOWS_OFFICIAL = 3697615;

        /**
         * Unofficial client, mods and messengers
         */
        public static final int KATE_MODILE = 2685278;
        public static final int EUPHORIA = 4510232;
        public static final int LYNT = 3469984;
        public static final int SWEET = 4856309;
        public static final int AMBERFOG = 4445970;
        public static final int PHOENIX = 4994316;
        public static final int MESSENGER = 4894723;
        public static final int ZEUS = 4831060;
        public static final int ROCKET = 4757672;
        public static final int VK_MD = 4967124;

    }

    /**
     * Constants for api. List is not full
     */
    public class VKConst {
        public static final String ITEMS = "items";
        public static final String USER_ID = "user_id";

        /**
         * Commons
         */
        public static final String RESPONSE = "response";
        public static final String USER_IDS = "user_ids";
        public static final String OWNER_ID = "owner_id";
        public static final String FIELDS = "fields";
        public static final String SORT = "sort";
        public static final String OFFSET = "offset";
        public static final String COUNT = "count";
        public static final String UPLOAD_URL = "upload_url";
        public static final String SERVER = "server";

        /**
         * Auth
         */
        public static final String VERSION = "v";
        public static final String HTTPS = "https";
        public static final String LANG = "lang";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String SIG = "sig";

        /**
         * Get users
         */
        public static final String NAME_CASE = "name_case";

        /**
         * Messages
         */
        public static final String MESSAGE_IDS = "message_ids";
        public static final String MESSAGE_ID = "message_id";
        public static final String IMPORTANT = "important";
        public static final String OUT = "out";
        public static final String TIME_OFFSET = "time_offset";
        public static final String FILTERS = "filters";
        public static final String LAST_MESSAGE_ID = "last_message_id";
        public static final String START_MESSAGE_ID = "start_message_id";
        public static final String PREVIEW_LENGTH = "preview_length";
        public static final String UNREAD = "unread";
        public static final String PEER_ID = "peer_id";
        public static final String DOMAIN = "domain";
        public static final String CHAT_ID = "chat_id";
        public static final String GUID = "guid";
        public static final String ATTACHMENT = "attachment";
        public static final String FORWARD_MESSAGES = "forward_messages";
        public static final String STICKER_ID = "sticker_id";
        public static final String USE_SSL = "use_ssl";
        public static final String NEED_PTS = "need_pts";
        public static final String TS = "ts";
        public static final String PTS = "pts";
        public static final String ONLINES = "onlines";
        public static final String MSGS_LIMIT = "msgs_limit";
        public static final String MAX_MSG_ID = "max_msg_id";
        public static final String TITLE = "title";
        public static final String MEDIA_TYPE = "media_type";

        /**
         * Media types for attachments
         */
        public static final String MEDIA_TYPE_AUDIO = "audio";
        public static final String MEDIA_TYPE_VIDEO = "video";
        public static final String MEDIA_TYPE_PHOTO = "photo";
        public static final String MEDIA_TYPE_LINK = "link";
        public static final String MEDIA_TYPE_DOC = "photo";

        /**
         * Friends
         */
        public static final String LIST_ID = "list_id";
        public static final String LIST_IDS = "list_ids";
        public static final String SOURCE_UID = "source_uid";
        public static final String TARGET_UID = "target_uid";
        public static final String TARGET_UIDS = "target_uids";
        public static final String NEED_MUTUAL = "need_mutual";
        public static final String NEED_VIEWED = "need_viewed";
        public static final String SUGGESTED = "suggested";
        public static final String TEXT = "text";
        public static final String RETURN_SYSTEM = "return_system";
        public static final String NAME = "name";
        public static final String ADD_USER_IDS = "add_user_ids";
        public static final String DELETE_USER_IDS = "delete_user_ids";
        public static final String PHONES = "phones";
        public static final String FILTER = "filter";

        /**
         * Get subscriptions
         */
        public static final String EXTENDED = "extended";

        /**
         * Report users
         */
        public static final String TYPE = "type";
        public static final String COMMENT = "comment";

        /**
         * Get nearby users
         */
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ACCURACY = "accuracy";
        public static final String TIMEOUT = "timeout";
        public static final String RADIUS = "timeout";

        /**
         * Search
         */
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
        public static final String ONLINE_MOBUILE = "online_mobile";
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

        /**
         * Errors
         */
        public static final String ERROR_CODE = "error_code";
        public static final String ERROR_MSG = "error_msg";
        public static final String REQUEST_PARAMS = "request_params";

        /**
         * Captcha
         */
        public static final String CAPTCHA_IMG = "captcha_img";
        public static final String CAPTCHA_SID = "captcha_sid";
        public static final String CAPTCHA_KEY = "captcha_key";
        public static final String REDIRECT_URI = "redirect_uri";

        /**
         * Photos
         */
        public static final String PHOTO = "photo";
        public static final String PHOTOS = "photos";
        public static final String ALBUM_ID = "album_id";
        public static final String PHOTO_IDS = "photo_ids";
        public static final String PHOTO_SIZES = "photo_sizes";
        public static final String REV = "rev";
        public static final String FEED_TYPE = "feed_type";
        public static final String FEED = "feed";

        /**
         * Videos
         */
        public static final String ADULT = "adult";

        /**
         * Others
         */
        public static final String CODE = "code";
        public static final String HASH = "hash";

    }

    /**
     * Scope constants used for authorization, see http://vk.com/dev/permissions
     */
    public static class VKScope {
        /**
         * User allowed to send notifications to him/her
         */
        public static final String NOTIFY = "notify";
        /**
         * Access to friends
         */
        public static final String FRIENDS = "friends";
        /**
         * Access to photos
         */
        public static final String PHOTOS = "photos";
        /**
         * Access to audios
         */
        public static final String AUDIO = "audio";
        /**
         * Access to videos
         */
        public static final String VIDEO = "video";
        /**
         * Access to documents
         */
        public static final String DOCS = "docs";
        /**
         * Access to user notes
         */
        public static final String NOTES = "notes";
        /**
         * Access to wiki pages
         */
        public static final String PAGES = "pages";
        /**
         * Access to user status
         */
        public static final String STATUS = "status";
        /**
         * Access to standard and advanced methods for the wall
         */
        public static final String WALL = "wall";
        /**
         * Access to user groups
         */
        public static final String GROUPS = "groups";
        /**
         * Access to advanced methods for messaging
         */
        public static final String MESSAGES = "messages";
        /**
         * Access to notifications about answers to the user
         */
        public static final String NOTIFICATIONS = "notifications";
        /**
         * Access to statistics of user groups and
         * applications where he/she is an administrator
         */
        public static final String STATS = "stats";
        /**
         * Access to advanced methods for Ads API
         */
        public static final String ADS = "ads";
        /**
         * Access to API at any time
         */
        public static final String OFFLINE = "offline";
        public static final String EMAIL = "email";
        /**
         * Possibility to make API requests without HTTPS.
         * Note that this functionality is under testing and can be changed
         */
        public static final String NOHTTPS = "nohttps";
        public static final String DIRECT = "direct";

        /**
         * Converts integer value of permissions into {@link ArrayList} of constants
         *
         * @param permissionsValue integer permissions value
         * @return ArrayList contains string constants of permissions (scope)
         */
        public static ArrayList<String> parseVkPermissionsFromInteger(int permissionsValue) {
            ArrayList<String> res = new ArrayList<String>(16);
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
         * Gets all permissions as String
         */
        public static String getAllPermissions() {
            return "notify, friends, photos, audio, video, docs, notes, pages, status, wall, groups, messages, notifications, email, offline";
        }
    }

    /**
     * Base class for upload files to server.
     * Files (photos, audios, videos or documents) are uploaded in several steps:
     * receiving of URL for uploading {@link #getUploadServer()},
     * transfer of uploaded files content to the received URL
     * (note: files shall be transferred in multipart/form-data format)
     * and saving of information received after uploading
     * {@link #getSaveRequest(JSONObject)}.
     * <p/>
     * <p/>See for more information: https://vk.com/dev/upload_files
     *
     * @param <E> the element of upload response object,
     *            used by {@link VKUploader#getSaveRequest(JSONObject)}
     * @see VKMessageUploader
     * @see VKDocUploader
     */
    public static abstract class VKUploader<E> {
        /**
         * Media types
         */
        public static final int TYPE_DOC = 0;   // file
        public static final int TYPE_PHOTO = 1; // photo
        public static final int TYPE_VIDEO = 2; // video_file
        public static final int TYPE_AUDIO = 3; // file
        protected String mBoundary;
        protected int mType;
        private VKOnProgressListener mListener;

        /**
         * Creates a new VKUploader with type
         *
         * @param type the type of the uploaded file (photo, file)
         */
        public VKUploader(int type) {
            this.mBoundary = Long.toHexString(System.currentTimeMillis());
            this.mType = type;
        }

        protected static String getMimeType(String url) {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            if (extension != null) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                type = mime.getMimeTypeFromExtension(extension);
            }
            return type;
        }

        public Pair<String, String> getContentType() {
            return new Pair<>("Content-Type", String.format("multipart/form-data; boundary=%s", mBoundary));
        }

        protected String getFileDescription(File uploadFile) {
            String fileName = "file";
            switch (mType) {
                case TYPE_DOC:
                    fileName = "file";
                    break;
                case TYPE_AUDIO:
                    fileName = "file";
                    break;

                case TYPE_PHOTO:
                    fileName = "photo";
                    break;
                case TYPE_VIDEO:
                    fileName = "video_file";
                    break;
            }

            String extension = MimeTypeMap.getFileExtensionFromUrl(uploadFile.getAbsolutePath());
            return String.format("\r\n--%s\r\n", mBoundary) +
                    String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s.%s\"\r\n", fileName, fileName, extension) +
                    String.format("Content-Type: %s\r\n\r\n", getMimeType(uploadFile.getAbsolutePath()));
        }

        private String getBoundaryEnd() {
            return String.format("\r\n--%s--\r\n", mBoundary);
        }

        /**
         * Uploads file to server
         *
         * @param file     the file to upload
         * @param listener the callback that will run on progress change
         *                 listener called on UI Thread
         */
        public E uploadFile(File file, VKOnProgressListener listener) {
            Log.i(TAG, "Starting upload file: " + file.getName());
            this.mListener = listener;
            try {
                HttpRequest request = new HttpRequest(getUploadServer(), "POST", null, new VKHttpEntry(file));
                HttpResponse response = getInstance().mClient.execute(request);
                return getSaveRequest(response.getContentAsJson());
            } catch (HttpResponseCodeException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Returns a server address to upload file
         */
        protected abstract String getUploadServer();

        /**
         * After successful upload You can save the file using this method
         *
         * @param response the response for successful uploading
         */
        protected abstract E getSaveRequest(JSONObject response);

        /**
         * Class used for build upload multipart data for VK servers
         */
        private class VKHttpEntry extends HttpEntity {

            /**
             * Creates a new HttpEntity with file
             *
             * @param file the file for upload to server
             */
            public VKHttpEntry(File file) {
                super(file);
            }

            @Override
            public String getContentType() {
                return VKUploader.this.getContentType().second;
            }


            @Override
            protected void writeTo(OutputStream os) {
                if (mListener != null) {
                    AndroidUtils.runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onStart(getFile());
                        }
                    });
                }
                FileInputStream fis = null;
                try {
                    File uploadFile = getFile();
                    os.write(getFileDescription(uploadFile).getBytes("UTF-8"));

                    fis = new FileInputStream(uploadFile);
                    long length = getContentLength();
                    byte[] buffer = new byte[8192];
                    int read;
                    int progress = 0;
                    while ((read = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                        progress += read;

                        if (mListener != null && length != -1) {
                            updateProgress(buffer, (int) (progress * 100 / length), length);
                        }
                    }

                    os.write(getBoundaryEnd().getBytes("UTF-8"));
                    updateProgress(buffer, 100, length);
                    buffer = null;

                    if (mListener != null) {
                        onSuccess();
                    }
                } catch (Exception e) {
                    if (DEBUG) e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }

            private void updateProgress(final byte[] buffer, final int progress, final long total) {
                AndroidUtils.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onProgress(buffer, progress, total);
                    }
                });
            }

            private void onSuccess() {
                AndroidUtils.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onSuccess();
                    }
                });
            }

            @Override
            public long getContentLength() {
                try {
                    return getFile().length() +
                            getFileDescription(getFile()).getBytes("UTF-8").length +
                            getBoundaryEnd().getBytes("UTF-8").length;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return -1;
            }
        }
    }

    /**
     * Photo uploader in a private message
     */
    public static class VKMessageUploader extends VKUploader<VKPhoto> {
        /**
         * Creates a new VKUploader with type
         */
        public VKMessageUploader() {
            super(TYPE_PHOTO);
        }

        @Override
        protected String getUploadServer() {
            try {
                return VKApi.photos().getMessagesUploadServer()
                        .execute()
                        .optJSONObject(VKConst.RESPONSE)
                        .optString(VKConst.UPLOAD_URL);
//                return Api.get().getPhotoMessageUploadServer();
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
            return null;
        }

        @Override
        protected VKPhoto getSaveRequest(JSONObject response) {
            try {
                JSONObject photosResponse = VKApi.photos()
                        .saveMessagesPhoto(response.optInt("server"),
                                response.optString("photo"),
                                response.optString("hash")).execute();
                return VKPhoto.parsePhotos(VKJsonParser.responseArray(photosResponse)).get(0);

//                return Api.get().saveMessagesPhoto(String.valueOf(response.optInt("server")), response.optString(VKConst.PHOTO), response.optString("hash")).get(0);
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
            return null;
        }

    }

    /**
     * Document uploader in your section "Docs"
     */
    public static class VKDocUploader extends VKUploader<VKDocument> {

        /**
         * Creates a new VKUploader with type
         */
        public VKDocUploader() {
            super(VKUploader.TYPE_DOC);
        }

        @Override
        protected String getUploadServer() {
            try {
                return Api.get().getDocsUploadServer();
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
            return null;
        }

        @Override
        protected VKDocument getSaveRequest(JSONObject response) {
            try {
                return Api.get().saveDoc(response.optString("file"));
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * The response handler, auto parse models
     *
     * @param <E> the class type of object for result
     */
    public static abstract class VKResponseHandler<E> implements VKOnResponseListener {
        @Override
        @SuppressWarnings("unchecked")
        public void onResponse(VKRequest request, JSONObject responseJson) {
            Object result = VKJsonParser.fromJson(responseJson, request.model.getClass());
            onResponse((E) result);
        }

        @Override
        public void onError(VKException exception) {

        }

        /**
         * Called when successfully receiving the response from WEB
         *
         * @param result the parsed model object
         */
        public abstract void onResponse(E result);
    }

    /**
     * Callback for user direct authorization
     *
     * @see #authorization(String, String, VKOnAuthorizationListener)
     */
    public interface VKOnAuthorizationListener {
        /**
         * Called when user authorization is successful
         *
         * @param newConfig the authorized user config.
         *                  contains: access_token, user_id and email
         */
        void onSuccess(VKUserConfig newConfig);

        /**
         * Called when authorization is failed. e.g. captcha needed
         *
         * @param e the details info of error
         */
        void onError(VKException e);
    }

    /**
     * Callback for listen current progress of uploaded file
     * Used by {@link VKUploader}
     */
    public interface VKOnProgressListener {
        /**
         * Called before is started uploading file to the server, for example,
         * we can show notification
         *
         * @param file the file for upload into vk server
         */
        void onStart(File file);

        /**
         * Called when progress level has changed
         *
         * @param buffer    intermediate buffer data, which are uploading at moment
         * @param progress  current progress in percent, range [0...100]
         * @param totalSize the total length of uploading data in bytes
         */
        void onProgress(byte[] buffer, int progress, long totalSize);

        /**
         * Called after a successful file upload and save on the server
         */
        void onSuccess();
    }

    /**
     * Callback for Async execute
     */
    public interface VKOnResponseListener {
        /**
         * Called when successfully receiving the response from WEB
         *
         * @param request      the request, which was executed. May be null
         * @param responseJson json object from response
         */
        void onResponse(VKRequest request, JSONObject responseJson);

        /**
         * Called when an error occurs on the server side
         * Visit website to get description of error codes: http://vk.com/dev/errors
         * and {@link VKErrorCodes}
         * It is useful if the server requires you to enter a captcha
         *
         * @param exception the information of error
         */
        void onError(VKException exception);
    }

}
