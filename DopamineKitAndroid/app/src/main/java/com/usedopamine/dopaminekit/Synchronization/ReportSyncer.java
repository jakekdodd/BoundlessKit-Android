//package com.usedopamine.dopaminekit.Synchronization;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//
//import com.usedopamine.dopaminekit.DataStore.Contracts.ReportedActionContract;
//import com.usedopamine.dopaminekit.DataStore.SQLReportedActionDataHelper;
//import com.usedopamine.dopaminekit.DataStore.SQLiteDataStore;
//import com.usedopamine.dopaminekit.DopamineKit;
//import com.usedopamine.dopaminekit.DopeAction;
//import com.usedopamine.dopaminekit.RESTfulAPI.DopamineAPI;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//
///**
// * Created by cuddergambino on 8/17/16.
// */
//
//public class ReportSyncer extends Syncer {
//
//    private static ReportSyncer sharedInstance;
//
//    private static final String preferencesName = "DopamineReportSyncer";
//    private final Object storelock = new Object();
//
//    protected ReportSyncer(Context context) {
//        super(context);
//    }
//
//    public static ReportSyncer getInstance(Context context) {
//        if (sharedInstance == null) {
//            sharedInstance = new ReportSyncer(context);
//        }
//        return sharedInstance;
//    }
//
//    public void store(Context context, DopeAction action) {
//        synchronized (storelock) {
//            SQLiteDatabase db = SQLiteDataStore.getInstance(context).getWritableDatabase();
//            String metaData = (action.metaData==null) ? null : action.metaData.toString();
//            long rowId = SQLReportedActionDataHelper.insert(db, new ReportedActionContract(
//                    0, action.actionID, action.reinforcementDecision, metaData, action.utc, action.timezoneOffset
//            ));
//            DopamineKit.debugLog("SQL Reported Actions", "Inserted into row " + rowId);
//        }
//    }
//
//    protected boolean shouldSync(Context context) {
//        int actionsCount = SQLReportedActionDataHelper.count(SQLiteDataStore.getInstance(context).getReadableDatabase());
//        DopamineKit.debugLog("ReportSyncer", "Report has " + actionsCount + "/" + suggestedSize + " actions" );
//        return actionsCount >= suggestedSize || timerTriggered();
//    }
//
//    @Override
//    public boolean triggered() {
//        boolean triggered = System.currentTimeMillis() >= (timerMarker + timerLength);
//        if (triggered) DopamineKit.debugLog("Syncer", "Timer has been triggered at " + timerMarker + timerLength + " and it is " + System.currentTimeMillis() + " now.");
//        return  triggered;
//    }
//
//    public void sync(final Context context, final SyncCallback callback) {
//        if (syncInProgress) {
//            DopamineKit.debugLog("ReportSyncer", "Report sync already happening");
//            if (callback!=null) callback.onSyncComplete(200);
//            return;
//        } else {
//            syncInProgress = true;
//        }
//
//        synchronized (syncInProgress) {
//
//            final SQLiteDatabase db = SQLiteDataStore.getInstance(context).getWritableDatabase();
//            final ArrayList<ReportedActionContract> sqlActions = SQLReportedActionDataHelper.findAll(db);
//            if (sqlActions.size() == 0) {
//                DopamineKit.debugLog("ReportSyncer", "No reported actions to be synced.");
//                if (callback != null) callback.onSyncComplete(200);
//                return;
//            }
//
//            DopeAction dopeActions[] = new DopeAction[sqlActions.size()];
//            for (int i = 0; i < sqlActions.size(); i++) {
//                ReportedActionContract action = sqlActions.get(i);
//                try {
//                    dopeActions[i] = new DopeAction(action.actionID, action.reinforcementDecision, new JSONObject(action.metaData), action.utc, action.timezoneOffset);
//                } catch (JSONException e) {
//                    dopeActions[i] = new DopeAction(action.actionID, action.reinforcementDecision, null, action.utc, action.timezoneOffset);
//                } catch (NullPointerException e) {
//                    dopeActions[i] = new DopeAction(action.actionID, action.reinforcementDecision, null, action.utc, action.timezoneOffset);
//                }
//            }
//
//            DopamineAPI.report(context, dopeActions, new DopamineAPIRequestCallback() {
//                @Override
//                public void onDopamineAPIRequestPostExecute(JSONObject response) {
//                    try {
//                        int statusCode = response.optInt("status", 404);
//                        if (statusCode == 200) {
//                            for (int i = 0; i < sqlActions.size(); i++) {
//                                SQLReportedActionDataHelper.delete(db, sqlActions.get(i));
//                            }
//                            DopamineKit.debugLog("ReportSyncer", "Synced and deleted all reported actions!");
//                            updateTriggers(context, null, null, null);
//                        } else {
//                            DopamineKit.debugLog("ReportSyncer", "Something went wrong while syncing... Leaving reported actions in sqlite db");
//                        }
//                        if (callback != null) callback.onSyncComplete(statusCode);
//                    } finally {
//                        syncInProgress = false;
//                    }
//                }
//            });
//        }
//    }
//
//}
