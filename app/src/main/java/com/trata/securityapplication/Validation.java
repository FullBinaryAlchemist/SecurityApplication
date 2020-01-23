package com.trata.securityapplication;

//import android.support.design.widget.TextInputLayout;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
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


    private Pattern date_pattern,name_pattern,phone_pattern;
    private Matcher matcher;
    private static final String NAME_PATTERN ="^[A-Za-z\\s]{1,}[\\']{0,1}[A-Za-z\\s]{0,}$"; //modified regex to catch ".","-"
    private static final String DATE_PATTERN =
            "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";
    private static final String PHONE_PATTERN = "^[0-9]+$";
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
        phone_pattern = Pattern.compile(PHONE_PATTERN);
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
        } else if (nameinput.length() > 20) { //NOTE: name length changed to 20 from 40
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

    boolean validatePassword(TextInputEditText textInputPassword, TextView t1) {
        String passwordinput = textInputPassword.getText().toString().trim();

        if (passwordinput.isEmpty()) {
            SignUp1Activity.setError("Password can't be empty",t1);
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordinput).matches()) {
            SignUp1Activity.setError("Password does not fit the specified criteria",t1);
            snack(textInputPassword);

            return false;
        } else {
            SignUp1Activity.setError(null,t1);
            return true;
        }
    }
    boolean validateCnfPassword(TextInputEditText textInputPassword, TextInputEditText textInputCnfPassword,TextView t2){
        String passinput = textInputPassword.getText().toString().trim();
        String cnfpassinput = textInputCnfPassword.getText().toString().trim();

        if (cnfpassinput.isEmpty()) {
            SignUp1Activity.setError("Confirm Password required",t2);
            return false;
        }else if (!cnfpassinput.equals(passinput)){
            SignUp1Activity.setError("Passwords does not match",t2);
            return false;
        }else{
            SignUp1Activity.setError(null,t2);
            return true;
        }
    }

    //Validation Functions for EditProfileActivity
    boolean EditvalidateName(EditText edit_Name) {
        String Name=edit_Name.getText().toString().trim();
        matcher = name_pattern.matcher(Name);
        if (Name.isEmpty()) {
            edit_Name.setError("Field can't be empty");
            return false;
        } else if (Name.length() > 40) {
            edit_Name.setError("Name too long");
            return false;
        }else if(!matcher.find()){
            edit_Name.setError("Enter a Valid Name");
            return false;
        }
        else {
            edit_Name.setError(null);
            return true;
        }
    }

    Boolean EditvalidateEmail(EditText edit_email) {
        String emailInput = edit_email.getText().toString().trim();

        if (emailInput.isEmpty()) {
            edit_email.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            edit_email.setError("Please enter a valid email address");
            return false;
        } else {
            edit_email.setError(null);
            return true;
        }
    }

    Boolean EditvalidatePhone(EditText edit_Phone) {
        String phoneInput = edit_Phone.getText().toString().trim();
        matcher = phone_pattern.matcher(phoneInput);
        if (phoneInput.isEmpty()){
            edit_Phone.setError("Please Enter a valid Mobile Number");
            return false;
        }
        else if (!matcher.find()){
            edit_Phone.setError("Please Enter a valid Mobile Number");
            return false;
        }
        else if (phoneInput.length()!=10){
            edit_Phone.setError("Please Enter a valid Mobile Number");
            return false;
        }
        else {
            edit_Phone.setError(null);
            return true;
        }

    }

    Boolean EditvalidateAddress(EditText edit_address) {
        String addressInput = edit_address.getText().toString().trim();
        matcher = name_pattern.matcher(addressInput);

        if (addressInput.isEmpty()){
            edit_address.setError("Please enter an Address");
            return false;
        }
        else if(!matcher.find()){
            edit_address.setError("Please Enter a valid Address");
            return false;
        }
        else{
            edit_address.setError(null);
            return true;
        }
    }
    public void snack(View view) {
        Snackbar snackBar = Snackbar.make(view, "Password must contain atleast 1 uppercase, 1 lowercase, 1 special and 1 numeric character and length must be between 6 to 16 characters.", Snackbar.LENGTH_INDEFINITE) .setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        snackBar.setActionTextColor(Color.RED);
        View snackBarView = snackBar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }
}
