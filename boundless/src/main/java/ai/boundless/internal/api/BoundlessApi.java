package ai.boundless.internal.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ai.boundless.BoundlessKit;
import ai.boundless.internal.data.BoundlessCredentials;
import ai.boundless.internal.data.Telemetry;
import ai.boundless.internal.data.storage.contracts.BoundlessExceptionContract;
import ai.boundless.internal.data.storage.contracts.ReportedActionContract;
import ai.boundless.internal.data.storage.contracts.SyncOverviewContract;
import ai.boundless.internal.data.storage.contracts.TrackedActionContract;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.Nullable;
import android.util.Log;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 7/17/16.
 */
public class BoundlessApi extends ContextWrapper {

  private static BoundlessApi myInstance = null;
  /**
   * The Credentials.
   */
  public BoundlessCredentials credentials;
  private Telemetry telemetry;

  private BoundlessApi(Context context) {
    super(context);

    telemetry = Telemetry.getSharedInstance(context);

    // Read credentials from ("res/raw/boundlessproperties.json")
    int credentialResourceId = context.getResources()
        .getIdentifier("boundlessproperties", "raw", context.getPackageName());
    credentials = BoundlessCredentials.valueOf(context, credentialResourceId);
  }

  /**
   * Sends an api call to retrieve configuration details like reinforced actionIds and.
   * reinforcementEnabled.
   *
   * @param context The context
   * @param payload the payload
   * @return The api response as JSON.
   */
  @Nullable
  public static JSONObject boot(Context context, JSONObject payload) {
    return getInstance(context).send(CallType.BOOT, payload);
  }

  /**
   * This method sends a request to the BoundlessAPI.
   *
   * @param type The  API request type to send
   * @param payload JSON data to send
   * @return The api response as JSON.
   */
  @Nullable
  private JSONObject send(final CallType type, JSONObject payload) {

    String url = type.getPath();
    String payloadString;
    try {
      JSONObject credentialsJson = credentials.asJsonObject();
      Iterator<String> credentialsKeys = credentialsJson.keys();
      while (credentialsKeys.hasNext()) {
        String key = credentialsKeys.next();
        payload.put(key, credentialsJson.get(key));
      }

      payloadString = payload.toString(2);
    } catch (JSONException e) {
      e.printStackTrace();
      Log.v("BoundlessAPI", "Parse Error - " + e.getMessage());
      Telemetry.storeException(e);
      return null;
    }

    BoundlessKit.debugLog("BoundlessAPI",
        "Preparing api call to " + url + " with payload:\n" + payloadString
    );
    OkHttpClient client = new OkHttpClient();
    RequestBody body =
        RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payloadString);
    Request request = new Request.Builder().url(url).post(body).build();
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
        default:
      }
      return null;
    }

    JSONObject responseJson;
    try {
      responseJson = new JSONObject(responseString);
    } catch (JSONException e) {
      e.printStackTrace();
      Log.v("BoundlessAPI", "Parse Error - " + e.getMessage());
      Telemetry.storeException(e);
      return null;
    }

    int status = responseJson.optInt("status", -1);
    JSONArray errors = responseJson.optJSONArray("errors");
    String errorsString = (errors == null) ? null : errors.toString();
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
      default:
    }

    BoundlessKit.debugLog("BoundlessAPI", "Request resulted in - " + responseString);
    return responseJson;
  }

  /**
   * Gets instance.
   *
   * @param context the context
   * @return the instance
   */
  public static BoundlessApi getInstance(Context context) {
    if (myInstance == null) {
      myInstance = new BoundlessApi(context);
    }
    return myInstance;
  }

  /**
   * This method sends a Track API request.
   *
   * @param context The context
   * @param actions The actions to send
   * @return The api response as JSON.
   */
  @Nullable
  public static JSONObject track(Context context, ArrayList<TrackedActionContract> actions) {
    try {
      JSONObject payload = new JSONObject();

      JSONArray trackedActions = new JSONArray();
      for (int i = 0; i < actions.size(); i++) {
        trackedActions.put(actions.get(i).toJson());
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
  @Nullable
  public static JSONObject report(Context context, ArrayList<ReportedActionContract> actions) {
    try {
      JSONObject payload = new JSONObject();
      payload.put("reports", ReportedActionContract.valuesToJson(actions));

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
  @Nullable
  public static JSONObject refresh(Context context, String actionId) {
    try {
      JSONObject payload = new JSONObject();

      payload.put("actionName", actionId);

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
  @Nullable
  public static JSONObject sync(
      Context context,
      ArrayList<SyncOverviewContract> syncOverviews,
      ArrayList<BoundlessExceptionContract> exceptions) {
    try {
      JSONObject payload = new JSONObject();

      JSONArray syncOverviewsInJson = new JSONArray();
      JSONArray exceptionsJson = new JSONArray();
      for (int i = 0; i < syncOverviews.size(); i++) {
        syncOverviewsInJson.put(syncOverviews.get(i).toJson());
      }
      for (int i = 0; i < exceptions.size(); i++) {
        exceptionsJson.put(exceptions.get(i).toJson());
      }
      payload.put("syncOverviews", syncOverviewsInJson);
      payload.put("exceptions", exceptionsJson);

      return getInstance(context).send(CallType.SYNC, payload);
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
      return null;
    }
  }

  private enum CallType {
    /**
     * Boot call type.
     */
    BOOT("app/boot"), /**
     * Track call type.
     */
    TRACK("app/track"), /**
     * Report call type.
     */
    REPORT("app/report"), /**
     * Refresh call type.
     */
    REFRESH("app/refresh"), /**
     * Sync call type.
     */
    SYNC("telemetry/sync");

    /**
     * The Path base.
     */
    static final String PATH_BASE = "https://reinforce.boundless.ai/v6/";
    /**
     * The Path extension.
     */
    final String pathExtension;

    private CallType(final String value) {
      this.pathExtension = value;
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    String getPath() {
      return PATH_BASE + pathExtension;
    }
  }

}
