package com.usedopamine.dopaminekit.DataStore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.usedopamine.dopaminekit.DataStore.Contracts.TrackedActionContract;

/**
 * Created by cuddergambino on 8/9/16.
 */

public class SQLTrackedActionDataHelper {

    static public int count(Context context) {
        SQLiteDataStore ds = new SQLiteDataStore(context);
        SQLiteDatabase db = ds.getReadableDatabase();
        Cursor mCount= db.rawQuery("select count(*) from " + TrackedActionContract.TrackedActionEntry.TABLE_NAME, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        return count;
    }
}
