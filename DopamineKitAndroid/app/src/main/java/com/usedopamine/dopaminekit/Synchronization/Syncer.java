package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;

import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cuddergambino on 8/17/16.
 */

public abstract class Syncer extends ContextWrapper implements Callable<JSONObject> {

    protected static SQLiteDatabase sqlDB = null;
    protected static DopamineAPI dopamineAPI = null;

    public Syncer(Context context) {
        super(context);
        if(sqlDB == null) {
            sqlDB = SQLiteDataStore.getInstance(context).getWritableDatabase();
        }
        if(dopamineAPI == null) {
            dopamineAPI = DopamineAPI.getInstance(context);
        }
    }

    abstract public boolean isTriggered();

}
