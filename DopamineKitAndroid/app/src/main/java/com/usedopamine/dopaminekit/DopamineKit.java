package com.usedopamine.dopaminekit;

/**
 * Created by cuddergambino on 6/1/16.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.usedopamine.dopaminekit.Synchronization.SyncCoordinator;

import org.json.JSONObject;

public class DopamineKit {
    /**
     * The callback interface used by {@link DopamineKit} to inform its client
     * about a successful reinforcement decision.
     */
    public interface ReinforcementCallback {

        /**
         * Called when a response from the DopamineAPI has been received. The responses are configured
         * on the Dopamine Dashboard (@link http://dashboard.usedopamine.com)
         *
         * @param reinforcement The reinforcement response string returned by DopamineAPI
         */
        void onReinforcement(String reinforcement);
    }

    private static DopamineKit sharedInstance = null;

    private SyncCoordinator syncCoordinator;

    private DopamineKit(Context context) {
        syncCoordinator = SyncCoordinator.getInstance(context);
    }

    public static DopamineKit getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DopamineKit(context);
        }
        return sharedInstance;
    }

    /**
     * This method sends a tracking request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of an action
     * @param metaData			Optional metadata for better analytics
     */
    public static void track(Context context, String actionID, @Nullable JSONObject metaData) {
        DopamineKit instance = getInstance(context);

        DopeAction action = new DopeAction(actionID, null, metaData);
        instance.syncCoordinator.storeTrackedAction(action);
    }

    /**
     * This method sends a reinforcement request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of the registered action
     * @param metaData			Optional metadata for better analytics
     * @param callback          The callback to trigger when the reinforcement decision has been made
     */
    public static void reinforce(final Context context, final String actionID, @Nullable final JSONObject metaData, final ReinforcementCallback callback) {

        DopamineKit instance = getInstance(context);

        final String reinforcementDecision = instance.syncCoordinator.removeReinforcementDecision(context, actionID);
        AsyncTask<Void, Void, Void> reinforcementVisual = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                callback.onReinforcement(reinforcementDecision);
                return null;
            }
        }.execute();
        DopeAction action = new DopeAction(actionID, reinforcementDecision, metaData);
        instance.syncCoordinator.storeReportedAction(action);

    }

//    /**
//     * This method sends a tracking request for the specified actionID to the DopamineAPI.
//     *
//     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
//     * @param actionID			The name of an action
//     */
//    public static void track(Context context, String actionID) {
//        DopamineKit.track(context, actionID, null);
//    }


    public static boolean debugMode = false;
    /**
     * By default debug mode is set to `false`.
     * When debug mode is enabled, the data sent to and received from
     * the DopamineAPI will be logged.
     *
     * @param enable Used to set debug mode. `true` will enable, `false` will disable.
     */
    public static void enableDebugMode(boolean enable){
        debugMode = enable;
    }

    public static void debugLog(String tag, String msg){
        if(debugMode) {
            Log.v(tag, msg);
        }
    }
}
