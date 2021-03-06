package ai.boundless.internal.data;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.boundless.BoundlessKit;
import ai.boundless.internal.api.BoundlessApi;
import ai.boundless.internal.data.storage.SqlTrackedActionDataHelper;
import ai.boundless.internal.data.storage.SqliteDataStore;
import ai.boundless.internal.data.storage.contracts.TrackedActionContract;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 9/4/16.
 */
class Track extends ContextWrapper implements Callable<Integer> {

  private static Track sharedInstance;
  private final String tag = "TrackSyncer";
  private final String preferencesName = "boundless.boundlesskit.synchronization.track";
  private final String sizeKey = "size";
  private final String sizeToSyncKey = "sizeToSync";
  private final String timerStartsAtKey = "timerStartsAt";
  private final String timerExpiresInKey = "timerExpiresIn";
  private final Object apiSyncLock = new Object();
  private SQLiteDatabase sqlDb;
  private SharedPreferences preferences;
  private int sizeToSync;
  private long timerStartsAt;
  private long timerExpiresIn;
  private Boolean syncInProgress = false;

  private Track(Context base) {
    super(base);
    sqlDb = SqliteDataStore.getInstance(base).getWritableDatabase();
    preferences = getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    sizeToSync = preferences.getInt(sizeToSyncKey, 15);
    timerStartsAt = preferences.getLong(timerStartsAtKey, System.currentTimeMillis());
    timerExpiresIn = preferences.getLong(timerExpiresInKey, 172800000);
  }

  /**
   * Gets shared instance.
   *
   * @param base the base
   * @return the shared instance
   */
  static Track getSharedInstance(Context base) {
    if (sharedInstance == null) {
      sharedInstance = new Track(base);
    }
    return sharedInstance;
  }

  /**
   * Is triggered boolean.
   *
   * @return Whether a sync should be started
   */
  boolean isTriggered() {
    return timerDidExpire() || isSizeToSync();
  }

  private boolean timerDidExpire() {
    long currentTime = System.currentTimeMillis();
    boolean isExpired = currentTime >= (timerStartsAt + timerExpiresIn);
    return isExpired;
  }

  private boolean isSizeToSync() {
    int count = SqlTrackedActionDataHelper.count(sqlDb);
    boolean isSize = count >= sizeToSync;
    BoundlessKit.debugLog(
        tag,
        "Track has batched " + count + "/" + sizeToSync + " actions" + (isSize
            ? " so needs to sync..."
            : ".")
    );
    return isSize;
  }

  /**
   * Clears the saved track sync triggers.
   */
  void removeTriggers() {
    sizeToSync = 15;
    timerStartsAt = System.currentTimeMillis();
    timerExpiresIn = 172800000;
    preferences.edit().clear().apply();
  }

  /**
   * This function returns a snapshot of this instance as a JSONObject.
   *
   * @return A JSONObject containing the size and sync triggers
   */
  JSONObject jsonForTriggers() {
    JSONObject json = new JSONObject();
    try {
      json.put(sizeKey, SqlTrackedActionDataHelper.count(sqlDb));
      json.put(sizeToSyncKey, sizeToSync);
      json.put(timerStartsAtKey, timerStartsAt);
      json.put(timerExpiresInKey, timerExpiresIn);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return json;
  }

  /**
   * Stores a tracked action to be synced over the BoundlessAPI at a later time.
   *
   * @param action The action to be stored
   */
  void store(BoundlessAction action) {
    String metaData = (action.metaData == null) ? null : action.metaData.toString();
    long rowId = SqlTrackedActionDataHelper.insert(
        sqlDb,
        new TrackedActionContract(0, action.actionId, metaData, action.utc, action.timezoneOffset)
    );
    // BoundlessKit.debugLog("SQL Tracked Actions", "Inserted into row " + rowId);

  }

  @Override
  public Integer call() throws Exception {
    if (syncInProgress) {
      BoundlessKit.debugLog(tag, "Track sync already happening");
      return 0;
    } else {
      synchronized (apiSyncLock) {
        if (syncInProgress) {
          BoundlessKit.debugLog(tag, "Track sync already happening");
          return 0;
        } else {
          try {
            syncInProgress = true;
            BoundlessKit.debugLog(tag, "Beginning tracker sync!");

            final ArrayList<TrackedActionContract> sqlActions =
                SqlTrackedActionDataHelper.findAll(sqlDb);
            if (sqlActions.size() == 0) {
              BoundlessKit.debugLog(tag, "No tracked actions to be synced.");
              updateTriggers(null, System.currentTimeMillis(), null);
              return 0;
            } else {
              BoundlessKit.debugLog(tag, sqlActions.size() + " tracked actions to be synced.");
              JSONObject apiResponse = BoundlessApi.track(this, sqlActions);
              if (apiResponse != null) {
                for (int i = 0; i < sqlActions.size(); i++) {
                  SqlTrackedActionDataHelper.delete(sqlDb, sqlActions.get(i));
                }

                JSONArray errors = apiResponse.optJSONArray("errors");
                if (errors != null) {
                  BoundlessKit.debugLog(tag, "Got errors:" + errors);
                  return -1;
                }

                updateTriggers(null, System.currentTimeMillis(), null);
                return 200;
              } else {
                BoundlessKit.debugLog(tag, "Could not send request.");
                return -1;
              }
            }
          } finally {
            syncInProgress = false;
          }
        }
      }
    }
  }

  /**
   * Updates the sync triggers.
   *
   * @param size The number of tracked actions to trigger a sync
   * @param startTime The start time for a sync timer
   * @param expiresIn The timer length, in ms, for a sync timer
   */
  void updateTriggers(@Nullable Integer size, @Nullable Long startTime, @Nullable Long expiresIn) {

    if (size != null) {
      sizeToSync = size;
    }
    if (startTime != null) {
      timerStartsAt = startTime;
    }
    if (expiresIn != null) {
      timerExpiresIn = expiresIn;
    }

    preferences.edit()
        .putInt(sizeToSyncKey, sizeToSync)
        .putLong(timerStartsAtKey, timerStartsAt)
        .putLong(timerExpiresInKey, timerExpiresIn)
        .apply();
  }

}
