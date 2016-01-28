package ru.euphoriadev.vk.api;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.TreeMap;

import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.Account;
import ru.euphoriadev.vk.util.AndroidUtils;
import ru.euphoriadev.vk.util.AsyncHttpClient;
import ru.euphoriadev.vk.util.PrefManager;
import ru.euphoriadev.vk.util.ThreadExecutor;

/**
 * Created by Igor on 15.01.16.
 * <p/>
 * Simple VK Library for execute request
 * <p/>
 * <p/>
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
//  public static final String API_VERSION = "5.14";
    public static final String API_VERSION = "5.44";

    protected static volatile VKApi sInstante;

    private VKAccount mAccount;
    private AsyncHttpClient mClient;

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
     * Private constructor, Нou don't have use it
     *
     * @see #init(VKAccount)
     */
    private VKApi(VKAccount account) {
        this.mAccount = account;
        this.mClient = new AsyncHttpClient(null);
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
     * Methods for friends
     */
    public static VKFriends friends() {
        return new VKFriends();
    }

    /**
     * Execute request
     * A universal method for calling a sequence of other methods,
     * while saving and filtering interim results
     *
     * TODO: Inside the code may contain no more than 25 references to API methods
     * Read more: http://vk.com/dev/execute
     *
     * @param code the code algorithm in VKScript
     */
    public static JSONObject execute(String code) {
        VKRequest request = new VKRequest("execute");
        request.params.put(VKConst.CODE, code);
        return request.execute();
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
        new VKAsyncRequestTask(listener).execute(request);
    }

    /**
     * Direct authorization with Official client vk.
     * Read more: http://vk.com/dev/auth_direct
     */
    public static void authorization(String login, String password, final VKOnResponseListener listener) {
        String client_secret = "hHbZxrka2uZ6jB1inYsH";
        String client_id = "2274003";

        final String url = "https://oauth.vk.com/token";

        final AsyncHttpClient.HttpParams params = new AsyncHttpClient.HttpParams();
        params.addParam("grant_type", "password");
        params.addParam("client_id", client_id);
        params.addParam("client_secret", client_secret);
        params.addParam("username", login);
        params.addParam("password", password);
        params.addParam("scope", VKScope.getAllPermissions());
        params.addParam("v", API_VERSION);

        ThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AsyncHttpClient.HttpResponse response = getInstance().mClient.execute(new AsyncHttpClient.HttpRequest(url, "GET", params));
                    JSONObject json = response.getContentAsJson();
                    VKUtil.checkErrors(url, json);
                    if (listener != null) {
                        listener.onResponse(json);
                    }
                } catch (VKException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError(e);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
         * Create new vk account and restore properties from sd
         */
        public VKAccount(File file) {
            this.restore(file);
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
                e.printStackTrace();
                return false;
            }
            return true;
        }

        /**
         * Restores account properties from SharedPreferences
         *
         * @return this account
         */
        public VKAccount restore() {
            this.accessToken = PrefManager.getString(ACCESS_TOKEN);
            this.userId = PrefManager.getLong(USER_ID);
            this.apiId = PrefManager.getInt(API_ID);
            this.email = PrefManager.getString(EMAIL);
            return this;
        }

        /**
         * Restores account properties from SD
         *
         * @return this account
         */
        public VKAccount restore(File file) {
            try {
                String readText = FileUtils.readFileToString(file);
                JSONObject json = new JSONObject(readText);

                this.accessToken = json.optString(ACCESS_TOKEN);
                this.email = json.optString(EMAIL);
                this.userId = json.optInt(USER_ID);
                this.apiId = json.optInt(EMAIL);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return this;
        }


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
         * http://vk.com/dev/users.get
         */
        public VKUserMethodSetter get() {
            return new VKUserMethodSetter(new VKRequest("users.get", new VKParams()));
        }

        /**
         * http://vk.com/dev/users.search
         */
        public VKUserSearchMethodSetter search() {
            return new VKUserSearchMethodSetter(new VKRequest("users.search", new VKParams()));
        }

        /**
         * http://vk.com/dev/users.isAppUser
         */
        public VKUserMethodSetter isAppUser() {
            return new VKUserMethodSetter(new VKRequest(("users.isAppUser"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.getSubscriptions
         */
        public VKUserMethodSetter getSubscriptions() {
            return new VKUserMethodSetter(new VKRequest(("users.getSubscriptions"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.getFollowers
         */
        public VKUserMethodSetter getFollowers() {
            return new VKUserMethodSetter(new VKRequest(("users.getFollowers"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.report
         */
        public VKUserMethodSetter report() {
            return new VKUserMethodSetter(new VKRequest(("users.report"), new VKParams()));
        }

        /**
         * http://vk.com/dev/users.getNearby
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
         */
        public VKMessageMethodSetter get() {
            return new VKMessageMethodSetter(new VKRequest(("messages.get"), new VKParams()));
        }

        /**
         * Returns the list of dialogs of the current user
         * <p/>
         * http://vk.com/dev/messages.getDialogs
         */
        public VKMessageMethodSetter getDialogs() {
            return new VKMessageMethodSetter(new VKRequest(("messages.getDialogs"), new VKParams()));
        }

        /**
         * Returns messages by their IDs
         * <p/>
         * http://vk.com/dev/messages.getById
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
         *
         * Error codes:
         * 900	Cannot send sticker to user from blacklist
         *
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

    }

    /**
     * Api methods for friends
     * <p/>
     * http://vk.com/dev/friends
     */
    public static class VKFriends {

        /**
         * Returns a list of user IDs or detailed information about a user's friends
         *
         * http://vk.com/dev/friends.get
         */
        public VKFriendsMethodSetter get() {
            return new VKFriendsMethodSetter(new VKRequest("friends.get"));
        }

        /**
         * Returns a list of user IDs of a user's friends who are online
         *
         * http://vk.com/dev/friends.getOnline
         */
        public VKFriendsMethodSetter getOnline() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getOnline"));
        }

        /**
         * Returns a list of user IDs of the mutual friends of two users
         *
         * http://vk.com/dev/friends.getMutual
         */
        public VKFriendsMethodSetter getMutual() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getMutual"));
        }

        /**
         * Returns a list of user IDs of the current user's recently added friends
         *
         * http://vk.com/dev/friends.getRecent
         */
        public VKFriendsMethodSetter getRecent() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getRecent"));
        }

        /**
         * Returns information about the current user's incoming and outgoing friend requests
         *
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
         *
         * Returns one of the following values:
         * 1 — Friend request sent.
         * 2 — Friend request from the user approved.
         * 4 — Request resending.
         *
         * Errors:
         * 174	Cannot add user himself as friend
         * 175	Cannot add this user to friends as they have put you on their blacklist
         * 176  Cannot add this user to friends as you put him on blacklist
         *
         * http://vk.com/dev/friends.add
         */
        public VKFriendsMethodSetter add() {
            return new VKFriendsMethodSetter(new VKRequest("friends.add"));
        }

        /**
         * Edits the friend lists of the selected user
         *
         * Result:
         * Returns 1.
         *
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
         *
         * Returns one of the following values:
         * 1 — User deleted from the current user's friend list.
         * 2 — Friend request from the user declined.
         * 3 — Friend request suggestion for the user deleted.
         *
         * Starting from version 5.28 returns object with fields:
         * success —             managed successfully remove other
         * friend_deleted —      has been removed each
         * out_request_deleted — cancelled outgoing request
         * in_request_deleted —  rejected the incoming request
         * suggestion_deleted —  rejected the recommendation of a friend
         *
         * http://vk.com/dev/friends.delete
         */
        public VKFriendsMethodSetter delete() {
            return new VKFriendsMethodSetter(new VKRequest("friends.delete"));
        }

        /**
         * Returns a list of the current user's friend lists
         *
         * Returns an array of list objects, each containing the following fields:
         * lid —  Friend list ID.
         * name — Friend list name.
         *
         * http://vk.com/dev/friends.getLists
         */
        public VKFriendsMethodSetter getLists() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getLists"));
        }

        /**
         * Creates a new friend list for the current user
         *
         * Returns the ID (lid) of the friend list that was created
         *
         * http://vk.com/dev/friends.addList
         */
        public VKFriendsMethodSetter addList() {
            return new VKFriendsMethodSetter(new VKRequest("friends.addList"));
        }

        /**
         * Edits a friend list of the current user.
         *
         * Returns 1.
         *
         * http://vk.com/dev/friends.editList
         */
        public VKFriendsMethodSetter editList() {
            return new VKFriendsMethodSetter(new VKRequest("friends.editList"));
        }

        /**
         * Deletes a friend list of the current user.
         *
         * Returns 1.
         *
         * http://vk.com/dev/friends.deleteList
         */
        public VKFriendsMethodSetter deleteList() {
            return new VKFriendsMethodSetter(new VKRequest("friends.deleteList"));
        }

        /**
         * Returns a list of IDs of the current user's friends
         * who installed the application.
         *
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
         *
         * Result:
         * For users whose validated phone numbers are in the list,
         * returns information as an array of user objects,
         * each containing a set of fields defined by the fields parameter.
         * The uid, first_name, last_name, and phone fields are always returned,
         * regardless of the selected fields.
         * The phone field of each object contains a phone number from
         * the list of phone numbers intended for search.
         *
         * http://vk.com/dev/friends.getByPhones
         */
        public VKFriendsMethodSetter getByPhones() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getByPhones"));
        }

        /**
         * Marks all incoming friend requests as viewed
         *
         * Result:
         * Returns 1
         *
         * http://vk.com/dev/friends.deleteAllRequests
         */
        public VKFriendsMethodSetter deleteAllRequests() {
            return new VKFriendsMethodSetter(new VKRequest("friends.deleteAllRequests"));
        }

        /**
         * Returns a list of profiles of users whom the current user may know.
         * For the method to return enough suggestions,
         * method account.importContacts will be called first.
         *
         * Result:
         * Returns an array of user objects,
         * each containing a set of fields defined by the fields parameter.
         * The uid, first_name, last_name, lists,
         * and online fields are always returned, regardless of the selected fields
         *
         * http://vk.com/dev/friends.getSuggestions
         */
        public VKFriendsMethodSetter getSuggestions() {
            return new VKFriendsMethodSetter(new VKRequest("friends.getSuggestions"));
        }

    }


    /**
     * Common setters, e.g. fields, user_ids, offset
     */
    public static class VKMethodSetter {
        protected VKRequest request;

        /**
         * Create new VKMethodSetter
         *
         * @param request the request which set params
         */
        public VKMethodSetter(VKRequest request) {
            this.request = request;
        }

        /**
         * User IDs or screen names (screen_name). By default, current user ID.
         */
        public VKMethodSetter userIds(Collection<Integer> uids) {
            this.request.params.put(VKConst.USER_IDS, VKUtil.arrayToString(uids));
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
         * Profile fields
         */
        public VKMethodSetter fields(String fields) {
            this.request.params.put(VKConst.FIELDS, fields);
            return this;
        }

        /**
         * Number of users/messages/audios... to return
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

        /** Setters for users.get */

        /**
         * Case for declension of user name and surname:
         * nom — nominative (default)
         * gen — genitive
         * dat — dative
         * acc — accusative
         * ins — instrumental
         * abl — prepositional
         */
        public VKUserMethodSetter nameCase(String nameCase) {
            this.request.params.put(VKConst.NAME_CASE, nameCase);
            return this;
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
            this.request.params.put(VKConst.FIELDS, filters);
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
         *
         * TODO: accessible for versions from 5.38
         */
        public VKMessageMethodSetter peerId(int peerId) {
            this.request.params.put(VKConst.PEER_ID, peerId);
            return this;
        }

        /**
         * Sort order:
         * 1 — return messages in chronological order.
         * 0 — return messages in reverse chronological order
         */
        public VKMessageMethodSetter rev(int rev) {
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
         * (Required if message is not set.)
         * List of objects attached to the message, separated by commas
         */
        public final VKMessageMethodSetter attachment(Collection<String> attachments) {
            this.request.params.put(VKConst.ATTACHMENT, VKUtil.arrayToString(attachments));
            return this;
        }


        /**
         * (Required if message is not set.)
         * List of objects attached to the message, separated by commas
         */
        public final VKMessageMethodSetter attachment(String... attachments) {
            this.request.params.put(VKConst.ATTACHMENT, VKUtil.arrayToString(attachments));
            return this;
        }

        /**
         * (Required if message is not set.)
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
            request.params.put(VKConst.LIST_IDS, VKUtil.arrayToString(new int[] { id }));
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
            request.params.put(VKConst.ADD_USER_IDS, VKUtil.arrayToString(new int[] { id }));
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
            request.params.put(VKConst.DELETE_USER_IDS, VKUtil.arrayToString(new int[] { id }));
            return this;
        }


        /** Setters for friends.getByPhones */

        /**
         * List of phone numbers in MSISDN format (maximum 1000).
         *
         * Example:
         * +79219876543,+79111234567
         */
        public VKFriendsMethodSetter phones(String... phones) {
            request.params.put(VKConst.PHONES, VKUtil.arrayToString(phones));
            return this;
        }

        /**
         * List of phone numbers in MSISDN format (maximum 1000).
         *
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

        /**
         * Execute request and convert to {@link JSONObject}
         */
        public JSONObject execute() {
            String url = getSignedUrl();
            AsyncHttpClient.HttpRequest request;

            request = new AsyncHttpClient.HttpRequest(url, isPost ? "POST" : "GET", null);

            AsyncHttpClient.HttpResponse response = null;
            try {
                response = getInstance().mClient.execute(request);
                if (response != null) {
                    try {
                        return new JSONObject(response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (AsyncHttpClient.HttpResponseCodeException e) {
                e.printStackTrace();
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

        /** Friends */
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

        /** Others */
        public static final String CODE = "code";

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
         * In some cases, Vkontakte requires passing a validation procedure of the user,
         * resulting in since version 5.0 API
         * (for older versions will be prompted captcha_error)
         * any request to API the following error is returned
         * <p/>
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
     * Numeric status codes of errors
     * See website http://vk.com/dev/errors
     */
    public static class VKErrorCodes {
        private VKErrorCodes() {
            // empty
        }

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

        /** Message errors */

        public static final int CANNOT_SEND_MESSAGE_BLACK_LIST = 900;
        public static final int CANNOT_SEND_MESSAGE_GROUP = 901;

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
         * Gets all permissions as String
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
