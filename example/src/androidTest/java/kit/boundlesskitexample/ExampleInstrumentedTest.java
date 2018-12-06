package kit.boundlesskitexample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import kit.boundless.BoundlessKit;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    public static Context appContext;

    @Before
    public void useAppContext() throws Exception {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getTargetContext();
        BoundlessKit.debugMode = true;

        assertEquals("boundless.boundlesskitexample", appContext.getPackageName());
    }

    @Test
    public void useBoundlessKitTrack() throws Exception {
        BoundlessKit.track(appContext, "jUnitTestTrack", null);
    }

    @Test
    public void testMultipleQuickBoundlessKitTracks() throws Exception {
        for(int i = 0; i < 5; i++) {
            BoundlessKit.track(appContext, "multipleTrackCallTest", null);
        }
    }

    @Test
    public void testBoundlessKitReinforce() throws Exception {
        BoundlessKit.reinforce(appContext, "taskCompleted", null, new BoundlessKit.ReinforcementCallback() {
            @Override
            public void onReinforcement(String reinforcement) {
                Log.v("InstrumentationTest", "BoundlessKit Reinforce ended up with ("+reinforcement+")");
            }
        });
    }

    @Test
    public void testMultipleQuickBoundlessKitReinforces() throws Exception {
        JSONObject metaData;
        for(int i = 0; i < 10; i++) {
            metaData = new JSONObject().put("unitTest", "testMultipleQuickBoundlessKitReinforces").put( "index", i);
            BoundlessKit.reinforce(appContext, "action1", metaData, new BoundlessKit.ReinforcementCallback() {
                @Override
                public void onReinforcement(String reinforcement) {
                    Log.v("InstrumentationTest", "BoundlessKit Reinforce ended up with ("+reinforcement+")");
                }
            });
        }
    }

    @Test
    public void testMultipleSlowBoundlessKitReinforces() throws Exception {
        long timeBetweenReinforces = 3000;

        JSONObject metaData;
        for(int i = 0; i < 5; i++) {
            metaData = new JSONObject().put("unitTest", "testMultipleSlowBoundlessKitReinforces").put( "index", i).put("timeBetweenReinforces", timeBetweenReinforces);
            BoundlessKit.reinforce(appContext, "action1", metaData, new BoundlessKit.ReinforcementCallback() {
                @Override
                public void onReinforcement(String reinforcement) {
                    Log.v("InstrumentationTest", "BoundlessKit Reinforce ended up with ("+reinforcement+")");
                }
            });
            Log.v("InstrumentationTest", "Sleeping for " + timeBetweenReinforces + "ms...");
            Thread.sleep(timeBetweenReinforces);
        }
    }

    @Test
    public void testBoundlessKitMapUserId() throws Exception {
        BoundlessKit.mapExternalUserId(appContext, "testMappedId");
    }

    @After
    public void waitForSyncersToComplete() throws Exception {
        Thread.sleep(10000);
    }
}