package com.usedopamine.dopaminekit.RESTfulAPI;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.annotation.Nullable;
import android.util.Log;

import com.usedopamine.dopaminekit.DataStore.Contracts.DopeExceptionContract;
import com.usedopamine.dopaminekit.DataStore.Contracts.ReportedActionContract;
import com.usedopamine.dopaminekit.DataStore.Contracts.SyncOverviewContract;
import com.usedopamine.dopaminekit.DataStore.Contracts.TrackedActionContract;
import com.usedopamine.dopaminekit.DopamineKit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TimeZone;

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

    private final String DopamineAPIURL = "https://staging-api.usedopamine.com/v4/";
//    private final String DopamineAPIURL = "https://api.usedopamine.com/v4/";
    private final String clientSDKVersion = "4.0.1";
    private final String clientOS = "Android";
    private final int clientOSVersion = android.os.Build.VERSION.SDK_INT;

    private enum CallType {
        TRACK("app/track"),
        REPORT("app/report"),
        REFRESH("app/refresh"),
        SYNC("telemetry/sync");

        private final String pathExtension;

        private CallType(final String value) {
            this.pathExtension = value;
        }
    }

    private JSONObject configurationData = new JSONObject();;

    public static DopamineAPI getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DopamineAPI(context);
        }
        return sharedInstance;
    }

    private DopamineAPI(Context context) {
        // Basic configuration
        try {
            configurationData.put("clientOS", clientOS);
            configurationData.put("clientOSVersion", clientOSVersion);
            configurationData.put("clientSDKVersion", clientSDKVersion);
            configurationData.put("primaryIdentity", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
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

    /**
     * This method sends a Track {@link CallType}.
     *
     * @param actions			The actions to send
     */
    public @Nullable JSONObject track(ArrayList<TrackedActionContract> actions) {
        try {
            JSONObject payload = new JSONObject(configurationData.toString());

            JSONArray trackedActions = new JSONArray();
            for (int i = 0; i < actions.size(); i++) {
                trackedActions.put(actions.get(i).toJSON());
            }
            payload.put("actions", trackedActions);

            return send(CallType.TRACK, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method sends a Report {@link CallType}.
     *
     * @param actions			The actions to send
     */
    public @Nullable JSONObject report(ArrayList<ReportedActionContract> actions) {
        try {
            JSONObject payload = new JSONObject(configurationData.toString());

            JSONArray reportedActions = new JSONArray();
            for (int i = 0; i < actions.size(); i++) {
                reportedActions.put(actions.get(i).toJSON());
            }
            payload.put("actions", reportedActions);

            return send(CallType.REPORT, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method sends a Refresh {@link CallType}.
     *
     * @param actionID			The actionID for the cartridge to reload
     */
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

    /**
     * This method sends Telemetry Sync {@link CallType}.
     *
     * @param syncOverviews			The sync overviews to send
     */
    public @Nullable JSONObject sync(ArrayList<SyncOverviewContract> syncOverviews, ArrayList<DopeExceptionContract> dopeExceptions) {
        try {
            JSONObject payload = new JSONObject(configurationData.toString());

            JSONArray syncOverviewsInJSON = new JSONArray();
            JSONArray dopeExceptionsInJSON = new JSONArray();
            for (int i = 0; i < syncOverviews.size(); i++) {
                syncOverviewsInJSON.put(syncOverviews.get(i).toJSON());
            }
            for (int i = 0; i < dopeExceptions.size(); i++) {
                dopeExceptionsInJSON.put(dopeExceptions.get(i).toJSON());
            }
            payload.put("syncOverviews", syncOverviewsInJSON);
            payload.put("exceptions", dopeExceptionsInJSON );

            return send(CallType.SYNC, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method sends a request to the DopamineAPI.
     *
     * @param type			    The {@link CallType} to send
     * @param payload			JSON data to send
     */
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
