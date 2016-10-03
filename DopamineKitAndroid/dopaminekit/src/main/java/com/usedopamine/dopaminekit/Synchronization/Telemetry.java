package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.DopeExceptionContract;
import com.usedopamine.dopaminekit.DataStore.Contracts.SyncOverviewContract;
import com.usedopamine.dopaminekit.DataStore.SQLDopeExceptionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLSyncOverviewDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by cuddergambino on 9/30/16.
 */

class Telemetry extends ContextWrapper implements Callable<JSONObject> {
    private static Telemetry sharedInstance;

    private DopamineAPI dopamineAPI;
    private SQLiteDatabase sqlDB;

    private SyncOverview currentSyncOverview = null;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    public static Telemetry getSharedInstance(Context base) {
        if (sharedInstance == null) {
            sharedInstance = new Telemetry(base);
        }
        return sharedInstance;
    }

    private Telemetry(Context base) {
        super(base);
        dopamineAPI = DopamineAPI.getInstance(base);
        sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
    }

    public void startRecordingSync(String cause, Track track, Report report, HashMap<String, Cartridge> cartidges) {
        HashMap<String, JSONObject> cartridgeJSONs = new HashMap<>();
        for (HashMap.Entry<String, Cartridge> entry : cartidges.entrySet()) {
            cartridgeJSONs.put(entry.getKey(), entry.getValue().jsonForTriggers());
        }
        currentSyncOverview = new SyncOverview(cause, track.jsonForTriggers(), report.jsonForTriggers(), cartridgeJSONs);
    }

    public void stopRecordingSync() {
        if (currentSyncOverview != null) {
            currentSyncOverview.totalSyncTime = System.currentTimeMillis() - currentSyncOverview.utc;
            currentSyncOverview.store(this);
            currentSyncOverview = null;
        }
    }

    @Override
    public @Nullable JSONObject call() throws Exception {
        if (syncInProgress) {
            DopamineKit.debugLog("Telemetry", "Telemetry sync already happening");
            return null;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("Telemetry", "Telemetry sync already happening");
                    return null;
                } else {
                    JSONObject apiResponse = null;
                    try {
                        DopamineKit.debugLog("Telemetry", "Beginning telemetry sync!");
                        syncInProgress = true;


                        final ArrayList<SyncOverviewContract> syncOverviews = SQLSyncOverviewDataHelper.findAll(sqlDB);
                        final ArrayList<DopeExceptionContract> dopeExceptions = SQLDopeExceptionDataHelper.findAll(sqlDB);
                        if (syncOverviews.size() == 0 && dopeExceptions.size() == 0) {
                            DopamineKit.debugLog("Telemetry", "No sync overviews or exceptions to be synced.");
                            apiResponse = new JSONObject().put("status", 0);
                        } else {
                            apiResponse = dopamineAPI.sync(syncOverviews, dopeExceptions);
                        }

                        if (apiResponse == null) {
                            DopamineKit.debugLog("Telemetry", "Something went wrong during the call...");
                        } else if (apiResponse.optInt("status", 404) == 200) {
                            DopamineKit.debugLog("Telemetry", "Deleting " + syncOverviews.size() + " sync overviews and " + dopeExceptions.size() + " stored exceptions...");
                            for (int i = 0; i < syncOverviews.size(); i++) {
                                SQLSyncOverviewDataHelper.delete(sqlDB, syncOverviews.get(i));
                            }
                            for (int i = 0; i < dopeExceptions.size(); i++) {
                                SQLDopeExceptionDataHelper.delete(sqlDB, dopeExceptions.get(i));
                            }
                        } else {
                            DopamineKit.debugLog("Telemetry", "Something went wrong while syncing... Leaving sync overviews and stored exceptions in sqlite db");
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
