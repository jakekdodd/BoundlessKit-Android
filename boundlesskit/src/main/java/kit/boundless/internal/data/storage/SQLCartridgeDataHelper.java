package kit.boundless.internal.data.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import kit.boundless.BoundlessKit;
import kit.boundless.internal.data.storage.contracts.ReinforcementDecisionContract;

/**
 * Created by cuddergambino on 8/21/16.
 */

public class SQLCartridgeDataHelper extends SQLDataHelper {

    static void createTable(SQLiteDatabase db) {
        db.execSQL(ReinforcementDecisionContract.SQL_CREATE_TABLE);
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL(ReinforcementDecisionContract.SQL_DROP_TABLE);
    }

    public static long insert(SQLiteDatabase db, ReinforcementDecisionContract item) {
        ContentValues values = new ContentValues();
        values.put(ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID, item.actionID);
        values.put(ReinforcementDecisionContract.COLUMNS_NAME_REINFORCEMENTDECISION, item.reinforcementDecision);

        item.id = db.insert(ReinforcementDecisionContract.TABLE_NAME, null, values);
        return item.id;
    }

    public static void delete(SQLiteDatabase db, ReinforcementDecisionContract item) {
        String selection = ReinforcementDecisionContract._ID + " LIKE ? ";
        String[] args = {String.valueOf(item.id)};
        db.delete(ReinforcementDecisionContract.TABLE_NAME, selection, args);
    }

    public static int deleteAllFor(SQLiteDatabase db, String actionID) {
        String selection = ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID + " LIKE ? ";
        String[] args = {actionID};
        int numDeleted = db.delete(ReinforcementDecisionContract.TABLE_NAME, selection, args);
        BoundlessKit.debugLog("SQLCartridge", "Deleted "+numDeleted+" items from Table:"+ReinforcementDecisionContract.TABLE_NAME+" with actionID"+actionID+" successful.");
        return numDeleted;
    }

    public static ArrayList<ReinforcementDecisionContract> findAll(SQLiteDatabase db) {
        ArrayList<ReinforcementDecisionContract> results = new ArrayList<ReinforcementDecisionContract>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + ReinforcementDecisionContract.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                do {
                    ReinforcementDecisionContract reinforcementDecision = ReinforcementDecisionContract.fromCursor(cursor);
                    results.add(reinforcementDecision);
                    BoundlessKit.debugLog("SQLCartridgeDataHelper", "Found in table " + ReinforcementDecisionContract.TABLE_NAME + " row:" + reinforcementDecision.id + " reinforcementDecision:" + reinforcementDecision.reinforcementDecision);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return results;
    }

    public static @Nullable ReinforcementDecisionContract findFirstFor(SQLiteDatabase db, String actionID) {
        ReinforcementDecisionContract result = null;
        Cursor cursor = null;
        try {
            cursor = db.query(ReinforcementDecisionContract.TABLE_NAME,
                    new String[] {ReinforcementDecisionContract._ID, ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID, ReinforcementDecisionContract.COLUMNS_NAME_REINFORCEMENTCARTRIDGEID, ReinforcementDecisionContract.COLUMNS_NAME_REINFORCEMENTDECISION},
                    ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID+"=?", new String[] {actionID},
                    null, null,
                    ReinforcementDecisionContract._ID + " ASC", "1"
            );
            if (cursor.moveToFirst()) {
                result = ReinforcementDecisionContract.fromCursor(cursor);
            }
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return result;
    }

    public static int count(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + ReinforcementDecisionContract.TABLE_NAME, null);
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            if (cursor != null) { cursor.close(); }
        }
    }

    public static int countFor(SQLiteDatabase db, String actionID) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT("+ReinforcementDecisionContract._ID+") FROM " + ReinforcementDecisionContract.TABLE_NAME+" WHERE "+ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID+" LIKE '"+actionID+"'" , null);
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            if (cursor != null) { cursor.close(); }
        }
    }

}
