package ai.boundless;

import ai.boundless.internal.data.BoundlessAction;
import ai.boundless.internal.data.BoundlessUser;
import ai.boundless.internal.data.SyncCoordinator;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONObject;

/**
 * The type Boundless kit.
 */
public class BoundlessKit extends ContextWrapper {
  /**
   * The constant debugMode.
   */
  public static boolean debugMode = false;
  private static BoundlessKit sharedInstance = null;

  // Manages local storage and api calls
  private SyncCoordinator syncCoordinator;

  private BoundlessKit(Context base) {
    super(base);
    syncCoordinator = SyncCoordinator.getInstance(this);
  }

  /**
   * Reinforce actions to increase engagement. The actionIds must be configured on the Developer.
   * Dashboard.
   *
   * @param context Context to retrieve api key from file res/raw/boundlessproperties.json.
   *     ActionIds are specific to the included versionId.
   * @param actionId The name of the action.
   * @param metaData Optional metadata for fine grained reinforcement.
   * @param callback The callback to trigger when the reinforcement decision has been made. The
   *     reinforcement decision ids are configured on the Developer Dashboard.
   */
  public static void reinforce(
      final Context context,
      final String actionId,
      @Nullable final JSONObject metaData,
      final ReinforcementCallback callback) {
    AsyncTask<Void, Void, String> reinforcementTask = new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... voids) {
        return BoundlessKit.getInstance(context)
            .syncCoordinator
            .removeReinforcementDecisionFor(context, actionId);
      }

      @Override
      protected void onPostExecute(String reinforcementDecision) {
        callback.onReinforcement(reinforcementDecision);
        BoundlessAction action = new BoundlessAction(actionId, reinforcementDecision, metaData);
        BoundlessKit.getInstance(context).syncCoordinator.storeReportedAction(action);
      }
    }.execute();
  }

  /**
   * Gets instance.
   *
   * @param context the context
   * @return the instance
   */
  protected static BoundlessKit getInstance(Context context) {
    if (sharedInstance == null) {
      sharedInstance = new BoundlessKit(context);
    }
    return sharedInstance;
  }

  /**
   * Track events to measure how reinforcements change user behavior.
   *
   * @param context Context to retrieve api key from file res/raw/boundlessproperties.json.
   * @param actionId The name of an action event.
   * @param metaData Optional metadata for detailed analytics.
   */
  public static void track(Context context, String actionId, @Nullable JSONObject metaData) {
    BoundlessAction action = new BoundlessAction(actionId, null, metaData);
    getInstance(context).syncCoordinator.storeTrackedAction(action);
  }

  /**
   * Map an identity to match users across other systems, like a Leanplum userId.
   *
   * @param context Context to retrieve api key from file res/raw/boundlessproperties.json.
   * @param externalUserId A userId to map to the user.
   */
  public static void mapUserId(final Context context, final String externalUserId) {
    getInstance(context).syncCoordinator.mapExternalId(externalUserId);
  }

  /**
   * Get the identity for the user.
   *
   * @param context Context to retrieve api key from file res/raw/boundlessproperties.json.
   * @return The user's identity used for requests.
   */
  public static String getUserId(final Context context) {
    return getInstance(context).syncCoordinator.getUser().externalId;
  }

  /**
   * Get the experiment group assigned to the user.
   *
   * @param context Context to retrieve api key from file res/raw/boundlessproperties.json.
   * @return The user's experiment group. Could be a string like DEVELOPMENT, CONTROL, or BOUNDLESS
   */
  @Nullable
  public static String getExperimentGroup(final Context context) {
    BoundlessUser user = getInstance(context).syncCoordinator.getUser();
    if (user != null) {
      return user.experimentGroup;
    }
    return null;
  }

  /**
   * By default debug mode is set to `false`.
   * When debug mode is enabled, the data sent to and received from
   * the BoundlessAPI will be logged.
   *
   * @param enable Used to set debug mode. `true` will enable, `false` will disable.
   */
  public static void enableDebugMode(boolean enable) {
    debugMode = enable;
  }

  /**
   * Debug log.
   *
   * @param tag the tag
   * @param msg the msg
   */
  public static void debugLog(String tag, String msg) {
    if (debugMode) {
      Log.v("BoundlessKit", tag + ":" + msg);
    }
  }

  /**
   * The callback interface used by {@link BoundlessKit} to inform its client about a reinforcement
   * decision.
   *
   * Use {@link #reinforce(Context, String, JSONObject, ReinforcementCallback)} to initiate a
   * reinforcement decision.
   */
  public interface ReinforcementCallback {

    /**
     * Called when a response from the BoundlessAPI has been received. The responses are configured
     * on the Boundless Developer Dashboard (@link https://dashboard.boundless.ai)
     *
     * @param reinforcementDecision The reinforcement response string returned by BoundlessAPI
     */
    void onReinforcement(String reinforcementDecision);
  }
}
