package ai.boundless.internal.data.storage;

import java.util.ArrayList;

import ai.boundless.BoundlessKit;
import ai.boundless.internal.data.storage.contracts.ReinforcementDecisionContract;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

/**
 * Created by cuddergambino on 8/21/16.
 */
public class SqlCartridgeDataHelper extends SqlDataHelper {

  /**
   * Create table.
   *
   * @param db the db
   */
  static void createTable(SQLiteDatabase db) {
    db.execSQL(ReinforcementDecisionContract.SQL_CREATE_TABLE);
  }

  /**
   * Drop table.
   *
   * @param db the db
   */
  static void dropTable(SQLiteDatabase db) {
    db.execSQL(ReinforcementDecisionContract.SQL_DROP_TABLE);
  }

  /**
   * Insert long.
   *
   * @param db the db
   * @param item the item
   * @return the long
   */
  public static long insert(SQLiteDatabase db, ReinforcementDecisionContract item) {
    ContentValues values = new ContentValues();
    values.put(ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID, item.actionId);
    values.put(
        ReinforcementDecisionContract.COLUMNS_NAME_REINFORCEMENTDECISION,
        item.reinforcementDecision
    );

    item.id = db.insert(ReinforcementDecisionContract.TABLE_NAME, null, values);
    return item.id;
  }

  /**
   * Delete.
   *
   * @param db the db
   * @param item the item
   */
  public static void delete(SQLiteDatabase db, ReinforcementDecisionContract item) {
    String selection = ReinforcementDecisionContract._ID + " LIKE ? ";
    String[] args = {String.valueOf(item.id)};
    db.delete(ReinforcementDecisionContract.TABLE_NAME, selection, args);
  }

  /**
   * Delete all for int.
   *
   * @param db the db
   * @param actionId the action id
   * @return the int
   */
  public static int deleteAllFor(SQLiteDatabase db, String actionId) {
    String selection = ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID + " LIKE ? ";
    String[] args = {actionId};
    int numDeleted = db.delete(ReinforcementDecisionContract.TABLE_NAME, selection, args);
    BoundlessKit.debugLog(
        "SQLCartridge",
        "Deleted " + numDeleted + " items from Table:" + ReinforcementDecisionContract.TABLE_NAME
            + " with actionId" + actionId + " successful."
    );
    return numDeleted;
  }

  /**
   * Find all array list.
   *
   * @param db the db
   * @return the array list
   */
  public static ArrayList<ReinforcementDecisionContract> findAll(SQLiteDatabase db) {
    ArrayList<ReinforcementDecisionContract> results =
        new ArrayList<ReinforcementDecisionContract>();
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT * FROM " + ReinforcementDecisionContract.TABLE_NAME, null);
      if (cursor.moveToFirst()) {
        do {
          ReinforcementDecisionContract reinforcementDecision =
              ReinforcementDecisionContract.fromCursor(cursor);
          results.add(reinforcementDecision);
          BoundlessKit.debugLog(
              "SQLCartridgeDataHelper",
              "Found in table " + ReinforcementDecisionContract.TABLE_NAME + " row:"
                  + reinforcementDecision.id + " reinforcementDecision:"
                  + reinforcementDecision.reinforcementDecision
          );
        } while (cursor.moveToNext());
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return results;
  }

  /**
   * Find first for reinforcement decision contract.
   *
   * @param db the db
   * @param actionId the action id
   * @return the reinforcement decision contract
   */
  @Nullable
  public static ReinforcementDecisionContract findFirstFor(SQLiteDatabase db, String actionId) {
    ReinforcementDecisionContract result = null;
    Cursor cursor = null;
    try {
      cursor = db.query(
          ReinforcementDecisionContract.TABLE_NAME,
          new String[]{ReinforcementDecisionContract._ID, ReinforcementDecisionContract
              .COLUMNS_NAME_ACTIONID, ReinforcementDecisionContract
              .COLUMNS_NAME_REINFORCEMENTCARTRIDGEID, ReinforcementDecisionContract
              .COLUMNS_NAME_REINFORCEMENTDECISION},
          ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID + "=?",
          new String[]{actionId},
          null,
          null,
          ReinforcementDecisionContract._ID + " ASC",
          "1"
      );
      if (cursor.moveToFirst()) {
        result = ReinforcementDecisionContract.fromCursor(cursor);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return result;
  }

  /**
   * Count int.
   *
   * @param db the db
   * @return the int
   */
  public static int count(SQLiteDatabase db) {
    Cursor cursor = null;
    try {
      cursor =
          db.rawQuery("SELECT COUNT(*) FROM " + ReinforcementDecisionContract.TABLE_NAME, null);
      return cursor.moveToFirst() ? cursor.getInt(0) : 0;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  /**
   * Count for int.
   *
   * @param db the db
   * @param actionId the action id
   * @return the int
   */
  public static int countFor(SQLiteDatabase db, String actionId) {
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT COUNT(" + ReinforcementDecisionContract._ID + ") FROM "
          + ReinforcementDecisionContract.TABLE_NAME + " WHERE "
          + ReinforcementDecisionContract.COLUMNS_NAME_ACTIONID + " LIKE '" + actionId + "'", null);
      return cursor.moveToFirst() ? cursor.getInt(0) : 0;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

}
