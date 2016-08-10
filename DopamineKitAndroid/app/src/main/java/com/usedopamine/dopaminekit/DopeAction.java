package com.usedopamine.dopaminekit;

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
    public String reinforcementDecision;
    public HashMap<String, String> metaData;
    public long utc;

    public DopeAction(String actionID, String reinforcementDecision, HashMap<String, String> metaData, long utc){
        this.actionID = actionID;
        this.reinforcementDecision = reinforcementDecision;
        this.metaData = metaData;
        this.utc = utc;
    }

    public DopeAction(String actionID, HashMap<String, String> metaData){
        this(actionID, null , metaData, System.currentTimeMillis());
    }

    public DopeAction(String actionID){
        this(actionID, null);
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            JSONObject utcObject = new JSONObject().put("timeType", "utc").put("value", utc);
            JSONArray timeArray = new JSONArray().put(utcObject);
            json.put("time", timeArray);
            json.put("actionID", actionID);
            json.put("reinforcement", reinforcementDecision);
            json.put("metaData", new JSONObject(metaData));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
