package com.usedopamine.dopaminekit;

/**
 * Created by cuddergambino on 6/1/16.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.util.Log;

import com.usedopamine.dopaminekit.RESTfulAPI.DopamineRequest;
import com.usedopamine.dopaminekit.Synchronization.TrackSyncer;

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

    private static DopamineKit sharedInstance = new DopamineKit();

    static Context context = null;
    private TrackSyncer trackSyncer = TrackSyncer.getInstance();

    private DopamineKit() { }

    public static DopamineKit getInstance() { return sharedInstance; }

    /**
     * This method sends a reinforcement request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of the registered action
     * @param callback          The callback to trigger when the reinforcement decision has been made
     */
    public static void reinforce(Context context, String actionID, ReinforcementCallback callback) {
        DopamineKit.reinforce(context, actionID, null, callback);
    }

    /**
     * This method sends a reinforcement request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of the registered action
     * @param metaData			Optional metadata for better analytics
     * @param callback          The callback to trigger when the reinforcement decision has been made
     */
    public static void reinforce(Context context, String actionID, Map<String, String> metaData, ReinforcementCallback callback) {
        DopamineKit.context = context;
        DopamineRequest dr = new DopamineRequest(context, DopamineRequest.RequestType.REINFORCE, callback);

        // add reinforce specific data
        if(actionID != null) dr.addData("actionID", actionID);
        if(metaData != null) dr.addData("metaData", metaData);


        String resultFunction = null;
        try {
            if(DopamineKit.debugMode) dr.printData();
            dr.execute();
            dr.get();

            resultFunction = dr.resultData;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Log.v("DopmineKit", "Reinforcement Decision - " + resultFunction);
//        return resultFunction;
    }

    /**
     * This method sends a tracking request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of an action
     */
    public static void track(Context context, String actionID) {
        DopamineKit.track(context, actionID, null);
    }

    /**
     * This method sends a tracking request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of an action
     * @param metaData			Optional metadata for better analytics
     */
    public static void track(Context context, String actionID, HashMap<String, String> metaData) {
        DopamineKit.context = context;
//        DopamineRequest dr = new DopamineRequest(context, DopamineRequest.RequestType.TRACK, null);
//
//        // add reinforce specific data
//        if(actionID != null) dr.addData("actionID", actionID);
//        if(metaData != null) dr.addData("metaData", metaData);
//
//
//        try {
//            if(DopamineKit.debugMode) dr.printData();
//            dr.execute();
//            dr.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        debugLog("test", "here");

        DopeAction action = new DopeAction(actionID, metaData);

        sharedInstance.trackSyncer.store(context, action);
    }




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
