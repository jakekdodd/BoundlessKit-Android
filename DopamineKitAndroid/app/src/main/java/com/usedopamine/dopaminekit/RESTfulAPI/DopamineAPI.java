package com.usedopamine.dopaminekit.RESTfulAPI;



import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.usedopamine.dopaminekit.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class DopamineAPI {

    private static DopamineAPI sharedInstance = null;

//    private final String DopamineAPIURL = "https://staging-api.usedopamine.com/v4/app/";
    private final String DopamineAPIURL = "https://api.usedopamine.com/v4/app/";
    private final String clientSDKVersion = "4.0.0.beta";
    private final String clientOS = "Android";
    private final int clientOSVersion = android.os.Build.VERSION.SDK_INT;

    private JSONObject configurationData = new JSONObject();;
    private final ExecutorService threadpool = Executors.newFixedThreadPool(3);

    private DopamineAPI(Context context) {
        // Basic configuration
        try {
            configurationData.put("clientOS", clientOS);
            configurationData.put("clientOSVersion", clientOSVersion);
            configurationData.put("clientSDKVersion", clientSDKVersion);
            configurationData.put("primaryIdentity", Settings.Secure.ANDROID_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Read credentials from ("res/raw/dopamineproperties.json")
        int credentialRes = context.getResources().getIdentifier("dopamineproperties", "raw", context.getPackageName());
        if (credentialRes != 0) {
            DopamineKit.debugLog("DopamineAPIRequest", "Found dopamineproperties.json");
        } else {
            DopamineKit.debugLog("DopamineAPIRequest", "Couldn't find dopamineproperties.json");
            return;
        }

        String credentialsFile;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = context.getResources().openRawResource(credentialRes);
            for (int character = inputStream.read(); character != -1; character = inputStream.read()) {
                byteArrayOutputStream.write(character);
            }
            credentialsFile = byteArrayOutputStream.toString();
            inputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            DopamineKit.debugLog("DopamineAPI", "Error - cannot find dopamineproperties.json");
            e.printStackTrace();
            return;
        }

        try {
            JSONObject credentials = new JSONObject(credentialsFile);
            configurationData.put("appID", credentials.getString("appID"));
            configurationData.put("versionID", credentials.getString("versionID"));
            if (credentials.getBoolean("inProduction")) {
                configurationData.put("secret", credentials.getString("productionSecret"));
            } else {
                configurationData.put("secret", credentials.getString("developmentSecret"));
            }
        } catch (JSONException e) {
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

    public @Nullable JSONObject track(DopeAction[] actions) {
        try {
            JSONObject payload = new JSONObject(configurationData.toString());

            JSONArray trackedActions = new JSONArray();
            for (int i = 0; i < actions.length; i++) {
                trackedActions.put(actions[i].toJSON());
            }
            payload.put("actions", trackedActions);

            return send(CallType.TRACK, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public @Nullable JSONObject report(DopeAction[] actions) {
        try {
            JSONObject payload = new JSONObject(configurationData.toString());

            JSONArray reportedActions = new JSONArray();
            for (int i = 0; i < actions.length; i++) {
                reportedActions.put(actions[i].toJSON());
            }
            payload.put("actions", reportedActions);

            return send(CallType.REPORT, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public @Nullable JSONObject refresh(String actionID) {
        try {
            JSONObject payload = new JSONObject(configurationData.toString());

            payload.put("actionID", actionID);

            return send(CallType.REFRESH, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    private enum CallType {
        TRACK("track"),
        REPORT("report"),
        REFRESH("refresh");

        private final String pathExtension;

        private CallType(final String value) {
            this.pathExtension = value;
        }
    }

    private @Nullable JSONObject send(final CallType type, JSONObject payload) {
        try {
            String url = DopamineAPIURL + type.pathExtension;
            try {
                long utc = System.currentTimeMillis();
                payload.put("utc", utc);
                payload.put("timezoneOffset", TimeZone.getDefault().getOffset(utc));
                DopamineKit.debugLog("DopamineAPI", "Preparing api call to " + url + " with payload:\n" + payload.toString(2));
            } catch (JSONException e) {
            }

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString(2));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            JSONObject result = new JSONObject(response.body().string());
            DopamineKit.debugLog("DopamineAPIRequest", "Request resulted in - " + result.toString());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("DopamineKit", "Network Error - " + e.getMessage());
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("DopamineKit", "Parse Error - " + e.getMessage());
            return null;
        }
    }

}
