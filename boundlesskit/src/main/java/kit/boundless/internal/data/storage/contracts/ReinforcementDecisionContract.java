package kit.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by cuddergambino on 8/4/16.
 */

public final class ReinforcementDecisionContract implements BaseColumns {

    public static final String TABLE_NAME = "Reinforcement_Decisions";
    public static final String COLUMNS_NAME_ACTIONID = "actionName";
    public static final String COLUMNS_NAME_REINFORCEMENTCARTRIDGEID = "cartridgeId";
    public static final String COLUMNS_NAME_REINFORCEMENTDECISION = "reinforcementDecision";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + COLUMNS_NAME_ACTIONID + " TEXT,"
            + COLUMNS_NAME_REINFORCEMENTCARTRIDGEID + " TEXT,"
            + COLUMNS_NAME_REINFORCEMENTDECISION + " TEXT"
            + " )";


    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public long id;
    public String actionId;
    public String cartridgeID;
    public String reinforcementDecision;

    public ReinforcementDecisionContract(long id, String actionId, String cartridgeID, String reinforcementDecision) {
        this.id = id;
        this.actionId = actionId;
        this.cartridgeID = cartridgeID;
        this.reinforcementDecision = reinforcementDecision;
    }

    public static ReinforcementDecisionContract fromCursor(Cursor cursor) {
        return new ReinforcementDecisionContract(
                cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)
        );
    }
}
