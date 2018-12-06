package ai.boundless.internal.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.boundless.BoundlessKit;
import ai.boundless.internal.api.BoundlessApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

/**
 * Created by cuddergambino on 8/4/16.
 */
public class SyncCoordinator extends ContextWrapper implements Callable<Void> {

  private static SyncCoordinator sharedInstance;
  private final String preferencesName = "boundless.boundlesskit.synchronization.synccoordinator";
  private final String preferencesActionIdSet = "actionidset";
  private final Object syncLock = new Object();
  private Telemetry telemetry;
  private BoundlessApi api;
  private BoundlessUser user;
  private Boot boot;
  private Track track;
  private Report report;
  private HashMap<String, Cartridge> cartridges;
  // static reference to known actionIds
  private SharedPreferences preferences;
  private ExecutorService syncerExecutor = Executors.newSingleThreadExecutor();
  private ExecutorService myExecutor = Executors.newFixedThreadPool(3);
  private Boolean syncInProgress = false;

  private SyncCoordinator(Context base) {
    super(base);

    api = BoundlessApi.getInstance(base);
    user = BoundlessUser.getSharedInstance(base);
    telemetry = Telemetry.getSharedInstance(base);
    boot = Boot.getSharedInstance(base);
    track = Track.getSharedInstance(base);
    report = Report.getSharedInstance(base);
    cartridges = new HashMap<>();

    preferences = getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    Set<String> actionIds = preferences.getStringSet(preferencesActionIdSet, new HashSet<String>());
    BoundlessKit.debugLog("SyncCoordinator", "Loading known actionsIDS...");
    for (String actionId : actionIds) {
      cartridges.put(actionId, new Cartridge(base, actionId));
      BoundlessKit.debugLog("SyncCoordinator", "Loaded cartridge for actionId:" + actionId);
    }
    BoundlessKit.debugLog("SyncCoordinator", "Done loading known actionsIDS.");

    beforeBoot();
  }

  private void beforeBoot() {
    boot.internalId = user.internalId;
    boot.externalId = user.externalId;
    boot.experimentGroup = user.experimentGroup;

    if (boot.versionId == null) {
      boot.versionId = api.credentials.versionId;
    }
    api.credentials.primaryIdentity = boot.externalId;
  }

  /**
   * Gets instance.
   *
   * @param context the context
   * @return the instance
   */
  public static SyncCoordinator getInstance(Context context) {
    if (sharedInstance == null) {
      sharedInstance = new SyncCoordinator(context);
    }
    return sharedInstance;
  }

  /**
   * Map external id.
   *
   * @param externalId the external id
   */
  public void mapExternalId(String externalId) {
    BoundlessKit.debugLog("SyncCoordinator", "Mapping externalID:" + externalId + "...");
    user.externalId = externalId;
    user.update();
    boot.didSync = false;
    performSync();
  }

  /**
   * Checks which syncers have been triggered, and syncs them in an order.
   * that allows time for the BoundlessAPI to generate fresh cartridges.
   */
  public void performSync() {
    myExecutor.submit(this);
  }

  /**
   * Gets user.
   *
   * @return the user
   */
  public BoundlessUser getUser() {
    return user;
  }

  /**
   * Stores a tracked action to be synced.
   *
   * @param action A tracked action
   */
  public void storeTrackedAction(BoundlessAction action) {
    if (boot.trackingEnabled) {
      track.store(action);
      performSync();
      return;
    }
    BoundlessKit.debugLog("SyncCoordinator", "Tracking disabled");
  }

  /**
   * Stores a reinforced action to be synced.
   *
   * @param action A reinforced action
   */
  public void storeReportedAction(BoundlessAction action) {
    if (boot.reinforcementEnabled) {
      report.store(action);
      performSync();
      return;
    }
    BoundlessKit.debugLog("SyncCoordinator", "Reinforcements disabled");
  }

  /**
   * Finds the right cartridge for an action and returns a reinforcement decision.
   *
   * @param context Context
   * @param actionId The action to retrieve a reinforcement decision for
   * @return A reinforcement decision
   */
  public String removeReinforcementDecisionFor(Context context, String actionId) {
    if (boot.reinforcementEnabled) {
      Cartridge cartridge = cartridges.get(actionId);
      if (cartridge == null) {
        cartridge = new Cartridge(this, actionId);
        cartridges.put(actionId, cartridge);
        preferences.edit().putStringSet(preferencesActionIdSet, cartridges.keySet()).apply();
        BoundlessKit.debugLog("SyncCoordinator",
            "Created a cartridge for " + actionId + " for the first time!"
        );
      }
      return cartridge.dispenseReinforcement();
    }
    BoundlessKit.debugLog("SyncCoordinator", "Reinforcements disabled");
    return BoundlessAction.NEUTRAL_DECISION;
  }

  @Override
  public Void call() throws Exception {

    if (syncInProgress) {
      BoundlessKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
    } else {
      synchronized (syncLock) {
        if (syncInProgress) {
          BoundlessKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
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
            boolean bootShouldSync = !boot.didSync;
            boolean reportShouldSync = (someCartridgeToSync != null) || report.isTriggered();
            boolean trackShouldSync = reportShouldSync || track.isTriggered();

            if (bootShouldSync || trackShouldSync) {
              String syncCause = bootShouldSync ? "Will send boot call.\n" : "";
              if (someCartridgeToSync != null) {
                syncCause += "Cartridge " + someCartridgeToSync.actionId + " needs to sync.";
              } else if (reportShouldSync) {
                syncCause += "Report needs to sync.";
              } else if (trackShouldSync) {
                syncCause += "Track needs to sync.";
              }
              BoundlessKit.debugLog("SyncCoordinator", "Sync cause:" + syncCause);

              Future<Integer> apiCall;
              Integer apiResponse;
              telemetry.startRecordingSync(syncCause, track, report, cartridges);

              // Boot syncing
              if (bootShouldSync) {
                beforeBoot();

                apiCall = syncerExecutor.submit(boot);
                if (BoundlessKit.debugMode) {
                  while (!apiCall.isDone()) {
                    BoundlessKit.debugLog("SyncCoordinator",
                        "Waiting for boot syncer to be done..."
                    );
                    Thread.sleep(200);
                  }
                }
                apiResponse = apiCall.get();
                if (apiResponse == 200) {
                  BoundlessKit.debugLog("Boot", "Boot Syncer is done!");

                  afterBoot();
                } else if (apiResponse < 0) {
                  BoundlessKit.debugLog("SyncCoordinator",
                      "Boot failed during sync cycle. Halting sync cycle early."
                  );
                  telemetry.stopRecordingSync(false);
                  return null;
                }
              }


              // Track syncing
              //
              if (trackShouldSync) {
                apiCall = syncerExecutor.submit(track);
                if (BoundlessKit.debugMode) {
                  while (!apiCall.isDone()) {
                    BoundlessKit.debugLog("SyncCoordinator",
                        "Waiting for track syncer to be done..."
                    );
                    Thread.sleep(200);
                  }
                }
                apiResponse = apiCall.get();
                if (apiResponse == 200) {
                  BoundlessKit.debugLog("SyncCoordinator", "Track Syncer is done!");
                } else if (apiResponse < 0) {
                  BoundlessKit.debugLog("SyncCoordinator",
                      "Track failed during sync cycle. Halting sync cycle early."
                  );
                  telemetry.stopRecordingSync(false);
                  return null;
                }
              }

              // Report syncing
              //
              if (reportShouldSync) {
                apiCall = syncerExecutor.submit(report);
                if (BoundlessKit.debugMode) {
                  while (!apiCall.isDone()) {
                    BoundlessKit.debugLog("SyncCoordinator",
                        "Waiting for report syncer to be done..."
                    );
                    Thread.sleep(200);
                  }
                }
                apiResponse = apiCall.get();
                if (apiResponse == 200) {
                  BoundlessKit.debugLog("SyncCoordinator", "Report Syncer is done!");
                } else if (apiResponse < 0) {
                  BoundlessKit.debugLog(
                      "SyncCoordinator",
                      "Report failed during sync cycle. Halting sync cycle early."
                  );
                  telemetry.stopRecordingSync(false);
                  return null;
                }
              }


              // Cartridge syncing
              // lazily check
              for (Map.Entry<String, Cartridge> entry : cartridges.entrySet()) {
                if (entry.getValue().isTriggered()) {
                  apiCall = syncerExecutor.submit(entry.getValue());
                  if (BoundlessKit.debugMode) {
                    while (!apiCall.isDone()) {
                      BoundlessKit.debugLog("SyncCoordinator",
                          "Waiting for " + entry.getKey() + " cartridge refresh to be done..."
                      );
                      Thread.sleep(200);
                    }
                  }
                  apiResponse = apiCall.get();
                  if (apiResponse == 200) {
                    BoundlessKit.debugLog("SyncCoordinator",
                        entry.getKey() + " cartridge syncer is done!"
                    );
                  } else if (apiResponse < 0) {
                    BoundlessKit.debugLog("SyncCoordinator",
                        "Cartridge " + entry.getKey()
                            + " failed during sync cycle. Halting sync cycle early."
                    );
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

  private void afterBoot() {
    // update App Experiment info after boot
    user.internalId = boot.internalId;
    user.externalId = boot.externalId;
    user.experimentGroup = boot.experimentGroup;
    user.update();

    if (boot.versionId != null) {
      api.credentials.versionId = boot.versionId;
    }
    api.credentials.primaryIdentity = boot.externalId;
  }

  /**
   * Erase the syncer triggers.
   */
  public void removeSyncers() {
    track.removeTriggers();
    report.removeTriggers();
    for (Cartridge cartridge : cartridges.values()) {
      cartridge.removeTriggers();
    }
    cartridges.clear();
    preferences.edit().remove(preferencesActionIdSet).apply();
  }

}
