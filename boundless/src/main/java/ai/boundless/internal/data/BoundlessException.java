package ai.boundless.internal.data;

import java.util.TimeZone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import ai.boundless.BoundlessKit;
import ai.boundless.internal.data.storage.SqlBoundlessExceptionDataHelper;
import ai.boundless.internal.data.storage.SqliteDataStore;
import ai.boundless.internal.data.storage.contracts.BoundlessExceptionContract;
import org.json.JSONException;

/**
 * Created by cuddergambino on 10/3/16.
 */

class BoundlessException {

  private static SQLiteDatabase sqlDb;

  private long utc;
  private long timezoneOffset;
  private String exceptionClassName;
  private String message;
  private String stackTrace;

  BoundlessException(Throwable exception) {
    this.utc = System.currentTimeMillis();
    this.timezoneOffset = TimeZone.getDefault().getOffset(this.utc);
    this.exceptionClassName = exception.getClass().getName();
    this.message = exception.getMessage();
    this.stackTrace = Log.getStackTraceString(exception);
  }

  /**
   * Convenient method for creating and storing an exception.
   *
   * @param context Context
   * @param exception An exception thrown within BoundlessKit
   */
  static void store(Context context, Throwable exception) {
    BoundlessException boundlessException = new BoundlessException(exception);
    boundlessException.store(context);
  }

  /**
   * Stores the exception to be synced with the BoundlessAPI at a later time.
   *
   * @param context Context
   */
  void store(Context context) {
    if (sqlDb == null) {
      sqlDb = SqliteDataStore.getInstance(context).getWritableDatabase();
    }

    BoundlessExceptionContract exceptionContract = new BoundlessExceptionContract(0,
        utc,
        timezoneOffset,
        exceptionClassName,
        message,
        stackTrace
    );
    long rowId = SqlBoundlessExceptionDataHelper.insert(sqlDb, exceptionContract);

    // BoundlessKit.debugLog("BoundlessException", "Inserted into row " + rowId);
    try {
      BoundlessKit.debugLog("BoundlessException", exceptionContract.toJson().toString(2));
    } catch (JSONException e) {
      e.printStackTrace();
      Telemetry.storeException(e);
    }
  }

}
