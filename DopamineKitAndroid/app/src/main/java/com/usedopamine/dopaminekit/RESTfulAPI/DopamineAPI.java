package com.usedopamine.dopaminekit.RESTfulAPI;



import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.usedopamine.dopaminekit.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.TimeZone;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class DopamineAPI{

    static int PreferredTrackLength = 5;
    static int PreferredReportLength = 5;
    static double PreferredMinimumCartridgeCapacity = 0.25;

    //    final String DopamineAPIURL = "https://staging-api.usedopamine.com/v4/app/";
    protected static final String DopamineAPIURL = "https://api.usedopamine.com/v3/app/";
    private static final String clientSDKVersion = "4.0.0.beta";
    private static final String clientOS = "Android";
    private static final int clientOSVersion = android.os.Build.VERSION.SDK_INT;


    private static JSONObject configurationData = new JSONObject();

    private static DopamineAPI instance = null;
    private DopamineAPI(Context context) {
        // Basic configuration
        try {
            configurationData.put("clientOS", clientOS);
            configurationData.put("clientOSVersion", clientOSVersion);
            configurationData.put("clientSDKVersion", clientSDKVersion);
            configurationData.put("primaryIdentity", Settings.Secure.ANDROID_ID);
        } catch (JSONException e) { e.printStackTrace(); }

        // Read credentials from ("res/raw/dopamineproperties.json")
        int credentialRes = context.getResources().getIdentifier("dopamineproperties", "raw", context.getPackageName());
        if(credentialRes != 0){
            DopamineKit.debugLog("DopamineAPIRequest", "Found dopamineproperties.json");
        } else{
            DopamineKit.debugLog("DopamineAPIRequest", "Couldn't find dopamineproperties.json");
            return;
        }

        String credentialsFile;
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = context.getResources().openRawResource(credentialRes);
            for (int character = inputStream.read(); character != -1; character = inputStream.read()){
                byteArrayOutputStream.write(character);
            }
            credentialsFile = byteArrayOutputStream.toString();
            inputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e){
            Log.v("DopamineKit","Error - cannot find dopamineproperties.json");
            e.printStackTrace();
            return;
        }

        try{
            JSONObject credentials = new JSONObject( credentialsFile );
            configurationData.put("appID", credentials.getString("appID"));
            configurationData.put("versionID", credentials.getString("versionID"));
            if( credentials.getBoolean("inProduction") ){
                configurationData.put("secret", credentials.getString("productionSecret"));
            } else{
                configurationData.put("secret", credentials.getString("developmentSecret"));
            }
        } catch (JSONException e){
            Log.v("DopamineKit", "Error - dopamineproperties.json not configured properly");
            e.printStackTrace();
        }
    }
    static DopamineAPI getInstance(Context context) {
        if (instance == null) {
            instance = new DopamineAPI(context);
        }
        return instance;
    }

    public static void track(Context context, DopeAction[] actions, DopamineAPIRequestCallback callback) {
        DopamineAPIRequest request = new DopamineAPIRequest(context, DopamineAPIRequest.RequestType.TRACK, callback);
        try {
            JSONObject payload = new JSONObject(configurationData.toString());

            JSONArray trackedActions = new JSONArray();
            for (int i = 0; i < actions.length; i++ ) {
                trackedActions.put(actions[i].toJSON());
            }
            payload.put("actions", trackedActions);

            long utc = System.currentTimeMillis();
            payload.put("utc", utc);
            payload.put("timezoneOffset", TimeZone.getDefault().getOffset(utc));

            request.execute(payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
