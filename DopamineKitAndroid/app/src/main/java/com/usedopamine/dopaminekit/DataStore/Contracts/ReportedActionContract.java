package com.usedopamine.dopaminekit.DataStore.Contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

/**
 * Created by cuddergambino on 8/4/16.
 */

public final class ReportedActionContract implements BaseColumns{

    public static final String TABLE_NAME = "Reported_Actions";
    public static final String COLUMNS_NAME_ACTIONID = "actionid";
    public static final String COLUMNS_NAME_REINFORCEMENTDECISION= "reinforcementdecision";
    public static final String COLUMNS_NAME_METADATA = "metadata";
    public static final String COLUMNS_NAME_UTC = "utc";
    public static final String COLUMNS_NAME_TIMEZONEOFFSET = "timezoneoffset";

    public long id;
    public String actionID;
    public String reinforcementDecision;
    public @Nullable String metaData;
    public long utc;
    public long timezoneOffset;

    public ReportedActionContract(long id, String actionID, String reinforcementDecision, @Nullable String metaData, long utc, long timezoneOffset) {
        this.id = id;
        this.actionID = actionID;
        this.reinforcementDecision = reinforcementDecision;
        this.metaData = metaData;
        this.utc = utc;
        this.timezoneOffset = timezoneOffset;
    }

    public static ReportedActionContract fromCursor(Cursor cursor) {
        return new ReportedActionContract(
                cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getLong(4), cursor.getLong(5)
        );
    }

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY,"  +
            COLUMNS_NAME_ACTIONID + " TEXT," +
            COLUMNS_NAME_REINFORCEMENTDECISION + " TEXT," +
            COLUMNS_NAME_METADATA + " TEXT," +
            COLUMNS_NAME_UTC + " INTEGER," +
            COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER" +
            " )";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

}
