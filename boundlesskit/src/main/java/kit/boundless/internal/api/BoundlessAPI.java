package kit.boundless.internal.api;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import kit.boundless.BoundlessKit;
import kit.boundless.internal.data.BoundlessCredentials;
import kit.boundless.internal.data.Telemetry;
import kit.boundless.internal.data.storage.contracts.BoundlessExceptionContract;
import kit.boundless.internal.data.storage.contracts.ReportedActionContract;
import kit.boundless.internal.data.storage.contracts.SyncOverviewContract;
import kit.boundless.internal.data.storage.contracts.TrackedActionContract;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class BoundlessAPI extends ContextWrapper {

    private static BoundlessAPI myInstance = null;

    private Telemetry telemetry;

    private BoundlessCredentials credentials;

    private enum CallType {
        BOOT("app/boot"),
        TRACK("app/track"),
        REPORT("app/report"),
        REFRESH("app/refresh"),
        SYNC("telemetry/sync");

        static final String pathBase = "https://reinforce.boundless.ai/v6/";
        final String pathExtension;

        private CallType(final String value) {
            this.pathExtension = value;
        }

        String getPath() {
            return pathBase + pathExtension;
        }
    }

    private static BoundlessAPI getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new BoundlessAPI(context);
        }
        return myInstance;
    }

    private BoundlessAPI(Context context) {
        super(context);

        telemetry = Telemetry.getSharedInstance(context);

        // Read credentials from ("res/raw/boundlessproperties.json")
        int credentialResourceID = context.getResources().getIdentifier("boundlessproperties", "raw", context.getPackageName());
        credentials = BoundlessCredentials.valueOf(context, credentialResourceID);
    }

    /**
     * This method sends a Boot {@link CallType}.
     *
     * @param
     */
    public static
    @Nullable
    JSONObject boot(Context context) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("initialBoot", true);
            payload.put("currentVersion", "001");
            payload.put("currentConfig", "0");

            return getInstance(context).send(CallType.BOOT, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
            return null;
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
            Telemetry.storeException(e);
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
            payload.put("reports", ReportedActionContract.valuesToJSON(actions));

            return getInstance(context).send(CallType.REPORT, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
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
            Telemetry.storeException(e);
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
    JSONObject sync(Context context, ArrayList<SyncOverviewContract> syncOverviews, ArrayList<BoundlessExceptionContract> exceptions) {
        try {
            JSONObject payload = new JSONObject();

            JSONArray syncOverviewsInJSON = new JSONArray();
            JSONArray exceptionsJSON = new JSONArray();
            for (int i = 0; i < syncOverviews.size(); i++) {
                syncOverviewsInJSON.put(syncOverviews.get(i).toJSON());
            }
            for (int i = 0; i < exceptions.size(); i++) {
                exceptionsJSON.put(exceptions.get(i).toJSON());
            }
            payload.put("syncOverviews", syncOverviewsInJSON);
            payload.put("exceptions", exceptionsJSON);

            return getInstance(context).send(CallType.SYNC, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
            return null;
        }
    }

    /**
     * This method sends a request to the BoundlessAPI.
     *
     * @param type    The {@link CallType} to send
     * @param payload JSON data to send
     */
    private
    @Nullable
    JSONObject send(final CallType type, JSONObject payload) {

        String url = type.getPath();
        String payloadString;
        try {
            JSONObject credentialsJSON = credentials.asJSONObject();
            Iterator<String> credentialsKeys = credentialsJSON.keys();
            while (credentialsKeys.hasNext()) {
                String key = credentialsKeys.next();
                payload.put(key, credentialsJSON.get(key));
            }

            payloadString = payload.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("BoundlessAPI", "Parse Error - " + e.getMessage());
            Telemetry.storeException(e);
            return null;
        }

        BoundlessKit.debugLog("BoundlessAPI", "Preparing api call to " + url + " with payload:\n" + payloadString);
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
            Log.v("BoundlessAPI", "Network Error - " + e.getMessage());
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
            Log.v("BoundlessAPI", "Parse Error - " + e.getMessage());
            Telemetry.storeException(e);
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

        BoundlessKit.debugLog("BoundlessAPI", "Request resulted in - " + responseString);
        return responseJSON;
    }

}
