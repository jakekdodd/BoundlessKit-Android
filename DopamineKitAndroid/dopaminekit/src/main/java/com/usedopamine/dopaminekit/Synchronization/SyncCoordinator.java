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

/**
 * Created by cuddergambino on 8/4/16.
 */

public class SyncCoordinator extends Syncer {

    private static SyncCoordinator sharedInstance;

    private ExecutorService syncerExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService myExecutor = Executors.newFixedThreadPool(2);
    private TrackSyncer trackSyncer;
    private ReportSyncer reportSyncer;

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
    }

    public void storeReportedAction(DopeAction action) {
        reportSyncer.store(action);
    }

    public String removeReinforcementDecisionFor(Context context, String actionID) {
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
                            apiCall = syncerExecutor.submit(trackSyncer); // apiThreadPool.schedule(trackSyncer, 1000, TimeUnit.MILLISECONDS);
                            while (DopamineKit.debugMode && !apiCall.isDone()) {
                                Log.v("SyncCoordinator", "Waiting for track syncer to be done...");
                                Thread.sleep(200);
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
                            apiCall = syncerExecutor.submit(reportSyncer);
                            while (DopamineKit.debugMode && !apiCall.isDone()) {
                                Log.v("SyncCoordinator", "Waiting for report syncer to be done...");
                                Thread.sleep(200);
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
                            apiCall = syncerExecutor.submit(cartridge.getValue());
                            while (DopamineKit.debugMode && !apiCall.isDone()) {
                                Log.v("SyncCoordinator", "Waiting for " + cartridge.getKey() + " cartridge refresh to be done...");
                                Thread.sleep(200);
                            }

                            apiResponse = apiCall.get();
                            Log.v("CartridgeSyncResponse", apiResponse == null ? "null" : apiResponse.toString());
                            if (apiResponse == null || apiResponse.optInt("status", 404) == 404) {
                                DopamineKit.debugLog("SyncCoordinator", "Something went wrong while syncing cartridge... Halting early.");
                                return null;
                            } else {
                                Log.v("SyncCoordinator", cartridge.getKey() + " cartridge syncer is done!");
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
