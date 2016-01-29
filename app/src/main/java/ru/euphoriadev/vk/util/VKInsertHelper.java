package ru.euphoriadev.vk.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.euphoriadev.vk.api.VKApi;
import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;

/**
 * Created by Igor on 22.01.16.
 * <p/>
 * This allows you to easily insert data into the database
 */
public class VKInsertHelper {
    /** Will use a single object for inserting to save memory */
    public static final ContentValues sValues = new ContentValues(16);


    /**
     * Insert one dialog into SQLite database
     *
     * @param database the {@link SQLiteDatabase} to insert message it
     * @param dialog   dialog to insert into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public static long insertDialog(SQLiteDatabase database, VKMessage dialog) {
        prepareContentValuesForDialog(dialog);
        return database.insert(DBHelper.DIALOGS_TABLE, null, sValues);
    }

    /**
     * Insert dialogs to SQLite database with use transaction
     *
     * @param database the {@link SQLiteDatabase} to insert messages it
     * @param dialog   a list of dialogs to insert into the database
     */
    public static void insertDialogs(SQLiteDatabase database, ArrayList<VKMessage> dialog) {
        insertDialogs(database, dialog, false);
    }

    /**
     * Insert dialogs to SQLite database with use transaction
     *
     * @param database the {@link SQLiteDatabase} to insert messages it
     * @param dialogs  a list of dialogs to insert into the database
     */
    public static void insertDialogs(SQLiteDatabase database, ArrayList<VKMessage> dialogs, boolean useTransaction) {
        checkOpen(database);
        if (checkIsEmpty(dialogs)) {
            return;
        }

        if (useTransaction) database.beginTransaction();
        for (int i = 0; i < dialogs.size(); i++) {
            insertDialog(database, dialogs.get(i));
        }
        sValues.clear();
        if (useTransaction) {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }


    /**
     * Insert one message into SQLite database
     *
     * @param database the {@link SQLiteDatabase} to insert message it
     * @param message  messages to insert into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public static long insertMessage(SQLiteDatabase database, VKMessage message) {
        prepareContentValuesForMessage(message);
        return database.insert(DBHelper.MESSAGES_TABLE, null, sValues);
    }


    /**
     * Insert messages to SQLite database with use transaction
     *
     * @param database the {@link SQLiteDatabase} to insert messages it
     * @param messages a list of messages to insert into the database
     */
    public static void insertMessages(SQLiteDatabase database, ArrayList<VKMessage> messages) {
        insertMessages(database, messages, false);
    }

    /**
     * Insert messages to SQLite database
     *
     * @param database    the {@link SQLiteDatabase} to insert messages it
     * @param messages    a list of messages to insert into the database
     * @param transaction true to use transactions, speeds up the inserting
     * @see SQLiteDatabase#beginTransaction()
     */
    public static void insertMessages(SQLiteDatabase database, ArrayList<VKMessage> messages, boolean transaction) {
        checkOpen(database);
        if (checkIsEmpty(messages)) {
            return;
        }

        if (transaction) database.beginTransaction();
        for (VKMessage msg : messages) {
            insertMessage(database, msg);
        }
        sValues.clear();

        if (transaction) {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    /**
     * Insert one user into SQLite database
     *
     * @param database the {@link SQLiteDatabase} to insert message it
     * @param user     user to insert into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public static long insertUser(SQLiteDatabase database, VKUser user) {
        prepareContentValuesFor(user);
        return database.insert(DBHelper.USERS_TABLE, null, sValues);
    }

    /**
     * Insert users to SQLite database with use transactions
     *
     * @param database the {@link SQLiteDatabase} to insert messages it
     * @param users    a list of users to insert into the database
     */
    public static void insertUsers(SQLiteDatabase database, List<VKUser> users) {
        insertUsers(database, users, true);
    }

    /**
     * Insert users to SQLite database
     *
     * @param database       the {@link SQLiteDatabase} to insert messages it
     * @param users          a list of users to insert into the database
     * @param useTransaction true to use transactions, speeds up the inserting
     * @see SQLiteDatabase#beginTransaction()
     */
    public static void insertUsers(SQLiteDatabase database, List<VKUser> users, boolean useTransaction) {
        checkOpen(database);
        if (checkIsEmpty(users)) {
            return;
        }

        if (useTransaction) database.beginTransaction();
        for (int i = 0; i < users.size(); i++) {
            insertUser(database, users.get(i));
        }
        sValues.clear();
        if (useTransaction) {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    /**
     * Insert or update users to database
     *
     * @param database the {@link SQLiteDatabase} to insert/update it
     * @param user     the user to insert/update into database
     * @return the number of rows affected,
     */
    public static long updateUser(SQLiteDatabase database, VKUser user) {
        prepareContentValuesFor(user);
        int updateRows = database.update(DBHelper.USERS_TABLE, sValues, DBHelper.USER_ID + " = " + user.user_id, null);
        if (updateRows <= 0) {
            return database.insert(DBHelper.USERS_TABLE, null, sValues);
        }
        return updateRows;
    }

    /**
     * Insert or update users to database with use transactions
     *
     * @param database the {@link SQLiteDatabase} to update users it
     * @param users    a list of users to update into the database
     */
    public static void updateUsers(SQLiteDatabase database, List<VKUser> users) {
        updateUsers(database, users, true);
    }

    /**
     * Insert or update users to SQLite database
     *
     * @param database       the {@link SQLiteDatabase} to insert messages it
     * @param users          a list of users to insert/update into database
     * @param useTransaction true to use transactions, for speeds up inserting
     * @see SQLiteDatabase#beginTransaction()
     */
    public static void updateUsers(SQLiteDatabase database, List<VKUser> users, boolean useTransaction) {
        checkOpen(database);
        if (checkIsEmpty(users)) {
            return;
        }

        if (useTransaction) database.beginTransaction();
        for (int i = 0; i < users.size(); i++) {
            updateUser(database, users.get(i));
        }
        sValues.clear();
        if (useTransaction) {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    private static void prepareContentValuesForMessage(VKMessage message) {
        sValues.put(DBHelper.MESSAGE_ID, message.mid);
        sValues.put(DBHelper.USER_ID, message.uid);
        sValues.put(DBHelper.CHAT_ID, message.chat_id);
        sValues.put(DBHelper.BODY, message.body);
        sValues.put(DBHelper.DATE, message.date);
        sValues.put(DBHelper.READ_STATE, message.read_state);
        sValues.put(DBHelper.IS_OUT, message.is_out);
        sValues.put(DBHelper.IMPORTANT, message.is_important);
    }

    private static void prepareContentValuesFor(VKUser user) {
        sValues.put(DBHelper.USER_ID, user.user_id);
        sValues.put(DBHelper.FIRST_NAME, user.first_name);
        sValues.put(DBHelper.LAST_NAME, user.last_name);
        sValues.put(DBHelper.ONLINE, user.online);
        sValues.put(DBHelper.ONLINE_MOBILE, user.online_mobile);
        sValues.put(DBHelper.STATUS, user.status);
        sValues.put(DBHelper.PHOTO_50, user.photo_50);
        sValues.put(DBHelper.PHOTO_100, user.photo_100);
        sValues.put(DBHelper.PHOTO_200, user.photo_200);
    }

    private static void prepareContentValuesForDialog(VKMessage message) {
        sValues.put(DBHelper.USER_ID, message.uid);
        sValues.put(DBHelper.CHAT_ID, message.chat_id);
        sValues.put(DBHelper.TITLE, message.title);
        sValues.put(DBHelper.BODY, message.body);
        sValues.put(DBHelper.IS_OUT, message.is_out);
        sValues.put(DBHelper.READ_STATE, message.read_state);
        sValues.put(DBHelper.USERS_COUNT, message.users_count);
        sValues.put(DBHelper.UNREAD_COUNT, message.unread);
        sValues.put(DBHelper.DATE, message.date);
        sValues.put(DBHelper.PHOTO_50, message.photo_50);
        sValues.put(DBHelper.PHOTO_100, message.photo_100);
    }

    /**
     * Check, open the database for writing data. If not - get writable database
     */
    private static void checkOpen(SQLiteDatabase database) {
        sValues.clear();
        AndroidUtils.checkDatabase(AppLoader.appContext, database);
    }

    private static boolean checkIsEmpty(List list) {
        return VKApi.VKUtil.isEmpty(list);
    }
}
