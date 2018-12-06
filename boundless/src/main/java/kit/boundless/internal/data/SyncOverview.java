package kit.boundless.internal.data;

import java.util.HashMap;
import java.util.TimeZone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import kit.boundless.BoundlessKit;
import kit.boundless.internal.data.storage.SQLSyncOverviewDataHelper;
import kit.boundless.internal.data.storage.SQLiteDataStore;
import kit.boundless.internal.data.storage.contracts.SyncOverviewContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 9/30/16.
 */

class SyncOverview {

  private static final String syncResponseKey = "syncResponse";
  private static final String utcKey = "utc";
  private static final String roundTripTimeKey = "roundTripTime";
  private static final String statusKey = "status";
  private static final String errorKey = "error";
  private static SQLiteDatabase sqlDB;
  private long utc;
  private long timezoneOffset;
  private long totalSyncTime;
  private String cause;
  private JSONObject track;
  private JSONObject report;
  private HashMap<String, JSONObject> cartridges;

  SyncOverview(
      String cause,
      JSONObject trackTriggers,
      JSONObject reportTriggers,
      HashMap<String, JSONObject> cartridgeTriggers) {
    this.utc = System.currentTimeMillis();
    this.timezoneOffset = TimeZone.getDefault().getOffset(this.utc);
    this.totalSyncTime = -1;
    this.cause = cause;
    this.track = trackTriggers;
    this.report = reportTriggers;
    this.cartridges = cartridgeTriggers;
  }

  /**
   * Sets the `syncResponse` for `Track` in the current sync overview.
   *
   * @param status The HTTP status code received from the BoundlessAPI
   * @param error An error if one was received
   * @param startedAt The time the API call started at
   */
  void setTrackSyncResponse(int status, @Nullable String error, long startedAt) {
    JSONObject syncResponse = new JSONObject();
    try {
      syncResponse.put(utcKey, startedAt);
      syncResponse.put(roundTripTimeKey, System.currentTimeMillis() - startedAt);
      syncResponse.put(statusKey, status);
      syncResponse.put(errorKey, error);

      track.put(syncResponseKey, syncResponse);
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
  }

  /**
   * Sets the `syncResponse` for `Report` in the current sync overview.
   *
   * @param status The HTTP status code received from the BoundlessAPI
   * @param error An error if one was received
   * @param startedAt The time the API call started at
   */
  void setReportSyncResponse(int status, @Nullable String error, long startedAt) {
    JSONObject syncResponse = new JSONObject();
    try {
      syncResponse.put(utcKey, startedAt);
      syncResponse.put(roundTripTimeKey, System.currentTimeMillis() - startedAt);
      syncResponse.put(statusKey, status);
      syncResponse.put(errorKey, error);

      report.put(syncResponseKey, syncResponse);
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
  }

  /**
   * Sets the `syncResponse` for the cartridge in the current sync overview.
   *
   * @param actionId The name of the cartridge's action
   * @param status The HTTP status code received from the BoundlessAPI
   * @param error An error if one was received
   * @param startedAt The time the API call started at
   */
  void setCartridgeSyncResponse(
      String actionId, int status, @Nullable String error, long startedAt) {
    JSONObject syncResponse = new JSONObject();
    try {
      syncResponse.put(utcKey, startedAt);
      syncResponse.put(roundTripTimeKey, System.currentTimeMillis() - startedAt);
      syncResponse.put(statusKey, status);
      syncResponse.put(errorKey, error);

      JSONObject cartridge = cartridges.get(actionId);
      if (cartridge != null) {
        cartridge.put(syncResponseKey, syncResponse);
      }
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
  }

  /**
   * Use to finalize a sync overview. This will mark the total sync time.
   */
  void finish() {
    totalSyncTime = System.currentTimeMillis() - utc;
  }

  /**
   * Stores the sync overview in the sql database
   *
   * @param context Context
   */
  void store(Context context) {
    if (sqlDB == null) {
      sqlDB = SQLiteDataStore.getInstance(context).getWritableDatabase();
    }

    SyncOverviewContract overviewContract = new SyncOverviewContract(
        0,
        utc,
        timezoneOffset,
        totalSyncTime,
        cause,
        track.toString(),
        report.toString(),
        new JSONArray(cartridges.values()).toString()
    );
    long rowId = SQLSyncOverviewDataHelper.insert(sqlDB, overviewContract);

    BoundlessKit.debugLog("SQL Sync Overviews", "Inserted into row " + rowId);
    try {
      BoundlessKit.debugLog("SQL Sync Overviews", overviewContract.toJSON().toString(2));
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
  }

}
