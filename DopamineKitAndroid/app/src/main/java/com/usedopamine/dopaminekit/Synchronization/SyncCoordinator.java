package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.usedopamine.dopaminekit.DataStore.SQLReportedActionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLTrackedActionDataHelper;
import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;
import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class SyncCoordinator {

    private static SyncCoordinator sharedInstance;

    private ExecutorService apiSyncThreadpool = Executors.newFixedThreadPool(3);
    private TrackSyncer trackSyncer;
//    private ReportSyncer reportSyncer;

    protected SQLiteDatabase sqlDB;
    protected DopamineAPI dopamineAPI;

    private Boolean syncInProgress = false;

    private SyncCoordinator(Context context) {
//        dataStore = SQLiteDataStore.getInstance(context);
        trackSyncer = TrackSyncer.getInstance(context);
//        reportSyncer = ReportSyncer.getInstance(context);

        sqlDB = SQLiteDataStore.getInstance(context).getWritableDatabase();
        dopamineAPI = DopamineAPI.getInstance(context);

    }

    public static SyncCoordinator getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new SyncCoordinator(context);
        }

        return sharedInstance;
    }

    public void storeTrackedAction(DopeAction action) {
        trackSyncer.store(action);
        sync();
    }

//    public void storeReportedAction(Context context, DopeAction action) {
//        reportSyncer.store(context, action);
//        sync(context);
//    }

    public void sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (syncInProgress) {
                    DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
                } else {
                    synchronized (syncInProgress) {
                        if (syncInProgress) {
                            DopamineKit.debugLog("SyncCoordinator", "Coordinated sync process already happening");
                        } else {
                            try {
                                syncInProgress = true;

                                boolean trackerShouldSync = trackSyncer.isTriggered();

                                if (trackerShouldSync) {

                                    Future<JSONObject> futureTrack = apiSyncThreadpool.submit(trackSyncer); // apiSyncThreadpool.schedule(trackSyncer, 1000, TimeUnit.MILLISECONDS);
                                    apiSyncThreadpool.submit(trackSyncer);
                                    while (!futureTrack.isDone()) {
                                        Log.v("SyncCoordinator", "Waiting for track syncer to be done...");
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Log.v("SyncCoordinator", "Track Syncer is done!");


                                    JSONObject response = futureTrack.get();
                                    if (response == null) {
                                        Log.v("TrackSyncResponse", "null");
                                    } else {
                                        Log.v("TrackSyncResponse", response.toString());
                                    }

                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } finally {
                                syncInProgress = false;
                            }
                        }

                    }

                }
                return null;
            }
        }.execute();
    }

//        synchronized (syncInProgress) {
//
////            boolean reporterShouldSync = reportSyncer.shouldSync(context);
//            boolean reporterShouldSync = false;
//            final boolean trackerShouldSync = reporterShouldSync || trackSyncer.isTriggered(context);
//
//            AsyncTask<Void, Void, Void> trackSyncTask = new AsyncTask<Void, Void, Void>() {
//                @Override
//                protected Void doInBackground(Void... voids) {
//
//                    if (trackerShouldSync) {
//                        SQLiteDatabase db = SQLiteDataStore.getInstance(context).getWritableDatabase();
//                        DopamineKit.debugLog("SyncCoordinator", "Sending " + SQLTrackedActionDataHelper.count(db) + " tracked actions...");
//                        trackSyncer.sync(context, new Syncer.SyncCallback() {
//                            @Override
//                            public void onSyncComplete(int statusCode) {
//                                DopamineKit.debugLog("SyncCoordinator", "Test track sync got back " + statusCode);
//                            }
//                        });
//                    }
//
//                    return null;
//                }
//
//                @Override
//                protected void onPostExecute(Void v) {
//                    syncInProgress = false;
//                    return;
//                }
//            }.execute();
//
////            AsyncTask<Boolean, Void, Boolean> reportSyncTask = new AsyncTask<Boolean, Void, Boolean>() {
////                @Override
////                protected Boolean doInBackground(Boolean... booleen) {
////
////                    boolean reportShouldSync = booleen[0];
////
////                    if (reportShouldSync) {
////                        SQLiteDatabase db = SQLiteDataStore.getInstance(context).getWritableDatabase();
////                        DopamineKit.debugLog("SyncCoordinator", "Sending " + SQLReportedActionDataHelper.count(db) + " reported actions...");
////                        reportSyncer.sync(context, new Syncer.SyncCallback() {
////                            @Override
////                            public void onSyncComplete(int statusCode) {
////                                DopamineKit.debugLog("SyncCoordinator", "Test report sync got back " + statusCode);
////                            }
////                        });
////                    }
////
////                    return reportShouldSync;
////                }
////
////                @Override
////                protected void onPostExecute(Boolean reportDidSync) {
////
////
////                }
////            }.execute(reportSyncer.shouldSync(context));
//
//        }
}
