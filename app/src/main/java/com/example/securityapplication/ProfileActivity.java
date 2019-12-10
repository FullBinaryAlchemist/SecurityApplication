package com.example.securityapplication;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securityapplication.model.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView textName,textEmail,textPhone,textAddress,textGender,textDob,textChangePass;
    private Button btn_edit;
    SQLiteDBHelper mydb ;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        user = new User();

        initObjects();
        initviews();
        FetchAllData();
        DisplayData();
        initListeners();

    }

    private void initObjects() {

//        user = getIntent().getParcelableExtra("User");
        mydb = new SQLiteDBHelper(this);
    }

    private void initListeners() {
        btn_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ProfileActivity.this,EditProfileActivity.class);
                    intent.putExtra("User",user);
                    startActivityForResult(intent,1);
                }//Sending Data to EditProfileActivity
            }
        );
        textChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Toast.makeText(ProfileActivity.this,"Code being Developed",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void FetchAllData(){
        Cursor res;
        res = mydb.getAllData();
        if (res.getCount() == 0){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No User Data Found",
                    Toast.LENGTH_LONG);
            toast.show();
            Log.d("Profile Activity","FetchAll: No Data found");
        }
//        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()){
            user.setName(res.getString(0));
            user.setEmail(res.getString(1));
            user.setGender(res.getString(2));
            user.setMobile(res.getString(3));
//            ansAadhaar = res.getString(6);
            user.setLocation(res.getString(4));
            user.setDob(res.getString(6));
            Log.d("Profile Activity","FetchAll: User Object set in Profile activity successfully");
        }
    }

    private void DisplayData() {

        textName.setText(user.getName());
//        textAadhaar.setText(ansAadhaar);
        textDob.setText(user.getDob());
        textGender.setText(user.getGender());
        textAddress.setText(user.getLocation());
        textEmail.setText(user.getEmail());
        textPhone.setText(user.getMobile());

        Log.d("Profile Activity","DisplayData : DATA displayed on profile Successfully");
    }

    private void initviews() {
        textName = findViewById(R.id.text_Name);
        textEmail = findViewById(R.id.text_Email);
        textPhone = findViewById(R.id.text_Phone);
        textAddress = findViewById(R.id.text_Address);
        textGender = findViewById(R.id.text_Gender);
        textDob = findViewById(R.id.text_DOB);
        btn_edit = findViewById(R.id.btn_Edit);
        textChangePass = findViewById(R.id.text_changepass);

//        textAadhaar = findViewById(R.id.text_Aadhaar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1){
            if (resultCode == 110){
                user = data.getParcelableExtra("ResultUser");
                Log.d("Profile Activity","ActivityResult: User object returned"+user.getEmail());
                mydb.updateUser(user);
                DisplayData();
            }
        }
    }
}
