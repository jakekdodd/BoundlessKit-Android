package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.TrackedActionContract;
import com.usedopamine.dopaminekit.DataStore.SQLTrackedActionDataHelper;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class TrackSyncer extends Syncer {

    private static TrackSyncer sharedInstance;

    private SharedPreferences preferences;
    private static final String preferencesName = "DopamineTrackSyncer";
    private static final String preferencesSizeToSync = "sizeToSync";
    private static final String preferencesTimerStartAt = "timerStartAt";
    private static final String preferencesTimerExpiresIn = "timerExpiresIn";

    private int sizeToSync;
    private long timerStartAt;
    private long timerExpiresIn;

    private final Object apisynclock = new Object();
    private Boolean syncInProgress = false;

    private TrackSyncer(Context context) {
        super(context);
        preferences = context.getSharedPreferences(preferencesName, 0);
        sizeToSync = preferences.getInt(preferencesSizeToSync, 15);
        timerStartAt = preferences.getLong(preferencesTimerStartAt, 0);
        timerExpiresIn = preferences.getLong(preferencesTimerExpiresIn, 48 * 3600000);
    }

    public static TrackSyncer getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new TrackSyncer(context);
        }
        return sharedInstance;
    }

    public void store(DopeAction action) {
        String metaData = (action.metaData == null) ? null : action.metaData.toString();
        long rowId = SQLTrackedActionDataHelper.insert(sqlDB, new TrackedActionContract(
                0, action.actionID, metaData, action.utc, action.timezoneOffset
        ));
        DopamineKit.debugLog("SQL Tracked Actions", "Inserted into row " + rowId);
    }

    @Override
    public boolean isTriggered() {
        int actionsCount = SQLTrackedActionDataHelper.count(sqlDB);
        DopamineKit.debugLog("TrackSyncer", "Track has " + actionsCount + "/" + sizeToSync + " actions" );
        return actionsCount >= sizeToSync || System.currentTimeMillis() > timerStartAt + timerExpiresIn;
    }

    public void updateTriggers(@Nullable Integer size, @Nullable Long startTime, @Nullable Long expiresIn) {

        if (size != null) { sizeToSync = size; }
        if (startTime != null) { timerStartAt = startTime; }
        else { timerStartAt = System.currentTimeMillis(); }
        if (expiresIn != null) { timerExpiresIn = expiresIn; }

        preferences.edit()
                .putInt(preferencesSizeToSync, sizeToSync)
                .putLong(preferencesTimerStartAt, timerStartAt)
                .putLong(preferencesTimerExpiresIn, timerExpiresIn)
                .apply();
    }

    @Override
    public @Nullable JSONObject call() throws Exception {
        if (syncInProgress) {
            DopamineKit.debugLog("TrackSyncer", "Track sync already happening");
            return null;
        } else {
            synchronized (apisynclock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("TrackSyncer", "Track sync already happening");
                    return null;
                } else {
                    JSONObject apiResponse = null;

                    try {
                        DopamineKit.debugLog("TrackSyncer", "Beginning tracker sync!");
                        syncInProgress = true;

                        final ArrayList<TrackedActionContract> sqlActions = SQLTrackedActionDataHelper.findAll(sqlDB);
                        if (sqlActions.size() == 0) {
                            DopamineKit.debugLog("TrackSyncer", "No tracked actions to be synced.");
                            apiResponse = new JSONObject().put("status", 200);
                        } else {
                            DopeAction dopeActions[] = new DopeAction[sqlActions.size()];
                            for (int i = 0; i < sqlActions.size(); i++) {
                                TrackedActionContract action = sqlActions.get(i);
                                try {
                                    dopeActions[i] = new DopeAction(action.actionID, null, new JSONObject(action.metaData), action.utc, action.timezoneOffset);
                                } catch (JSONException e) {
                                    dopeActions[i] = new DopeAction(action.actionID, null, null, action.utc, action.timezoneOffset);
                                } catch (NullPointerException e) {
                                    dopeActions[i] = new DopeAction(action.actionID, null, null, action.utc, action.timezoneOffset);
                                }
                            }
                            apiResponse = dopamineAPI.track(dopeActions);
                        }

                        if (apiResponse == null) {
                            DopamineKit.debugLog("TrackSyncer", "Something went wrong during the call...");
                        } else if (apiResponse.optInt("status", 404) == 200) {
                            DopamineKit.debugLog("TrackSyncer", "Deleting " + sqlActions.size() + " tracked actions...");
                            for (int i = 0; i < sqlActions.size(); i++) {
                                SQLTrackedActionDataHelper.delete(sqlDB, sqlActions.get(i));
                            }
                            updateTriggers(null, null, null);
                        } else {
                            DopamineKit.debugLog("TrackSyncer", "Something went wrong while syncing... Leaving tracked actions in sqlite db");
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
