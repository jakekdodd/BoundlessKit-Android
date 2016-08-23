package com.usedopamine.dopaminekit.DataStore.Contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

/**
 * Created by cuddergambino on 8/4/16.
 */

public final class ReinforcementDecisionContract implements BaseColumns {

    public static final String TABLE_NAME_PREFIX = "Reinforcement_Decisions_";
    public static final String COLUMNS_NAME_REINFORCEMENT_DECISION = "reinforcementdecision";

    public long id;
    public String actionID;
    public String reinforcementDecision;

    public ReinforcementDecisionContract(long id, String actionID, String reinforcementDecision) {
        this.id = id;
        this.actionID = actionID;
        this.reinforcementDecision = reinforcementDecision;
    }

    public static ReinforcementDecisionContract fromCursor(String actionID, Cursor cursor) {
        return new ReinforcementDecisionContract(
                cursor.getLong(0), actionID, cursor.getString(1)
        );
    }

    public static final String TABLE_NAME(String actionID) {
        return TABLE_NAME_PREFIX + actionID;
    }

    public static final String SQL_CREATE_TABLE(String actionID) {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME(actionID) + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMNS_NAME_REINFORCEMENT_DECISION + " TEXT" +
                        " )";
    }

    public static final String SQL_DROP_TABLE(String actionID) {
        return "DROP TABLE IF EXISTS " + TABLE_NAME(actionID);
    }
}
