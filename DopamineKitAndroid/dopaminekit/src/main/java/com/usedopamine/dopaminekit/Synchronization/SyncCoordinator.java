package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.util.Log;

import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class SyncCoordinator extends ContextWrapper implements Callable<Void> {

    private static SyncCoordinator sharedInstance;

    // static reference to known actionIDs
    private SharedPreferences preferences;
    private final String preferencesName = "com.usedopamine.synchronization.synccoordinator";
    private final String preferencesActionIDSet = "actionidset";

    private ExecutorService syncerExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService myExecutor = Executors.newFixedThreadPool(3);

    private Track track;
    private Report report;
    private HashMap<String, Cartridge> cartridges;

    private final Object syncLock = new Object();
    private Boolean syncInProgress = false;

    public static SyncCoordinator getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new SyncCoordinator(context);
        }
        return sharedInstance;
    }

    private SyncCoordinator(Context base) {
        super(base);
        track = Track.getSharedInstance(base);
        report = Report.getSharedInstance(base);

        cartridges = new HashMap<>();
        preferences = getSharedPreferences(preferencesName, 0);
        Set<String> actionIDs = preferences.getStringSet(preferencesActionIDSet, new HashSet<String>());
        DopamineKit.debugLog("SyncCoordinator", "Loading known actionsIDS...");
        for (String actionID : actionIDs) {
            cartridges.put(actionID, new Cartridge(base, actionID));
            DopamineKit.debugLog("SyncCoordinator", "Loaded cartridge for actionID:" + actionID);
        }
        DopamineKit.debugLog("SyncCoordinator", "Done loading known actionsIDS.");
        performSync();
    }

    public void storeTrackedAction(DopeAction action) {
        track.store(action);
        performSync();
    }

    public void storeReportedAction(DopeAction action) {
        report.store(action);
        performSync();
    }

    public String removeReinforcementDecisionFor(Context context, String actionID) {
        Cartridge cartridge = cartridges.get(actionID);
        if (cartridge == null) {
            cartridge = new Cartridge(this, actionID);
            cartridges.put(actionID, cartridge);
            preferences.edit().putStringSet(preferencesActionIDSet, cartridges.keySet()).apply();
            DopamineKit.debugLog("SyncCoordinator", "Created a cartridge for " + actionID + " for the first time!");
        }
        return cartridge.remove();
    }

    public void performSync() {
        myExecutor.submit(this);
    }

    @Override
    public Void call() throws Exception {

        if (syncInProgress) {
            DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
            return null;
        } else {
            synchronized (syncLock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
                    return null;
                } else {
                    try {
                        syncInProgress = true;

                        //////
                        // Begin syncing logic
                        //////

                        boolean someCartridgeShouldSync = false;
                        for (Cartridge cartridge : cartridges.values()) {
                            if (cartridge.isTriggered()) {
                                someCartridgeShouldSync = true;
                                break;
                            }
                        }
                        boolean reportShouldSync = someCartridgeShouldSync || report.isTriggered();
                        boolean trackShouldSync = reportShouldSync || track.isTriggered();

                        Future<JSONObject> apiCall = null;
                        JSONObject apiResponse = null;

                        if (trackShouldSync) {
                            apiCall = syncerExecutor.submit(track); // apiThreadPool.schedule(track, 1000, TimeUnit.MILLISECONDS);
                            if (DopamineKit.debugMode) {
                                while (!apiCall.isDone()) {
                                    DopamineKit.debugLog("SyncCoordinator", "Waiting for track syncer to be done...");
                                    Thread.sleep(200);
                                }
                            }

                            apiResponse = apiCall.get();
                            Log.v("SyncCoordinator", "Track api response:" + apiResponse == null ? "null" : apiResponse.toString());
                            if (apiResponse == null || apiResponse.optInt("status", 404) == 404) {
                                DopamineKit.debugLog("SyncCoordinator", "Something went wrong while syncing tracker... Halting early.");
                                return null;
                            } else {
                                DopamineKit.debugLog("SyncCoordinator", "Track Syncer is done!");
                                Thread.sleep(1000);
                            }

                        }

                        if (reportShouldSync) {
                            apiCall = syncerExecutor.submit(report);
                            if (DopamineKit.debugMode) {
                                while (!apiCall.isDone()) {
                                    DopamineKit.debugLog("SyncCoordinator", "Waiting for report syncer to be done...");
                                    Thread.sleep(200);
                                }
                            }

                            apiResponse = apiCall.get();
                            DopamineKit.debugLog("SyncCoordinator", "Report api response:" + apiResponse == null ? "null" : apiResponse.toString());
                            if (apiResponse == null || apiResponse.optInt("status", 404) == 404) {
                                DopamineKit.debugLog("SyncCoordinator", "Something went wrong while syncing reporter... Halting early.");
                                return null;
                            } else {
                                DopamineKit.debugLog("SyncCoordinator", "Report Syncer is done!");
                                Thread.sleep(5000);
                            }
                        }

                        for (Map.Entry<String, Cartridge> entry : cartridges.entrySet()) {
                            if (entry.getValue().isTriggered()) {
                                apiCall = syncerExecutor.submit(entry.getValue());
                                if (DopamineKit.debugMode) {
                                    while (!apiCall.isDone()) {
                                        DopamineKit.debugLog("SyncCoordinator", "Waiting for " + entry.getKey() + " cartridge refresh to be done...");
                                        Thread.sleep(200);
                                    }
                                }

                                apiResponse = apiCall.get();
                                DopamineKit.debugLog("SyncCoordinator", "Refresh api response:" + (apiResponse == null ? "null" : apiResponse.toString()));
                                if (apiResponse == null || apiResponse.optInt("status", 404) == 404) {
                                    DopamineKit.debugLog("SyncCoordinator", "Something went wrong while syncing cartridge... Halting early.");
                                    return null;
                                } else {
                                    DopamineKit.debugLog("SyncCoordinator", entry.getKey() + " cartridge syncer is done!");
                                }
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

    public void removeSyncers() {
        track.removeTriggers();
        report.removeTriggers();
        for (Cartridge cartridge : cartridges.values()) {
            cartridge.removeTriggers();
        }
        cartridges.clear();
        preferences.edit().remove(preferencesActionIDSet).apply();
    }
}
