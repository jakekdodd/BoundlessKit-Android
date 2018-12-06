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

    public BoundlessCredentials credentials;

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

    public static BoundlessAPI getInstance(Context context) {
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
     * Sends an api call to retrieve configuration details like reinforced actionIds and reinforcementEnabled.
     *
     * @param context The context
     * @param initialBoot Whether this is the first time the device has made a boot call. If true, the asks the api to include the newest config details even if the sdk already has the newest configId. Will reset any manual modifications to the config.
     * @param currentVersion The current experiment versionId. If a new version is available, the response will include the new version details.
     * @param currentConfig The current configuration configId. If a new config is available, the response will include the new config details.
     * @param internalId An experiment id for the user.
     * @param externalId A localized id for the user. Can be set by the client.
     * @return The api response as JSON.
     */
    public static
    @Nullable
    JSONObject boot(Context context, JSONObject payload) {
        return getInstance(context).send(CallType.BOOT, payload);
    }

    /**
     * This method sends a Track API request.
     *
     * @param context The context
     * @param actions The actions to send
     * @return The api response as JSON.
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
            payload.put("tracks", trackedActions);

            return getInstance(context).send(CallType.TRACK, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
            return null;
        }
    }

    /**
     * This method sends a Report API request.
     *
     * @param context The context
     * @param actions The actions to send
     * @return The api response as JSON.
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
     * This method sends a Refresh API request.
     *
     * @param context The context
     * @param actionId The actionId for the cartridge to reload
     * @return The api response as JSON.
     */
    public static
    @Nullable
    JSONObject refresh(Context context, String actionId) {
        try {
            JSONObject payload = new JSONObject();

            payload.put(
                    "actionName", actionId);

            return getInstance(context).send(CallType.REFRESH, payload);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
            return null;
        }
    }

    /**
     * This method sends Telemetry Sync API request.
     *
     * @param context The context
     * @param syncOverviews The sync overviews to send
     * @param exceptions Any exceptions caught during execution.
     * @return The api response as JSON.
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
     * @param type    The  API request type to send
     * @param payload JSON data to send
     * @return The api response as JSON.
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
                    String actionId = payload.optString("actionName");
                    telemetry.setResponseForCartridgeSync(actionId, -1, e.getMessage(), startTime);
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
                String actionId = payload.optString("actionName");
                telemetry.setResponseForCartridgeSync(actionId, status, errorsString, startTime);
                break;

            case SYNC:
                break;
        }

        BoundlessKit.debugLog("BoundlessAPI", "Request resulted in - " + responseString);
        return responseJSON;
    }

}