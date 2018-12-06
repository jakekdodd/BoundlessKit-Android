package ai.boundless.internal.data.storage;

import java.util.ArrayList;

import ai.boundless.internal.data.storage.contracts.BoundlessExceptionContract;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

/**
 * Created by cuddergambino on 10/3/16.
 */
public class SqlBoundlessExceptionDataHelper extends SqlDataHelper {

  /**
   * Create table.
   *
   * @param db the db
   */
  static void createTable(SQLiteDatabase db) {
    db.execSQL(BoundlessExceptionContract.SQL_CREATE_TABLE);
  }

  /**
   * Drop table.
   *
   * @param db the db
   */
  static void dropTable(SQLiteDatabase db) {
    db.execSQL(BoundlessExceptionContract.SQL_DROP_TABLE);
  }

  /**
   * Insert long.
   *
   * @param db the db
   * @param item the item
   * @return the long
   */
  public static long insert(SQLiteDatabase db, BoundlessExceptionContract item) {
    ContentValues values = new ContentValues();
    values.put(BoundlessExceptionContract.COLUMNS_NAME_UTC, item.utc);
    values.put(BoundlessExceptionContract.COLUMNS_NAME_TIMEZONEOFFSET, item.timezoneOffset);
    values.put(BoundlessExceptionContract.COLUMNS_NAME_EXCEPTIONCLASSNAME, item.exceptionClassName);
    values.put(BoundlessExceptionContract.COLUMNS_NAME_MESSAGE, item.message);
    values.put(BoundlessExceptionContract.COLUMNS_NAME_STACKTRACE, item.stackTrace);

    item.id = db.insert(BoundlessExceptionContract.TABLE_NAME, null, values);
    return item.id;
  }

  /**
   * Delete.
   *
   * @param db the db
   * @param item the item
   */
  public static void delete(SQLiteDatabase db, BoundlessExceptionContract item) {
    String selection = BoundlessExceptionContract._ID + " LIKE ? ";
    String[] args = {String.valueOf(item.id)};
    db.delete(BoundlessExceptionContract.TABLE_NAME, selection, args);
  }

  /**
   * Find boundless exception contract.
   *
   * @param db the db
   * @param item the item
   * @return the boundless exception contract
   */
  @Nullable
  public static BoundlessExceptionContract find(
      SQLiteDatabase db, BoundlessExceptionContract item) {
    BoundlessExceptionContract result = null;
    Cursor cursor = null;
    try {
      cursor = db.query(
          BoundlessExceptionContract.TABLE_NAME,
          new String[]{BoundlessExceptionContract._ID, BoundlessExceptionContract
              .COLUMNS_NAME_UTC, BoundlessExceptionContract.COLUMNS_NAME_TIMEZONEOFFSET,
              BoundlessExceptionContract.COLUMNS_NAME_EXCEPTIONCLASSNAME,
              BoundlessExceptionContract.COLUMNS_NAME_MESSAGE, BoundlessExceptionContract
              .COLUMNS_NAME_STACKTRACE},
          BoundlessExceptionContract._ID + "=?",
          new String[]{String.valueOf(item.id)},
          null,
          null,
          null,
          null
      );
      result = cursor.moveToFirst() ? BoundlessExceptionContract.fromCursor(cursor) : null;
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
  public static ArrayList<BoundlessExceptionContract> findAll(SQLiteDatabase db) {
    ArrayList<BoundlessExceptionContract> exceptions = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT * FROM " + BoundlessExceptionContract.TABLE_NAME, null);
      if (cursor.moveToFirst()) {
        do {
          exceptions.add(BoundlessExceptionContract.fromCursor(cursor));
        } while (cursor.moveToNext());
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return exceptions;
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
      cursor = db.rawQuery("SELECT COUNT(*) FROM " + BoundlessExceptionContract.TABLE_NAME, null);
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
