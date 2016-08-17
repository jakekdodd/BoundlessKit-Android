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
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class DopamineAPI{

    private static DopamineAPI sharedInstance = null;

    protected static final String DopamineAPIURL = "https://staging-api.usedopamine.com/v4/app/";
//    protected static final String DopamineAPIURL = "https://api.usedopamine.com/v3/app/";
    private static final String clientSDKVersion = "4.0.0.beta";
    private static final String clientOS = "Android";
    private static final int clientOSVersion = android.os.Build.VERSION.SDK_INT;

    private JSONObject configurationData = new JSONObject();

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
            DopamineKit.debugLog("DopamineAPI","Error - cannot find dopamineproperties.json");
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
            DopamineKit.debugLog("DopamineAPI", "Error - dopamineproperties.json not configured properly");
            e.printStackTrace();
        }
    }

    public static DopamineAPI getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DopamineAPI(context);
        }
        return sharedInstance;
    }

    public static void track(Context context, DopeAction[] actions, DopamineAPIRequestCallback callback) {
        DopamineAPI api = getInstance(context);
        try {
            JSONObject payload = new JSONObject(api.configurationData.toString());

            long utc = System.currentTimeMillis();
            payload.put("utc", utc);
            payload.put("timezoneOffset", TimeZone.getDefault().getOffset(utc));

            JSONArray trackedActions = new JSONArray();
            for (int i = 0; i < actions.length; i++ ) {
                trackedActions.put(actions[i].toJSON());
            }
            payload.put("actions", trackedActions);

            api.send(CallType.TRACK, payload, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private enum CallType{
        TRACK("track"),
        REPORT("report"),
        REFRESH("refresh")
        ;

        private final String pathExtension;
        private CallType(final String value){
            this.pathExtension = value;
        }
    }

    private void send(final CallType type, JSONObject payload, final DopamineAPIRequestCallback callback) {
        final String url = DopamineAPIURL + type.pathExtension;
        try {
            DopamineKit.debugLog("DopamineAPI", "Preparing api call to " + url + " with payload:\n" + payload.toString(2));
        } catch (JSONException e) {}

        AsyncTask<JSONObject, Void, JSONObject> task = new AsyncTask<JSONObject, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(JSONObject... payload) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload[0].toString(2));
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();

                    JSONObject result = new JSONObject( response.body().string() );
                    DopamineKit.debugLog("DopamineAPIRequest", "Request resulted in - " + result.toString());
                    return result;
                } catch (IOException e){
                    e.printStackTrace();
                    Log.v("DopamineKit", "Network Error - " + e.getMessage());
                    return new JSONObject();
                } catch (JSONException e){
                    e.printStackTrace();
                    Log.v("DopamineKit", "Parse Error - " + e.getMessage());
                    return new JSONObject();
                }
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                super.onPostExecute(response);

                if(callback != null){
                    callback.onDopamineAPIRequestPostExecute(response);
                }
            }


        };

        task.execute(payload);

    }

}
