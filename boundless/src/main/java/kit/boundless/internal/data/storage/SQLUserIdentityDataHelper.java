package kit.boundless.internal.data.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import kit.boundless.BoundlessKit;
import kit.boundless.internal.data.storage.contracts.UserIdentityContract;

public class SQLUserIdentityDataHelper extends SQLDataHelper {

  static void createTable(SQLiteDatabase db) {
    db.execSQL(UserIdentityContract.SQL_CREATE_TABLE);
  }

  static void dropTable(SQLiteDatabase db) {
    db.execSQL(UserIdentityContract.SQL_DROP_TABLE);
  }

  public static long insert(SQLiteDatabase db, UserIdentityContract item) {
    ContentValues values = new ContentValues();
    values.put(UserIdentityContract._ID, 0);
    values.put(UserIdentityContract.COLUMNS_NAME_INTERNALID, item.internalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXTERNALID, item.externalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXPERIMENTGROUP, item.experimentGroup);

    item.id = db.insert(UserIdentityContract.TABLE_NAME, null, values);
    return item.id;
  }

  public static long update(SQLiteDatabase db, UserIdentityContract item) {
    ContentValues values = new ContentValues();
    values.put(UserIdentityContract._ID, 0);
    values.put(UserIdentityContract.COLUMNS_NAME_INTERNALID, item.internalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXTERNALID, item.externalId);
    values.put(UserIdentityContract.COLUMNS_NAME_EXPERIMENTGROUP, item.experimentGroup);

    item.id = db.update(UserIdentityContract.TABLE_NAME, values, null, null);
    return item.id;
  }

  public static int delete(SQLiteDatabase db) {
    int numDeleted = db.delete(UserIdentityContract.TABLE_NAME, null, null);
    BoundlessKit.debugLog(
        "SQLUserIdentityDataHelper",
        "Deleted " + numDeleted + " items from Table:" + UserIdentityContract.TABLE_NAME
            + " successful."
    );
    return numDeleted;
  }

  public static @Nullable
  UserIdentityContract find(SQLiteDatabase db) {
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
