package com.usedopamine.dopaminekit.Synchronization;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

/**
 * Created by cuddergambino on 8/17/16.
 */

public abstract class Syncer {
    private static Syncer sharedInstance;

    private static String preferencesName;
    private static final String preferencesSuggestedSize = "suggestedSize";
    private static final String preferencesTimerMarker = "timerMarker";
    private static final String preferencesTimerLength = "timerLength";

    private int suggestedSize;
    private long timerMarker;
    private long timerLength;

    private Boolean syncInProgress = false;
    private final Object storelock = new Object();

    protected Syncer(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, 0);
        suggestedSize = preferences.getInt(preferencesSuggestedSize, 15);
        timerMarker = preferences.getLong(preferencesTimerMarker, 0);
        timerLength = preferences.getLong(preferencesTimerLength, 48 * 3600000);
    }

    public void updateTriggers(Context context, @Nullable Integer size, @Nullable Long startTime, @Nullable Long length) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, 0);

        if (size != null) { suggestedSize = size; }
        if (startTime != null) { timerMarker = startTime; }
        if (length != null) { timerLength = length; }
        else { timerLength = System.currentTimeMillis(); }

        preferences.edit()
                .putInt(preferencesSuggestedSize, suggestedSize)
                .putLong(preferencesTimerMarker, timerMarker)
                .putLong(preferencesTimerLength, timerLength)
                .apply();
    }

    protected boolean timerTriggered() {
        return (timerMarker + timerLength) < System.currentTimeMillis();
    }

}
