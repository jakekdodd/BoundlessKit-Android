package com.usedopamine.dopaminekit;

import org.json.JSONArray;

import java.util.Vector;

/**
 * Created by cuddergambino on 7/17/16.
 */

class Cartridge {
    Vector<DopeAction> events;

    int end = -1;
    int max;


    public Cartridge(){
        max = 100;
        events = new Vector<DopeAction>(max);
    }

    public Cartridge(int size){
        max = size;
        events = new Vector<DopeAction>(size);
    }

    DopeAction pop(){
        if(end >= 0){
            return events.elementAt(end--);
        } else{
            DopamineKit.debugLog("Cartridge", "Trying to pop empty cartridge");
            return null;
        }
    }

    void push(DopeAction event){
        events.set(++end, event);
    }

    boolean isEmpty(){
        return end > -1;
    }

    JSONArray toJSON(){
        JSONArray json = new JSONArray();

        for(int i = 0; i < events.size(); i++){
            json.put(events.elementAt(i).toJSON());
        }

        return json;
    }

}
