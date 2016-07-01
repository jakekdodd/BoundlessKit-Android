package com.usedopamine.dopaminekitandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.usedopamine.dopaminekit.*;

public class MainActivity extends AppCompatActivity implements DopamineKit.ReinforcementCallback {
    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DopamineKit.enableDebugMode(true);
        DopamineKit.reinforce(getBaseContext(), "action1", this);

    }

    @Override
    public void onReinforcement(String reinforement) {
        Log.v(TAG, "Response: " + reinforement);
    }
}
