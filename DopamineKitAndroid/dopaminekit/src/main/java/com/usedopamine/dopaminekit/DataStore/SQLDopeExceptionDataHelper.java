package com.usedopamine.dopaminekit.DataStore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.DopeExceptionContract;

import java.util.ArrayList;

/**
 * Created by cuddergambino on 10/3/16.
 */

public class SQLDopeExceptionDataHelper extends SQLDataHelper {

    static void createTable(SQLiteDatabase db) {
        db.execSQL(DopeExceptionContract.SQL_CREATE_TABLE);
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL(DopeExceptionContract.SQL_DROP_TABLE);
    }

    public static long insert(SQLiteDatabase db, DopeExceptionContract item) {
        ContentValues values = new ContentValues();
        values.put(DopeExceptionContract.COLUMNS_NAME_UTC, item.utc);
        values.put(DopeExceptionContract.COLUMNS_NAME_TIMEZONEOFFSET, item.timezoneOffset);
        values.put(DopeExceptionContract.COLUMNS_NAME_EXCEPTIONCLASSNAME, item.exceptionClassName);
        values.put(DopeExceptionContract.COLUMNS_NAME_MESSAGE, item.message);
        values.put(DopeExceptionContract.COLUMNS_NAME_STACKTRACE, item.stackTrace);

        item.id = db.insert(DopeExceptionContract.TABLE_NAME, null, values);
        return item.id;
    }

    public static void delete(SQLiteDatabase db, DopeExceptionContract item) {
        String selection = DopeExceptionContract._ID + " LIKE ? ";
        String[] args = {String.valueOf(item.id)};
        db.delete(DopeExceptionContract.TABLE_NAME, selection, args);
    }

    @Nullable
    public static DopeExceptionContract find(SQLiteDatabase db, DopeExceptionContract item) {
        DopeExceptionContract result = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DopeExceptionContract.TABLE_NAME,
                    new String[] {DopeExceptionContract._ID, DopeExceptionContract.COLUMNS_NAME_UTC, DopeExceptionContract.COLUMNS_NAME_TIMEZONEOFFSET, DopeExceptionContract.COLUMNS_NAME_EXCEPTIONCLASSNAME, DopeExceptionContract.COLUMNS_NAME_MESSAGE, DopeExceptionContract.COLUMNS_NAME_STACKTRACE},
                    DopeExceptionContract._ID + "=?",
                    new String[] {String.valueOf(item.id) }, null, null, null, null
            );
            result = cursor.moveToFirst() ? DopeExceptionContract.fromCursor(cursor) : null;
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return result;
    }

    public static ArrayList<DopeExceptionContract> findAll(SQLiteDatabase db) {
        ArrayList<DopeExceptionContract> dopeExceptions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DopeExceptionContract.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                do {
                    dopeExceptions.add( DopeExceptionContract.fromCursor(cursor) );
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return dopeExceptions;
    }

    public static int count(SQLiteDatabase db) {
        int result = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DopeExceptionContract.TABLE_NAME, null);
            if (cursor.moveToFirst()) { result = cursor.getInt(0); }
        } finally {
            if(cursor != null) { cursor.close(); }
        }
        return result;
    }
}
