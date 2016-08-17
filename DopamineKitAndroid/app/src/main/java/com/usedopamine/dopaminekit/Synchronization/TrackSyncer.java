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
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPIRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class TrackSyncer {

    private static TrackSyncer sharedInstance;

    private static final String preferencesName = "DopamineTrackSyncer";
    private static final String preferencesSuggestedSize = "suggestedSize";
    private static final String preferencesTimerMarker = "timerMarker";
    private static final String preferencesTimerLength = "timerLength";

    private int suggestedSize;
    private long timerMarker;
    private long timerLength;

    private Boolean syncInProgress = false;
//    private final Object synclock = new Object();
    private final Object storelock = new Object();

    private TrackSyncer(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, 0);
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

    public void store(Context context, DopeAction action) {
        synchronized (storelock) {
            SQLiteDatabase db = SQLiteDataStore.getInstance(context).getWritableDatabase();
            String metaData = (action.metaData==null) ? null : action.metaData.toString();
            long rowId = SQLTrackedActionDataHelper.insert(db, new TrackedActionContract(
                    0, action.actionID, metaData, action.utc, action.timezoneOffset
            ));
            DopamineKit.debugLog("SQL Tracked Actions", "Inserted into row " + rowId);
        }
    }

    public void updateTriggers(Context context, @Nullable Integer size, @Nullable Long timerMarker, @Nullable Long timerLength) {
        TrackSyncer instance = getInstance(context);
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, 0);

        if (size != null) { instance.suggestedSize = size; }
        if (timerLength != null) { instance.timerMarker = timerLength; }
        if (timerMarker != null) { instance.timerMarker = timerMarker; }
        else { instance.timerMarker = System.currentTimeMillis(); }

        preferences.edit()
                .putInt(preferencesSuggestedSize, instance.suggestedSize)
                .putLong(preferencesTimerMarker, instance.timerMarker)
                .putLong(preferencesTimerLength, instance.timerLength)
                .apply();
    }

    protected boolean shouldSync(Context context) {
        long currentTime = System.currentTimeMillis();
        int actionsCount = SQLTrackedActionDataHelper.count(SQLiteDataStore.getInstance(context).getReadableDatabase());

        if (actionsCount >= suggestedSize) {
            DopamineKit.debugLog("TrackSyncer", "Track has " + actionsCount + " actions and should only have " + suggestedSize);
        } else if ((timerMarker + timerLength) < currentTime) {
            DopamineKit.debugLog("TrackSyncer", "Track has expired at " + (timerMarker + timerLength) + " and it is " + currentTime + " now.");
        } else {
            DopamineKit.debugLog("TrackSyncer", "Track has " + actionsCount + "/" + suggestedSize + " actions and last synced " + timerMarker + " with a timer set " + timerLength + "ms from now so does not need sync...");
        }


        return actionsCount >= suggestedSize ||
                (timerMarker + timerLength) < currentTime;
    }

    public void sync(final Context context, final SyncerCallback callback) {
        if (syncInProgress) {
            DopamineKit.debugLog("TrackSyncer", "Track sync already happening");
            if (callback!=null) callback.onSyncComplete(200);
            return;
        } else {
            syncInProgress = true;
        }

        synchronized (syncInProgress) {

            final SQLiteDatabase db = SQLiteDataStore.getInstance(context).getWritableDatabase();
            final ArrayList<TrackedActionContract> sqlActions = SQLTrackedActionDataHelper.findAll(db);
            if (sqlActions.size() == 0) {
                DopamineKit.debugLog("TrackSyncer", "No tracked actions to be synced.");
                if (callback != null) callback.onSyncComplete(200);
                return;
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

            DopamineAPI.track(context, dopeActions, new DopamineAPIRequestCallback() {
                @Override
                public void onDopamineAPIRequestPostExecute(JSONObject response) {
                    try {
                        int statusCode = response.optInt("status", 404);
                        if (statusCode == 200) {
                            for (int i = 0; i < sqlActions.size(); i++) {
                                SQLTrackedActionDataHelper.delete(db, sqlActions.get(i));
                            }
                            DopamineKit.debugLog("TrackSyncer", "Synced and deleted all tracked actions!");
                            updateTriggers(context, null, null, null);
                        } else {
                            DopamineKit.debugLog("TrackSyncer", "Something went wrong while syncing... Leaving tracked actions in sqlite db");
                        }
                        if (callback != null) callback.onSyncComplete(statusCode);
                    } finally {
                        syncInProgress = false;
                    }
                }
            });
        }
    }

}
