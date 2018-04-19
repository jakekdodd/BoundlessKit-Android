package boundless.kit.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import boundless.kit.BoundlessKit;
import boundless.kit.data.storage.contracts.BoundlessExceptionContract;
import boundless.kit.data.storage.contracts.SyncOverviewContract;
import boundless.kit.data.storage.SQLBoundlessExceptionDataHelper;
import boundless.kit.data.storage.SQLSyncOverviewDataHelper;
import boundless.kit.data.storage.SQLiteDataStore;
import boundless.kit.integration.BoundlessAPI;

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

    /**
     * Creates a BoundlessException object and reports it to BoundlessAPI for increased stability.
     *
     * @param exception The exception thrown by BoundlessKit
     */
    public static void storeException(Throwable exception) {
        if (sharedInstance != null) {
            BoundlessException.store(sharedInstance, exception);
        } else {
            BoundlessKit.debugLog("Telemetry", "Trying to store exception, but Telemetry was never initialized.");
        }
    }

    /**
     * Creates a SyncOverview object to record to sync performance and take a snapshot of the syncers.
     * Use the functions setResponseForTrackSync(), setResponseForReportSync(), and setResponseForCartridgeSync()
     * to record progress throughout the synchornization.
     * Use stopRecordingSync() to finalize the recording.
     *
     * @param cause      The reason the synchronization process has been triggered
     * @param track      The Track object to snapshot its triggers
     * @param report     The Report object to snapshot its triggers
     * @param cartridges The cartridges dictionary to snapshot its triggers
     */
    public void startRecordingSync(String cause, Track track, Report report, HashMap<String, Cartridge> cartridges) {
        synchronized (syncOverviewLock) {
            HashMap<String, JSONObject> cartridgeJSONs = new HashMap<>();
            for (HashMap.Entry<String, Cartridge> entry : cartridges.entrySet()) {
                cartridgeJSONs.put(entry.getKey(), entry.getValue().jsonForTriggers());
            }
            currentSyncOverview = new SyncOverview(cause, track.jsonForTriggers(), report.jsonForTriggers(), cartridgeJSONs);
        }
    }

    /**
     * Sets the `syncResponse` for `Track` in the current sync overview.
     *
     * @param status    The HTTP status code received from the BoundlessAPI
     * @param error     An error if one was received
     * @param startedAt The time the API call started at
     */
    public void setResponseForTrackSync(int status, @Nullable String error, long startedAt) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.setTrackSyncResponse(status, error, startedAt);
            }
        }
    }

    /**
     * Sets the `syncResponse` for `Report` in the current sync overview.
     *
     * @param status    The HTTP status code received from the BoundlessAPI
     * @param error     An error if one was received
     * @param startedAt The time the API call started at
     */
    public void setResponseForReportSync(int status, @Nullable String error, long startedAt) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.setReportSyncResponse(status, error, startedAt);
            }
        }
    }

    /**
     * Sets the `syncResponse` for the cartridge in the current sync overview.
     *
     * @param actionID  The name of the cartridge's action
     * @param status    The HTTP status code received from the BoundlessAPI
     * @param error     An error if one was received
     * @param startedAt The time the API call started at
     */
    public void setResponseForCartridgeSync(String actionID, int status, @Nullable String error, long startedAt) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.setCartridgeSyncResponse(actionID, status, error, startedAt);
            }
        }
    }

    /**
     * Finalizes the current syncOverview object.
     *
     * @param successfulSync Whether a successful sync was made with the BoundlessAPI
     */
    public void stopRecordingSync(boolean successfulSync) {
        synchronized (syncOverviewLock) {
            if (currentSyncOverview != null) {
                currentSyncOverview.finish();
                currentSyncOverview.store(this);
                currentSyncOverview = null;
                BoundlessKit.debugLog("Telemetry", "Saved a sync overview, totalling " + SQLSyncOverviewDataHelper.count(sqlDB) + " overviews");
            } else {
                BoundlessKit.debugLog("Telemetry", "No recording has started. Did you remember to execute startRecordingSync() at the beginning of the sync performance?");
            }
        }

        if (successfulSync) {
            syncerExecutor.submit(this);
        }
    }

    @Override
    public Integer call() throws Exception {
        if (syncInProgress) {
            BoundlessKit.debugLog("Telemetry", "Telemetry sync already happening");
            return 0;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    BoundlessKit.debugLog("Telemetry", "Telemetry sync already happening");
                    return 0;
                } else {
                    try {
                        syncInProgress = true;
                        BoundlessKit.debugLog("Telemetry", "Beginning telemetry sync!");

                        final ArrayList<SyncOverviewContract> syncOverviews = SQLSyncOverviewDataHelper.findAll(sqlDB);
                        final ArrayList<BoundlessExceptionContract> exceptions = SQLBoundlessExceptionDataHelper.findAll(sqlDB);
                        if (syncOverviews.size() == 0 && exceptions.size() == 0) {
                            BoundlessKit.debugLog("Telemetry", "No sync overviews or exceptions to be synced.");
                            return 0;
                        } else {
                            BoundlessKit.debugLog("Telemetry", syncOverviews.size() + " sync overviews and " + exceptions.size() + " exceptions to be synced.");
                            JSONObject apiResponse = BoundlessAPI.sync(this, syncOverviews, exceptions);
                            if (apiResponse != null) {
                                int statusCode = apiResponse.optInt("status", 404);
                                if (statusCode == 200) {
                                    for (int i = 0; i < syncOverviews.size(); i++) {
                                        SQLSyncOverviewDataHelper.delete(sqlDB, syncOverviews.get(i));
                                    }
                                    for (int i = 0; i < exceptions.size(); i++) {
                                        SQLBoundlessExceptionDataHelper.delete(sqlDB, exceptions.get(i));
                                    }
                                }
                                return statusCode;
                            } else {
                                BoundlessKit.debugLog("Telemetry", "Something went wrong making the call...");
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
