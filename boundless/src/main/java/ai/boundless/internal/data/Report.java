package ai.boundless.internal.data;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import ai.boundless.BoundlessKit;
import ai.boundless.internal.api.BoundlessApi;
import ai.boundless.internal.data.storage.SqlReportedActionDataHelper;
import ai.boundless.internal.data.storage.SqliteDataStore;
import ai.boundless.internal.data.storage.contracts.ReportedActionContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 9/4/16.
 */

class Report extends ContextWrapper implements Callable<Integer> {

  private static Report sharedInstance;
  private final String tag = "ReportSyncer";
  private final String preferencesName = "boundless.boundlesskit.synchronization.report";
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

  private Report(Context base) {
    super(base);
    sqlDb = SqliteDataStore.getInstance(base).getWritableDatabase();
    preferences = getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    sizeToSync = preferences.getInt(sizeToSyncKey, 20);
    timerStartsAt = preferences.getLong(timerStartsAtKey, System.currentTimeMillis());
    timerExpiresIn = preferences.getLong(timerExpiresInKey, 172800000);
  }

  static Report getSharedInstance(Context base) {
    if (sharedInstance == null) {
      sharedInstance = new Report(base);
    }
    return sharedInstance;
  }

  /**
   * Indicates whether the sync should be triggered.
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
    int count = SqlReportedActionDataHelper.count(sqlDb);
    boolean isSize = count >= sizeToSync;
    BoundlessKit.debugLog(
        "Report",
        "Report has batched " + count + "/" + sizeToSync + " actions" + (isSize
            ? " so needs to sync..."
            : ".")
    );
    return isSize;
  }

  /**
   * Clears the saved report sync triggers.
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
      json.put(sizeKey, SqlReportedActionDataHelper.count(sqlDb));
      json.put(sizeToSyncKey, sizeToSync);
      json.put(timerStartsAtKey, timerStartsAt);
      json.put(timerExpiresInKey, timerExpiresIn);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return json;
  }

  /**
   * Stores a reported action to be synced over the BoundlessAPI at a later time.
   *
   * @param action The action to be stored
   */
  void store(BoundlessAction action) {
    String metaData = (action.metaData == null) ? null : action.metaData.toString();
    long rowId = SqlReportedActionDataHelper.insert(sqlDb, new ReportedActionContract(
        0,
        action.actionId,
        action.cartridgeId,
        action.reinforcementDecision,
        metaData,
        action.utc,
        action.timezoneOffset
    ));
    // BoundlessKit.debugLog("SQL Reported Actions", "Inserted into row " + rowId);
  }

  @Override
  public Integer call() throws Exception {
    if (syncInProgress) {
      BoundlessKit.debugLog(tag, "Report sync already happening");
      return 0;
    } else {
      synchronized (apiSyncLock) {
        if (syncInProgress) {
          BoundlessKit.debugLog(tag, "Report sync already happening");
          return 0;
        } else {
          try {
            syncInProgress = true;
            BoundlessKit.debugLog(tag, "Beginning reporter sync!");

            final ArrayList<ReportedActionContract> sqlActions =
                SqlReportedActionDataHelper.findAll(sqlDb);
            if (sqlActions.size() == 0) {
              BoundlessKit.debugLog(tag, "No reported actions to be synced.");
              updateTriggers(null, System.currentTimeMillis(), null);
              return 0;
            } else {
              BoundlessKit.debugLog(tag, sqlActions.size() + " reported actions to be synced.");
              JSONObject apiResponse = BoundlessApi.report(this, sqlActions);
              if (apiResponse != null) {
                for (int i = 0; i < sqlActions.size(); i++) {
                  remove(sqlActions.get(i));
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
   * @param size The number of reported actions to trigger a sync
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

  void remove(ReportedActionContract action) {
    SqlReportedActionDataHelper.delete(sqlDb, action);
  }

}
