package ru.euphoriadev.vk.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import ru.euphoriadev.vk.api.Api;
import ru.euphoriadev.vk.api.model.VKDocument;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.common.AppLoader;
import ru.euphoriadev.vk.helper.DBHelper;
import ru.euphoriadev.vk.util.ArrayUtil;

/**
 * Created by Igor on 19.03.16.
 */
public class VKSqliteHelper {
    private static final String TAG = "VKSqliteHelper";

    public static ArrayList<VKUser> getAllUsers(SQLiteDatabase database) {
        Cursor cursor = CursorBuilder.create().selectAllFrom(DBHelper.USERS_TABLE).cursor(database);
        ArrayList<VKUser> users = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                VKUser user = userForRow(cursor);
                users.add(user);
            }
        }
        cursor.close();
        return users;
    }

    public static ArrayList<VKUser> getUsers(SQLiteDatabase database, int... ids) {
        Cursor cursor = CursorBuilder.create()
                .selectAllFrom(DBHelper.USERS_TABLE)
                .cursor(database);

        ArrayList<VKUser> users = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() <= 0) {
            // cursor is empty
            return users;
        }

        int modCount = 0;
        while (cursor.moveToNext()) {
            if (modCount >= ids.length) {
                // find all users
                break;
            }
            int id = cursor.getInt(0);
            if (ArrayUtil.contains(ids, id)) {
//              if ids array contains specified id
                VKUser user = userForRow(cursor);
                users.add(user);
                modCount++;
            }
        }
        cursor.close();
        return users;
    }

    public static ArrayList<VKUser> getAllFriends(SQLiteDatabase database) {
        Cursor cursor = CursorBuilder.create().
                selectAllFrom(DBHelper.FRIENDS_TABLE)
                .where(DBHelper.USER_ID, Api.get().getUserId())
                .cursor(database);

        ArrayList<Integer> friendIds = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(2);
                friendIds.add(id);
            }
        }
        cursor.close();

        ArrayList<VKUser> friends = new ArrayList<>(friendIds.size());
        for (int i = 0; i < friendIds.size(); i++) {
            VKUser user = getUser(database, friendIds.get(i));
            if (user != null) {
                friends.add(user);
            }
        }
        return friends;

    }

    public static VKUser getUser(SQLiteDatabase database, int userId) {
        Cursor cursor = CursorBuilder.create()
                .selectAllFrom(DBHelper.USERS_TABLE)
                .where(DBHelper.USER_ID, userId)
                .cursor(database);

        VKUser user = null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                user = userForRow(cursor);
                if (user != null) {
                    break;
                }
            }
        }
        cursor.close();
        return user;
    }

    public static ArrayList<VKDocument> getDocs(SQLiteDatabase database) {
        Cursor cursor = CursorBuilder.create().
                selectAllFrom(DBHelper.DOCS_TABLE).
                cursor(database);
        ArrayList<VKDocument> docs = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                VKDocument doc = docForRow(cursor);
                docs.add(doc);
            }
        }
        cursor.close();
        return docs;
    }

    public static VKUser userForRow(Cursor cursor) {
        VKUser user = new VKUser();
        user.user_id = cursor.getInt(0);
        user.first_name = cursor.getString(1);
        user.last_name = cursor.getString(2);
        user.screen_name = cursor.getString(3);
        user.status = cursor.getString(7);
        user.photo_50 = cursor.getString(10);
        user.photo_100 = cursor.getString(11);
        user.photo_200 = cursor.getString(12);

        user.online = cursor.getInt(cursor.getColumnIndex(DBHelper.ONLINE)) == 1;
        user.online_mobile = cursor.getInt(cursor.getColumnIndex(DBHelper.ONLINE_MOBILE)) == 1;
        return user;
    }

    public static VKDocument docForRow(Cursor cursor) {
        VKDocument doc = new VKDocument();
        doc.id = cursor.getInt(1);
        doc.owner_id = cursor.getInt(2);
        doc.size = cursor.getInt(4);
        doc.type = cursor.getInt(5);

        doc.title = cursor.getString(3);
        doc.ext = cursor.getString(6);
        doc.url = cursor.getString(7);
        doc.photo_100 = cursor.getString(8);
        doc.photo_130 = cursor.getString(9);
        return doc;
    }

    public static void test() {
        log("------ SQLite Helper test ------");
        SQLiteDatabase database = DBHelper.get(AppLoader.appContext).getWritableDatabase();
        log("database is open? " + database.isOpen());

        VKUser user = getUser(database, Api.get().getUserId());
        log("getUser by my id: " + user.toString());

        ArrayList<VKUser> users = getUsers(database, 17791724, 138479323, 185957061);
        log("getUserS by Lev, Victor and Roman ids: " + Arrays.toString(users.toArray()));

        final ArrayList<VKUser> allUsers = getAllUsers(database);
        log("getAllUsers, size " + allUsers.size() + ": " + Arrays.toString(allUsers.toArray()));

        ArrayList<VKUser> friends = getAllFriends(database);
        log("getAllFriends, size " + friends.size() + ": " + Arrays.toString(friends.toArray()));

        log("------ SQLite Helper test is FINISHED ------\n");
        log(" ");
    }

    private static void log(String message) {
        Log.w(TAG, message);
    }
}
