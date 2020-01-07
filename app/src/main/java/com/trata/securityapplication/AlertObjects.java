package com.trata.securityapplication;

import android.util.Log;

import com.trata.securityapplication.model.AlertDetails;

import java.util.HashMap;

public class AlertObjects {
    private static HashMap<String, AlertDetails> alerts= new HashMap<String, AlertDetails>();

    //adds a AlertDetail with key as the uid of the victim
    public static void setAlertDetail(String uid,AlertDetails alert){
        if(!alerts.containsKey(uid))
            Log.d("AlertObjects","Added an alertDetail object with uid:"+uid);
        else
            Log.d("AlertObjects","Updated alertDetail object with uid:"+uid);
        
        alerts.put(uid,alert);

    }

    //returns the AlertDetail with provided key
    public static AlertDetails getAlert(String uid){
        return alerts.get(uid);
    }

    public static HashMap<String,AlertDetails> getAllAlerts(){
        return alerts;
    }
}
