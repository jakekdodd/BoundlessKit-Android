package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.ReportedActionContract;
import com.usedopamine.dopaminekit.DataStore.SQLReportedActionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by cuddergambino on 9/4/16.
 */

public class Report extends ContextWrapper implements Callable<JSONObject> {

    private static Report sharedInstance;

    private DopamineAPI dopamineAPI;
    private SQLiteDatabase sqlDB;
    private SharedPreferences preferences;
    private final String preferencesName = "com.usedopamine.synchronization.report";
    private final String preferencesSizeToSync = "sizetosync";
    private final String preferencesTimerStartsAt = "timerstartsat";
    private final String preferencesTimerExpiresIn = "timerexpiresin";

    private int sizeToSync;
    private long timerStartsAt;
    private long timerExpiresIn;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    public static Report getSharedInstance(Context base) {
        if (sharedInstance == null) {
            sharedInstance = new Report(base);
        }
        return sharedInstance;
    }

    private Report(Context base) {
        super(base);
        dopamineAPI = DopamineAPI.getInstance(base);
        sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
        preferences = getSharedPreferences(preferencesName, 0);
        sizeToSync = preferences.getInt(preferencesSizeToSync, 15);
        timerStartsAt = preferences.getLong(preferencesTimerStartsAt, 0);
        timerExpiresIn = preferences.getLong(preferencesTimerExpiresIn, 48 * 3600000);
    }

    public boolean isTriggered() {
        return timerDidExpire() || isSizeToSync();
    }

    public void updateTriggers(@Nullable Integer size, @Nullable Long startTime, @Nullable Long expiresIn) {

        if (size != null) {
            sizeToSync = size;
        }
        if (startTime != null) {
            timerStartsAt = startTime;
        } else {
            timerStartsAt = System.currentTimeMillis();
        }
        if (expiresIn != null) {
            timerExpiresIn = expiresIn;
        }

        preferences.edit()
                .putInt(preferencesSizeToSync, sizeToSync)
                .putLong(preferencesTimerStartsAt, timerStartsAt)
                .putLong(preferencesTimerExpiresIn, timerExpiresIn)
                .apply();
    }

    public void removeTriggers() {
        sizeToSync = 15;
        timerStartsAt = 0;
        timerExpiresIn = 172800000;
        preferences.edit().clear().apply();
    }

    private boolean timerDidExpire() {
        long currentTime = System.currentTimeMillis();
        boolean isExpired = currentTime >= (timerStartsAt + timerExpiresIn);
        DopamineKit.debugLog("Report", "Report timer expires in "+(timerStartsAt + timerExpiresIn - currentTime)+"ms so "+(isExpired ? "does" : "doesn't")+" need to sync...");
        return isExpired;
    }

    private boolean isSizeToSync() {
        int count = SQLReportedActionDataHelper.count(sqlDB);
        boolean isSize = count >= sizeToSync;
        DopamineKit.debugLog("Report", "Report has "+count+"/"+sizeToSync+" actions so "+(isSize ? "does" : "doesn't")+" need to sync...");
        return isSize;
    }

    public void store(DopeAction action) {
        String metaData = (action.metaData == null) ? null : action.metaData.toString();
        long rowId = SQLReportedActionDataHelper.insert(sqlDB, new ReportedActionContract(
                0, action.actionID, action.reinforcementDecision, metaData, action.utc, action.timezoneOffset
        ));
        DopamineKit.debugLog("SQL Reported Actions", "Inserted into row " + rowId);
    }

    public void remove(ReportedActionContract action) {
        SQLReportedActionDataHelper.delete(sqlDB, action);
    }

    @Override
    public @Nullable JSONObject call() throws Exception {
        if (syncInProgress) {
            DopamineKit.debugLog("ReportSyncer", "Report sync already happening");
            return null;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("ReportSyncer", "Report sync already happening");
                    return null;
                } else {
                    JSONObject apiResponse = null;
                    try {
                        DopamineKit.debugLog("ReportSyncer", "Beginning reporter sync!");
                        syncInProgress = true;


                        final ArrayList<ReportedActionContract> sqlActions = SQLReportedActionDataHelper.findAll(sqlDB);
                        if (sqlActions.size() == 0) {
                            DopamineKit.debugLog("ReportSyncer", "No reported actions to be synced.");
                            apiResponse = new JSONObject().put("status", 200);
                        } else {
                            DopeAction[] dopeActions = new DopeAction[sqlActions.size()];
                            for (int i = 0; i < sqlActions.size(); i++) {
                                ReportedActionContract action = sqlActions.get(i);
                                try {
                                    dopeActions[i] = new DopeAction(action.actionID, action.reinforcementDecision, new JSONObject(action.metaData), action.utc, action.timezoneOffset);
                                } catch (JSONException e) {
                                    dopeActions[i] = new DopeAction(action.actionID, action.reinforcementDecision, null, action.utc, action.timezoneOffset);
                                } catch (NullPointerException e) {
                                    dopeActions[i] = new DopeAction(action.actionID, action.reinforcementDecision, null, action.utc, action.timezoneOffset);
                                }
                            }
                            apiResponse = dopamineAPI.report(dopeActions);
                        }

                        if (apiResponse == null) {
                            DopamineKit.debugLog("ReportSyncer", "Something went wrong during the call...");
                        } else if (apiResponse.optInt("status", 404) == 200) {
                            DopamineKit.debugLog("ReportSyncer", "Deleting " + sqlActions.size() + " reported actions...");
                            for (int i = 0; i < sqlActions.size(); i++) {
                                remove(sqlActions.get(i));
                            }
                            updateTriggers(null, null, null);
                        } else {
                            DopamineKit.debugLog("ReportSyncer", "Something went wrong while syncing... Leaving reported actions in sqlite db");
                        }

                    } catch (JSONException e) {
                    } finally {
                        syncInProgress = false;
                        return apiResponse;
                    }
                }
            }
        }
    }
}
