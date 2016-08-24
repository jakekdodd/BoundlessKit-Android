package com.usedopamine.dopaminekit.DataStore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;

import com.usedopamine.dopaminekit.DataStore.Contracts.ReinforcementDecisionContract;
import com.usedopamine.dopaminekit.DopamineKit;

import java.util.ArrayList;

/**
 * Created by cuddergambino on 8/21/16.
 */

public class SQLCartridgeDataHelper {

    public static void createTable(SQLiteDatabase db, String actionID) {
        db.execSQL(ReinforcementDecisionContract.SQL_CREATE_TABLE(actionID));
    }

    public static void dropTable(SQLiteDatabase db, String actionID) {
        db.execSQL(ReinforcementDecisionContract.SQL_DROP_TABLE(actionID));
    }

    public static long insert(SQLiteDatabase db, ReinforcementDecisionContract item) {
        ContentValues values = new ContentValues();
        values.put(ReinforcementDecisionContract.COLUMNS_NAME_REINFORCEMENT_DECISION, item.reinforcementDecision);

        item.id = db.insert(ReinforcementDecisionContract.TABLE_NAME(item.actionID), null, values);
        return item.id;
    }

    public static void delete(SQLiteDatabase db, ReinforcementDecisionContract item) {
        String tableName = ReinforcementDecisionContract.TABLE_NAME(item.actionID);
        String selection = ReinforcementDecisionContract._ID + " LIKE ? ";
        String[] args = {String.valueOf(item.id)};
        db.delete(tableName, selection, args);
    }

    public static void deleteAll(SQLiteDatabase db, String actionID) {
        db.execSQL("DELETE FROM " + ReinforcementDecisionContract.TABLE_NAME(actionID));
    }

    public static @Nullable ReinforcementDecisionContract find(SQLiteDatabase db, ReinforcementDecisionContract item) {
        Cursor cursor = null;
        try {
            cursor = db.query(ReinforcementDecisionContract.TABLE_NAME(item.actionID),
                    new String[] {ReinforcementDecisionContract._ID, ReinforcementDecisionContract.COLUMNS_NAME_REINFORCEMENT_DECISION},
                    ReinforcementDecisionContract._ID + "=?",
                    new String[] {String.valueOf(item.id)}, null, null, null, null
            );
            return cursor.moveToFirst() ? ReinforcementDecisionContract.fromCursor(item.actionID, cursor) : null;
        } finally {
            if (cursor != null) { cursor.close(); }
        }
    }

    public static @Nullable ReinforcementDecisionContract pop(SQLiteDatabase db, String actionID) {
        ReinforcementDecisionContract result = null;
        Cursor cursor = null;
        try {
            cursor = db.query(ReinforcementDecisionContract.TABLE_NAME(actionID),
                    new String[] {ReinforcementDecisionContract._ID, ReinforcementDecisionContract.COLUMNS_NAME_REINFORCEMENT_DECISION},
                    null, null, null, null,
                    ReinforcementDecisionContract._ID + " ASC", "1"
            );
            if (cursor.moveToFirst()) {
                result = ReinforcementDecisionContract.fromCursor(actionID, cursor);
                delete(db, result);
            }
        } finally {
            if(cursor != null) { cursor.close(); }
            return result;
        }
    }

    public static ArrayList<ReinforcementDecisionContract> findAll(SQLiteDatabase db, String actionID) {
        ArrayList<ReinforcementDecisionContract> results = new ArrayList<ReinforcementDecisionContract>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + ReinforcementDecisionContract.TABLE_NAME(actionID), null);
            if (cursor.moveToFirst()) {
                do {
                    ReinforcementDecisionContract reinforcementDecision = ReinforcementDecisionContract.fromCursor(actionID, cursor);
                    results.add(reinforcementDecision);
                    DopamineKit.debugLog("SQLCartridgeDataHelper", "Found in table " + ReinforcementDecisionContract.TABLE_NAME(actionID) + " row:" + reinforcementDecision.id + " reinforcementDecision:" + reinforcementDecision.reinforcementDecision);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) { cursor.close(); }
            return results;
        }
    }

    public static int count(SQLiteDatabase db, String actionID) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + ReinforcementDecisionContract.TABLE_NAME(actionID), null);
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            if(cursor != null) { cursor.close(); }
        }
    }

}
