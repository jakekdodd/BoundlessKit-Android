package ai.boundless.internal.data.storage;

import java.util.ArrayList;

import ai.boundless.internal.data.storage.contracts.TrackedActionContract;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

/**
 * Created by cuddergambino on 8/9/16.
 */
public class SqlTrackedActionDataHelper extends SqlDataHelper {

  /**
   * Create table.
   *
   * @param db the db
   */
  static void createTable(SQLiteDatabase db) {
    db.execSQL(TrackedActionContract.SQL_CREATE_TABLE);
  }

  /**
   * Drop table.
   *
   * @param db the db
   */
  static void dropTable(SQLiteDatabase db) {
    db.execSQL(TrackedActionContract.SQL_DROP_TABLE);
  }

  /**
   * Insert long.
   *
   * @param db the db
   * @param item the item
   * @return the long
   */
  public static long insert(SQLiteDatabase db, TrackedActionContract item) {
    ContentValues values = new ContentValues();
    values.put(TrackedActionContract.COLUMNS_NAME_ACTIONNAME, item.actionId);
    values.put(TrackedActionContract.COLUMNS_NAME_METADATA, item.metaData);
    values.put(TrackedActionContract.COLUMNS_NAME_UTC, item.utc);
    values.put(TrackedActionContract.COLUMNS_NAME_TIMEZONEOFFSET, item.timezoneOffset);

    item.id = db.insert(TrackedActionContract.TABLE_NAME, null, values);
    return item.id;
  }

  /**
   * Delete.
   *
   * @param db the db
   * @param item the item
   */
  public static void delete(SQLiteDatabase db, TrackedActionContract item) {
    String selection = TrackedActionContract._ID + " LIKE ? ";
    String[] args = {String.valueOf(item.id)};
    db.delete(TrackedActionContract.TABLE_NAME, selection, args);
  }

  /**
   * Find tracked action contract.
   *
   * @param db the db
   * @param item the item
   * @return the tracked action contract
   */
  @Nullable
  public static TrackedActionContract find(SQLiteDatabase db, TrackedActionContract item) {
    TrackedActionContract result = null;
    Cursor cursor = null;
    try {
      cursor = db.query(
          TrackedActionContract.TABLE_NAME,
          new String[]{TrackedActionContract._ID, TrackedActionContract.COLUMNS_NAME_ACTIONNAME,
              TrackedActionContract.COLUMNS_NAME_METADATA, TrackedActionContract
              .COLUMNS_NAME_UTC, TrackedActionContract.COLUMNS_NAME_TIMEZONEOFFSET},
          TrackedActionContract._ID + "=?",
          new String[]{String.valueOf(item.id)},
          null,
          null,
          null,
          null
      );
      result = cursor.moveToFirst() ? TrackedActionContract.fromCursor(cursor) : null;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return result;
  }

  /**
   * Find all array list.
   *
   * @param db the db
   * @return the array list
   */
  public static ArrayList<TrackedActionContract> findAll(SQLiteDatabase db) {
    ArrayList<TrackedActionContract> actions = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT * FROM " + TrackedActionContract.TABLE_NAME, null);
      if (cursor.moveToFirst()) {
        do {
          TrackedActionContract action = TrackedActionContract.fromCursor(cursor);
          actions.add(action);
        } while (cursor.moveToNext());
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return actions;
  }

  /**
   * Count int.
   *
   * @param db the db
   * @return the int
   */
  public static int count(SQLiteDatabase db) {
    int result = 0;
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT COUNT(*) FROM " + TrackedActionContract.TABLE_NAME, null);
      if (cursor.moveToFirst()) {
        result = cursor.getInt(0);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return result;
  }
}
