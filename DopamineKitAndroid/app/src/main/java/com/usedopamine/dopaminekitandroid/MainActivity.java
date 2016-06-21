package com.usedopamine.dopaminekitandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.usedopamine.dopaminekit.*;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String response = DopamineKit.reinforce("action1", getBaseContext());
        Log.v(TAG, "Response: " + response);
    }
}
