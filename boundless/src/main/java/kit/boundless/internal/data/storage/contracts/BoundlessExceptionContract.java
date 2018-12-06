package kit.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import kit.boundless.internal.data.Telemetry;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 10/3/16.
 */
public class BoundlessExceptionContract implements BaseColumns {

  /**
   * The constant TABLE_NAME.
   */
  public static final String TABLE_NAME = "Boundless_Exceptions";
  /**
   * The constant COLUMNS_NAME_UTC.
   */
  public static final String COLUMNS_NAME_UTC = "utc";
  /**
   * The constant COLUMNS_NAME_TIMEZONEOFFSET.
   */
  public static final String COLUMNS_NAME_TIMEZONEOFFSET = "timezoneOffset";
  /**
   * The constant COLUMNS_NAME_EXCEPTIONCLASSNAME.
   */
  public static final String COLUMNS_NAME_EXCEPTIONCLASSNAME = "class";
  /**
   * The constant COLUMNS_NAME_MESSAGE.
   */
  public static final String COLUMNS_NAME_MESSAGE = "message";
  /**
   * The constant COLUMNS_NAME_STACKTRACE.
   */
  public static final String COLUMNS_NAME_STACKTRACE = "stackTrace";

  /**
   * The constant SQL_CREATE_TABLE.
   */
  public static final String SQL_CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
          + COLUMNS_NAME_UTC + " INTEGER," + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER,"
          + COLUMNS_NAME_EXCEPTIONCLASSNAME + " TEXT," + COLUMNS_NAME_MESSAGE + " TEXT,"
          + COLUMNS_NAME_STACKTRACE + " TEXT" + " )";

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
   * The Exception class name.
   */
  public String exceptionClassName;
  /**
   * The Message.
   */
  public String message;
  /**
   * The Stack trace.
   */
  public String stackTrace;

  /**
   * Instantiates a new Boundless exception contract.
   *
   * @param id the id
   * @param utc the utc
   * @param timezoneOffset the timezone offset
   * @param exceptionClassName the exception class name
   * @param message the message
   * @param stackTrace the stack trace
   */
  public BoundlessExceptionContract(
      long id,
      long utc,
      long timezoneOffset,
      String exceptionClassName,
      String message,
      String stackTrace) {
    this.id = id;
    this.utc = utc;
    this.timezoneOffset = timezoneOffset;
    this.exceptionClassName = exceptionClassName;
    this.message = message;
    this.stackTrace = stackTrace;
  }

  /**
   * From cursor boundless exception contract.
   *
   * @param cursor the cursor
   * @return the boundless exception contract
   */
  public static BoundlessExceptionContract fromCursor(Cursor cursor) {
    return new BoundlessExceptionContract(
        cursor.getLong(0),
        cursor.getLong(1),
        cursor.getLong(2),
        cursor.getString(3),
        cursor.getString(4),
        cursor.getString(5)
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
      json.put(COLUMNS_NAME_EXCEPTIONCLASSNAME, exceptionClassName);
      json.put(COLUMNS_NAME_MESSAGE, message);
      json.put(COLUMNS_NAME_STACKTRACE, stackTrace);
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }

    return json;
  }

}
