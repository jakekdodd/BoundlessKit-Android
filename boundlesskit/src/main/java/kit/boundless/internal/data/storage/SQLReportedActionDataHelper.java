package kit.boundless.internal.data.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import kit.boundless.BoundlessKit;
import kit.boundless.internal.data.storage.contracts.ReportedActionContract;

/**
 * Created by cuddergambino on 8/9/16.
 */

public class SQLReportedActionDataHelper extends SQLDataHelper {

    static void createTable(SQLiteDatabase db) {
        db.execSQL(ReportedActionContract.SQL_CREATE_TABLE);
    }

    static void dropTable(SQLiteDatabase db) {
        db.execSQL(ReportedActionContract.SQL_DROP_TABLE);
    }

    public static long insert(SQLiteDatabase db, ReportedActionContract item) {
        ContentValues values = new ContentValues();
        values.put(ReportedActionContract.COLUMNS_NAME_ACTIONNAME, item.actionID);
        values.put(ReportedActionContract.COLUMNS_NAME_CARTRIDGEID, item.cartridgeId);
        values.put(ReportedActionContract.COLUMNS_NAME_REINFORCEMENTDECISION, item.reinforcementDecision);
        values.put(ReportedActionContract.COLUMNS_NAME_METADATA, item.metaData);
        values.put(ReportedActionContract.COLUMNS_NAME_UTC, item.utc);
        values.put(ReportedActionContract.COLUMNS_NAME_TIMEZONEOFFSET, item.timezoneOffset);

        item.id = db.insert(ReportedActionContract.TABLE_NAME, null, values);
        return item.id;
    }

    public static void delete(SQLiteDatabase db, ReportedActionContract item) {
        String selection = ReportedActionContract._ID + " LIKE ? ";
        String[] args = {String.valueOf(item.id)};
        db.delete(ReportedActionContract.TABLE_NAME, selection, args);
    }

    public static @Nullable ReportedActionContract find(SQLiteDatabase db, ReportedActionContract item) {
        ReportedActionContract result = null;
        Cursor cursor = null;
        try {
            cursor = db.query(ReportedActionContract.TABLE_NAME,
                    new String[] {ReportedActionContract._ID, ReportedActionContract.COLUMNS_NAME_ACTIONNAME, ReportedActionContract.COLUMNS_NAME_CARTRIDGEID, ReportedActionContract.COLUMNS_NAME_CARTRIDGEID, ReportedActionContract.COLUMNS_NAME_METADATA, ReportedActionContract.COLUMNS_NAME_UTC, ReportedActionContract.COLUMNS_NAME_TIMEZONEOFFSET},
                    ReportedActionContract._ID + "=?",
                    new String[] {String.valueOf(item.id) }, null, null, null, null
            );
            result = cursor.moveToFirst() ? ReportedActionContract.fromCursor(cursor) : null;
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return result;
    }

    public static ArrayList<ReportedActionContract> findAll(SQLiteDatabase db) {
        ArrayList<ReportedActionContract> actions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + ReportedActionContract.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                do {
                    ReportedActionContract action = ReportedActionContract.fromCursor(cursor);
                    actions.add(action);
                    BoundlessKit.debugLog("SQLReportedActionDataHelper", "Found row:" + action.id + " actionID:" + action.actionID + " reinforcementDecision:" + action.reinforcementDecision + " metaData:" + action.metaData + " utc:" + action.utc + " timezoneOffset:" + action.timezoneOffset);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return actions;
    }

    public static int count(SQLiteDatabase db) {
        int result = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + ReportedActionContract.TABLE_NAME, null);
            if (cursor.moveToFirst()) { result = cursor.getInt(0); }
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return result;
    }
}
