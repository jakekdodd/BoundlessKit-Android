package kit.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import kit.boundless.internal.data.Telemetry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 8/4/16.
 */

public final class TrackedActionContract implements BaseColumns {

  public static final String TABLE_NAME = "Tracked_Actions";
  public static final String COLUMNS_NAME_ACTIONNAME = "actionName";
  public static final String COLUMNS_NAME_METADATA = "metaData";
  public static final String COLUMNS_NAME_UTC = "utc";
  public static final String COLUMNS_NAME_TIMEZONEOFFSET = "deviceTimezoneOffset";

  public static final String SQL_CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
          + COLUMNS_NAME_ACTIONNAME + " TEXT," + COLUMNS_NAME_METADATA + " TEXT," + COLUMNS_NAME_UTC
          + " INTEGER," + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER" + " )";

  public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

  public long id;
  public String actionId;
  public @Nullable
  String metaData;
  public long utc;
  public long timezoneOffset;

  public TrackedActionContract(
      long id, String actionId, @Nullable String metaData, long utc, long timezoneOffset) {
    this.id = id;
    this.actionId = actionId;
    this.metaData = metaData;
    this.utc = utc;
    this.timezoneOffset = timezoneOffset;
  }

  public static TrackedActionContract fromCursor(Cursor cursor) {
    return new TrackedActionContract(
        cursor.getLong(0),
        cursor.getString(1),
        cursor.getString(2),
        cursor.getLong(3),
        cursor.getLong(4)
    );
  }

  public JSONObject toJSON() {
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
