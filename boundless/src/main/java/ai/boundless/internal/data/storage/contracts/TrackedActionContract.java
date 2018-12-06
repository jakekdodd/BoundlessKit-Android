package ai.boundless.internal.data.storage.contracts;

import ai.boundless.internal.data.Telemetry;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 8/4/16.
 */
public final class TrackedActionContract implements BaseColumns {

  /**
   * The constant TABLE_NAME.
   */
  public static final String TABLE_NAME = "Tracked_Actions";
  /**
   * The constant COLUMNS_NAME_ACTIONNAME.
   */
  public static final String COLUMNS_NAME_ACTIONNAME = "actionName";
  /**
   * The constant COLUMNS_NAME_METADATA.
   */
  public static final String COLUMNS_NAME_METADATA = "metaData";
  /**
   * The constant COLUMNS_NAME_UTC.
   */
  public static final String COLUMNS_NAME_UTC = "utc";
  /**
   * The constant COLUMNS_NAME_TIMEZONEOFFSET.
   */
  public static final String COLUMNS_NAME_TIMEZONEOFFSET = "deviceTimezoneOffset";

  /**
   * The constant SQL_CREATE_TABLE.
   */
  public static final String SQL_CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
          + COLUMNS_NAME_ACTIONNAME + " TEXT," + COLUMNS_NAME_METADATA + " TEXT," + COLUMNS_NAME_UTC
          + " INTEGER," + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER" + " )";

  /**
   * The constant SQL_DROP_TABLE.
   */
  public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

  /**
   * The Id.
   */
  public long id;
  /**
   * The Action id.
   */
  public String actionId;
  /**
   * The Meta data.
   */
  @Nullable
  public String metaData;
  /**
   * The Utc.
   */
  public long utc;
  /**
   * The Timezone offset.
   */
  public long timezoneOffset;

  /**
   * Instantiates a new Tracked action contract.
   *
   * @param id the id
   * @param actionId the action id
   * @param metaData the meta data
   * @param utc the utc
   * @param timezoneOffset the timezone offset
   */
  public TrackedActionContract(
      long id, String actionId, @Nullable String metaData, long utc, long timezoneOffset) {
    this.id = id;
    this.actionId = actionId;
    this.metaData = metaData;
    this.utc = utc;
    this.timezoneOffset = timezoneOffset;
  }

  /**
   * From cursor tracked action contract.
   *
   * @param cursor the cursor
   * @return the tracked action contract
   */
  public static TrackedActionContract fromCursor(Cursor cursor) {
    return new TrackedActionContract(
        cursor.getLong(0),
        cursor.getString(1),
        cursor.getString(2),
        cursor.getLong(3),
        cursor.getLong(4)
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
      json.put(COLUMNS_NAME_ACTIONNAME, actionId);
      if (metaData != null) {
        json.put(COLUMNS_NAME_METADATA, new JSONObject(metaData));
      }
      json.put(
          "time",
          new JSONArray().put(new JSONObject().put("timeType", COLUMNS_NAME_UTC).put("value", utc))
              .put(new JSONObject().put("timeType", COLUMNS_NAME_TIMEZONEOFFSET)
                  .put("value", timezoneOffset))
      );
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }

    return json;
  }

}
