package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import com.usedopamine.dopaminekit.DopamineKit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class SyncCoordinator extends ContextWrapper implements Callable<Void> {

    private static SyncCoordinator sharedInstance;

    private Telemetry telemetry;
    private Track track;
    private Report report;
    private HashMap<String, Cartridge> cartridges;

    // static reference to known actionIDs
    private SharedPreferences preferences;
    private final String preferencesName = "com.usedopamine.synchronization.synccoordinator";
    private final String preferencesActionIDSet = "actionidset";

    private ExecutorService syncerExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService myExecutor = Executors.newFixedThreadPool(3);

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

        telemetry = Telemetry.getSharedInstance(base);
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
    }

    /**
     * Stores a tracked action to be synced.
     *
     * @param action A tracked action
     */
    public void storeTrackedAction(DopeAction action) {
        track.store(action);
        performSync();
    }

    /**
     * Stores a reinforced action to be synced.
     *
     * @param action A reinforced action
     */
    public void storeReportedAction(DopeAction action) {
        report.store(action);
        performSync();
    }

    /**
     * Finds the right cartridge for an action and returns a reinforcement decision.
     *
     * @param context  Context
     * @param actionID The action to retrieve a reinforcement decision for
     * @return A reinforcement decision
     */
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

    /**
     * Checks which syncers have been triggered, and syncs them in an order
     * that allows time for the DopamineAPI to generate fresh cartridges.
     */
    public void performSync() {
        myExecutor.submit(this);
    }

    @Override
    public Void call() throws Exception {

        if (syncInProgress) {
            DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
        } else {
            synchronized (syncLock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
                } else {
                    try {
                        syncInProgress = true;

                        // since a cartridge might be triggered during the sleep time,
                        // lazily check which are triggered
                        Cartridge someCartridgeToSync = null;
                        for (Cartridge cartridge : cartridges.values()) {
                            if (cartridge.isTriggered()) {
                                someCartridgeToSync = cartridge;
                                break;
                            }
                        }
                        boolean reportShouldSync = (someCartridgeToSync != null) || report.isTriggered();
                        boolean trackShouldSync = reportShouldSync || track.isTriggered();

                        if (trackShouldSync) {
                            String syncCause;
                            if (someCartridgeToSync != null) {
                                syncCause = "Cartridge " + someCartridgeToSync.actionID + " needs to sync.";
                            } else if (reportShouldSync) {
                                syncCause = "Report needs to sync.";
                            } else {
                                syncCause = "Track needs to sync.";
                            }

                            Future<Integer> apiCall;
                            Integer apiResponse;
                            telemetry.startRecordingSync(syncCause, track, report, cartridges);

                            // Track syncing
                            //
                            apiCall = syncerExecutor.submit(track);
                            if (DopamineKit.debugMode) {
                                while (!apiCall.isDone()) {
                                    DopamineKit.debugLog("SyncCoordinator", "Waiting for track syncer to be done...");
                                    Thread.sleep(200);
                                }
                            }
                            apiResponse = apiCall.get();
                            if (apiResponse == 200) {
                                DopamineKit.debugLog("SyncCoordinator", "Track Syncer is done!");
                                Thread.sleep(1000);
                            } else if (apiResponse < 0) {
                                DopamineKit.debugLog("SyncCoordinator", "Track failed during sync cycle. Halting sync cycle early.");
                                telemetry.stopRecordingSync(false);
                                return null;
                            }

                            // Report syncing
                            //
                            if (reportShouldSync) {
                                apiCall = syncerExecutor.submit(report);
                                if (DopamineKit.debugMode) {
                                    while (!apiCall.isDone()) {
                                        DopamineKit.debugLog("SyncCoordinator", "Waiting for report syncer to be done...");
                                        Thread.sleep(200);
                                    }
                                }
                                apiResponse = apiCall.get();
                                if (apiResponse == 200) {
                                    DopamineKit.debugLog("SyncCoordinator", "Report Syncer is done!");
                                    Thread.sleep(5000);
                                } else if (apiResponse < 0) {
                                    DopamineKit.debugLog("SyncCoordinator", "Report failed during sync cycle. Halting sync cycle early.");
                                    telemetry.stopRecordingSync(false);
                                    return null;
                                }
                            }


                            // Cartridge syncing
                            // lazily check
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
                                    if (apiResponse == 200) {
                                        DopamineKit.debugLog("SyncCoordinator", entry.getKey() + " cartridge syncer is done!");
                                    } else if (apiResponse < 0) {
                                        DopamineKit.debugLog("SyncCoordinator", "Cartridge " + entry.getKey() + " failed during sync cycle. Halting sync cycle early.");
                                        telemetry.stopRecordingSync(false);
                                        return null;
                                    }
                                }
                            }

                            telemetry.stopRecordingSync(true);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Telemetry.storeException(e);
                        telemetry.stopRecordingSync(false);
                    } finally {
                        syncInProgress = false;
                    }
                }

            }

        }
        return null;
    }

    /**
     * Erase the syncer triggers
     */
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
