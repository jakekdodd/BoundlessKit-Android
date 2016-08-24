package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.Contracts.ReinforcementDecisionContract;
import com.usedopamine.dopaminekit.DataStore.SQLCartridgeDataHelper;
import com.usedopamine.dopaminekit.DopamineKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cuddergambino on 8/21/16.
 */

public class CartridgeSyncer extends Syncer {

    private static HashMap<String, CartridgeSyncer> cartridgeSyncers = null;

    private static SharedPreferences preferences;
    private static final String preferencesName = "DopamineCartridgeSyncer";
    private static final String preferencesReinforceableActions = "reinforceableActions";
    private static final String preferencesInitialSize = "initialSize";
    private static final String preferencesTimerStartsAt = "timerStartsAt";
    private static final String preferencesTimerExpiresIn = "timerExpiresIn";

    private static final double capacityToSync = 0.25;
    private static final int minimumCount = 15;

    private String actionID;
    private int initialSize;
    private long timerStartsAt;
    private long timerExpiresIn;

    private final Object apisynclock = new Object();
    private Boolean syncInProgress = false;

    private static void initializeStaticSyncer(Context context) {
        if (preferences==null || cartridgeSyncers == null) {
            preferences = context.getSharedPreferences(preferencesName, 0);
            cartridgeSyncers = new HashMap<>();
            Set<String> reinforceableActions = new HashSet<String>(preferences.getStringSet(preferencesReinforceableActions, new HashSet<String>()));
            for(String action : reinforceableActions) {
                cartridgeSyncers.put(action, new CartridgeSyncer(context, action));
            }
        }
    }

    public static CartridgeSyncer getCartridgeSyncerFor(Context context, String actionID) {
        initializeStaticSyncer(context);

        CartridgeSyncer syncer = cartridgeSyncers.get(actionID);
        if (syncer == null) {
            // Create a cartridge for the first time
            syncer = new CartridgeSyncer(context, actionID);

            // Store the action name for later retrieval
            Set<String> reinforceableDecisions = new HashSet<String>( preferences.getStringSet(preferencesReinforceableActions, new HashSet<String>()) );
            reinforceableDecisions.add(actionID);
            preferences.edit().putStringSet(preferencesReinforceableActions, reinforceableDecisions)
                    .commit();

            // Store the triggers for the first time
            syncer.updateTriggers(syncer.initialSize, syncer.timerStartsAt, syncer.timerExpiresIn);

            // Create the sql table
            SQLCartridgeDataHelper.createTable(sqlDB, actionID);

            cartridgeSyncers.put(actionID, syncer);

            DopamineKit.debugLog("CartridgeSyncer", "Created the first cartridge for " + actionID + "!");
        }

        return syncer;

    }

    private CartridgeSyncer(Context context, String actionID) {
        super(context);
        this.actionID = actionID;
        initialSize = preferences.getInt(preferencesInitialSize, 0);
        timerStartsAt = preferences.getLong(preferencesTimerStartsAt, 0);
        timerExpiresIn = preferences.getLong(preferencesTimerExpiresIn, 3600000);
    }

    public static HashMap<String, CartridgeSyncer> whichShouldSync(Context context) {
        initializeStaticSyncer(context);

        HashMap<String, CartridgeSyncer> cartridgesToSync = new HashMap<>();
        for(Map.Entry<String, CartridgeSyncer> entry : cartridgeSyncers.entrySet()) {
            if (entry.getValue().isTriggered()) {
                DopamineKit.debugLog("CartridgeSyncer", entry.getKey() + " cartridge needs to sync..");
                cartridgesToSync.put(entry.getKey(), entry.getValue());
            }
        }
        return cartridgesToSync;
    }

    @Override
    public boolean isTriggered() {
        int count = SQLCartridgeDataHelper.count(sqlDB, actionID);
        boolean isSizeToSync = count < minimumCount || (double)count/initialSize <= capacityToSync;
        boolean isExpired = System.currentTimeMillis() >= timerStartsAt+timerExpiresIn;
        DopamineKit.debugLog("CartridgeSyncer", actionID + " cartridge has " + count + "/" + initialSize + " decisions. The cartridge " + (isSizeToSync? "is" : "isnt't") + " below the "+ capacityToSync*100 +"% lower capacity limit and the timer " + (isExpired? "is" : "isn't") + " expired." );
        return isSizeToSync || isExpired;
    }

    public boolean isFresh() {
        int count = SQLCartridgeDataHelper.count(sqlDB, actionID);
        boolean isExpired = System.currentTimeMillis() >= timerStartsAt+timerExpiresIn;
        return count >= 1 && !isExpired;
    }

    public void updateTriggers(Integer size, @Nullable Long startTime, Long expiresIn) {

        initialSize = size;
        if (startTime != null) { timerStartsAt = startTime; }
        else { timerStartsAt = System.currentTimeMillis(); }
        timerExpiresIn = expiresIn;

        preferences.edit()
                .putInt(preferencesInitialSize, initialSize)
                .putLong(preferencesTimerStartsAt, timerStartsAt)
                .putLong(preferencesTimerExpiresIn, timerExpiresIn)
                .apply();
    }

    @Override
    public JSONObject call() throws Exception {
        if (syncInProgress) {
            DopamineKit.debugLog("CartridgeSyncer", "Cartridge sync already happening for " + actionID);
            return null;
        } else {
            synchronized (apisynclock) {
                if (syncInProgress) {
                    DopamineKit.debugLog("CartridgeSyncer", "Cartridge sync already happening for " + actionID);
                    return null;
                } else {
                    JSONObject apiResponse = null;
                    try {
                        DopamineKit.debugLog("CartridgeSyncer", "Beginning cartridge sync for " + actionID + "!");
                        syncInProgress = true;

                        apiResponse = dopamineAPI.refresh(actionID);
                        if (apiResponse == null) {
                            DopamineKit.debugLog("CartridgeSyncer", "Something went wrong during the call...");
                        } else {
                            if (apiResponse.optInt("status", 404) == 200) {
                                DopamineKit.debugLog("CartridgeSyncer", "Replacing cartridge for " + actionID + "...");

                                JSONArray reinforcementCartridge = apiResponse.getJSONArray("reinforcementCartridge");
                                long expiresIn = apiResponse.getLong("expiresIn");

                                SQLCartridgeDataHelper.deleteAll(sqlDB, actionID);
                                for(int i = 0; i < reinforcementCartridge.length(); i++) {
                                    SQLCartridgeDataHelper.insert(sqlDB, new ReinforcementDecisionContract( 0, actionID, reinforcementCartridge.getString(i) ));
                                }

                                updateTriggers(reinforcementCartridge.length(), null, expiresIn);
                            } else {
                                DopamineKit.debugLog("CartridgeSyncer", "Something went wrong while syncing cartridge for " + actionID + "... Leaving decisions actions in sqlite db");
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

    public String unload() {
        String decision = "neutralFeedback";

        if(isFresh()) {
            decision = SQLCartridgeDataHelper.pop(sqlDB, actionID).reinforcementDecision;
        }

        return decision;
    }
}
