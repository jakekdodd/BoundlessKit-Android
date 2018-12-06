package kit.boundless.internal.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import kit.boundless.BoundlessKit;
import kit.boundless.internal.data.storage.contracts.ReinforcementDecisionContract;
import kit.boundless.internal.data.storage.SQLCartridgeDataHelper;
import kit.boundless.internal.data.storage.SQLiteDataStore;
import kit.boundless.internal.api.BoundlessAPI;

/**
 * Created by cuddergambino on 9/6/16.
 */

class Cartridge extends ContextWrapper implements Callable<Integer> {

    private final String TAG = "CartridgeSyncer";

    private SQLiteDatabase sqlDB;
    private SharedPreferences preferences;

    private String preferencesName() {
        return "boundless.boundlesskit.synchronization.cartridge." + actionId;
    }

    private static final String actionIdKey = "actionName";
    private static final String sizeKey = "size";
    private static final String capacityToSyncKey = "capacityToSync";
    private static final String initialSizeKey = "initialSize";
    private static final String timerStartsAtKey = "timerStartsAt";
    private static final String timerExpiresInKey = "timerExpiresIn";

    public final String actionId;
    private int initialSize;
    private long timerStartsAt;
    private long timerExpiresIn;

    private static final double capacityToSync = 0.25;
    private static final int minimumCount = 2;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    protected Cartridge(Context base, String actionId) {
        super(base);
        this.actionId = actionId;

        sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
        preferences = getSharedPreferences(preferencesName(), Context.MODE_PRIVATE);
        initialSize = preferences.getInt(initialSizeKey, 0);
        timerStartsAt = preferences.getLong(timerStartsAtKey, 0);
        timerExpiresIn = preferences.getLong(timerExpiresInKey, 0);
    }

    /**
     * @return Whether a sync should be started
     */
    public boolean isTriggered() {
        return timerDidExpire() || isCapacityToSync();
    }

    /**
     * @return Whether there is a live ammo in the cartridge
     */
    public boolean isFresh() {
        return !timerDidExpire() && SQLCartridgeDataHelper.countFor(sqlDB, actionId) >= 1;
    }

    /**
     * Updates the sync triggers.
     *
     * @param size      The number of reported actions to trigger a sync
     * @param startTime The start time for a sync timer
     * @param expiresIn The timer length, in ms, for a sync timer
     */
    public void updateTriggers(Integer size, Long startTime, Long expiresIn) {
        initialSize = size;
        timerStartsAt = startTime;
        timerExpiresIn = expiresIn;

        preferences.edit()
                .putInt(initialSizeKey, initialSize)
                .putLong(timerStartsAtKey, timerStartsAt)
                .putLong(timerExpiresInKey, timerExpiresIn)
                .apply();
    }

    /**
     * Clears the saved cartridge sync triggers.
     */
    public void removeTriggers() {
        initialSize = 0;
        timerStartsAt = 0;
        timerExpiresIn = 0;
        preferences.edit().clear().apply();
    }

    /**
     * This function returns a snapshot of this instance as a JSONObject.
     *
     * @return A JSONObject containing the size and sync triggers
     */
    public JSONObject jsonForTriggers() {
        JSONObject json = new JSONObject();
        try {
            json.put(actionIdKey, actionId);
            json.put(sizeKey, SQLCartridgeDataHelper.countFor(sqlDB, actionId));
            json.put(initialSizeKey, initialSize);
            json.put(capacityToSyncKey, capacityToSync);
            json.put(timerStartsAtKey, timerStartsAt);
            json.put(timerExpiresInKey, timerExpiresIn);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
        }
        return json;
    }

    private boolean timerDidExpire() {
        long currentTime = System.currentTimeMillis();
        boolean isExpired = currentTime >= (timerStartsAt + timerExpiresIn);
        return isExpired;
    }

    private boolean isCapacityToSync() {
        int count = SQLCartridgeDataHelper.countFor(sqlDB, actionId);
        boolean isCapacity = count < minimumCount || (double) count / initialSize <= capacityToSync;
        BoundlessKit.debugLog(TAG, "Cartridge for actionId:(" + actionId + ") has " + count + "/" + initialSize + " decisions remaining in its queue" + (isCapacity ? " so needs to sync..." : "."));
        return isCapacity;
    }

    /**
     * Stores a reinforcement decision in the cartridge
     *
     * @param reinforcementDecision The decision to be stored
     */
    public void store(String cartridgeId, String reinforcementDecision) {
        long rowId = SQLCartridgeDataHelper.insert(sqlDB, new ReinforcementDecisionContract(
                0, actionId, cartridgeId, reinforcementDecision
        ));
//        BoundlessKit.debugLog(TAG, "Inserted "+reinforcementDecision+" into row "+rowId+" for action "+actionId);
    }

    /**
     * Dispenses a reinforcement decision from the cartridge.
     *
     * @return A reinforcement decision string. If there are no reinforcements, a neutral decision is returned.
     */
    public String dispenseReinforcement() {
        String reinforcementDecision = BoundlessAction.NEUTRAL_DECISION;

        if (isFresh()) {
            ReinforcementDecisionContract rdc = SQLCartridgeDataHelper.findFirstFor(sqlDB, actionId);
            if (rdc != null) {
                reinforcementDecision = rdc.reinforcementDecision;
                SQLCartridgeDataHelper.delete(sqlDB, rdc);
            }
        }

        return reinforcementDecision;
    }

    @Override
    public Integer call() throws Exception {
        if (syncInProgress) {
            BoundlessKit.debugLog(TAG, "Cartridge sync already happening for " + actionId);
            return 0;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    BoundlessKit.debugLog(TAG, "Cartridge sync already happening for " + actionId);
                    return 0;
                } else {
                    try {
                        syncInProgress = true;
                        BoundlessKit.debugLog(TAG, "Beginning cartridge sync for " + actionId + "!");

                        JSONObject apiResponse = BoundlessAPI.refresh(this, actionId);
                        if (apiResponse != null) {

                            JSONArray errors = apiResponse.optJSONArray("errors");
                            if (errors != null) {
                                BoundlessKit.debugLog(TAG, "Got errors:" + errors);
                                return -1;
                            }

                            BoundlessKit.debugLog(TAG, "Replacing cartridge for " + actionId + "...");
                            JSONArray reinforcementCartridge = apiResponse.getJSONArray("reinforcements");
                            long expiresIn = apiResponse.getLong("ttl");
                            String  cartridgeId = apiResponse.getString("cartridgeId");

                            SQLCartridgeDataHelper.deleteAllFor(sqlDB, actionId);
                            for (int i = 0; i < reinforcementCartridge.length(); i++) {
                                store(cartridgeId, reinforcementCartridge.getJSONObject(i).getString("reinforcementName"));
                            }
                            updateTriggers(reinforcementCartridge.length(), System.currentTimeMillis(), expiresIn);
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
}