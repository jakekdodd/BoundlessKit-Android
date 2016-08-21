package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.TrackedActionContract;
import com.usedopamine.dopaminekit.DataStore.SQLTrackedActionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class TrackSyncer extends Syncer {

    private static TrackSyncer sharedInstance;

    private SharedPreferences preferences;
    private static final String preferencesName = "DopamineTrackSyncer";
    private static final String preferencesSuggestedSize = "suggestedSize";
    private static final String preferencesTimerMarker = "timerMarker";
    private static final String preferencesTimerLength = "timerLength";

    private int suggestedSize;
    private long timerMarker;
    private long timerLength;

    private final Object storelock = new Object();
    private final Object apisynclock = new Object();
    private Boolean syncInProgress = false;

    private TrackSyncer(Context context) {
        super(context);
        preferences = context.getSharedPreferences(preferencesName, 0);
        suggestedSize = preferences.getInt(preferencesSuggestedSize, 15);
        timerMarker = preferences.getLong(preferencesTimerMarker, 0);
        timerLength = preferences.getLong(preferencesTimerLength, 48 * 3600000);
    }

    public static TrackSyncer getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new TrackSyncer(context);
        }
        return sharedInstance;
    }

    public void store(DopeAction action) {
        synchronized (storelock) {
            String metaData = (action.metaData==null) ? null : action.metaData.toString();
            long rowId = SQLTrackedActionDataHelper.insert(sqlDB, new TrackedActionContract(
                    0, action.actionID, metaData, action.utc, action.timezoneOffset
            ));
            DopamineKit.debugLog("SQL Tracked Actions", "Inserted into row " + rowId);
        }
    }

    @Override
    public boolean isTriggered() {
        int actionsCount = SQLTrackedActionDataHelper.count(sqlDB);
        DopamineKit.debugLog("TrackSyncer", "Track has " + actionsCount + "/" + suggestedSize + " actions" );
        return actionsCount >= suggestedSize || System.currentTimeMillis() > timerMarker + timerLength;
    }

    public void updateTriggers(@Nullable Integer size, @Nullable Long startTime, @Nullable Long length) {

        if (size != null) { suggestedSize = size; }
        if (startTime != null) { timerMarker = startTime; }
        else { timerMarker = System.currentTimeMillis(); }
        if (length != null) { timerLength = length; }

        preferences.edit()
                .putInt(preferencesSuggestedSize, suggestedSize)
                .putLong(preferencesTimerMarker, timerMarker)
                .putLong(preferencesTimerLength, timerLength)
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
                            updateTriggers(null, null, null);
                            return null;
                        }

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
                        if (apiResponse == null) {
                            DopamineKit.debugLog("TrackSyncer", "Something went wrong during the call...");
                        } else {
                            if (apiResponse.optInt("status", 404) == 200) {
                                DopamineKit.debugLog("TrackSyncer", "Deleting synced actions...");
                                for (int i = 0; i < sqlActions.size(); i++) {
                                    SQLTrackedActionDataHelper.delete(sqlDB, sqlActions.get(i));
                                }
                                updateTriggers(null, null, null);
                            } else {
                                DopamineKit.debugLog("TrackSyncer", "Something went wrong while syncing... Leaving tracked actions in sqlite db");
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
