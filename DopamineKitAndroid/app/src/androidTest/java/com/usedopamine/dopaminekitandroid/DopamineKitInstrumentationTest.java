package com.usedopamine.dopaminekitandroid;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.usedopamine.dopaminekit.DopamineKit;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class DopamineKitInstrumentationTest {

    public static Context appContext;

    @Before
    public void useAppContext() throws Exception {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getTargetContext();
        DopamineKit.getInstance(appContext).enableDebugMode(true);

        assertEquals("com.usedopamine.dopaminekitandroid", appContext.getPackageName());
    }

    @Test
    public void useDopamineKitTrack() throws Exception {
        DopamineKit.track(appContext, "jUnitTestTrack", null);
    }

    @Test
    public void testMultipleQuickDopamineKitTracks() throws Exception {
        for(int i = 0; i < 5; i++) {
            DopamineKit.track(appContext, "multipleTrackCallTest", null);
        }
    }

    @Test
    public void testDopamineKitReinforce() throws Exception {
        DopamineKit.reinforce(appContext, "taskCompleted", null, new DopamineKit.ReinforcementCallback() {
            @Override
            public void onReinforcement(String reinforcement) {
                Log.v("DKInstrumentationTest", "Dopaminekit Reinforce ended up with ("+reinforcement+")");
            }
        });
    }

    @Test
    public void testMultipleQuickDopamineKitReinforces() throws Exception {
        JSONObject metaData;
        for(int i = 0; i < 10; i++) {
            metaData = new JSONObject().put("unitTest", "testMultipleQuickDopamineKitReinforces").put( "index", i);
            DopamineKit.reinforce(appContext, "action1", metaData, new DopamineKit.ReinforcementCallback() {
                @Override
                public void onReinforcement(String reinforcement) {
                    Log.v("DKInstrumentationTest", "Dopaminekit Reinforce ended up with ("+reinforcement+")");
                }
            });
        }
    }

    @Test
    public void testMultipleSlowDopamineKitReinforces() throws Exception {
        long timeBetweenReinforces = 3000;

        JSONObject metaData;
        for(int i = 0; i < 5; i++) {
            metaData = new JSONObject().put("unitTest", "testMultipleSlowDopamineKitReinforces").put( "index", i).put("timeBetweenReinforces", timeBetweenReinforces);
            DopamineKit.reinforce(appContext, "action1", metaData, new DopamineKit.ReinforcementCallback() {
                @Override
                public void onReinforcement(String reinforcement) {
                    Log.v("DKInstrumentationTest", "Dopaminekit Reinforce ended up with ("+reinforcement+")");
                }
            });
            Log.v("DKInstrumentationTest", "Sleeping for " + timeBetweenReinforces + "ms...");
            Thread.sleep(timeBetweenReinforces);
        }
    }

    @After
    public void waitForSyncersToComplete() throws Exception {
        Thread.sleep(10000);
    }
}