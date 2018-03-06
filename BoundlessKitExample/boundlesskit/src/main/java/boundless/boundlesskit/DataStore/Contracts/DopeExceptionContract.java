package boundless.boundlesskit.DataStore.Contracts;

import android.database.Cursor;
import android.provider.BaseColumns;

import boundless.boundlesskit.Synchronization.Telemetry;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 10/3/16.
 */

public class DopeExceptionContract implements BaseColumns {

    public static final String TABLE_NAME = "Dope_Exceptions";
    public static final String COLUMNS_NAME_UTC = "utc";
    public static final String COLUMNS_NAME_TIMEZONEOFFSET = "timezoneOffset";
    public static final String COLUMNS_NAME_EXCEPTIONCLASSNAME = "class";
    public static final String COLUMNS_NAME_MESSAGE = "message";
    public static final String COLUMNS_NAME_STACKTRACE = "stackTrace";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + COLUMNS_NAME_UTC + " INTEGER,"
            + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER,"
            + COLUMNS_NAME_EXCEPTIONCLASSNAME + " TEXT,"
            + COLUMNS_NAME_MESSAGE + " TEXT,"
            + COLUMNS_NAME_STACKTRACE + " TEXT"
            + " )";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public long id;
    public long utc;
    public long timezoneOffset;
    public String exceptionClassName;
    public String message;
    public String stackTrace;
    
    public DopeExceptionContract(long id, long utc, long timezoneOffset, String exceptionClassName, String message, String stackTrace) {
        this.id = id;
        this.utc = utc;
        this.timezoneOffset = timezoneOffset;
        this.exceptionClassName = exceptionClassName;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public static DopeExceptionContract fromCursor(Cursor cursor) {
        return new DopeExceptionContract(
                cursor.getLong(0), cursor.getLong(1), cursor.getLong(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)
        );
    }

    public JSONObject toJSON() {
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
