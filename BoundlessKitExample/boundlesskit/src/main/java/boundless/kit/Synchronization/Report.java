package boundless.kit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import boundless.kit.BoundlessKit;
import boundless.kit.DataStore.Contracts.ReportedActionContract;
import boundless.kit.DataStore.SQLReportedActionDataHelper;
import boundless.kit.DataStore.SQLiteDataStore;
import boundless.kit.Dashboard.BoundlessAPI;

/**
 * Created by cuddergambino on 9/4/16.
 */

class Report extends ContextWrapper implements Callable<Integer> {

    private static Report sharedInstance;

    private SQLiteDatabase sqlDB;
    private SharedPreferences preferences;
    private final String preferencesName = "boundless.boundlesskit.synchronization.report";
    private final String sizeKey = "size";
    private final String sizeToSyncKey = "sizeToSync";
    private final String timerStartsAtKey = "timerStartsAt";
    private final String timerExpiresInKey = "timerExpiresIn";

    private int sizeToSync;
    private long timerStartsAt;
    private long timerExpiresIn;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    static Report getSharedInstance(Context base) {
        if (sharedInstance == null) {
            sharedInstance = new Report(base);
        }
        return sharedInstance;
    }

    private Report(Context base) {
        super(base);
        sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
        preferences = getSharedPreferences(preferencesName, 0);
        sizeToSync = preferences.getInt(sizeToSyncKey, 15);
        timerStartsAt = preferences.getLong(timerStartsAtKey, System.currentTimeMillis());
        timerExpiresIn = preferences.getLong(timerExpiresInKey, 172800000);
    }

    /**
     * @return Whether a sync should be started
     */
    boolean isTriggered() {
        return timerDidExpire() || isSizeToSync();
    }

    /**
     * Updates the sync triggers.
     *
     * @param size      The number of reported actions to trigger a sync
     * @param startTime The start time for a sync timer
     * @param expiresIn The timer length, in ms, for a sync timer
     */
    void updateTriggers(@Nullable Integer size, @Nullable Long startTime, @Nullable Long expiresIn) {

        if (size != null) {
            sizeToSync = size;
        }
        if (startTime != null) {
            timerStartsAt = startTime;
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

    /**
     * Clears the saved report sync triggers.
     */
    void removeTriggers() {
        sizeToSync = 15;
        timerStartsAt = System.currentTimeMillis();
        timerExpiresIn = 172800000;
        preferences.edit().clear().apply();
    }

    /**
     * This function returns a snapshot of this instance as a JSONObject.
     *
     * @return A JSONObject containing the size and sync triggers
     */
    JSONObject jsonForTriggers() {
        JSONObject json = new JSONObject();
        try {
            json.put(sizeKey, SQLReportedActionDataHelper.count(sqlDB));
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
        return isExpired;
    }

    private boolean isSizeToSync() {
        int count = SQLReportedActionDataHelper.count(sqlDB);
        boolean isSize = count >= sizeToSync;
        BoundlessKit.debugLog("Report", "Report has batched " + count + "/" + sizeToSync + " actions" + (isSize ? " so needs to sync..." : "."));
        return isSize;
    }

    /**
     * Stores a reported action to be synced over the BoundlessAPI at a later time.
     *
     * @param action The action to be stored
     */
    void store(BoundlessAction action) {
        String metaData = (action.metaData == null) ? null : action.metaData.toString();
        long rowId = SQLReportedActionDataHelper.insert(sqlDB, new ReportedActionContract(
                0, action.actionID, action.reinforcementDecision, metaData, action.utc, action.timezoneOffset
        ));
//        BoundlessKit.debugLog("SQL Reported Actions", "Inserted into row " + rowId);
    }

    void remove(ReportedActionContract action) {
        SQLReportedActionDataHelper.delete(sqlDB, action);
    }

    @Override
    public Integer call() throws Exception {
        if (syncInProgress) {
            BoundlessKit.debugLog("ReportSyncer", "Report sync already happening");
            return 0;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    BoundlessKit.debugLog("ReportSyncer", "Report sync already happening");
                    return 0;
                } else {
                    try {
                        syncInProgress = true;
                        BoundlessKit.debugLog("ReportSyncer", "Beginning reporter sync!");

                        final ArrayList<ReportedActionContract> sqlActions = SQLReportedActionDataHelper.findAll(sqlDB);
                        if (sqlActions.size() == 0) {
                            BoundlessKit.debugLog("ReportSyncer", "No reported actions to be synced.");
                            updateTriggers(null, System.currentTimeMillis(), null);
                            return 0;
                        } else {
                            BoundlessKit.debugLog("ReportSyncer", sqlActions.size() + " reported actions to be synced.");
                            JSONObject apiResponse = BoundlessAPI.report(this, sqlActions);
                            if (apiResponse != null) {
                                int statusCode = apiResponse.optInt("status", -2);
                                if (statusCode == 200) {
                                    for (int i = 0; i < sqlActions.size(); i++) {
                                        remove(sqlActions.get(i));
                                    }
                                    updateTriggers(null, System.currentTimeMillis(), null);
                                }
                                return statusCode;
                            } else {
                                BoundlessKit.debugLog("ReportSyncer", "Something went wrong making the call...");
                                return -1;
                            }
                        }
                    } finally {
                        syncInProgress = false;
                    }
                }
            }
        }
    }

}
