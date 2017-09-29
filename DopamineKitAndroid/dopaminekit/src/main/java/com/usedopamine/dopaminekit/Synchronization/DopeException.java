package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.usedopamine.dopaminekit.DataStore.Contracts.DopeExceptionContract;
import com.usedopamine.dopaminekit.DataStore.SQLDopeExceptionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;

import org.json.JSONException;

import java.util.TimeZone;

/**
 * Created by cuddergambino on 10/3/16.
 */

class DopeException {

    private static SQLiteDatabase sqlDB;

    private long utc;
    private long timezoneOffset;
    private String exceptionClassName;
    private String message;
    private String stackTrace;

    DopeException(Throwable exception) {
        this.utc = System.currentTimeMillis();
        this.timezoneOffset = TimeZone.getDefault().getOffset(this.utc);
        this.exceptionClassName = exception.getClass().getName();
        this.message = exception.getMessage();
        this.stackTrace = Log.getStackTraceString(exception);
    }

    /**
     * Convenient method for creating and storing an exception.
     *
     * @param context   Context
     * @param exception An exception thrown within DopamineKit
     */
    static void store(Context context, Throwable exception) {
        DopeException dopeException = new DopeException(exception);
        dopeException.store(context);
    }

    /**
     * Stores the exception to be synced with the DopamineAPI at a later time.
     *
     * @param context Context
     */
    void store(Context context) {
        if (sqlDB == null) {
            sqlDB = SQLiteDataStore.getInstance(context).getWritableDatabase();
        }

        DopeExceptionContract exceptionContract = new DopeExceptionContract(0, utc, timezoneOffset, exceptionClassName, message, stackTrace);
        long rowId = SQLDopeExceptionDataHelper.insert(sqlDB, exceptionContract);

//        DopamineKit.debugLog("SQL Dope Exceptions", "Inserted into row " + rowId);
        try {
            DopamineKit.debugLog("SQL Dope Exceptions", exceptionContract.toJSON().toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
            Telemetry.storeException(e);
        }
    }

}
