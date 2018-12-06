package kit.boundless.internal.data;

import java.util.concurrent.Callable;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import kit.boundless.BoundlessKit;
import kit.boundless.internal.api.BoundlessAPI;
import kit.boundless.internal.data.storage.SQLiteDataStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Boot extends ContextWrapper implements Callable<Integer> {

  private static final String initialBootKey = "initialBoot";
  private static final String versionIdKey = "versionId";
  private static final String configIdKey = "configId";
  private static final String reinforcementEnabledKey = "reinforcementEnabled";
  private static final String trackingEnabledKey = "trackingEnabled";
  private static Boot sharedInstance;
  private final String TAG = "BootSyncer";
  private final Object apiSyncLock = new Object();
  // stored
  public boolean initialBoot;
  public @Nullable
  String versionId;
  public String configId;
  public boolean reinforcementEnabled;
  public boolean trackingEnabled;
  // transient
  public boolean didSync;
  public @Nullable
  String internalId;
  public @Nullable
  String externalId;
  public @Nullable
  String experimentGroup;
  private SQLiteDatabase sqlDB;
  private SharedPreferences preferences;
  private Boolean syncInProgress = false;

  private Boot(Context base) {
    super(base);
    didSync = false;

    sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
    preferences = getSharedPreferences(preferencesName(), Context.MODE_PRIVATE);
    initialBoot = preferences.getBoolean(initialBootKey, true);
    versionId = preferences.getString(versionIdKey, null);
    configId = preferences.getString(configIdKey, "0");
    reinforcementEnabled = preferences.getBoolean(reinforcementEnabledKey, true);
    trackingEnabled = preferences.getBoolean(trackingEnabledKey, true);
  }

  private String preferencesName() {
    return "boundless.boundlesskit.synchronization.boot";
  }

  static Boot getSharedInstance(Context base) {
    if (sharedInstance == null) {
      sharedInstance = new Boot(base);
    }
    return sharedInstance;
  }

  @Override
  public Integer call() throws Exception {
    if (syncInProgress) {
      BoundlessKit.debugLog(TAG, "Boot sync already happening");
      return 0;
    } else {
      synchronized (apiSyncLock) {
        if (syncInProgress) {
          BoundlessKit.debugLog(TAG, "Boot sync already happening");
          return 0;
        } else {
          try {
            syncInProgress = true;
            BoundlessKit.debugLog(TAG, "Beginning sync for boot");

            JSONObject apiResponse = BoundlessAPI.boot(this, requestJSON());
            if (apiResponse != null) {
              didSync = true;
              initialBoot = false;

              JSONArray errors = apiResponse.optJSONArray("errors");
              if (errors != null) {
                BoundlessKit.debugLog(TAG, "Got errors:" + errors);
                return -1;
              }
              BoundlessKit.debugLog(TAG, "Successful boot");

              try {
                JSONObject configJSON = apiResponse.optJSONObject("config");
                if (configJSON != null) {
                  configId = configJSON.getString("configID");
                  reinforcementEnabled = configJSON.getBoolean("reinforcementEnabled");
                  trackingEnabled = configJSON.getBoolean("trackingEnabled");
                }

                JSONObject versionJSON = apiResponse.optJSONObject("version");
                if (versionJSON != null) {
                  versionId = versionJSON.getString("versionID");
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
              BoundlessKit.debugLog(TAG, "Could not send request.");
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
  public JSONObject requestJSON() {
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

  void update() {
    preferences.edit()
        .putBoolean(initialBootKey, initialBoot)
        .putString(versionIdKey, versionId)
        .putString(configIdKey, configId)
        .putBoolean(reinforcementEnabledKey, reinforcementEnabled)
        .putBoolean(trackingEnabledKey, trackingEnabled)
        .apply();
  }
}
