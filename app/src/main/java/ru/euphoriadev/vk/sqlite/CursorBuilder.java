package ru.euphoriadev.vk.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Igor on 08.03.16.
 */
public class CursorBuilder {
    private static final String TAG = "CursorBuilder";
    private StringBuffer buffer;

    public CursorBuilder() {
        buffer = new StringBuffer();
    }

    public static CursorBuilder create() {
        return new CursorBuilder();
    }

    public CursorBuilder select() {
        buffer.append("SELECT ");
        return this;
    }

    public CursorBuilder all() {
        buffer.append("* ");
        return this;
    }

    public CursorBuilder from(String table) {
        buffer.append("FROM ").append(table).append(" ");
        return this;
    }

    public CursorBuilder where(String whereClause) {
        buffer.append("WHERE ").append(whereClause).append(" ");
        return this;
    }

    public CursorBuilder where(String column, Object value) {
        return where(String.format("%s = %s", column, value));
    }

    public CursorBuilder selectAllFrom(String table) {
        return select().all().from(table);
    }

    public Cursor cursor(SQLiteDatabase database) {
        return database.rawQuery(toString(), null);
    }

    public CursorBuilder printSql() {
        Log.w(TAG, "SQL command: ".concat(toString()));
        return this;
    }

    @Override
    public String toString() {
        return buffer.toString().trim();
    }
}
