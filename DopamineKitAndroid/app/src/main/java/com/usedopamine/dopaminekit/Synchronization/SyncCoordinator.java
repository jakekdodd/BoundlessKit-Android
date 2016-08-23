package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.util.Log;

import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class SyncCoordinator extends Syncer {

    private static SyncCoordinator sharedInstance;

    private ScheduledExecutorService apiThreadPool = Executors.newScheduledThreadPool(2);
    private ExecutorService myExecutor = Executors.newFixedThreadPool(2);
    private TrackSyncer trackSyncer;
    private ReportSyncer reportSyncer;

//    protected SQLiteDatabase sqlDB;
//    protected DopamineAPI dopamineAPI;

    private Boolean syncInProgress = false;

    private SyncCoordinator(Context base) {
        super(base);
        trackSyncer = TrackSyncer.getInstance(this);
        reportSyncer = ReportSyncer.getInstance(this);

        sqlDB = SQLiteDataStore.getInstance(this).getWritableDatabase();
        dopamineAPI = DopamineAPI.getInstance(this);

    }

    public static SyncCoordinator getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new SyncCoordinator(context);
        }

        return sharedInstance;
    }

    public void storeTrackedAction(DopeAction action) {
        trackSyncer.store(action);
        sync();
    }

    public void storeReportedAction(DopeAction action) {
        reportSyncer.store(action);
        sync();
    }

    public String removeReinforcementDecision(Context context, String actionID) {
        return CartridgeSyncer.getCartridgeSyncerFor(context, actionID).unload();
    }

    @Override
    public boolean isTriggered() {
        return true;
    }

    public void sync() {
        myExecutor.submit(this);
    }

    @Override
    public JSONObject call() throws Exception {

        if (syncInProgress) {
            DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
            return null;
        } else {
            synchronized (syncInProgress) {
                if (syncInProgress) {
                    DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
                    return null;
                } else {
                    try {
                        syncInProgress = true;

                        //////
                        // Begin syncing logic
                        //////

                        HashMap<String, CartridgeSyncer> cartridgesToSync = CartridgeSyncer.whichShouldSync(SyncCoordinator.this);
                        boolean reporterShouldSync = cartridgesToSync.size() > 0 || reportSyncer.isTriggered();
                        boolean trackerShouldSync = reporterShouldSync || trackSyncer.isTriggered();

                        Future<JSONObject> apiCall = null;
                        JSONObject apiResponse = null;

                        if (trackerShouldSync) {
                            apiCall = apiThreadPool.submit(trackSyncer); // apiThreadPool.schedule(trackSyncer, 1000, TimeUnit.MILLISECONDS);
                            while (!apiCall.isDone()) {
                                Log.v("SyncCoordinator", "Waiting for track syncer to be done...");
                                Thread.sleep(1000);
                            }

                            apiResponse = apiCall.get();
                            Log.v("TrackSyncResponse", apiResponse == null ? "null" : apiResponse.toString());
                            if (apiResponse == null || apiResponse.optInt("status", 404) == 404) {
                                DopamineKit.debugLog("SyncCoordinator", "Something went wrong while syncing tracker... Halting early.");
                                return null;
                            } else {
                                Log.v("SyncCoordinator", "Track Syncer is done!");
                                Thread.sleep(1000);
                            }

                        }

                        if (reporterShouldSync) {
                            apiCall = apiThreadPool.schedule(reportSyncer, 1, TimeUnit.SECONDS);
                            while (!apiCall.isDone()) {
                                Log.v("SyncCoordinator", "Waiting for report syncer to be done...");
                                Thread.sleep(1000);
                            }

                            apiResponse = apiCall.get();
                            Log.v("ReportSyncResponse", apiResponse == null ? "null" : apiResponse.toString());
                            if (apiResponse == null || apiResponse.optInt("status", 404) == 404) {
                                DopamineKit.debugLog("SyncCoordinator", "Something went wrong while syncing reporter... Halting early.");
                                return null;
                            } else {
                                Log.v("SyncCoordinator", "Report Syncer is done!");
                                Thread.sleep(5000);
                            }
                        }

                        for (Map.Entry<String, CartridgeSyncer> cartridge : cartridgesToSync.entrySet()) {
                            apiCall = apiThreadPool.submit(cartridge.getValue());
                            while (!apiCall.isDone()) {
                                Log.v("SyncCoordinator", "Waiting for " + cartridge.getKey() + " cartridge refresh to be done...");
                                Thread.sleep(1000);
                            }

                            apiResponse = apiCall.get();
                            Log.v("CartridgeSyncResponse", apiResponse == null ? "null" : apiResponse.toString());
                            if (apiResponse == null || apiResponse.optInt("status", 404) == 404) {
                                DopamineKit.debugLog("SyncCoordinator", "Something went wrong while syncing cartridge... Halting early.");
                                return null;
                            } else {
                                Log.v("SyncCoordinator", cartridge.getKey() + " cartridge syncer is done!");
                                Thread.sleep(1000);
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } finally {
                        syncInProgress = false;
                        return null;
                    }
                }

            }

        }
    }
}
