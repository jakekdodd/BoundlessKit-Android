package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.usedopamine.dopaminekit.DataStore.Contracts.SyncOverviewContract;
import com.usedopamine.dopaminekit.DataStore.SQLSyncOverviewDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by cuddergambino on 9/30/16.
 */

class SyncOverview {

    private static SQLiteDatabase sqlDB;

    long utc;
    long timezoneOffset;
    long totalSyncTime;
    String cause;
    JSONObject track;
    JSONObject report;
    HashMap<String, JSONObject> cartridges;

    public SyncOverview(String cause, JSONObject trackTriggers, JSONObject reportTriggers, HashMap<String, JSONObject> cartridgeTriggers) {
        this.utc = System.currentTimeMillis();
        this.timezoneOffset = TimeZone.getDefault().getOffset(this.utc);
        this.totalSyncTime = -1;
        this.cause = cause;
        this.track = trackTriggers;
        this.report = reportTriggers;
        this.cartridges = cartridgeTriggers;
    }

    public void store(Context context) {
        if (sqlDB == null) {
            sqlDB = SQLiteDataStore.getInstance(context).getWritableDatabase();
        }
        long rowId = SQLSyncOverviewDataHelper.insert(sqlDB,
                new SyncOverviewContract(0, utc, timezoneOffset, totalSyncTime, cause, track.toString(), report.toString(), new JSONArray(cartridges.values()).toString())
        );

        DopamineKit.debugLog("SQL Sync Overviews", "Inserted into row " + rowId);
    }

}
