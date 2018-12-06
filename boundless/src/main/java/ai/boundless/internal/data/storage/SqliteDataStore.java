package ai.boundless.internal.data.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by cuddergambino on 8/4/16.
 */
public class SqliteDataStore extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 4;
  private static final String DATABASE_NAME = "boundless.boundlesskit.sqlite";

  private static SqliteDataStore sharedInstance = null;

  private SqliteDataStore(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  /**
   * Gets instance.
   *
   * @param context the context
   * @return the instance
   */
  public static SqliteDataStore getInstance(Context context) {
    if (sharedInstance == null) {
      sharedInstance = new SqliteDataStore(context);
    }
    return sharedInstance;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    SqlTrackedActionDataHelper.createTable(db);
    SqlReportedActionDataHelper.createTable(db);
    SqlCartridgeDataHelper.createTable(db);
    SqlSyncOverviewDataHelper.createTable(db);
    SqlBoundlessExceptionDataHelper.createTable(db);
    SqlUserIdentityDataHelper.createTable(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    SqlTrackedActionDataHelper.dropTable(db);
    SqlReportedActionDataHelper.dropTable(db);
    SqlCartridgeDataHelper.dropTable(db);
    SqlSyncOverviewDataHelper.dropTable(db);
    SqlBoundlessExceptionDataHelper.dropTable(db);
    SqlUserIdentityDataHelper.dropTable(db);
    onCreate(db);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }
}
