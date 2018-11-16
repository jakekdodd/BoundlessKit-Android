package kit.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kit.boundless.internal.data.Telemetry;

/**
 * Created by cuddergambino on 8/4/16.
 */

public final class ReportedActionContract implements BaseColumns {

    public static final String TABLE_NAME = "Reported_Actions";
    public static final String COLUMNS_NAME_ACTIONNAME = "actionName";
    public static final String COLUMNS_NAME_CARTRIDGEID = "cartridgeId";
    public static final String COLUMNS_NAME_REINFORCEMENTDECISION = "reinforcementDecision";
    public static final String COLUMNS_NAME_METADATA = "metaData";
    public static final String COLUMNS_NAME_UTC = "utc";
    public static final String COLUMNS_NAME_TIMEZONEOFFSET = "deviceTimezoneOffset";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + COLUMNS_NAME_ACTIONNAME + " TEXT,"
            + COLUMNS_NAME_CARTRIDGEID + " TEXT,"
            + COLUMNS_NAME_REINFORCEMENTDECISION + " TEXT,"
            + COLUMNS_NAME_METADATA + " TEXT,"
            + COLUMNS_NAME_UTC + " INTEGER,"
            + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER"
            + " )";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public long id;
    public String actionID;
    public String cartridgeId;
    public String reinforcementDecision;
    public @Nullable String metaData;
    public long utc;
    public long timezoneOffset;

    public ReportedActionContract(long id, String actionID, String cartridgeId, String reinforcementDecision, @Nullable String metaData, long utc, long timezoneOffset) {
        this.id = id;
        this.actionID = actionID;
        this.cartridgeId = cartridgeId;
        this.reinforcementDecision = reinforcementDecision;
        this.metaData = metaData;
        this.utc = utc;
        this.timezoneOffset = timezoneOffset;
    }

    public static ReportedActionContract fromCursor(Cursor cursor) {
        return new ReportedActionContract(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getLong(5),
                cursor.getLong(6)
        );
    }

    public static JSONArray valuesToJSON(ArrayList<ReportedActionContract> actions) {
        HashMap<String, HashMap<String, List<ReportedActionContract>>> actionCartridges = new HashMap<>();

        for (ReportedActionContract action: actions) {
            if (actionCartridges.get(action.actionID) == null) { actionCartridges.put(action.actionID, new HashMap<String, List<ReportedActionContract>>()); }
            if (actionCartridges.get(action.actionID).get(action.cartridgeId) == null) { actionCartridges.get(action.actionID).put(action.cartridgeId, new ArrayList<ReportedActionContract>()); }
            actionCartridges.get(action.actionID).get(action.cartridgeId).add(action);
        }

        JSONArray reportsJSON = new JSONArray();
        try {
            for (Map.Entry<String, HashMap<String, List<ReportedActionContract>>> actionCartridge: actionCartridges.entrySet()) {
                for (Map.Entry<String, List<ReportedActionContract>> cartridge: actionCartridge.getValue().entrySet()) {
                    JSONObject reportJSON = new JSONObject();
                    JSONArray eventsJSON = new JSONArray();
                    for (ReportedActionContract action: cartridge.getValue()) {
                        eventsJSON.put(action.toJSON());
                    }

                    reportJSON.put(COLUMNS_NAME_ACTIONNAME, actionCartridge.getKey());
                    reportJSON.put(COLUMNS_NAME_CARTRIDGEID, cartridge.getKey());
                    reportJSON.put("events", eventsJSON);

                    reportsJSON.put(reportJSON);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
        }
        return reportsJSON;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put(COLUMNS_NAME_REINFORCEMENTDECISION, reinforcementDecision);
            json.put(COLUMNS_NAME_METADATA, (metaData == null) ? null : new JSONObject(metaData));
            json.put(COLUMNS_NAME_UTC, utc);
            json.put(COLUMNS_NAME_TIMEZONEOFFSET, timezoneOffset);
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
        }

        return json;
    }
}
