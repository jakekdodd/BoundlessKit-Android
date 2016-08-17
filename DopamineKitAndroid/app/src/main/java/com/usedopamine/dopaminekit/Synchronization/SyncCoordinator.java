package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.usedopamine.dopaminekit.DataStore.SQLTrackedActionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class SyncCoordinator {

    private static SyncCoordinator sharedInstance = new SyncCoordinator();

    private static Boolean syncInProgress = false;

    private SyncCoordinator() {}

    public static SyncCoordinator getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new SyncCoordinator();
        }
        return sharedInstance;
    }

    public static void sync(Context context) {
        synchronized (syncInProgress) {

            boolean trackerShouldSync = TrackSyncer.getInstance(context).shouldSync(context);
            if (trackerShouldSync) {
                SQLiteDatabase db = SQLiteDataStore.getInstance(context).getWritableDatabase();
                DopamineKit.debugLog("SyncCoordinator", "Sending " + SQLTrackedActionDataHelper.count(db) + " tracked actions...");
                TrackSyncer.getInstance(context).sync(context, new SyncerCallback() {
                    @Override
                    public void onSyncComplete(int statusCode) {
                        DopamineKit.debugLog("SyncCoordinator", "Test track sync got back " + statusCode);
                    }
                });
            }
        }
    }
}
