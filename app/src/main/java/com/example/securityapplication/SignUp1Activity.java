package com.example.securityapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securityapplication.model.User;

import java.util.Calendar;

/*
        *NOTE: Earlier TextInputLayout was used as parameter for all validation function
        *They have been changed to TextInputEditText
        * and the functions TextInputLayout.getEditText() have been replaced to just TextInputEditText.getText()
*/
public class SignUp1Activity extends AppCompatActivity {
//
   Database_Helper myDb;
    Validation val = new Validation();
    private TextView text_view;
    private Button Btn_Submit;
    private RadioGroup gender_grp;
    private RadioButton Radio_Gender;
    //Added user object to send to next
    private User user;
    //
DatePickerDialog datePickerDialog;
private TextInputEditText textinputName,textinputDOB,textinputEmail,textinputPass,textinputCnfPass; // was earlier TextInputLayout
    private TextInputEditText date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup1);
       myDb = new Database_Helper(this);
       java.util.Calendar calendar=Calendar.getInstance();
      final int year=calendar.get(Calendar.YEAR);

      //removed most the view castings as they're unnecessary

        text_view = findViewById(R.id.text_gender);
        textinputName = findViewById(R.id.textlayout_Name);
        textinputDOB = findViewById(R.id.textlayout_Dob);
        date=findViewById(R.id.textlayout_Dob);
        textinputEmail = findViewById(R.id.textlayout_Email);
        textinputPass =  findViewById(R.id.textlayout_Pass);
        textinputCnfPass = findViewById(R.id.textlayout_CnfPass);
        gender_grp = findViewById(R.id.radiogrp);
        Btn_Submit = findViewById(R.id.btn_sub);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c= java.util.Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(SignUp1Activity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                date.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        user=new User();

    }


    private void ShowMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
    }

    public void Validater(View view) {
        if (val.validateName(textinputName) & val.validateGender(gender_grp,text_view) & val.validateDob(textinputDOB) & val.validateEmail(textinputEmail) &
                val.validatePassword(textinputPass) & val.validateCnfPassword(textinputPass,textinputCnfPass)){
            AddData();
        }
        else {
            Toast.makeText(this,"Enter Valid Credentials",Toast.LENGTH_SHORT).show();
        }
    }

    private void AddData() {
        int selected_id = gender_grp.getCheckedRadioButtonId();
        Radio_Gender = (RadioButton) findViewById(selected_id);
        String gender = Radio_Gender.getText().toString().trim(); //function .getEditText() have been removed as TextInputEditText doesn't require it.
        //Sending the user object
        myDb.setUser(user);
        Boolean isInserted = myDb.insert_data(textinputName.getText().toString().trim(),
                gender,
                textinputDOB.getText().toString().trim(),
                textinputEmail.getText().toString().trim(),
                textinputPass.getText().toString().trim());
        if (isInserted) {
//            textinputName.setText(null);
//            gender_grp.clearCheck();
//            textinputDOB.setText(null);
//            textinputEmail.setText(null);
//            textinputPass.setText(null);
//            textinputCnfPass.setText(null);
            //updates the Usr object with filled fields
            user=myDb.getUser();
            //starting signup activity
            Intent intent=new Intent(SignUp1Activity.this,SignUp2.class);
            intent.putExtra("User",user);
            startActivityForResult(intent,1);

        }
        else {
            String UserEmail = textinputEmail.getText().toString().trim();
            boolean res = myDb.CheckUserEmail(UserEmail);
            if (res){
                Toast.makeText(this,"Email already taken",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"User Entry Unsuccessful",Toast.LENGTH_SHORT).show();
           }
        }
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==10 && requestCode==1)
            finish();

    }
}
