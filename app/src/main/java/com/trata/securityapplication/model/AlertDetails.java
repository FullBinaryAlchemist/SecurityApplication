package com.trata.securityapplication.model;

import android.util.Log;

//stores the details received from Cloud Message
public class AlertDetails extends Alert {
    protected String uid;
    protected String imageUrl;
    protected String name;

    private String TAG="AlertDetails";

    public AlertDetails() {
        super();
    }

    public void setUid(String uid) {
        this.uid = uid;
        Log.d(TAG,"Settign alertDetail uid:"+this.uid);
    }
    public String getUid() {
        return uid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        Log.d(TAG,"Settign alertDetail imageUrl:"+this.imageUrl);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        Log.d(TAG,"Settign alertDetail name:"+this.name);
    }
}
