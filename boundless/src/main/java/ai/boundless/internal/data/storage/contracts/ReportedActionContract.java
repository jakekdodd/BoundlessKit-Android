package ai.boundless.internal.data.storage.contracts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public final class ReportedActionContract implements BaseColumns {

  /**
   * The constant TABLE_NAME.
   */
  public static final String TABLE_NAME = "Reported_Actions";
  /**
   * The constant COLUMNS_NAME_ACTIONNAME.
   */
  public static final String COLUMNS_NAME_ACTIONNAME = "actionName";
  /**
   * The constant COLUMNS_NAME_CARTRIDGEID.
   */
  public static final String COLUMNS_NAME_CARTRIDGEID = "cartridgeId";
  /**
   * The constant COLUMNS_NAME_REINFORCEMENTDECISION.
   */
  public static final String COLUMNS_NAME_REINFORCEMENTDECISION = "reinforcementDecision";
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
          + COLUMNS_NAME_ACTIONNAME + " TEXT," + COLUMNS_NAME_CARTRIDGEID + " TEXT,"
          + COLUMNS_NAME_REINFORCEMENTDECISION + " TEXT," + COLUMNS_NAME_METADATA + " TEXT,"
          + COLUMNS_NAME_UTC + " INTEGER," + COLUMNS_NAME_TIMEZONEOFFSET + " INTEGER" + " )";

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
   * The Cartridge id.
   */
  public String cartridgeId;
  /**
   * The Reinforcement decision.
   */
  public String reinforcementDecision;
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
   * Instantiates a new Reported action contract.
   *
   * @param id the id
   * @param actionId the action id
   * @param cartridgeId the cartridge id
   * @param reinforcementDecision the reinforcement decision
   * @param metaData the meta data
   * @param utc the utc
   * @param timezoneOffset the timezone offset
   */
  public ReportedActionContract(
      long id,
      String actionId,
      String cartridgeId,
      String reinforcementDecision,
      @Nullable String metaData,
      long utc,
      long timezoneOffset) {
    this.id = id;
    this.actionId = actionId;
    this.cartridgeId = cartridgeId;
    this.reinforcementDecision = reinforcementDecision;
    this.metaData = metaData;
    this.utc = utc;
    this.timezoneOffset = timezoneOffset;
  }

  /**
   * From cursor reported action contract.
   *
   * @param cursor the cursor
   * @return the reported action contract
   */
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

  /**
   * Values to json json array.
   *
   * @param actions the actions
   * @return the json array
   */
  public static JSONArray valuesToJson(ArrayList<ReportedActionContract> actions) {
    HashMap<String, HashMap<String, List<ReportedActionContract>>> actionCartridges =
        new HashMap<>();

    for (ReportedActionContract action : actions) {
      if (actionCartridges.get(action.actionId) == null) {
        actionCartridges.put(action.actionId, new HashMap<String, List<ReportedActionContract>>());
      }
      if (actionCartridges.get(action.actionId).get(action.cartridgeId) == null) {
        actionCartridges.get(action.actionId)
            .put(action.cartridgeId, new ArrayList<ReportedActionContract>());
      }
      actionCartridges.get(action.actionId).get(action.cartridgeId).add(action);
    }

    JSONArray reportsJson = new JSONArray();
    try {
      for (Map.Entry<String, HashMap<String, List<ReportedActionContract>>> actionCartridge :
          actionCartridges
          .entrySet()) {
        for (Map.Entry<String, List<ReportedActionContract>> cartridge : actionCartridge.getValue()
            .entrySet()) {
          JSONObject reportJson = new JSONObject();
          JSONArray eventsJson = new JSONArray();
          for (ReportedActionContract action : cartridge.getValue()) {
            eventsJson.put(action.toJson());
          }

          reportJson.put(COLUMNS_NAME_ACTIONNAME, actionCartridge.getKey());
          reportJson.put(COLUMNS_NAME_CARTRIDGEID, cartridge.getKey());
          reportJson.put("events", eventsJson);

          reportsJson.put(reportJson);
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
    return reportsJson;
  }

  /**
   * To json json object.
   *
   * @return the json object
   */
  public JSONObject toJson() {
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
