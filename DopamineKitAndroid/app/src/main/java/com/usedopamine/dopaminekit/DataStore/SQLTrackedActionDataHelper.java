package com.usedopamine.dopaminekit.DataStore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.usedopamine.dopaminekit.DataStore.Contracts.TrackedActionContract;
import com.usedopamine.dopaminekit.DopeAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuddergambino on 8/9/16.
 */

public class SQLTrackedActionDataHelper {

    public static int count(Context context) {
        SQLiteDatabase db = SQLiteDataStore.getInstance(context).getReadableDatabase();
        Cursor mCount= db.rawQuery("select count(*) from " + TrackedActionContract.TrackedActionEntry.TABLE_NAME, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        return count;
    }

    public static void insert(TrackedActionContract.TrackedActionEntry item) {

    }

    public static List<TrackedActionContract> findAll(Context context) {
        List<TrackedActionContract> actions = new ArrayList<>();

        SQLiteDatabase db = SQLiteDataStore.getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TrackedActionContract.TrackedActionEntry.TABLE_NAME, null);
        if (cursor.moveToFirst() ) {
            do {
                TrackedActionContract action = TrackedActionContract.fromCursor(cursor);
                actions.add(action);
            } while (cursor.moveToNext());
        }

        return actions;
    }
}
