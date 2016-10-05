package com.usedopamine.dopaminekit.RESTfulAPI;

import android.content.Context;
import android.content.ContextWrapper;
import android.provider.Settings.Secure;
import android.support.annotation.Nullable;
import android.util.Log;

import com.usedopamine.dopaminekit.DataStore.Contracts.DopeExceptionContract;
import com.usedopamine.dopaminekit.DataStore.Contracts.ReportedActionContract;
import com.usedopamine.dopaminekit.DataStore.Contracts.SyncOverviewContract;
import com.usedopamine.dopaminekit.DataStore.Contracts.TrackedActionContract;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.Synchronization.Telemetry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class DopamineAPI extends ContextWrapper {

    private static DopamineAPI myInstance = null;

    private Telemetry telemetry;

    private final String DopamineAPIURL = "https://staging-api.usedopamine.com/v4/";
    //    private final String DopamineAPIURL = "https://api.usedopamine.com/v4/";
    private final String clientSDKVersion = "4.0.1";
    private final String clientOS = "Android";
    private final int clientOSVersion = android.os.Build.VERSION.SDK_INT;
    private JSONObject configurationData = new JSONObject();

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

    private static DopamineAPI getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new DopamineAPI(context);
        }
        return myInstance;
    }

    private DopamineAPI(Context context) {
        super(context);

        telemetry = Telemetry.getSharedInstance(context);

        // Basic configuration
        try {
            configurationData.put("clientOS", clientOS);
            configurationData.put("clientOSVersion", clientOSVersion);
            configurationData.put("clientSDKVersion", clientSDKVersion);
            configurationData.put("primaryIdentity", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.recordException(e);
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
            Telemetry.recordException(e);
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
            Telemetry.recordException(e);
        }
    }

    /**
     * This method sends a Track {@link CallType}.
     *
     * @param actions The actions to send
     */
    public static
    @Nullable
    JSONObject track(Context context, ArrayList<TrackedActionContract> actions) {
        try {
            JSONObject payload = new JSONObject();

            JSONArray trackedActions = new JSONArray();
            for (int i = 0; i < actions.size(); i++) {
                trackedActions.put(actions.get(i).toJSON());
            }
            payload.put("actions", trackedActions);

            return getInstance(context).send(CallType.TRACK, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.recordException(e);
            return null;
        }
    }

    /**
     * This method sends a Report {@link CallType}.
     *
     * @param actions The actions to send
     */
    public static
    @Nullable
    JSONObject report(Context context, ArrayList<ReportedActionContract> actions) {
        try {
            JSONObject payload = new JSONObject();

            JSONArray reportedActions = new JSONArray();
            for (int i = 0; i < actions.size(); i++) {
                reportedActions.put(actions.get(i).toJSON());
            }
            payload.put("actions", reportedActions);

            return getInstance(context).send(CallType.REPORT, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.recordException(e);
            return null;
        }
    }

    /**
     * This method sends a Refresh {@link CallType}.
     *
     * @param actionID The actionID for the cartridge to reload
     */
    public static
    @Nullable
    JSONObject refresh(Context context, String actionID) {
        try {
            JSONObject payload = new JSONObject();

            payload.put("actionID", actionID);

            return getInstance(context).send(CallType.REFRESH, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.recordException(e);
            return null;
        }
    }

    /**
     * This method sends Telemetry Sync {@link CallType}.
     *
     * @param syncOverviews The sync overviews to send
     */
    public static
    @Nullable
    JSONObject sync(Context context, ArrayList<SyncOverviewContract> syncOverviews, ArrayList<DopeExceptionContract> dopeExceptions) {
        try {
            JSONObject payload = new JSONObject();

            JSONArray syncOverviewsInJSON = new JSONArray();
            JSONArray dopeExceptionsInJSON = new JSONArray();
            for (int i = 0; i < syncOverviews.size(); i++) {
                syncOverviewsInJSON.put(syncOverviews.get(i).toJSON());
            }
            for (int i = 0; i < dopeExceptions.size(); i++) {
                dopeExceptionsInJSON.put(dopeExceptions.get(i).toJSON());
            }
            payload.put("syncOverviews", syncOverviewsInJSON);
            payload.put("exceptions", dopeExceptionsInJSON);

            return getInstance(context).send(CallType.SYNC, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.recordException(e);
            return null;
        }
    }

    /**
     * This method sends a request to the DopamineAPI.
     *
     * @param type    The {@link CallType} to send
     * @param payload JSON data to send
     */
    private
    @Nullable
    JSONObject send(final CallType type, JSONObject payload) {

        String url = DopamineAPIURL + type.pathExtension;
        String payloadString;
        try {
            long utc = System.currentTimeMillis();
            payload.put("utc", utc);
            payload.put("timezoneOffset", TimeZone.getDefault().getOffset(utc));

            Iterator<String> configurationKeys = configurationData.keys();
            while (configurationKeys.hasNext()) {
                String key = configurationKeys.next();
                payload.put(key, configurationData.get(key));
            }

            payloadString = payload.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.recordException(e);
            Log.v("DopamineKit", "Parse Error - " + e.getMessage());
            return null;
        }

        DopamineKit.debugLog("DopamineAPI", "Preparing api call to " + url + " with payload:\n" + payloadString);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payloadString);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);

        long startTime = System.currentTimeMillis();
        String responseString;
        try {
            Response response = call.execute();
            responseString = response.body().string();
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("DopamineKit", "Network Error - " + e.getMessage());
            switch (type) {
                case TRACK:
                    telemetry.setResponseForTrackSync(-1, e.getMessage(), startTime);
                    break;

                case REPORT:
                    telemetry.setResponseForReportSync(-1, e.getMessage(), startTime);
                    break;

                case REFRESH:
                    String actionID = payload.optString("actionID");
                    telemetry.setResponseForCartridgeSync(actionID, -1, e.getMessage(), startTime);
                    break;

                case SYNC:
                    break;
            }
            return null;
        }

        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(responseString);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("DopamineKit", "Parse Error - " + e.getMessage());
            Telemetry.recordException(e);
            return null;
        }

        int status = responseJSON.optInt("status", -1);
        JSONArray errors = responseJSON.optJSONArray("errors");
        String errorsString = (errors==null) ? null : errors.toString();
        switch (type) {
            case TRACK:
                telemetry.setResponseForTrackSync(status, errorsString, startTime);
                break;

            case REPORT:
                telemetry.setResponseForReportSync(status, errorsString, startTime);
                break;

            case REFRESH:
                String actionID = payload.optString("actionID");
                telemetry.setResponseForCartridgeSync(actionID, status, errorsString, startTime);
                break;

            case SYNC:
                break;
        }

        DopamineKit.debugLog("DopamineAPIRequest", "Request resulted in - " + responseString);
        return responseJSON;
    }

}
