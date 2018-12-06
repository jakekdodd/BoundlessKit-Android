package ai.boundless.internal.data.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import ai.boundless.BoundlessKit;
import ai.boundless.internal.data.storage.contracts.UserIdentityContract;

/**
 * The type Sql user identity data helper.
 */
public class SqlUserIdentityDataHelper extends SqlDataHelper {

  /**
   * Create table.
   *
   * @param db the db
   */
  static void createTable(SQLiteDatabase db) {
    db.execSQL(UserIdentityContract.SQL_CREATE_TABLE);
  }

  /**
   * Drop table.
   *
   * @param db the db
   */
  static void dropTable(SQLiteDatabase db) {
    db.execSQL(UserIdentityContract.SQL_DROP_TABLE);
  }

  /**
   * Insert long.
   *
   * @param db the db
   * @param item the item
   * @return the long
   */
  public static long insert(SQLiteDatabase db, UserIdentityContract item) {
    ContentValues values = new ContentValues();
    values.put(UserIdentityContract._ID, 0);
    values.put(UserIdentityContract.COLUMNS_NAME_INTERNALID, item.internalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXTERNALID, item.externalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXPERIMENTGROUP, item.experimentGroup);

    item.id = db.insert(UserIdentityContract.TABLE_NAME, null, values);
    return item.id;
  }

  /**
   * Update long.
   *
   * @param db the db
   * @param item the item
   * @return the long
   */
  public static long update(SQLiteDatabase db, UserIdentityContract item) {
    ContentValues values = new ContentValues();
    values.put(UserIdentityContract._ID, 0);
    values.put(UserIdentityContract.COLUMNS_NAME_INTERNALID, item.internalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXTERNALID, item.externalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXPERIMENTGROUP, item.experimentGroup);

    item.id = db.update(UserIdentityContract.TABLE_NAME, values, null, null);
    return item.id;
  }

  /**
   * Delete int.
   *
   * @param db the db
   * @return the int
   */
  public static int delete(SQLiteDatabase db) {
    int numDeleted = db.delete(UserIdentityContract.TABLE_NAME, null, null);
    BoundlessKit.debugLog(
        "SQLUserIdentityDataHelper",
        "Deleted " + numDeleted + " items from Table:" + UserIdentityContract.TABLE_NAME
            + " successful."
    );
    return numDeleted;
  }

  /**
   * Find user identity contract.
   *
   * @param db the db
   * @return the user identity contract
   */
  @Nullable
  public static UserIdentityContract find(SQLiteDatabase db) {
    UserIdentityContract result = null;
    Cursor cursor = null;
    try {
      cursor = db.query(
          UserIdentityContract.TABLE_NAME,
          new String[]{UserIdentityContract._ID, UserIdentityContract.COLUMNS_NAME_INTERNALID,
              UserIdentityContract.COLUMNS_NAME_EXTERNALID, UserIdentityContract
              .COLUMNS_NAME_EXPERIMENTGROUP},
          null,
          null,
          null,
          null,
          null
      );
      if (cursor.moveToFirst()) {
        result = UserIdentityContract.fromCursor(cursor);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return result;
  }

}
