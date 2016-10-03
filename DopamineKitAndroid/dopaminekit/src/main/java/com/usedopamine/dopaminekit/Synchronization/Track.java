package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
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
import java.util.concurrent.Callable;

/**
 * Created by cuddergambino on 9/4/16.
 */

class Track extends ContextWrapper implements Callable<JSONObject> {

    private static Track sharedInstance;

    private DopamineAPI dopamineAPI;
    private SQLiteDatabase sqlDB;
    private SharedPreferences preferences;
    private final String preferencesName = "com.usedopamine.synchronization.track";
    private final String sizeKey = "size";
    private final String sizeToSyncKey = "sizeToSync";
    private final String timerStartsAtKey = "timerStartsAt";
    private final String timerExpiresInKey = "timerExpiresIn";

    private int sizeToSync;
    private long timerStartsAt;
    private long timerExpiresIn;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    public static Track getSharedInstance(Context base) {
        if (sharedInstance == null) {
            sharedInstance = new Track(base);
        }
        return sharedInstance;
    }

    private Track(Context base) {
        super(base);
        dopamineAPI = DopamineAPI.getInstance(base);
        sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
        preferences = getSharedPreferences(preferencesName, 0);
        sizeToSync = preferences.getInt(sizeToSyncKey, 15);
        timerStartsAt = preferences.getLong(timerStartsAtKey, System.currentTimeMillis());
        timerExpiresIn = preferences.getLong(timerExpiresInKey, 48 * 3600000);
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
                .putInt(sizeToSyncKey, sizeToSync)
                .putLong(timerStartsAtKey, timerStartsAt)
                .putLong(timerExpiresInKey, timerExpiresIn)
                .apply();
    }

    public void removeTriggers() {
        sizeToSync = 15;
        timerStartsAt = 0;
        timerExpiresIn = 172800000;
        preferences.edit().clear().commit();
    }

    public JSONObject jsonForTriggers() {
        JSONObject json = new JSONObject();
        try {
            json.put(sizeKey, SQLTrackedActionDataHelper.count(sqlDB));
            json.put(sizeToSyncKey, sizeToSync);
            json.put(timerStartsAtKey, timerStartsAt);
            json.put(timerExpiresInKey, timerExpiresIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private boolean timerDidExpire() {
        long currentTime = System.currentTimeMillis();
        boolean isExpired = currentTime >= (timerStartsAt + timerExpiresIn);
        DopamineKit.debugLog("Track", "Track timer expires in "+(timerStartsAt + timerExpiresIn - currentTime)+"ms so "+(isExpired ? "does" : "doesn't")+" need to sync...");
        return isExpired;
    }

    private boolean isSizeToSync() {
        int count = SQLTrackedActionDataHelper.count(sqlDB);
        boolean isSize = count >= sizeToSync;
        DopamineKit.debugLog("Track", "Track has "+count+"/"+sizeToSync+" actions so "+(isSize ? "does" : "doesn't")+" need to sync...");
        return isSize;
    }

    public void store(DopeAction action) {
        String metaData = (action.metaData == null) ? null : action.metaData.toString();
        long rowId = SQLTrackedActionDataHelper.insert(sqlDB, new TrackedActionContract(
                0, action.actionID, metaData, action.utc, action.timezoneOffset
        ));DopamineKit.debugLog("SQL Tracked Actions", "Inserted into row " + rowId);

    }

    public void remove(TrackedActionContract action) {
        SQLTrackedActionDataHelper.delete(sqlDB, action);
    }

    @Override
    public @Nullable JSONObject call() throws Exception {
        if (syncInProgress) {
            DopamineKit.debugLog("TrackSyncer", "Track sync already happening");
            return null;
        } else {
            synchronized (apiSyncLock) {
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
                            apiResponse = new JSONObject().put("status", 0);
                        } else {
                            DopamineKit.debugLog("TrackSyncer", sqlActions.size() + " tracked actions to be synced.");
                            apiResponse = dopamineAPI.track(sqlActions);
                        }

                        if (apiResponse == null) {
                            DopamineKit.debugLog("TrackSyncer", "Something went wrong during the call...");
                        } else if (apiResponse.optInt("status", 404) == 200) {
                            DopamineKit.debugLog("TrackSyncer", "Deleting " + sqlActions.size() + " tracked actions...");
                            for (int i = 0; i < sqlActions.size(); i++) {
                                remove(sqlActions.get(i));
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
