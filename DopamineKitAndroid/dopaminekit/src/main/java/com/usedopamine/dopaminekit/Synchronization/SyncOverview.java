package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.SyncOverviewContract;
import com.usedopamine.dopaminekit.DataStore.SQLSyncOverviewDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by cuddergambino on 9/30/16.
 */

class SyncOverview {

    private static SQLiteDatabase sqlDB;

    private static final String syncResponseKey = "syncResponse";
    private static final String utcKey = "utc";
    private static final String roundTripTimeKey = "roundTripTime";
    private static final String statusKey = "status";
    private static final String errorKey = "error";

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

    public void setTrackSyncResponse(int status, @Nullable String error, long startedAt) {
        JSONObject syncResponse = new JSONObject();
        try {
            syncResponse.put(utcKey, startedAt);
            syncResponse.put(roundTripTimeKey, System.currentTimeMillis() - startedAt);
            syncResponse.put(statusKey, status);
            syncResponse.put(errorKey, error);

            track.put(syncResponseKey, syncResponse);
        } catch (JSONException e) {
            Telemetry.recordException(e);
            e.printStackTrace();
        }
    }

    public void setReportSyncResponse(int status, @Nullable String error, long startedAt) {
        JSONObject syncResponse = new JSONObject();
        try {
            syncResponse.put(utcKey, startedAt);
            syncResponse.put(roundTripTimeKey, System.currentTimeMillis() - startedAt);
            syncResponse.put(statusKey, status);
            syncResponse.put(errorKey, error);

            report.put(syncResponseKey, syncResponse);
        } catch (JSONException e) {
            Telemetry.recordException(e);
            e.printStackTrace();
        }
    }

    public void setCartridgeSyncResponse(String actionID, int status, @Nullable String error, long startedAt) {
        JSONObject syncResponse = new JSONObject();
        try {
            syncResponse.put(utcKey, startedAt);
            syncResponse.put(roundTripTimeKey, System.currentTimeMillis() - startedAt);
            syncResponse.put(statusKey, status);
            syncResponse.put(errorKey, error);

            JSONObject cartridge = cartridges.get(actionID);
            if (cartridge != null) {
                cartridge.put(syncResponseKey, syncResponse);
            }
        } catch (JSONException e) {
            Telemetry.recordException(e);
            e.printStackTrace();
        }
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
