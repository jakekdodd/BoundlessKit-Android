package com.usedopamine.dopaminekit.DataStore.Contracts;

import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by cuddergambino on 8/4/16.
 */

public final class TrackedActionContract {
    public static abstract class TrackedActionEntry implements BaseColumns {
        public static final String TABLE_NAME = "Tracked_Actions";
        public static final String COLUMNS_NAME_ACTION_ID = "actionid";
        public static final String COLUMNS_NAME_METADATA= "metadata";
        public static final String COLUMNS_NAME_UTC= "utc";
    }

    public String actionID;
    public String metaData;
    public long utc;

    public TrackedActionContract(String actionID, String metaData, long utc) {
        this.actionID = actionID;
        this.metaData = metaData;
        this.utc = utc;
    }

    public static TrackedActionContract fromCursor(Cursor cursor) {
        return new TrackedActionContract(
                cursor.getString(2), cursor.getString(3), cursor.getLong(4)
        );
    }

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TrackedActionEntry.TABLE_NAME + " (" +
            TrackedActionEntry._ID + " INTEGER PRIMARY KEY,"  +
            TrackedActionEntry.COLUMNS_NAME_ACTION_ID + " TEXT," +
            TrackedActionEntry.COLUMNS_NAME_METADATA+ " TEXT," +
            TrackedActionEntry.COLUMNS_NAME_UTC+ " INTEGER" +
            " )";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TrackedActionEntry.TABLE_NAME;
}
