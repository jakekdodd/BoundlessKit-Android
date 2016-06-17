package com.usedopamine.dopaminekit;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.view.View;


public class DopamineKit {
    protected static boolean debugMode = true;

    static Context context = null;

    // Singleton declaration
	private static DopamineKit ourInstance = new DopamineKit();
	private DopamineKit() {}
    public static DopamineKit getInstance() { return ourInstance; }

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

	/**
	 * This method sends a reinforcement request for the specified actionID
	 *
	 * @param actionID			The name of the registered action
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
	 * @return					The scheduled response to reinforce a user to do {@code actionID}
	 */
	public static String reinforce(String actionID, Context context) {
		return DopamineKit.reinforce(actionID, null, null, context);
	}

	/**
	 * This method sends a reinforcement request for the specified actionID
	 *
	 * @param actionID			The name of the registered action
	 * @param secondaryIdentity	An optional string to better identify users for a more personalized reinforcement schedule
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
	 * @return					The scheduled response to reinforce a user to do {@code actionID}
	 */
	public static String reinforce(String actionID, String secondaryIdentity, Context context) {
		return DopamineKit.reinforce(actionID, secondaryIdentity, null, context);
	}

	 /**
	 * This method sends a reinforcement request for the specified actionID
	 *
	 * @param actionID			The name of the registered action
	 * @param metaData			Optional metadata for better analytics
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
	 * @return					The scheduled response to reinforce a user to do {@code actionID}
	 */
	public static String reinforce(String actionID, Map<String, String> metaData, Context context) {
		return DopamineKit.reinforce(actionID, null, metaData, context);
	}

	/**
	 * This method sends a reinforcement request for the specified actionID
	 *
	 * @param actionID			The name of the registered action
	 * @param secondaryIdentity	An optional string to better identify users for a more personalized reinforcement schedule
	 * @param metaData			Optional metadata for better analytics
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
     * @return					The scheduled response to reinforce a user to do {@code actionID}
     */
	public static String reinforce(String actionID, String secondaryIdentity, Map<String, String> metaData, Context context) {
        DopamineKit.context = context;
        DopamineRequest dr = new DopamineRequest(context, DopamineRequest.RequestType.REINFORCE);

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
		return resultFunction;
	}

	/**
	 * This method sends an asynchronous tracking request for the specified actionID
	 *
	 * @param actionID			The name of an action
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
	 */
	public static void track(String actionID, Context context) {
		DopamineKit.track(actionID, null, null, context);
	}

	/**
	 * This method sends an asynchronous tracking request for the specified actionID
	 *
	 * @param actionID			The name of an action
	 * @param secondaryIdentity	An optional additional identification
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
	 */
	public static void track(String actionID, String secondaryIdentity, Context context) {
		DopamineKit.track(actionID, secondaryIdentity, null, context);
	}

	/**
	 * This method sends an asynchronous tracking request for the specified actionID
	 *
	 * @param actionID			The name of an action
	 * @param metaData			Optional metadata for better analytics
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
	 */
	public static void track(String actionID, Map<String, String> metaData, Context context) {
		DopamineKit.track(actionID, null, metaData, context);
	}

	/**
	 * This method sends an asynchronous tracking request for the specified actionID
	 *
	 * @param actionID			The name of an action
	 * @param secondaryIdentity	An optional additional identification
	 * @param metaData			Optional metadata for better analytics
	 * @param context			Context to retreive api key from file res/raw/DopamineProperties.json
	 */
	public static void track(String actionID, String secondaryIdentity, Map<String, String> metaData, Context context) {
		DopamineKit.context = context;
		DopamineRequest dr = new DopamineRequest(context, DopamineRequest.RequestType.TRACK);

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

}
