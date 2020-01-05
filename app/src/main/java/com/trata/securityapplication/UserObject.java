package com.trata.securityapplication;

import com.trata.securityapplication.model.User;

public class UserObject {
    public static User user=new User();
    public static String print(){
        String s=user.getDob()+" "+user.isPaid()+" "+user.getName()+" "+user.getGender();
        return s;
    }
}
