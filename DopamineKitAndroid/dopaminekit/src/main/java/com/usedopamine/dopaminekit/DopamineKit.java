package com.usedopamine.dopaminekit;

/**
 * Created by cuddergambino on 6/1/16.
 */

import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.view.View;


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

    public static boolean debugMode = true;

    static Context context = null;

    // Singleton declaration
    private static DopamineKit ourInstance = new DopamineKit();
    private DopamineKit() {}
    public static DopamineKit getInstance() { return ourInstance; }

    /**
     * This method sends a reinforcement request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of the registered action
     * @param callback          The callback to trigger when the reinforcement decision has been made
     */
    public static void reinforce(Context context, String actionID, ReinforcementCallback callback) {
        DopamineKit.reinforce(context, actionID, null, null, callback);
    }

    /**
     * This method sends a reinforcement request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of the registered action
     * @param secondaryIdentity	An optional string to better identify users for a more personalized reinforcement schedule
     * @param callback          The callback to trigger when the reinforcement decision has been made
     */
    public static void reinforce(Context context, String actionID, String secondaryIdentity, ReinforcementCallback callback) {
        DopamineKit.reinforce(context, actionID, secondaryIdentity, null, callback);
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
        DopamineKit.reinforce(context, actionID, null, metaData, callback);
    }

    /**
     * This method sends a reinforcement request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of the registered action
     * @param secondaryIdentity	An optional string to better identify users for a more personalized reinforcement schedule
     * @param metaData			Optional metadata for better analytics
     * @param callback          The callback to trigger when the reinforcement decision has been made
     */
    public static void reinforce(Context context, String actionID, String secondaryIdentity, Map<String, String> metaData, ReinforcementCallback callback) {
        DopamineKit.context = context;
        DopamineRequest dr = new DopamineRequest(context, DopamineRequest.RequestType.REINFORCE, callback);

        // add reinforce specific data
        if(actionID != null) dr.addData("actionID", actionID);
        if(secondaryIdentity != null) dr.addData("secondaryIdentity", secondaryIdentity);
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

        System.out.println("DopmineKit: Result - " + resultFunction);
//        return resultFunction;
    }

    /**
     * This method sends a tracking request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of an action
     */
    public static void track(Context context, String actionID) {
        DopamineKit.track(context, actionID, null, null);
    }

    /**
     * This method sends a tracking request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of an action
     * @param secondaryIdentity	An optional additional identification
     */
    public static void track(Context context, String actionID, String secondaryIdentity) {
        DopamineKit.track(context, actionID, secondaryIdentity, null);
    }

    /**
     * This method sends a tracking request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of an action
     * @param metaData			Optional metadata for better analytics
     */
    public static void track(Context context, String actionID, Map<String, String> metaData) {
        DopamineKit.track(context, actionID, null, metaData);
    }

    /**
     * This method sends a tracking request for the specified actionID to the DopamineAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/dopamineproperties.json
     * @param actionID			The name of an action
     * @param secondaryIdentity	An optional additional identification
     * @param metaData			Optional metadata for better analytics
     */
    public static void track(Context context, String actionID, String secondaryIdentity, Map<String, String> metaData) {
        DopamineKit.context = context;
        DopamineRequest dr = new DopamineRequest(context, DopamineRequest.RequestType.TRACK, null);

        // add reinforce specific data
        if(actionID != null) dr.addData("actionID", actionID);
        if(secondaryIdentity != null) dr.addData("secondaryIdentity", secondaryIdentity);
        if(metaData != null) dr.addData("metaData", metaData);


        try {
            if(DopamineKit.debugMode) dr.printData();
            dr.execute();
            dr.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make and show a {@link CandyBar} that displays a {@link com.usedopamine.dopaminekit.CandyBar.Candy} Icon, Title, and Subtitle
     *
     * @param view				The view to find a parent from.
     * @param candy				The {@link com.usedopamine.dopaminekit.CandyBar.Candy} icon to show.
     * @param title				The title to show.  Will be formatted to show larger than the subtitle.
     * @param subtitle			The subtitle to show.
     * @param backgroundColor	The color of the background.
     * @param duration			How long to display the message.  Either {@link CandyBar#LENGTH_SHORT} or {@link CandyBar#LENGTH_LONG}
     */
    public static void showCandyBar(View view, CandyBar.Candy candy, String title, String subtitle, int backgroundColor, int duration){
        CandyBar candyBar = new CandyBar(view, candy, title, subtitle, backgroundColor, duration);
        candyBar.show();
    }

    /**
     * Make and show a {@link CandyBar} that displays a {@link com.usedopamine.dopaminekit.CandyBar.Candy} Icon and Text
     *
     * @param view				The view to find a parent from.
     * @param candy				The {@link com.usedopamine.dopaminekit.CandyBar.Candy} icon to show.
     * @param text				The text to show.
     * @param backgroundColor	The color of the background.
     * @param duration			How long to display the message.  Either {@link CandyBar#LENGTH_SHORT} or {@link CandyBar#LENGTH_LONG}
     */
    public static void showCandyBar(View view, CandyBar.Candy candy, String text, int backgroundColor, int duration){
        CandyBar candyBar = new CandyBar(view, candy, text, backgroundColor, duration);
        candyBar.show();
    }

}
