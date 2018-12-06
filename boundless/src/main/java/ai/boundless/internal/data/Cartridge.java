package ai.boundless.internal.data;

import java.util.concurrent.Callable;

import ai.boundless.BoundlessKit;
import ai.boundless.internal.api.BoundlessApi;
import ai.boundless.internal.data.storage.SqlCartridgeDataHelper;
import ai.boundless.internal.data.storage.SqliteDataStore;
import ai.boundless.internal.data.storage.contracts.ReinforcementDecisionContract;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 9/6/16.
 */
class Cartridge extends ContextWrapper implements Callable<Integer> {

  private static final String ACTION_ID_KEY = "actionName";
  private static final String SIZE_KEY = "size";
  private static final String CAPACITY_TO_SYNC_KEY = "capacityToSync";
  private static final String INITIAL_SIZE_KEY = "initialSize";
  private static final String TIMER_STARTS_AT_KEY = "timerStartsAt";
  private static final String TIMER_EXPIRES_IN_KEY = "timerExpiresIn";
  private static final double CAPACITY_TO_SYNC = 0.25;
  private static final int MINIMUM_COUNT = 2;
  /**
   * The Action id.
   */
  public final String actionId;
  private final String tag = "CartridgeSyncer";
  private final Object apiSyncLock = new Object();
  private SQLiteDatabase sqlDb;
  private SharedPreferences preferences;
  private int initialSize;
  private long timerStartsAt;
  private long timerExpiresIn;
  private Boolean syncInProgress = false;

  /**
   * Instantiates a new Cartridge.
   *
   * @param base the base
   * @param actionId the action id
   */
  protected Cartridge(Context base, String actionId) {
    super(base);
    this.actionId = actionId;

    sqlDb = SqliteDataStore.getInstance(base).getWritableDatabase();
    preferences = getSharedPreferences(preferencesName(), Context.MODE_PRIVATE);
    initialSize = preferences.getInt(INITIAL_SIZE_KEY, 0);
    timerStartsAt = preferences.getLong(TIMER_STARTS_AT_KEY, 0);
    timerExpiresIn = preferences.getLong(TIMER_EXPIRES_IN_KEY, 0);
  }

  private String preferencesName() {
    return "boundless.boundlesskit.synchronization.cartridge." + actionId;
  }

  /**
   * Is triggered boolean.
   *
   * @return Whether a sync should be started
   */
  public boolean isTriggered() {
    return timerDidExpire() || isCapacityToSync();
  }

  private boolean timerDidExpire() {
    long currentTime = System.currentTimeMillis();
    boolean isExpired = currentTime >= (timerStartsAt + timerExpiresIn);
    return isExpired;
  }

  private boolean isCapacityToSync() {
    int count = SqlCartridgeDataHelper.countFor(sqlDb, actionId);
    boolean isCapacity = count < MINIMUM_COUNT || (double) count / initialSize <= CAPACITY_TO_SYNC;
    BoundlessKit.debugLog(
        tag,
        "Cartridge for actionId:(" + actionId + ") has " + count + "/" + initialSize
            + " decisions remaining in its queue" + (isCapacity ? " so needs to sync..." : ".")
    );
    return isCapacity;
  }

  /**
   * Clears the saved cartridge sync triggers.
   */
  public void removeTriggers() {
    initialSize = 0;
    timerStartsAt = 0;
    timerExpiresIn = 0;
    preferences.edit().clear().apply();
  }

  /**
   * This function returns a snapshot of this instance as a JSONObject.
   *
   * @return A JSONObject containing the size and sync triggers
   */
  public JSONObject jsonForTriggers() {
    JSONObject json = new JSONObject();
    try {
      json.put(ACTION_ID_KEY, actionId);
      json.put(SIZE_KEY, SqlCartridgeDataHelper.countFor(sqlDb, actionId));
      json.put(INITIAL_SIZE_KEY, initialSize);
      json.put(CAPACITY_TO_SYNC_KEY, CAPACITY_TO_SYNC);
      json.put(TIMER_STARTS_AT_KEY, timerStartsAt);
      json.put(TIMER_EXPIRES_IN_KEY, timerExpiresIn);
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
    return json;
  }

  /**
   * Dispenses a reinforcement decision from the cartridge.
   *
   * @return A reinforcement decision string. If there are no reinforcements, a neutral decision
   *     is     returned.
   */
  public String dispenseReinforcement() {
    String reinforcementDecision = BoundlessAction.NEUTRAL_DECISION;

    if (isFresh()) {
      ReinforcementDecisionContract rdc = SqlCartridgeDataHelper.findFirstFor(sqlDb, actionId);
      if (rdc != null) {
        reinforcementDecision = rdc.reinforcementDecision;
        SqlCartridgeDataHelper.delete(sqlDb, rdc);
      }
    }

    return reinforcementDecision;
  }

  /**
   * Is fresh boolean.
   *
   * @return Whether there is a live ammo in the cartridge
   */
  public boolean isFresh() {
    return !timerDidExpire() && SqlCartridgeDataHelper.countFor(sqlDb, actionId) >= 1;
  }

  @Override
  public Integer call() throws Exception {
    if (syncInProgress) {
      BoundlessKit.debugLog(tag, "Cartridge sync already happening for " + actionId);
      return 0;
    } else {
      synchronized (apiSyncLock) {
        if (syncInProgress) {
          BoundlessKit.debugLog(tag, "Cartridge sync already happening for " + actionId);
          return 0;
        } else {
          try {
            syncInProgress = true;
            BoundlessKit.debugLog(tag, "Beginning cartridge sync for " + actionId + "!");

            JSONObject apiResponse = BoundlessApi.refresh(this, actionId);
            if (apiResponse != null) {

              JSONArray errors = apiResponse.optJSONArray("errors");
              if (errors != null) {
                BoundlessKit.debugLog(tag, "Got errors:" + errors);
                return -1;
              }

              BoundlessKit.debugLog(tag, "Replacing cartridge for " + actionId + "...");
              JSONArray reinforcementCartridge = apiResponse.getJSONArray("reinforcements");
              long expiresIn = apiResponse.getLong("ttl");
              String cartridgeId = apiResponse.getString("cartridgeId");

              SqlCartridgeDataHelper.deleteAllFor(sqlDb, actionId);
              for (int i = 0; i < reinforcementCartridge.length(); i++) {
                store(cartridgeId,
                    reinforcementCartridge.getJSONObject(i).getString("reinforcementName")
                );
              }
              updateTriggers(reinforcementCartridge.length(),
                  System.currentTimeMillis(),
                  expiresIn
              );
              return 200;
            } else {
              BoundlessKit.debugLog(tag, "Could not send request.");
              return -1;
            }
          } finally {
            syncInProgress = false;
          }
        }
      }
    }
  }

  /**
   * Stores a reinforcement decision in the cartridge.
   *
   * @param cartridgeId the cartridge id
   * @param reinforcementDecision The decision to be stored
   */
  public void store(String cartridgeId, String reinforcementDecision) {
    long rowId = SqlCartridgeDataHelper.insert(
        sqlDb,
        new ReinforcementDecisionContract(0, actionId, cartridgeId, reinforcementDecision)
    );
    //        BoundlessKit.debugLog(TAG, "Inserted "+reinforcementDecision+" into row "+rowId+" for
    // action "+actionId);
  }

  /**
   * Updates the sync triggers.
   *
   * @param size The number of reported actions to trigger a sync
   * @param startTime The start time for a sync timer
   * @param expiresIn The timer length, in ms, for a sync timer
   */
  public void updateTriggers(Integer size, Long startTime, Long expiresIn) {
    initialSize = size;
    timerStartsAt = startTime;
    timerExpiresIn = expiresIn;

    preferences.edit()
        .putInt(INITIAL_SIZE_KEY, initialSize)
        .putLong(TIMER_STARTS_AT_KEY, timerStartsAt)
        .putLong(TIMER_EXPIRES_IN_KEY, timerExpiresIn)
        .apply();
  }
}
