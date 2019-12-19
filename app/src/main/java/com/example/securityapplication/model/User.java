
package com.example.securityapplication.model;


import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String name;
    private String email;
    //private String password;
    private String gender;
    private String mobile;
    private String location;
    private String imei;
    private String dob;
    private Boolean paid=false;
    private HashMap<String,String> sosContacts;
    private Boolean googleAccountLinked;

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }


    public User(){}

    protected User(Parcel in) {
        //id = in.readInt();
        name = in.readString();
        email = in.readString();
        //password = in.readString();
        gender = in.readString();
        mobile = in.readString();
//        aadhar = in.readString();
        location = in.readString();
        imei = in.readString();
        dob = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    /*public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }*/

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(email);
        //dest.writeString(password);
        dest.writeString(gender);
        dest.writeString(mobile);
//        dest.writeString(aadhar);
        dest.writeString(location);
        dest.writeString(imei);
        dest.writeString(dob);
    }

    public boolean isPaid(){ return paid; }

    public void setPaid(Boolean paid){ this.paid = paid; }

    public HashMap<String,String> getSosContacts(){ return sosContacts; }

    public void setSosContacts(HashMap<String,String> sosContacts){
        this.sosContacts = sosContacts;
    }

    public boolean isGoogleAccountLinked(){ return googleAccountLinked; }

    public void setGoogleAccountLinked(Boolean googleAccountLinked){ this.googleAccountLinked = googleAccountLinked; }
}