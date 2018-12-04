package kit.boundless.internal.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import kit.boundless.BoundlessKit;
import kit.boundless.internal.api.BoundlessAPI;
import kit.boundless.internal.data.storage.SQLiteDataStore;

class Boot extends ContextWrapper implements Callable<Integer> {

    private static Boot sharedInstance;

    private SQLiteDatabase sqlDB;
    private SharedPreferences preferences;

    private String preferencesName() {
        return "boundless.boundlesskit.synchronization.boot";
    }

    private static final String initialBootKey = "initialBoot";
    private static final String versionIdKey = "versionId";
    private static final String configIdKey = "configId";
    private static final String reinforcementEnabledKey = "reinforcementEnabled";
    private static final String trackingEnabledKey = "trackingEnabled";

    // stored
    public boolean initialBoot;
    public @Nullable String versionId;
    public String configId;
    public boolean  reinforcementEnabled;
    public boolean trackingEnabled;

    // transient
    public boolean didSync;
    public @Nullable String internalId;
    public @Nullable String externalId;
    public @Nullable String experimentGroup;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    static Boot getSharedInstance(Context base) {
        if (sharedInstance == null) {
            sharedInstance = new Boot(base);
        }
        return sharedInstance;
    }

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

    void update() {
        preferences.edit()
                .putBoolean(initialBootKey, initialBoot)
                .putString(versionIdKey, versionId)
                .putString(configIdKey, configId)
                .putBoolean(reinforcementEnabledKey, reinforcementEnabled)
                .putBoolean(trackingEnabledKey, trackingEnabled)
                .apply();
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

    @Override
    public Integer call() throws Exception {
        if (syncInProgress) {
            BoundlessKit.debugLog("Boot", "Boot sync already happening");
            return 0;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    BoundlessKit.debugLog("Boot", "Boot sync already happening");
                    return 0;
                } else {
                    try {
                        syncInProgress = true;
                        BoundlessKit.debugLog("Boot", "Beginning sync for boot");

                        JSONObject apiResponse = BoundlessAPI.boot(this, requestJSON());
                        if (apiResponse != null) {
                            String error = apiResponse.optString("error");
                            if (!error.isEmpty()) {
                                BoundlessKit.debugLog("Boot", "Got error:" + error);
                                return -1;
                            }
                            BoundlessKit.debugLog("Boot", "Successful boot");
                            didSync = true;
                            initialBoot = false;

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
                            BoundlessKit.debugLog("Boot", "Something went wrong with boot call. No internet?");
                            return -1;
                        }
                    } finally {
                        syncInProgress = false;
                    }
                }
            }
        }
    }
}
