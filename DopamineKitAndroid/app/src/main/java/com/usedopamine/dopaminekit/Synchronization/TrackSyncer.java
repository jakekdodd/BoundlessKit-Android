package com.usedopamine.dopaminekit.Synchronization;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.usedopamine.dopaminekit.DataStore.Contracts.TrackedActionContract;
import com.usedopamine.dopaminekit.DataStore.SQLTrackedActionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;

import org.json.JSONObject;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class TrackSyncer extends AsyncTask<Void, Void, Integer> {

    private static TrackSyncer sharedInstance;

    private static final String preferencesName = "DopamineTrackSyncer";
    private static final String preferencesSuggestedSize = "suggestedSize";
    private static final String preferencesTimerMarker = "timerMarker";
    private static final String preferencesTimerLength = "timerLength";
    private int suggestedSize;
    private long timerMarker;
    private long timerLength;

    private static boolean syncInProgress = false;
    private static final Object synclock = new Object();
    private static final Object storelock = new Object();

    private TrackSyncer(Context context) {
        SharedPreferences settings = context.getSharedPreferences(preferencesName, 0);
        suggestedSize = settings.getInt(preferencesSuggestedSize, 15);
        timerMarker = settings.getLong(preferencesTimerMarker, 0);
        timerLength = settings.getLong(preferencesTimerLength, 48 * 3600000);
    }

    public static TrackSyncer getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new TrackSyncer(context);
        }
        return sharedInstance;
    }

    public void store(Context context, DopeAction action) {
        synchronized (storelock) {
            SQLiteDataStore ds = new SQLiteDataStore(context);
            SQLiteDatabase db = ds.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TrackedActionContract.TrackedActionEntry.COLUMNS_NAME_ACTION_ID, action.actionID);
            if (action.metaData != null) {
                values.put(TrackedActionContract.TrackedActionEntry.COLUMNS_NAME_METADATA, new JSONObject(action.metaData).toString());
            }
            values.put(TrackedActionContract.TrackedActionEntry.COLUMNS_NAME_UTC, action.utc);

            long rowId = db.insert(TrackedActionContract.TrackedActionEntry.TABLE_NAME, null, values);
            DopamineKit.debugLog("SQL Tracked Actions", "Inserted into " + rowId);
        }
    }

    public TrackSyncer updateSuggestedSize(Context context, int size) {
        SharedPreferences settings = context.getSharedPreferences(preferencesName, 0);
        suggestedSize = size;
        settings.edit().putInt(preferencesSuggestedSize, size).commit();
        return this;
    }

    public TrackSyncer updateTimer(Context context, long timerLength) {
        SharedPreferences settings = context.getSharedPreferences(preferencesName, 0);
        long currentTime = System.currentTimeMillis();
        timerMarker = currentTime;
        if (timerLength > 0) {
            this.timerLength = timerLength;
        }
        settings.edit().putLong(preferencesTimerMarker, this.timerMarker)
        .putLong(preferencesTimerLength, this.timerLength)
        .commit();
        return this;
    }

    protected boolean shouldSync(Context context) {
        long currentTime = System.currentTimeMillis();
        int actionsCount = SQLTrackedActionDataHelper.count(context);
        if (actionsCount >= suggestedSize) {
            DopamineKit.debugLog("TrackSyncer", "Track has " + actionsCount + " actions and should only have " + suggestedSize);
        } else if ((timerMarker + timerLength) < currentTime) {
            DopamineKit.debugLog("TrackSyncer", "Track has expired at " + (timerMarker + timerLength) + " and it is " + currentTime + " now.");
        } else {
            DopamineKit.debugLog("TrackSyncer", "Track has " + actionsCount + "/" + suggestedSize + " actions and last synced " + timerMarker + " with a timer set " + timerLength + "ms from now so does not need sync...");
        }


        return actionsCount >= suggestedSize ||
                (timerMarker + timerLength) < currentTime;
    }

//    private
    protected Integer doInBackground(Void... params) {
        synchronized (synclock) {
            if (syncInProgress) {
                return 200;
            } else {
                syncInProgress = true;
            }
        }



        int count = urls.length;
        long totalSize = 0;
        for (int i = 0; i < count; i++) {
            totalSize += Downloader.downloadFile(urls[i]);
            publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }
        return totalSize;
    }

    protected void onProgressUpdate(Integer... progress) {
        setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Integer result) {
        showDialog("Downloaded " + result + " bytes");
    }
}
