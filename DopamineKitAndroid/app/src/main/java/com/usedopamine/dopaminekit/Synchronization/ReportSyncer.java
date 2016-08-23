package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.ReportedActionContract;
import com.usedopamine.dopaminekit.DataStore.SQLReportedActionDataHelper;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class ReportSyncer extends Syncer {

    private static ReportSyncer sharedInstance;

    private SharedPreferences preferences;
    private static final String preferencesName = "DopamineReportSyncer";
    private static final String preferencessizeToSync = "sizeToSync";
    private static final String preferencesTimerStartsAt = "timerStartsAt";
    private static final String preferencesTimerExpiresIn = "timerExpiresIn";

    private int sizeToSync;
    private long timerStartsAt;
    private long timerExpiresIn;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    private ReportSyncer(Context context) {
        super(context);
        preferences = context.getSharedPreferences(preferencesName, 0);
        sizeToSync = preferences.getInt(preferencessizeToSync, 15);
        timerStartsAt = preferences.getLong(preferencesTimerStartsAt, 0);
        timerExpiresIn = preferences.getLong(preferencesTimerExpiresIn, 48 * 3600000);
    }

    public static ReportSyncer getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new ReportSyncer(context);
        }
        return sharedInstance;
    }

    public void store(DopeAction action) {
        String metaData = (action.metaData == null) ? null : action.metaData.toString();
        long rowId = SQLReportedActionDataHelper.insert(sqlDB, new ReportedActionContract(
                0, action.actionID, action.reinforcementDecision, metaData, action.utc, action.timezoneOffset
        ));
        DopamineKit.debugLog("SQL Reported Actions", "Inserted into row " + rowId);
    }

    @Override
    public boolean isTriggered() {
        int actionsCount = SQLReportedActionDataHelper.count(sqlDB);
        DopamineKit.debugLog("ReportSyncer", "Report has " + actionsCount + "/" + sizeToSync + " actions" );
        return actionsCount >= sizeToSync || System.currentTimeMillis() > timerStartsAt + timerExpiresIn;
    }

    public void updateTriggers(@Nullable Integer size, @Nullable Long startTime, @Nullable Long expiresIn) {

        if (size != null) { sizeToSync = size; }
        if (startTime != null) { timerStartsAt = startTime; }
        else { timerStartsAt = System.currentTimeMillis(); }
        if (expiresIn != null) { timerExpiresIn = expiresIn; }

        preferences.edit()
                .putInt(preferencessizeToSync, sizeToSync)
                .putLong(preferencesTimerStartsAt, timerStartsAt)
                .putLong(preferencesTimerExpiresIn, timerExpiresIn)
                .apply();
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
                            updateTriggers(null, null, null);
                            return new JSONObject().put("status", 200);
                        }

                        DopeAction dopeActions[] = new DopeAction[sqlActions.size()];

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
                        if (apiResponse == null) {
                            DopamineKit.debugLog("ReportSyncer", "Something went wrong during the call...");
                        } else {
                            if (apiResponse.optInt("status", 404) == 200) {
                                DopamineKit.debugLog("ReportSyncer", "Deleting reported actions...");
                                for (int i = 0; i < sqlActions.size(); i++) {
                                    SQLReportedActionDataHelper.delete(sqlDB, sqlActions.get(i));
                                }
                                updateTriggers(null, null, null);
                            } else {
                                DopamineKit.debugLog("ReportSyncer", "Something went wrong while syncing... Leaving reported actions in sqlite db");
                            }
                        }
                    } finally {
                        syncInProgress = false;
                        return apiResponse;
                    }
                }
            }
        }
    }
}
