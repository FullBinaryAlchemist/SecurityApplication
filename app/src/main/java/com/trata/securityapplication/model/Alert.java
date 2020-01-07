package com.trata.securityapplication.model;

import android.support.annotation.NonNull;

public class Alert {

    String location;
    String subzone;

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
}
