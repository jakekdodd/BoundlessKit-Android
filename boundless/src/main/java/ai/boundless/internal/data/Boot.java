package ai.boundless.internal.data;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

import ai.boundless.BoundlessKit;
import ai.boundless.internal.api.BoundlessApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The type Boot.
 */
class Boot extends ContextWrapper implements Callable<Integer> {

  private static final ReentrantLock LOCK = new ReentrantLock();

  private static final String INITIAL_BOOT_KEY = "initialBoot";
  private static final String VERSION_ID_KEY = "versionId";
  private static final String CONFIG_ID_KEY = "configId";
  private static final String REINFORCEMENT_ENABLED_KEY = "reinforcementEnabled";
  private static final String TRACKING_ENABLED_KEY = "trackingEnabled";

  private static volatile Boot sharedInstance;
  private final String tag = "BootSyncer";
  private final Object apiSyncLock = new Object();
  /**
   * The Initial boot.
   */
  // stored
  public boolean initialBoot;

  /**
   * The Version id.
   */
  @Nullable
  public String versionId;

  /**
   * The Config id.
   */
  public String configId;
  /**
   * The Reinforcement enabled.
   */
  public boolean reinforcementEnabled;
  /**
   * The Tracking enabled.
   */
  public boolean trackingEnabled;
  /**
   * The Did sync.
   */
  // transient
  public boolean didSync;

  /**
   * The Internal id.
   */
  @Nullable
  public String internalId;

  /**
   * The External id.
   */
  @Nullable
  public String externalId;

  /**
   * The Experiment group.
   */
  @Nullable
  public String experimentGroup;

  private final SharedPreferences preferences;
  private volatile boolean syncInProgress;

  private Boot(Context base) {
    super(base);
    didSync = false;

    preferences = getSharedPreferences(preferencesName(), Context.MODE_PRIVATE);
    initialBoot = preferences.getBoolean(INITIAL_BOOT_KEY, true);
    versionId = preferences.getString(VERSION_ID_KEY, null);
    configId = preferences.getString(CONFIG_ID_KEY, "0");
    reinforcementEnabled = preferences.getBoolean(REINFORCEMENT_ENABLED_KEY, true);
    trackingEnabled = preferences.getBoolean(TRACKING_ENABLED_KEY, true);
  }

  private String preferencesName() {
    return "boundless.boundlesskit.synchronization.boot";
  }

  /**
   * Gets shared instance.
   *
   * @param base the base
   * @return the shared instance
   */
  static Boot getSharedInstance(Context base) {
    if (sharedInstance == null) {
      LOCK.lock();
      try {
        if (sharedInstance == null) {
          sharedInstance = new Boot(base);
        }
      } finally {
        LOCK.unlock();
      }

    }
    return sharedInstance;
  }

  @Override
  public Integer call() throws Exception {
    if (syncInProgress) {
      BoundlessKit.debugLog(tag, "Boot sync already happening");
      return 0;
    } else {
      synchronized (apiSyncLock) {
        if (syncInProgress) {
          BoundlessKit.debugLog(tag, "Boot sync already happening");
          return 0;
        } else {
          try {
            syncInProgress = true;
            BoundlessKit.debugLog(tag, "Beginning sync for boot");

            JSONObject apiResponse = BoundlessApi.boot(this, requestJson());
            if (apiResponse != null) {
              didSync = true;
              initialBoot = false;

              JSONArray errors = apiResponse.optJSONArray("errors");
              if (errors != null) {
                BoundlessKit.debugLog(tag, "Got errors:" + errors);
                return -1;
              }
              BoundlessKit.debugLog(tag, "Successful boot");

              try {
                JSONObject configJson = apiResponse.optJSONObject("config");
                if (configJson != null) {
                  configId = configJson.getString("configID");
                  reinforcementEnabled = configJson.getBoolean("reinforcementEnabled");
                  trackingEnabled = configJson.getBoolean("trackingEnabled");
                }

                JSONObject versionJson = apiResponse.optJSONObject("version");
                if (versionJson != null) {
                  versionId = versionJson.getString("versionID");
                }

                String internalId = apiResponse.optString("internalId");
                if (!internalId.isEmpty()) {
                  this.internalId = internalId;
                }

                String experimentGroup = apiResponse.optString("experimentGroup");
                if (!experimentGroup.isEmpty()) {
                  this.experimentGroup = experimentGroup;
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }

              update();
              return 200;
            } else {
              BoundlessKit.debugLog(tag, "Could not send request.");
              return -1;
            }
          } finally {
            syncInProgress = false;
          }
        }
      }
    }
  }

  /**
   * This function returns a snapshot of this instance as a JSONObject.
   *
   * @return A JSONObject containing the size and sync triggers
   */
  public JSONObject requestJson() {
    JSONObject payload = new JSONObject();
    try {
      payload.put("initialBoot", initialBoot);
      payload.put("currentVersion", versionId);
      payload.put("currentConfig", configId);
      payload.put("internalId", internalId);
      payload.put("externalId", externalId);
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
    return payload;
  }

  /**
   * Update.
   */
  void update() {
    preferences.edit()
        .putBoolean(INITIAL_BOOT_KEY, initialBoot)
        .putString(VERSION_ID_KEY, versionId)
        .putString(CONFIG_ID_KEY, configId)
        .putBoolean(REINFORCEMENT_ENABLED_KEY, reinforcementEnabled)
        .putBoolean(TRACKING_ENABLED_KEY, trackingEnabled)
        .apply();
  }
}
