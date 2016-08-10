package com.usedopamine.dopaminekit;

import java.util.HashMap;

/**
 * Created by cuddergambino on 7/17/16.
 */

class DecisionEngine {


    private static DecisionEngine instance = new DecisionEngine();
    private DecisionEngine() {
        reinforcementDecisions = new HashMap<String, Cartridge>();
    }
    static DecisionEngine getInstance(){ return instance; }

    HashMap<String, Cartridge> reinforcementDecisions;
    static void reinforceEvent(DopeAction event){
        Cartridge eventCartridge = instance.reinforcementDecisions.get(event.actionID);
        if(eventCartridge==null){
            DopamineKit.debugLog("DecisionEngine", "No cartridge found for " + event.actionID);
            return;
        }

        String reinforcementDecision = "neutralFeedback";
        if(!eventCartridge.isEmpty()){
            reinforcementDecision = eventCartridge.pop().reinforcementDecision;
        }
    }

}
