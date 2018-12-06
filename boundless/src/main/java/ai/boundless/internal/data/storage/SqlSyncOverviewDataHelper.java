package ai.boundless.internal.data.storage;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import ai.boundless.internal.data.storage.contracts.SyncOverviewContract;

/**
 * Created by cuddergambino on 9/30/16.
 */
public class SqlSyncOverviewDataHelper extends SqlDataHelper {

  /**
   * Create table.
   *
   * @param db the db
   */
  static void createTable(SQLiteDatabase db) {
    db.execSQL(SyncOverviewContract.SQL_CREATE_TABLE);
  }

  /**
   * Drop table.
   *
   * @param db the db
   */
  static void dropTable(SQLiteDatabase db) {
    db.execSQL(SyncOverviewContract.SQL_DROP_TABLE);
  }

  /**
   * Insert long.
   *
   * @param db the db
   * @param item the item
   * @return the long
   */
  public static long insert(SQLiteDatabase db, SyncOverviewContract item) {
    ContentValues values = new ContentValues();
    values.put(SyncOverviewContract.COLUMNS_NAME_UTC, item.utc);
    values.put(SyncOverviewContract.COLUMNS_NAME_TIMEZONEOFFSET, item.timezoneOffset);
    values.put(SyncOverviewContract.COLUMNS_NAME_TOTALSYNCTIME, item.totalSyncTime);
    values.put(SyncOverviewContract.COLUMNS_NAME_CAUSE, item.cause);
    values.put(SyncOverviewContract.COLUMNS_NAME_TRACK, item.track);
    values.put(SyncOverviewContract.COLUMNS_NAME_REPORT, item.report);
    values.put(SyncOverviewContract.COLUMNS_NAME_CARTRIDGES, item.cartridges);

    item.id = db.insert(SyncOverviewContract.TABLE_NAME, null, values);
    return item.id;
  }

  /**
   * Delete.
   *
   * @param db the db
   * @param item the item
   */
  public static void delete(SQLiteDatabase db, SyncOverviewContract item) {
    String selection = SyncOverviewContract._ID + " LIKE ? ";
    String[] args = {String.valueOf(item.id)};
    db.delete(SyncOverviewContract.TABLE_NAME, selection, args);
  }

  /**
   * Find sync overview contract.
   *
   * @param db the db
   * @param item the item
   * @return the sync overview contract
   */
  @Nullable
  public static SyncOverviewContract find(SQLiteDatabase db, SyncOverviewContract item) {
    SyncOverviewContract result = null;
    Cursor cursor = null;
    try {
      cursor = db.query(
          SyncOverviewContract.TABLE_NAME,
          new String[]{SyncOverviewContract._ID, SyncOverviewContract.COLUMNS_NAME_UTC,
              SyncOverviewContract.COLUMNS_NAME_TIMEZONEOFFSET, SyncOverviewContract
              .COLUMNS_NAME_TOTALSYNCTIME, SyncOverviewContract.COLUMNS_NAME_CAUSE,
              SyncOverviewContract.COLUMNS_NAME_TRACK, SyncOverviewContract.COLUMNS_NAME_REPORT,
              SyncOverviewContract.COLUMNS_NAME_CARTRIDGES},
          SyncOverviewContract._ID + "=?",
          new String[]{String.valueOf(item.id)},
          null,
          null,
          null,
          null
      );
      result = cursor.moveToFirst() ? SyncOverviewContract.fromCursor(cursor) : null;
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
  public static ArrayList<SyncOverviewContract> findAll(SQLiteDatabase db) {
    ArrayList<SyncOverviewContract> syncOverviews = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("SELECT * FROM " + SyncOverviewContract.TABLE_NAME, null);
      if (cursor.moveToFirst()) {
        do {
          SyncOverviewContract syncOverview = SyncOverviewContract.fromCursor(cursor);
          syncOverviews.add(syncOverview);
        } while (cursor.moveToNext());
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return syncOverviews;
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
      cursor = db.rawQuery("SELECT COUNT(*) FROM " + SyncOverviewContract.TABLE_NAME, null);
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
