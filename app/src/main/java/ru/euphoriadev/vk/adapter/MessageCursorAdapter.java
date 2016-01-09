package ru.euphoriadev.vk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.euphoriadev.vk.api.model.VKMessage;
import ru.euphoriadev.vk.api.model.VKUser;
import ru.euphoriadev.vk.helper.DBHelper;

import java.util.ArrayList;

/**
 * Created by Igor on 23.10.15.
 */
public class MessageCursorAdapter extends MessageAdapter {

    private SQLiteDatabase database;
    private Cursor mCursor;
    private MessageItem mMessageItem;
    private VKMessage mMessage;
    private VKUser mUser;
    private String mSql;
    private long chat_id;

    public MessageCursorAdapter(Context context, ArrayList<MessageItem> messages, long uid, long chat_id) {
        super(context, messages, uid, chat_id);
    }

    public MessageCursorAdapter(Context context, Cursor cursor, String sql, long chat_id, long user_id) {
        this(context, null, user_id, chat_id);

        this.mCursor = cursor;
        this.mSql = sql;
        this.chat_id = chat_id;
    }


    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public MessageItem getItem(int position) {
//        MessageItem cacheMessage = mSparseCache.get;
//        if (cacheMessage != null) {
//            return cacheMessage;
//        }
        mCursor.moveToPosition(position);
        String body = mCursor.getString(4);
        String photo = chat_id != 0 ? mCursor.getString(mCursor.getColumnIndex(DBHelper.PHOTO_50)) : null;
        String firstName = chat_id != 0 ? mCursor.getString(mCursor.getColumnIndex(DBHelper.FIRST_NAME)) : null;
        String lastName = chat_id != 0 ? mCursor.getString(mCursor.getColumnIndex(DBHelper.LAST_NAME)) : null;
        int out = mCursor.getInt(7);
        int read_state = mCursor.getInt(6);
        int date = mCursor.getInt(5);
        int mid = mCursor.getInt(1);

        if (mMessage == null) {
            mMessage = new VKMessage();
        }
        if (mUser == null) {
            mUser = new VKUser();
        }

        mMessage.mid = mid;
        mMessage.chat_id = chat_id;
        mMessage.date = date;
        mMessage.body = body;
        mMessage.is_out = out == 1;
        mMessage.read_state = read_state == 1;


        if (firstName != null || lastName != null) {
            mUser.photo_50 = photo;
            mUser.first_name = firstName;
            mUser.last_name = lastName;
        }
        //        mSparseCache.append(position, messageItem);
        if (mMessageItem == null) {
            mMessageItem = new MessageItem(mMessage, mUser);
        }
        return mMessageItem.init(mMessage, mUser);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void close() {
        if (!mCursor.isClosed()) {
            mCursor.close();
        }
    }



    @Override
    public void notifyDataSetChanged() {
        mCursor.close();
        mCursor = database.rawQuery(mSql, null);
        super.notifyDataSetChanged();
    }

    public VKUser getUserFromDB(long uid) {
        if (database == null || !database.isOpen()) {
            database = DBHelper.get(getContext()).getWritableDatabase();
        }

        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USERS_TABLE + " WHERE " + DBHelper.USER_ID + " = " + uid, null);
        VKUser user = null;
        if (cursor.getCount() != 0)
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String fistName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String photo50 = cursor.getString(8);

                user = new VKUser();
                user.user_id = id;
                user.first_name = fistName;
                user.last_name = lastName;
                user.photo_50 = photo50;
            }
        cursor.close();
        return user;
    }
}
