package kit.boundless;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import kit.boundless.internal.data.BoundlessAction;
import kit.boundless.internal.data.SyncCoordinator;

public class BoundlessKit extends ContextWrapper {
    /**
     * The callback interface used by {@link BoundlessKit} to inform its client
     * about a successful reinforcement decision.
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

    private static BoundlessKit sharedInstance = null;

    // Manages local storage and api calls
    private SyncCoordinator syncCoordinator;

    private BoundlessKit(Context base) {
        super(base);
        syncCoordinator = SyncCoordinator.getInstance(this);
    }

    protected static BoundlessKit getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new BoundlessKit(context);
        }
        return sharedInstance;
    }

    /**
     * This method sends a tracking request for the specified actionID to the BoundlessAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/boundlessproperties.json
     * @param actionID			The name of an action
     * @param metaData			Optional metadata for better analytics
     */
    public static void track(Context context, String actionID, @Nullable JSONObject metaData) {
        BoundlessAction action = new BoundlessAction(actionID, null, metaData);
        getInstance(context).syncCoordinator.storeTrackedAction(action);
    }

    /**
     * This method sends a reinforcement request for the specified actionID to the BoundlessAPI.
     *
     * @param context			Context to retrieve api key from file res/raw/boundlessproperties.json
     * @param actionID			The name of the registered action
     * @param metaData			Optional metadata for better analytics
     * @param callback          The callback to trigger when the reinforcement decision has been made
     */
    public static void reinforce(final Context context, final String actionID, @Nullable final JSONObject metaData, final ReinforcementCallback callback) {
        AsyncTask<Void, Void, String> reinforcementTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return BoundlessKit.getInstance(context).syncCoordinator.removeReinforcementDecisionFor(context, actionID);
            }

            @Override
            protected void onPostExecute(String reinforcementDecision) {
                callback.onReinforcement(reinforcementDecision);
                BoundlessAction action = new BoundlessAction(actionID, reinforcementDecision, metaData);
                BoundlessKit.getInstance(context).syncCoordinator.storeReportedAction(action);
            }
        }.execute();
    }

    public static boolean debugMode = false;
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

    public static void debugLog(String tag, String msg) {
        if (debugMode) {
            Log.v(tag, msg);
        }
    }
}
