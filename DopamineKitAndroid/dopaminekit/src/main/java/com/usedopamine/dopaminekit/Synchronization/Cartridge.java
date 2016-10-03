package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.ReinforcementDecisionContract;
import com.usedopamine.dopaminekit.DataStore.SQLCartridgeDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

/**
 * Created by cuddergambino on 9/6/16.
 */

public class Cartridge extends ContextWrapper implements Callable<JSONObject> {

    private DopamineAPI dopamineAPI;
    private SQLiteDatabase sqlDB;
    private SharedPreferences preferences;
    private final String preferencesName() { return "com.usedopamine.synchronization.cartridge." + actionID; }
    private static final String actionIDKey = "actionID";
    private static final String sizeKey = "size";
    private static final String capacityToSyncKey = "capacityToSync";
    private static final String initialSizeKey = "initialSize";
    private static final String timerStartsAtKey = "timerStartsAt";
    private static final String timerExpiresInKey = "timerExpiresIn";

    public String actionID;
    private int initialSize;
    private long timerStartsAt;
    private long timerExpiresIn;
    
    private static final double capacityToSync = 0.25;
    private static final int minimumCount = 2;

    private final Object apiSyncLock = new Object();
    private Boolean syncInProgress = false;

    protected Cartridge(Context base, String actionID) {
        super(base);
        this.actionID = actionID;

        dopamineAPI = DopamineAPI.getInstance(base);
        sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();
        preferences = getSharedPreferences(preferencesName(), 0);
        initialSize = preferences.getInt(initialSizeKey, 0);
        timerStartsAt = preferences.getLong(timerStartsAtKey, 0);
        timerExpiresIn = preferences.getLong(timerExpiresInKey, 0);
    }

    public boolean isTriggered() {
        return timerDidExpire() || isCapacityToSync();
    }

    public boolean isFresh() {
        return !timerDidExpire() && SQLCartridgeDataHelper.countFor(sqlDB, actionID) >= 1;
    }

    public void updateTriggers(Integer size, @Nullable Long startTime, Long expiresIn) {
        initialSize = size;
        if (startTime != null) {
            timerStartsAt = startTime;
        } else {
            timerStartsAt = System.currentTimeMillis();
        }
        timerExpiresIn = expiresIn;

        preferences.edit()
                .putInt(initialSizeKey, initialSize)
                .putLong(timerStartsAtKey, timerStartsAt)
                .putLong(timerExpiresInKey, timerExpiresIn)
                .apply();
    }

    public void removeTriggers() {
        initialSize = 0;
        timerStartsAt = 0;
        timerExpiresIn = 0;
        preferences.edit().clear().apply();
    }

    public JSONObject jsonForTriggers() {
        JSONObject json = new JSONObject();
        try {
            json.put(actionIDKey, actionID);
            json.put(sizeKey, SQLCartridgeDataHelper.countFor(sqlDB, actionID));
            json.put(initialSizeKey, initialSize);
            json.put(capacityToSyncKey, capacityToSync);
            json.put(timerStartsAtKey, timerStartsAt);
            json.put(timerExpiresInKey, timerExpiresIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private boolean timerDidExpire() {
        long currentTime = System.currentTimeMillis();
        boolean isExpired = currentTime >= (timerStartsAt + timerExpiresIn);
        DopamineKit.debugLog("Cartridge", "Cartridge for actionID:("+actionID+") timer expires in "+(timerStartsAt + timerExpiresIn - currentTime)+"ms so "+(isExpired ? "does" : "doesn't")+" need to sync...");
        return isExpired;
    }

    private boolean isCapacityToSync() {
        int count = SQLCartridgeDataHelper.countFor(sqlDB, actionID);
        boolean isCapacity = count < minimumCount || (double)count / initialSize <= capacityToSync;
        DopamineKit.debugLog("Cartridge", "Cartridge for actionID:("+actionID+") has "+count+"/"+initialSize+" decisions remaining, or "+100.0*count/initialSize+"% capacity, so "+(isCapacity ? "does" : "doesn't")+" need to sync since a cartridge requires at least "+minimumCount+" decisions or "+(capacityToSync*100)+"% capacity.");
        return isCapacity;
    }

    public void store(String reinforcementDecision) {
        long rowId = SQLCartridgeDataHelper.insert(sqlDB, new ReinforcementDecisionContract(
                0, actionID, reinforcementDecision
        ));
        DopamineKit.debugLog("Cartridge", "Inserted "+reinforcementDecision+" into row "+rowId+" for action "+actionID);
    }

    public String remove() {
        String reinforcementDecision = "neutralResponse";

        if (isFresh()) {
            ReinforcementDecisionContract rdc = SQLCartridgeDataHelper.findFirstFor(sqlDB, actionID);
            if (rdc != null) {
                reinforcementDecision = rdc.reinforcementDecision;
                SQLCartridgeDataHelper.delete(sqlDB, rdc);
            }
        }

        return reinforcementDecision;
    }

    public void removeAll() {
        SQLCartridgeDataHelper.deleteAllFor(sqlDB, actionID);
    }

    @Override
    public JSONObject call() throws Exception {
        if (syncInProgress) {
            DopamineKit.debugLog("Cartridge", "Cartridge sync already happening for " + actionID);
            return null;
        } else {
            synchronized (apiSyncLock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("Cartridge", "Cartridge sync already happening for " + actionID);
                    return null;
                } else {
                    JSONObject apiResponse = null;
                    try {
                        DopamineKit.debugLog("Cartridge", "Beginning cartridge sync for " + actionID + "!");
                        syncInProgress = true;

                        apiResponse = dopamineAPI.refresh(actionID);
                        if (apiResponse == null) {
                            DopamineKit.debugLog("Cartridge", "Something went wrong during the call...");
                        } else {
                            if (apiResponse.optInt("status", 404) == 200) {
                                DopamineKit.debugLog("Cartridge", "Replacing cartridge for " + actionID + "...");

                                JSONArray reinforcementCartridge = apiResponse.getJSONArray("reinforcementCartridge");
                                long expiresIn = apiResponse.getLong("expiresIn");

                                SQLCartridgeDataHelper.deleteAllFor(sqlDB, actionID);
                                for(int i = 0; i < reinforcementCartridge.length(); i++) {
                                    SQLCartridgeDataHelper.insert(sqlDB, new ReinforcementDecisionContract( 0, actionID, reinforcementCartridge.getString(i) ));
                                }

                                updateTriggers(reinforcementCartridge.length(), null, expiresIn);
                            } else {
                                DopamineKit.debugLog("Cartridge", "Something went wrong while syncing cartridge for " + actionID + "... Leaving decisions actions in sqlite db");
                            }
                        }
                    } finally {
                        syncInProgress = false;
                        return apiResponse;
                    }
                }
            }
        }
    }
}
