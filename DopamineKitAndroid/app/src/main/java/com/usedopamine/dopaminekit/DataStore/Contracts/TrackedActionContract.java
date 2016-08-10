package com.usedopamine.dopaminekit.DataStore.Contracts;

import android.provider.BaseColumns;

/**
 * Created by cuddergambino on 8/4/16.
 */

public final class TrackedActionContract {

    private TrackedActionContract() {}

    public static abstract class TrackedActionEntry implements BaseColumns {
        public static final String TABLE_NAME = "Tracked_Actions";
//        public static final String COLUMNS_NAME_INDEX = "index";
        public static final String COLUMNS_NAME_ACTION_ID = "actionid";
        public static final String COLUMNS_NAME_METADATA= "metadata";
        public static final String COLUMNS_NAME_UTC= "utc";
    }

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TrackedActionEntry.TABLE_NAME + " (" +
            TrackedActionEntry._ID + " INTEGER PRIMARY KEY,"  +
            TrackedActionEntry.COLUMNS_NAME_ACTION_ID + " TEXT," +
            TrackedActionEntry.COLUMNS_NAME_METADATA+ " TEXT," +
            TrackedActionEntry.COLUMNS_NAME_UTC+ " INTEGER" +
            " )";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TrackedActionEntry.TABLE_NAME;
}
