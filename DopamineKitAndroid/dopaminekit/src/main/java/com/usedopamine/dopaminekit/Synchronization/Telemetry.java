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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cuddergambino on 9/30/16.
 */

public class Telemetry extends ContextWrapper implements Callable<Integer> {
    private static Telemetry sharedInstance;

    private SQLiteDatabase sqlDB;

    private SyncOverview currentSyncOverview = null;
    private final Object syncOverviewLock = new Object();

    private ExecutorService syncerExecutor = Executors.newSingleThreadExecutor();
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
        sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
    }

    public void startRecordingSync(String cause, Track track, Report report, HashMap<String, Cartridge> cartridges) {
        synchronized (syncOverviewLock) {
            HashMap<String, JSONObject> cartridgeJSONs = new HashMap<>();
            for (HashMap.Entry<String, Cartridge> entry : cartridges.entrySet()) {
                cartridgeJSONs.put(entry.getKey(), entry.getValue().jsonForTriggers());
            }
            currentSyncOverview = new SyncOverview(cause, track.jsonForTriggers(), report.jsonForTriggers(), cartridgeJSONs);
        }
    }

    public void setResponseForTrackSync(int status, @Nullable String error, long startedAt) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.setTrackSyncResponse(status, error, startedAt);
            }
        }
    }

    public void setResponseForReportSync(int status, @Nullable String error, long startedAt) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.setReportSyncResponse(status, error, startedAt);
            }
        }
    }

    public void setResponseForCartridgeSync(String actionID, int status, @Nullable String error, long startedAt) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.setCartridgeSyncResponse(actionID, status, error, startedAt);
            }
        }
    }

    public void stopRecordingSync(boolean successfulSync) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.totalSyncTime = System.currentTimeMillis() - currentSyncOverview.utc;
                currentSyncOverview.store(this);
                currentSyncOverview = null;
                DopamineKit.debugLog("Telemetry", "Saved a sync overview, totalling "+ SQLSyncOverviewDataHelper.count(sqlDB) +" overviews");
            } else {
                DopamineKit.debugLog("Telemetry", "No recording has started. Did you remember to execute startRecordingSync() at the beginning of the sync performance?");
            }
        }

        if (successfulSync) {
            syncerExecutor.submit(this);
        }
    }

    public static void recordException(Throwable e) {
        if (sharedInstance != null) {
            DopeException.store(sharedInstance, e);
        } else {
            DopamineKit.debugLog("Telemetry", "Trying to store exception, but Telemetry was never initialized.");
        }
    }

    @Override
    public Integer call() throws Exception {
        if (syncInProgress) {
            DopamineKit.debugLog("Telemetry", "Telemetry sync already happening");
            return 0;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("Telemetry", "Telemetry sync already happening");
                    return 0;
                } else {
                    try {
                        syncInProgress = true;
                        DopamineKit.debugLog("Telemetry", "Beginning telemetry sync!");

                        final ArrayList<SyncOverviewContract> syncOverviews = SQLSyncOverviewDataHelper.findAll(sqlDB);
                        final ArrayList<DopeExceptionContract> dopeExceptions = SQLDopeExceptionDataHelper.findAll(sqlDB);
                        if (syncOverviews.size() == 0 && dopeExceptions.size() == 0) {
                            DopamineKit.debugLog("Telemetry", "No sync overviews or exceptions to be synced.");
                            return 0;
                        } else {
                            DopamineKit.debugLog("Telemetry", syncOverviews.size() + " sync overviews and " + dopeExceptions.size() + " exceptions to be synced.");
                            JSONObject apiResponse = DopamineAPI.sync(this, syncOverviews, dopeExceptions);
                            if (apiResponse != null) {
                                int statusCode = apiResponse.optInt("status", 404);
                                if (statusCode == 200) {
                                    for (int i = 0; i < syncOverviews.size(); i++) {
                                        SQLSyncOverviewDataHelper.delete(sqlDB, syncOverviews.get(i));
                                    }
                                    for (int i = 0; i < dopeExceptions.size(); i++) {
                                        SQLDopeExceptionDataHelper.delete(sqlDB, dopeExceptions.get(i));
                                    }
                                }
                                return statusCode;
                            } else {
                                DopamineKit.debugLog("Telemetry", "Something went wrong making the call...");
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
