package com.usedopamine.dopaminekit;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class DopeAction extends JSONObject{

    public String actionID;
    public @Nullable String reinforcementDecision = null;
    public @Nullable JSONObject metaData = null;
    public long utc;
    public long timezoneOffset;

    public DopeAction(String actionID, @Nullable String reinforcementDecision, @Nullable JSONObject metaData, long utc, long timezoneOffset){
        this.actionID = actionID;
        this.reinforcementDecision = reinforcementDecision;
        this.metaData = metaData;
        this.utc = utc;
        this.timezoneOffset = timezoneOffset;
    }

    public DopeAction(String actionID, @Nullable String reinforcementDecision, @Nullable  JSONObject metaData){
        this(actionID, reinforcementDecision , metaData, System.currentTimeMillis(), TimeZone.getDefault().getOffset(System.currentTimeMillis()));
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put("actionID", actionID);
            json.put("reinforcementDecision", reinforcementDecision);
            json.put("metaData", metaData);
            json.put("time", new JSONArray()
                    .put( new JSONObject().put("timeType", "utc").put("value", utc) )
                    .put( new JSONObject().put("timeType", "deviceTimezoneOffset").put("value", timezoneOffset) )
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
