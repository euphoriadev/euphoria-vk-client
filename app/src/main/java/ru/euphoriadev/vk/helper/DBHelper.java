package ru.euphoriadev.vk.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.util.VKInsertHelper;

/**
 * Created by Igor on 06.03.15.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "euphoria.db";
    private static final int DATABASE_VERSION = 82;

    /** Tables */
    public static final String USERS_TABLE = "users";
    public static final String FRIENDS_TABLE = "friends";
    public static final String DIALOGS_TABLE = "dialogs";
    public static final String MESSAGES_TABLE = "messages";
    public static final String SAVED_MESSAGES_TABLE = "saved_messages";
    public static final String AUDIOS_TABLE = "audios";
    public static final String DOCS_TABLE = "docs";
    public static final String GROUPS_TABLE = "groups";
    public static final String USER_GROUP_TABLE = "user_group";
    public static final String STATS_MESSAGES_TABLE= "stats_messages";
    public static final String FAILED_MESSAGES_TABLE= "failed_messages";

    /** Columns */
    public static final String _ID = "_id";
    public static final String USER_ID = "user_id";
    public static final String OWNER_ID = "owner_id";
    public static final String GROUP_ID = "group_id";
    public static final String FRIEND_ID = "friend_id";
    public static final String AUDIO_ID = "audio_id";
    public static final String CHAT_ID = "chat_id";
    public static final String LYRICS_ID = "lyrics_id";
    public static final String MESSAGE_ID = "message_id";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String TOTAL_COUNT = "total_count";
    public static final String INCOMING_COUNT = "incoming_count";
    public static final String OUTGOING_COUNT = "outgoing_count";
    public static final String IMPORTANT = "important";
    public static final String USERS_COUNT = "users_count";
    public static final String IS_OUT = "is_out";
    public static final String READ_STATE = "read_state";
    public static final String DATE = "date";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String NICKNAME = "nickname";
    public static final String ONLINE = "online_status";
    public static final String ONLINE_MOBILE = "online_mobile";
    public static final String STATUS = "status";
    public static final String IS_FRIEND = "is_friend";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String PHOTO_400 = "photo_400";
    public static final String PHOTO_MAX = "photo_max";
    public static final String POSITION_DECIMAL = "position";
    public static final String LAST_SEEN = "last_seen";
    public static final String ARTIST = "artist";
    public static final String DURATION = "duration";
    public static final String URL = "url";
    public static final String SIZE = "size";
    public static final String EXT = "ext";
    public static final String NAME = "name";
    public static final String SCREEN_NAME = "screen_name";
    public static final String IS_CLOSED = "is_closed";
    public static final String IS_ADMIN = "is_admin";
    public static final String ADMIN_LEVER = "admin_level";
    public static final String TYPE = "type";
    public static final String DESCRIPTION = "description";
    public static final String MEMBERS_COUNT = "members_count";


    private static final String SQL_CREATE_TABLE_USERS = "CREATE TABLE " + USERS_TABLE +
            " (" + USER_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, " +
            " [" + FIRST_NAME + "] VARCHAR(255), " +
            " [" + LAST_NAME + "] VARCHAR(255), " +
            " [" + NICKNAME + "] VARCHAR(255), " +
            " [" + ONLINE + "] INTEGER VARCHAR(255), " +
            " [" + ONLINE_MOBILE + "] INTEGER VARCHAR(255), " +
            " [" + STATUS + "] VARCHAR(255), " +
            " [" + IS_FRIEND + "] VARCHAR(255), " +
            " [" + LAST_SEEN + "] INTEGER VARCHAR(255), " +
            " [" + PHOTO_50 + "] VARCHAR(255), " +
            " [" + PHOTO_100 + "] VARCHAR(255), " +
            " [" + PHOTO_200 + "] VARCHAR(255), " +
            " [" + PHOTO_400 + "] VARCHAR(255), " +
            " [" + PHOTO_MAX + "] VARCHAR(255), " +
            " [" + POSITION_DECIMAL + "] DECIMAL" +
            ");";
    private static final String SQL_CREATE_TABLE_FRIENDS = "CREATE TABLE " + FRIENDS_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " [" + USER_ID + "] INTEGER, " +
            " [" + FRIEND_ID + "] INTEGER " +
            ");";
    private static final String SQL_CREATE_TABLE_DIALOGS = "CREATE TABLE " + DIALOGS_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " [" + MESSAGE_ID + "] INTEGER, " +
            " [" + USER_ID + "] INTEGER, " +
            " [" + CHAT_ID + "] INTEGER, " +
            " [" + TITLE + "] VARCHAR(255), " +
            " [" + BODY + "] VARCHAR(255), " +
            " [" + IS_OUT + "] INTEGER, " +
            " [" + READ_STATE + "] INTEGER, " +
            " [" + USERS_COUNT + "] INTEGER, " +
            " [" + UNREAD_COUNT + "] INTEGER, " +
            " [" + DATE + "] INTEGER , " +
            " [" + PHOTO_50 + "] VARCHAR(255), " +
            " [" + PHOTO_100 + "] VARCHAR(255)" +
            ");";
    private final static String SQL_CREATE_TABLE_MESSAGES = "CREATE TABLE " + MESSAGES_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY, " +
            " [" + MESSAGE_ID + "] INTEGER, " +
            " [" + USER_ID + "] INTEGER, " +
            " [" + CHAT_ID + "] INTEGER, " +
            " [" + BODY + "] VARCHAR(255), " +
            " [" + DATE + "] INTEGER, " +
            " [" + READ_STATE + "] INTEGER, " +
            " [" + IS_OUT + "] INTEGER, " +
            " [" + IMPORTANT + "] INTEGER" +
            ");";
    private final static String SQL_CREATE_TABLE_SAVED_MESSAGES = "CREATE TABLE " + SAVED_MESSAGES_TABLE + "_%1$s" +
            " (" + _ID + " INTEGER PRIMARY KEY, " +
            " [" + MESSAGE_ID + "] INTEGER, " +
            " [" + USER_ID + "] INTEGER, " +
            " [" + CHAT_ID + "] INTEGER, " +
            " [" + BODY + "] VARCHAR(255), " +
            " [" + DATE + "] INTEGER, " +
            " [" + READ_STATE + "] INTEGER, " +
            " [" + IS_OUT + "] INTEGER, " +
            " [" + IMPORTANT + "] INTEGER" +
            ");";
    private final static String SQL_CREATE_TABLE_STATS_MESSAGES = "CREATE TABLE " + STATS_MESSAGES_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY, " +
            " [" + USER_ID + "] INTEGER, " +
            " [" + CHAT_ID + "] INTEGER, " +
            " [" + TOTAL_COUNT + "] INTEGER, " +
            " [" + INCOMING_COUNT + "] INTEGER, " +
            " [" + OUTGOING_COUNT + "] INTEGER" +
            ");";
    private final static String SQL_CREATE_TABLE_AUDIOS = "CREATE TABLE " + AUDIOS_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " [" + AUDIO_ID + "] INTEGER, " +
            " [" + OWNER_ID + "] INTEGER, " +
            " [" + ARTIST + "] VARCHAR(255), " +
            " [" + TITLE + "] VARCHAR(255), " +
            " [" + DURATION + "] INTEGER, " +
            " [" + URL + "] VARCHAR(255), " +
            " [" + LYRICS_ID + "] INTEGER " +
            ");";
    private final static String SQL_CREATE_TABLE_DOCS = "CREATE TABLE " + DOCS_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY, " +
            " [" + OWNER_ID + "] INTEGER, " +
            " [" + TITLE + "] VARCHAR(255), " +
            " [" + SIZE + "] INTEGER, " +
            " [" + EXT + "] VARCHAR(255), " +
            " [" + URL + "] VARCHAR(255), " +
            " [" + PHOTO_100 + "] VARCHAR(255) " +
            ");";
    private final static String SQL_CREATE_TABLE_GROUPS = "CREATE TABLE " +  GROUPS_TABLE +
            " (" + GROUP_ID + " INTEGER PRIMARY KEY, " +
            " [" + NAME + "] VARCHAR(255), " +
            " [" + SCREEN_NAME + "] VARCHAR(255), " +
            " [" + DESCRIPTION + "] VARCHAR(255), " +
            " [" + STATUS + "] VARCHAR(255), " +
            " [" + TYPE + "] INTEGER, " +
            " [" + IS_CLOSED + "] INTEGER, " +
            " [" + IS_ADMIN + "] INTEGER, " +
            " [" + ADMIN_LEVER + "] INTEGER, " +
            " [" + PHOTO_50 + "] VARCHAR(255), " +
            " [" + PHOTO_100 + "] VARCHAR(255), " +
            " [" + MEMBERS_COUNT + "] INTEGER " +
            ");";
    private final static String SQL_CREATE_TABLE_USER_GROUP = "CREATE TABLE " + USER_GROUP_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " [" + GROUP_ID + "] INTEGER, " +
            " [" + USER_ID + "] INTEGER " +
            ");";
    private final static String SQL_CREATE_TABLE_FAILED_MESSAGES = "CREATE TABLE " + FAILED_MESSAGES_TABLE +
            " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " [" + USER_ID + "] INTEGER, " +
            " [" + CHAT_ID + "] INTEGER, " +
            " [" + BODY + "] VARCHAR(255)" +
            ");";
    private static final String SQL_DELETE_DOCS = "DROP TABLE IF EXISTS " + DOCS_TABLE;
    private static final String SQL_DELETE_USERS = "DROP TABLE IF EXISTS " + USERS_TABLE;
    private static final String SQL_DELETE_AUDIOS = "DROP TABLE IF EXISTS " + AUDIOS_TABLE;
    private static final String SQL_DELETE_GROUPS = "DROP TABLE IF EXISTS " + GROUPS_TABLE;
    private static final String SQL_DELETE_FRIENDS = "DROP TABLE IF EXISTS " + FRIENDS_TABLE;
    private static final String SQL_DELETE_DIALOGS = "DROP TABLE IF EXISTS " + DIALOGS_TABLE;
    private static final String SQL_DELETE_MESSAGES = "DROP TABLE IF EXISTS " + MESSAGES_TABLE;
    private static final String SQL_DELETE_USER_GROUPS = "DROP TABLE IF EXISTS " + USER_GROUP_TABLE;
    private static final String SQL_DELETE_SAVED_MESSAGES = "DROP TABLE IF EXISTS " + SAVED_MESSAGES_TABLE;
    private static final String SQL_DElETE_STATS_MESSAGES = "DROP TABLE IF EXISTS " + STATS_MESSAGES_TABLE;
    private static final String SQL_DELETE_FAILED_MESSAGES = "DROP TABLE IF EXISTS " + FAILED_MESSAGES_TABLE;
    private static volatile DBHelper mHelper;
    private SQLiteDatabase mDatabase;
    private Context mContext;


    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static DBHelper get(Context context) {
        synchronized (DBHelper.class) {
            if (mHelper == null) {
                mHelper = new DBHelper(context);
            }
        }
        return mHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_DOCS);
        db.execSQL(SQL_CREATE_TABLE_USERS);
        db.execSQL(SQL_CREATE_TABLE_GROUPS);
        db.execSQL(SQL_CREATE_TABLE_AUDIOS);
        db.execSQL(SQL_CREATE_TABLE_DIALOGS);
        db.execSQL(SQL_CREATE_TABLE_FRIENDS);
        db.execSQL(SQL_CREATE_TABLE_MESSAGES);
        db.execSQL(SQL_CREATE_TABLE_USER_GROUP);
        db.execSQL(SQL_CREATE_TABLE_STATS_MESSAGES);
        db.execSQL(SQL_CREATE_TABLE_FAILED_MESSAGES);
        Log.w("SQLite", "Tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);

        dropTables(db);
        onCreate(db);
    }

    public void dropTables(SQLiteDatabase db) {
        // Удаляем старые таблици
        db.execSQL(SQL_DELETE_DOCS);
        db.execSQL(SQL_DELETE_USERS);
        db.execSQL(SQL_DELETE_AUDIOS);
        db.execSQL(SQL_DELETE_GROUPS);
        db.execSQL(SQL_DELETE_FRIENDS);
        db.execSQL(SQL_DELETE_DIALOGS);
        db.execSQL(SQL_DELETE_MESSAGES);
        db.execSQL(SQL_DELETE_USER_GROUPS);
        db.execSQL(SQL_DELETE_SAVED_MESSAGES);
        db.execSQL(SQL_DElETE_STATS_MESSAGES);
        db.execSQL(SQL_DELETE_FAILED_MESSAGES);
    }

    public void createTableSavedMessages(SQLiteDatabase db, long uid) {
        db.execSQL(String.format(SQL_CREATE_TABLE_SAVED_MESSAGES, String.valueOf(uid)));
    }

    public void open() {
        getWritableDatabase();
    }


    public boolean exists(SQLiteDatabase db, String table) {
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + table, null);
            cursor.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void addUserToDB(VKUser profile) {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = DBHelper.get(mContext).getWritableDatabase();
        }
        VKInsertHelper.clearValues();
        VKInsertHelper.updateUser(mDatabase, profile);
    }

    public void addMessageToDB(VKMessage message) {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = DBHelper.get(mContext).getWritableDatabase();
        }
        VKInsertHelper.clearValues();
        VKInsertHelper.insertMessage(mDatabase, message);
    }

    public VKUser getUserFromDB(long uid) {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = getWritableDatabase();
        }
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + DBHelper.USERS_TABLE + " WHERE " + DBHelper.USER_ID + " = " + uid, null);
        VKUser user = VKUser.EMPTY_USER;
        if (cursor.getCount() != 0)
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String fistName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String photo50 = cursor.getString(9);

                user = new VKUser();
                user.user_id = id;
                user.first_name = fistName;
                user.last_name = lastName;
                user.photo_50 = photo50;
            }
        cursor.close();
        return user;
    }

    @Override
    public synchronized void close() {
        super.close();
        Log.w(getClass().getSimpleName(), "Database closed...");
    }
}
