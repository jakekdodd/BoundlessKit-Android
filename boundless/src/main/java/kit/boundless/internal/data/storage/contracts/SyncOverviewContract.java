package kit.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import kit.boundless.internal.data.Telemetry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 9/30/16.
 */
public class SyncOverviewContract implements BaseColumns {

  /**
   * The constant TABLE_NAME.
   */
  public static final String TABLE_NAME = "Sync_Overviews";
  /**
   * The constant COLUMNS_NAME_UTC.
   */
  public static final String COLUMNS_NAME_UTC = "utc";
  /**
   * The constant COLUMNS_NAME_TIMEZONEOFFSET.
   */
  public static final String COLUMNS_NAME_TIMEZONEOFFSET = "timezoneOffset";
  /**
   * The constant COLUMNS_NAME_TOTALSYNCTIME.
   */
  public static final String COLUMNS_NAME_TOTALSYNCTIME = "totalSyncTime";
  /**
   * The constant COLUMNS_NAME_CAUSE.
   */
  public static final String COLUMNS_NAME_CAUSE = "cause";
  /**
   * The constant COLUMNS_NAME_TRACK.
   */
  public static final String COLUMNS_NAME_TRACK = "track";
  /**
   * The constant COLUMNS_NAME_REPORT.
   */
  public static final String COLUMNS_NAME_REPORT = "report";
  /**
   * The constant COLUMNS_NAME_CARTRIDGES.
   */
  public static final String COLUMNS_NAME_CARTRIDGES = "cartridges";

  /**
   * The constant SQL_CREATE_TABLE.
   */
  public static final String SQL_CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
          + COLUMNS_NAME_UTC + " INTEGER," + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER,"
          + COLUMNS_NAME_TOTALSYNCTIME + " INTEGER," + COLUMNS_NAME_CAUSE + " TEXT,"
          + COLUMNS_NAME_TRACK + " TEXT," + COLUMNS_NAME_REPORT + " TEXT," + COLUMNS_NAME_CARTRIDGES
          + " TEXT" + " )";

  /**
   * The constant SQL_DROP_TABLE.
   */
  public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

  /**
   * The Id.
   */
  public long id;
  /**
   * The Utc.
   */
  public long utc;
  /**
   * The Timezone offset.
   */
  public long timezoneOffset;
  /**
   * The Total sync time.
   */
  public long totalSyncTime;
  /**
   * The Cause.
   */
  public String cause;
  /**
   * The Track.
   */
  public String track;
  /**
   * The Report.
   */
  public String report;
  /**
   * The Cartridges.
   */
  public String cartridges;

  /**
   * Instantiates a new Sync overview contract.
   *
   * @param id the id
   * @param utc the utc
   * @param timezoneOffset the timezone offset
   * @param totalSyncTime the total sync time
   * @param cause the cause
   * @param track the track
   * @param report the report
   * @param cartridges the cartridges
   */
  public SyncOverviewContract(
      long id,
      long utc,
      long timezoneOffset,
      long totalSyncTime,
      String cause,
      String track,
      String report,
      @Nullable String cartridges) {
    this.id = id;
    this.utc = utc;
    this.timezoneOffset = timezoneOffset;
    this.totalSyncTime = totalSyncTime;
    this.cause = cause;
    this.track = track;
    this.report = report;
    this.cartridges = cartridges;
  }

  /**
   * From cursor sync overview contract.
   *
   * @param cursor the cursor
   * @return the sync overview contract
   */
  public static SyncOverviewContract fromCursor(Cursor cursor) {
    return new SyncOverviewContract(
        cursor.getLong(0),
        cursor.getLong(1),
        cursor.getLong(2),
        cursor.getLong(3),
        cursor.getString(4),
        cursor.getString(5),
        cursor.getString(6),
        cursor.getString(7)
    );
  }

  /**
   * To json json object.
   *
   * @return the json object
   */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();

    try {
      json.put(COLUMNS_NAME_UTC, utc);
      json.put(COLUMNS_NAME_TIMEZONEOFFSET, timezoneOffset);
      json.put(COLUMNS_NAME_TOTALSYNCTIME, totalSyncTime);
      json.put(COLUMNS_NAME_CAUSE, cause);
      json.put(COLUMNS_NAME_TRACK, new JSONObject(track));
      json.put(COLUMNS_NAME_REPORT, new JSONObject(report));
      json.put(COLUMNS_NAME_CARTRIDGES, new JSONArray(cartridges));
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }

    return json;
  }

}
