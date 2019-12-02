package com.example.securityapplication;


import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Patterns;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidation {
    private Context context;
    public InputValidation(Context context) {
        this.context = context;
    }


    public boolean is_Empty(TextInputEditText textInputEditText, String message){
        if(textInputEditText.getText().toString().trim().isEmpty()) {
            textInputEditText.setError(message);
            return true;
        }
        else
            return false;
    }


    public boolean all_Empty(TextInputEditText textInputEditText1, TextInputEditText textInputEditText2,
                             TextInputEditText textInputEditText3,String message){
        boolean i1 = textInputEditText1.getText().toString().trim().isEmpty();
        boolean i2 = textInputEditText2.getText().toString().trim().isEmpty();
        boolean i3 = textInputEditText3.getText().toString().trim().isEmpty();
        if(i1)
            textInputEditText1.setError(message);
        if(i2)
            textInputEditText2.setError(message);
        if(i3)
            textInputEditText3.setError(message);
        if(i1 || i2 || i3){
            return true;
        }
        else {
            return false;
        }
    }
    //return true if input is equal to minLength
    public boolean isMinLength(TextInputEditText textInputEditText, int minlength, String message){
        int value = textInputEditText.getText().toString().length();
        if(value != minlength) {
            //textInputEditText.setError(message);
            return false;
        }
        else {
            textInputEditText.setError(null);
            return true;
        }
    }

    //returns true if input is NUMERIC
    public boolean is_numeric(TextInputEditText textInputEditText){

        String value =  textInputEditText.getText().toString().trim();
        String message = "INVALID";
        String regex = "^[0-9]+$";
        Matcher matcher = Pattern.compile( regex ).matcher(value);
        if ( matcher.find()){
            textInputEditText.setError(null);
            return true;
        }
        else{
            textInputEditText.setError(message);
            return false;
        }

    }
}
