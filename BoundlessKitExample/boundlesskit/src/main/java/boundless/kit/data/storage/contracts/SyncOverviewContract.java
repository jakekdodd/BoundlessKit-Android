package boundless.kit.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import boundless.kit.data.Telemetry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 9/30/16.
 */

public class SyncOverviewContract implements BaseColumns {

    public static final String TABLE_NAME = "Sync_Overviews";
    public static final String COLUMNS_NAME_UTC = "utc";
    public static final String COLUMNS_NAME_TIMEZONEOFFSET = "timezoneOffset";
    public static final String COLUMNS_NAME_TOTALSYNCTIME = "totalSyncTime";
    public static final String COLUMNS_NAME_CAUSE = "cause";
    public static final String COLUMNS_NAME_TRACK = "track";
    public static final String COLUMNS_NAME_REPORT = "report";
    public static final String COLUMNS_NAME_CARTRIDGES = "cartridges";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + COLUMNS_NAME_UTC + " INTEGER,"
            + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER,"
            + COLUMNS_NAME_TOTALSYNCTIME + " INTEGER,"
            + COLUMNS_NAME_CAUSE + " TEXT,"
            + COLUMNS_NAME_TRACK + " TEXT,"
            + COLUMNS_NAME_REPORT + " TEXT,"
            + COLUMNS_NAME_CARTRIDGES + " TEXT"
            + " )";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public long id;
    public long utc;
    public long timezoneOffset;
    public long totalSyncTime;
    public String cause;
    public String track;
    public String report;
    public String cartridges;

    public SyncOverviewContract(long id, long utc, long timezoneOffset, long totalSyncTime, String cause, String track, String report, @Nullable String cartridges) {
        this.id = id;
        this.utc = utc;
        this.timezoneOffset = timezoneOffset;
        this.totalSyncTime = totalSyncTime;
        this.cause = cause;
        this.track = track;
        this.report = report;
        this.cartridges = cartridges;
    }

    public static SyncOverviewContract fromCursor(Cursor cursor) {
        return new SyncOverviewContract(
                cursor.getLong(0), cursor.getLong(1), cursor.getLong(2), cursor.getLong(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)
        );
    }

    public JSONObject toJSON() {
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
