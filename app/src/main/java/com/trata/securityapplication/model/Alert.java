package com.trata.securityapplication.model;

import android.support.annotation.NonNull;

public class Alert {

    String location;
    String subzone;
    String ts;//timestamp
    public Alert() {
    }

    @NonNull
    @Override
    public String toString() {
        return "Alert object- subzone:"+getSubzone()+" location:"+getLocation();

    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubzone() {
        return subzone;
    }

    public void setSubzone(String subzone) {
        this.subzone = subzone;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
}
