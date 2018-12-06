package ai.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by cuddergambino on 8/4/16.
 */
public final class ReinforcementDecisionContract implements BaseColumns {

  /**
   * The constant TABLE_NAME.
   */
  public static final String TABLE_NAME = "Reinforcement_Decisions";
  /**
   * The constant COLUMNS_NAME_ACTIONID.
   */
  public static final String COLUMNS_NAME_ACTIONID = "actionName";
  /**
   * The constant COLUMNS_NAME_REINFORCEMENTCARTRIDGEID.
   */
  public static final String COLUMNS_NAME_REINFORCEMENTCARTRIDGEID = "cartridgeId";
  /**
   * The constant COLUMNS_NAME_REINFORCEMENTDECISION.
   */
  public static final String COLUMNS_NAME_REINFORCEMENTDECISION = "reinforcementDecision";

  /**
   * The constant SQL_CREATE_TABLE.
   */
  public static final String SQL_CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
          + COLUMNS_NAME_ACTIONID + " TEXT," + COLUMNS_NAME_REINFORCEMENTCARTRIDGEID + " TEXT,"
          + COLUMNS_NAME_REINFORCEMENTDECISION + " TEXT" + " )";


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
   * Instantiates a new Reinforcement decision contract.
   *
   * @param id the id
   * @param actionId the action id
   * @param cartridgeId the cartridge id
   * @param reinforcementDecision the reinforcement decision
   */
  public ReinforcementDecisionContract(
      long id, String actionId, String cartridgeId, String reinforcementDecision) {
    this.id = id;
    this.actionId = actionId;
    this.cartridgeId = cartridgeId;
    this.reinforcementDecision = reinforcementDecision;
  }

  /**
   * From cursor reinforcement decision contract.
   *
   * @param cursor the cursor
   * @return the reinforcement decision contract
   */
  public static ReinforcementDecisionContract fromCursor(Cursor cursor) {
    return new ReinforcementDecisionContract(cursor.getLong(0),
        cursor.getString(1),
        cursor.getString(2),
        cursor.getString(3)
    );
  }
}
