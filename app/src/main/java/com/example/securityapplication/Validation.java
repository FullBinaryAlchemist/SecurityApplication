package com.example.securityapplication;

//import android.support.design.widget.TextInputLayout;
import android.graphics.Color;
import android.support.design.widget.TextInputEditText;
import android.util.Patterns;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
*NOTE: Earlier TextInputLayout was used as parameter for all validation function
*They have been changed to TextInputEditText
* and the functions TextInputLayout.getEditText() have been replaced to just TextInputEditText.getText()
 */
public class Validation {


    private Pattern date_pattern,name_pattern;
    private Matcher matcher;
    private static final String NAME_PATTERN ="^[\\p{L} .'-]+$";
    private static final String DATE_PATTERN =
            "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,16}" +               //at least 6 characters maximum 16
                    "$");

    public Validation(){
        date_pattern = Pattern.compile(DATE_PATTERN);
        name_pattern = Pattern.compile(NAME_PATTERN,Pattern.CASE_INSENSITIVE);
    }

    boolean validateEmail(TextInputEditText textInputEmail) {
        String emailInput = textInputEmail.getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            textInputEmail.setError("Please enter a valid email address");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    boolean validateGender(RadioGroup radioGroup_gender, TextView GenderVal){
        if (radioGroup_gender.getCheckedRadioButtonId() == -1)
        {
            GenderVal.setError("Select your Gender");
            return false;
        }
        else
        {
            // one of the radio buttons is checked
            GenderVal.setError(null);
            return true;
        }
    }
    boolean validateDob(TextInputEditText inputdob) {

        String dobinput = inputdob.getText().toString().trim();

        matcher = date_pattern.matcher(dobinput);

        if(matcher.matches()){

            matcher.reset();

            if(matcher.find()){

                String day = matcher.group(1);
                String month = matcher.group(2);
                int year = Integer.parseInt(matcher.group(3));

                if (day.equals("31") &&
                        (month.equals("4") || month .equals("6") || month.equals("9") ||
                                month.equals("11") || month.equals("04") || month .equals("06") ||
                                month.equals("09"))) {
                    inputdob.setError(null);
                    inputdob.setError("Invalid Date");
                    return false; // only 1,3,5,7,8,10,12 has 31 days
                } else if (month.equals("2") || month.equals("02")) {
                    //leap year
                    if(year % 4==0){
                        if(day.equals("30") || day.equals("31")){
                            inputdob.setError(null);
                            inputdob.setError("Invalid Date");
                            return false;
                        }else{
                            inputdob.setError(null);
                            return true;
                        }
                    }else{
                        if(day.equals("29")||day.equals("30")||day.equals("31")){
                            inputdob.setError(null);
                            inputdob.setError("Invalid Date");
                            return false;
                        }else{
                            inputdob.setError(null);
                            return true;
                        }
                    }
                }else{
                    inputdob.setError(null);
                    return true;
                }
            }else{
                inputdob.setError(null);
                inputdob.setError("Invalid Date");
                return false;
            }
        }else if(dobinput.isEmpty()){
            inputdob.setError("Field can't be empty");
            return false;
        }
        else{
            inputdob.setError(null);
            inputdob.setError("Invalid Date");
            return false;
        }

    }

    boolean validateName(TextInputEditText textInputName) {
        String nameinput = textInputName.getText().toString().trim();
        matcher = name_pattern.matcher(nameinput);
        if (nameinput.isEmpty()) {
            textInputName.setError("Field can't be empty");
            return false;
        } else if (nameinput.length() > 40) {
            textInputName.setError("Name too long");
            return false;
        }else if(!matcher.find()){
            textInputName.setError("Enter a Valid Name");
            return false;
        }
        else {
            textInputName.setError(null);
            return true;
        }
    }

    boolean validatePassword(TextInputEditText textInputPassword) {
        String passwordinput = textInputPassword.getText().toString().trim();

        if (passwordinput.isEmpty()) {
            textInputPassword.setError("Password can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordinput).matches()) {
            textInputPassword.setError("Password does not fit the specified criteria");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }
    boolean validateCnfPassword(TextInputEditText textInputPassword,TextInputEditText textInputCnfPassword){
        String passinput = textInputPassword.getText().toString().trim();
        String cnfpassinput = textInputCnfPassword.getText().toString().trim();

        if (cnfpassinput.isEmpty()) {
            textInputCnfPassword.setError("Confirm Password required");
            return false;
        }else if (!cnfpassinput.equals(passinput)){
            textInputCnfPassword.setError("Passwords does not match");
            return false;
        }else{
            textInputCnfPassword.setError(null);
            return true;
        }
    }
}
