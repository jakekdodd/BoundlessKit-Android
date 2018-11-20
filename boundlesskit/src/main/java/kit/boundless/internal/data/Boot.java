package kit.boundless.internal.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

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
    private static final String configIdKey = "configId";
    private static final String reinforcementEnabledKey = "reinforcementEnabled";
    private static final String trackingEnabledKey = "trackingEnabled";

    public boolean didSync;
    public boolean initialBoot;
    public String configId;
    public boolean  reinforcementEnabled;
    public boolean trackingEnabled;

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
        preferences = getSharedPreferences(preferencesName(), 0);
        initialBoot = preferences.getBoolean(initialBootKey, true);
        configId = preferences.getString(configIdKey, "0");
        reinforcementEnabled = preferences.getBoolean(reinforcementEnabledKey, true);
        trackingEnabled = preferences.getBoolean(trackingEnabledKey, true);
    }

    void update() {
        preferences.edit()
                .putBoolean(initialBootKey, initialBoot)
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
    public JSONObject valueToJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(initialBootKey, initialBoot);
            json.put(configIdKey, configId);
            json.put(reinforcementEnabledKey, reinforcementEnabled);
            json.put(trackingEnabledKey, trackingEnabled);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
        }
        return json;
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

                        JSONObject apiResponse = BoundlessAPI.boot(this);
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            update();
                            return 200;
                        } else {
                            BoundlessKit.debugLog("Boot", "Something with boot call.");
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
