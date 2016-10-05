package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.usedopamine.dopaminekit.DataStore.Contracts.DopeExceptionContract;
import com.usedopamine.dopaminekit.DataStore.SQLDopeExceptionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;

import java.util.TimeZone;

/**
 * Created by cuddergambino on 10/3/16.
 */

public class DopeException {

    private static SQLiteDatabase sqlDB;

    long utc;
    long timezoneOffset;
    String exceptionClassName;
    String message;
    String stackTrace;

    public DopeException(Throwable exception) {
        this.utc = System.currentTimeMillis();
        this.timezoneOffset = TimeZone.getDefault().getOffset(this.utc);
        this.exceptionClassName = exception.getClass().getName();
        this.message = exception.getMessage();
        this.stackTrace = Log.getStackTraceString(exception);
    }

    public static void store(Context context, Throwable exception) {
        DopeException dopeException = new DopeException(exception);
        dopeException.store(context);
    }

    public void store(Context context) {
        if (sqlDB == null) {
            sqlDB = SQLiteDataStore.getInstance(context).getWritableDatabase();
        }
        long rowId = SQLDopeExceptionDataHelper.insert(sqlDB,
                new DopeExceptionContract(0, utc, timezoneOffset, exceptionClassName, message, stackTrace)
        );

        DopamineKit.debugLog("SQL Dope Exceptions", "Inserted into row " + rowId);
    }

}
