package com.example.securityapplication;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.securityapplication.model.Device;
import com.example.securityapplication.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;




public class SignUp2 extends AppCompatActivity {
    private final AppCompatActivity activity = SignUp2.this;


    private TextInputEditText input_mobile;
//    private TextInputEditText input_aadhar;
    private AutoCompleteTextView input_location;
//    private TextView error_message;


    private Button btn_submit;
    private Intent ReturnIntent;
    private InputValidation inputValidation;
    private Validation validation = new Validation();
    private SQLiteDBHelper DBHelper;
    private User user;
    private Device device;
    private String blockcharset = "~#^|$%&*!,.";
    //adding requestCode variable for requestPermission
    private int RC;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDevicesDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mEmailDatabaseReference;
    private DatabaseReference mMobileDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleFirebaseSignIn googleFirebaseSignIn;

    private String uid;

    private String TAG = "SignUp2";
    private String imei;

    private TextView text_view;
    private RadioGroup gender_grp;
    private RadioButton Radio_Gender;
    private DatePickerDialog datePickerDialog;
    private TextInputEditText textinputName,textinputDOB,date; // was earlier TextInputLayout


    public static   ProgressBar Spinner;

    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);
        Spinner = (ProgressBar)findViewById(R.id.progress_bar);
        Spinner.setVisibility(View.INVISIBLE);

        initViews();
        initListeners();
        initObjects();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        initDatabaseReferences();

        mAuth=FirebaseAuth.getInstance();
        device = new Device();

        Resources res = getResources();
        String[] Locality = res.getStringArray(R.array.Locality);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Locality);
        input_location.setAdapter(adapter);
//        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // check if user is signed in to google or facebook
        if (GoogleSignIn.getLastSignedInAccount(this) != null)
        {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                if (personName != null) {
                    textinputName.setText(personName);
                }
            }
        }
        else
        {
            Log.d("isLoggedinGoogle","Not logged in");
        }

        //get single instance of user if logged in through google from user defined class GoogleFirebaseSignIn
        googleFirebaseSignIn = GoogleFirebaseSignIn.getInstance();


    }

    private void initializeGoogleFirebaseSignIn(){
        //deviceId();
        //googleFirebaseSignIn.init(SignUp2.this, mAuth, mFirebaseDatabase, imei);
    }

    private void signOut(){
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out from Firebase", Toast.LENGTH_SHORT).show();
        }
    }

    /**Initialize Views*/
    private void initViews(){
        input_mobile = findViewById(R.id.input_mobile);
        input_location = findViewById(R.id.AutoCompleteTextView);
//        input_actv = findViewById(R.id.AutoCompleteTextView);
       // error_message = findViewById(R.id.error_message);
        btn_submit = findViewById(R.id.btn_submit);
        text_view = findViewById(R.id.text_gender);
        textinputName = findViewById(R.id.textlayout_Name);
        textinputDOB = findViewById(R.id.textlayout_Dob);
        date=findViewById(R.id.textlayout_Dob);
        gender_grp = findViewById(R.id.radiogrp);
    }

    private void initObjects(){
        inputValidation = new InputValidation(activity);
        DBHelper = new SQLiteDBHelper(activity);
        ReturnIntent = new Intent();
        user = getIntent().getParcelableExtra("User"); //getting the User object from previous signup activity
        password = getIntent().getStringExtra("password");
    }

    private void initDatabaseReferences(){
        //Initialize Database
        mDevicesDatabaseReference = mFirebaseDatabase.getReference().child("Devices");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        mMobileDatabaseReference = mFirebaseDatabase.getReference().child("Mobile");
        mEmailDatabaseReference = mFirebaseDatabase.getReference().child("Email");
    }

    private void initListeners(){
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c= java.util.Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(SignUp2.this,
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
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                if (!(validation.validateName(textinputName) & validation.validateGender(gender_grp,text_view) & validation.validateDob(textinputDOB))){
                    Toast.makeText(SignUp2.this,"Enter Valid Credentials",Toast.LENGTH_SHORT).show();
                    return;
                }

                //Call the method to validate the fields
                boolean valid_mobile = false;
//                boolean valid_aadhar = false;
                boolean valid_location = false;
                boolean empty_mobile = inputValidation.is_Empty(input_mobile, "PLEASE ENTER MOBILE NO. ");
                if (!empty_mobile) {
                    if (!inputValidation.isMinLength(input_mobile, 10, getString(R.string.no_phone)) || !inputValidation.is_numeric(input_mobile)) {

                        input_mobile.setError(getString(R.string.no_phone));
;

                    }
                    else {
                        valid_mobile = true;
                        input_mobile.setError(null);
                    }
                }

                if (input_location.getText().toString().isEmpty()) {
                    input_location.setError(getString(R.string.no_location));

                    Spinner.setVisibility(View.GONE);
                    Enable();
                    btn_submit.setText("SIGNUP");

                }
                else{
                    valid_location = true;
                    input_location.setError(null);
                }
                /**NOTE:MOVED empty if statement here to ensure error messages are displayed before exiting
                 //if(empty){
                 //  return;

                 }*/

                //Moved IMEI reading code to this place so IMEI can be stored for user object
                boolean empty = inputValidation.all_Empty(input_mobile,input_location,getString(R.string.message));

                Log.d("SIgnUP2","Valid Mobile:"+valid_mobile+" Valid Location:"+valid_location);
                boolean valid = valid_mobile && valid_location;

                Log.d("SIgnUP2","Empty:"+empty+" Valid"+valid);

                int selected_id = gender_grp.getCheckedRadioButtonId();
                Radio_Gender = (RadioButton) findViewById(selected_id);
                String gender = Radio_Gender.getText().toString().trim(); //function .getEditText() have been removed as TextInputEditText doesn't require it.

                //NOTE: Allow database entry only if not empty AND valid
                if(!empty && valid){
                    Spinner.setVisibility(View.VISIBLE);
                    disable();
                    btn_submit.setText("");

                    imei = null;
                    TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                    String permission = Manifest.permission.READ_PHONE_STATE;
                    int res = getApplicationContext().checkCallingOrSelfPermission(permission);
                    if (res == PackageManager.PERMISSION_GRANTED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            imei = tm.getImei(0);
                            Log.d("IMEI", "IMEI Number of slot 1 is:" + imei);
                        } else {
                            imei=tm.getDeviceId();
                            Log.d("SignUp2", "SDK Version not of required level");
                            Log.d("SignUp2", "Using getDeviceId()"+imei);

                        }
                    } else {
                        Spinner.setVisibility(View.GONE);
                        Enable();
                        btn_submit.setText("SIGNUP");
                        Log.d("SIgnUP2", "PERMISSION FOR READ STATE NOT GRANTED, REQUESTING PERMSISSION...");
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.READ_PHONE_STATE}, RC);
                    }

                    if (DBHelper.checkUser(input_mobile.getText().toString().trim()) ){

                        if (user.getEmail() == null || password == null){
                           Spinner.setVisibility(View.GONE);
                            Enable();
                            btn_submit.setText("SIGNUP");
                            // go back to signUp1
                            finishActivity(2);
                        }

                        user.setName(textinputName.getText().toString().trim());
                        user.setGender(gender);
                        user.setDob(textinputDOB.getText().toString().trim());
                        user.setMobile(input_mobile.getText().toString().trim());
                        user.setLocation(input_location.getText().toString().trim());
                        user.setImei(imei);//setting IMEI
                        user.setPaid(false);
                        user.setSosContacts(setSosContacts());
                        user.setGoogleAccountLinked(false);

                        // check if mobile number exists
                        setUidFromFirebase(user.getMobile());
                        //added conditional checking and showing respective Toast message

                    } else {
                        Log.d("SignUp2", "User exists ");
                        Toast.makeText(getApplicationContext(), "MOBILE NO. ALREADY EXISTS", Toast.LENGTH_LONG).show();
                        Spinner.setVisibility(View.GONE);
                        Enable();
                        btn_submit.setText("SIGNUP");

                    }
                }
                }
            }

        );
    }

    private HashMap<String,String> setSosContacts(){
        HashMap<String,String> sosContacts = new HashMap<>();
        for (int i=1;i<=5;i++)
            sosContacts.put("c"+i,"null");
        return sosContacts;
    }

    private void writeDataToFirebase(FirebaseUser firebaseUser){
        //check internet connection

        device.setUID(firebaseUser.getUid());

        writeUserToFirebase(firebaseUser);
    }

    private void AddUser(){
        //added conditional checking and showing respective Toast message
        if (DBHelper.addUser(user))
        {
            Log.d("User Email:",user.getEmail());
            Log.d("User imei:",user.getImei());

            // create the User
             mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                    .addOnCompleteListener(SignUp2.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                Log.d(TAG,"new user ? ->"+task.getResult().getAdditionalUserInfo().isNewUser());
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                storeAndStartNextActivity(firebaseUser);

                            } else {
                                try{
                                    throw task.getException();
                                }
                                catch (FirebaseAuthUserCollisionException e){
                                    // user exists -> sign in the user
                                    mAuth.signInWithEmailAndPassword(user.getEmail(), password)
                                            .addOnCompleteListener(SignUp2.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                                        storeAndStartNextActivity(firebaseUser);
                                                    } else {
                                                        try{
                                                            throw task.getException();
                                                        }
                                                        catch (Exception e){
                                                            Log.d(TAG,"Exception:"+e.getMessage());
                                                            Toast.makeText(SignUp2.this, "Authentication failed.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            });
                                }
                                catch (Exception e){
                                    Spinner.setVisibility(View.GONE);
                                    Enable();
                                    btn_submit.setText("SIGNUP");

                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure"+e.getMessage());
                                    Toast.makeText(SignUp2.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
        else {
            Spinner.setVisibility(View.GONE);
            Enable();
            btn_submit.setText("SIGNUP");

            Toast.makeText(getApplicationContext(), "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
        }

    }

    private void storeAndStartNextActivity(FirebaseUser firebaseUser){

        // check if user is signed in to google or facebook
        if (GoogleSignIn.getLastSignedInAccount(SignUp2.this) != null){
            Log.d(TAG,"Logged in to google");
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(SignUp2.this);
            //initialize user defined class GoogleFirebaseSignIn with Firebase user instance andTAGusing user defined init method
            //initializeGoogleFirebaseSignIn();
            //link google acc to email acc
            //googleFirebaseSignIn.linkGoogleAccount(acct);
            linkGoogleAccount(acct);
        }
        else{
            Log.d("isLoggedinGoogle:","Not logged in");
            writeDataToFirebase(firebaseUser);
        }

        /*ReturnIntent.putExtra("ResultIntent",user);
        Log.d("SignUp2 ","Returned Completed User Object"+user.getMobile()+user.getLocation());
        setResult(10,ReturnIntent);//to finish sing up 1 activity
        activity.finish();*/

        /*user=data.getParcelableExtra("ResultIntent");
        Intent profileActivity = new Intent(SignUp2.this,ProfileActivity.class);
        profileActivity.putExtra("User",user);
        startActivity(profileActivity);*/
    }



    private void setUidFromFirebase(final String mobile){
        mMobileDatabaseReference.child(mobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot mobileNodeDataSnapshot) {
                Log.d("Mobile Data Snapshot:", mobileNodeDataSnapshot.toString());
                if (mobileNodeDataSnapshot.exists()) {
                    uid = mobileNodeDataSnapshot.getValue().toString();
                } else {
                    uid = null;
                }
                // validate before storing details..check if mobile no is registered
                validateBeforeStoring(mobile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void validateBeforeStoring(String mobile){

        if (uid == null){
            // user details can be pushed to db
            AddUser();
        }
        else{
            Spinner.setVisibility(View.GONE);
            Enable();
            btn_submit.setText("SIGNUP");

            // prompt user to enter different mobile no.
            Log.d(TAG, "Mobile no. already registered in firebase");
            Toast.makeText(SignUp2.this, "Mobile no. already registered. Enter different mobile number",Toast.LENGTH_LONG).show();
        }
    }

    //screen enable/disable

    public  void Enable()
    {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        textinputName.setAlpha(1);
        gender_grp.setAlpha(1);
        textinputDOB.setAlpha(1);
        input_mobile.setAlpha(1);
        input_location.setAlpha(1);
        btn_submit.setAlpha(1);
        text_view.setAlpha(1);
    }
    public void disable()
    {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        textinputName.setAlpha((float) 0.6);
        gender_grp.setAlpha((float) 0.6);
        textinputDOB.setAlpha((float) 0.6);
        input_mobile.setAlpha((float) 0.6);
        input_location.setAlpha((float) 0.6);
        btn_submit.setAlpha((float) 0.6);
        text_view.setAlpha((float)0.6);
    }
    
    private void writeUserToFirebase(final FirebaseUser firebaseUser){
        //push user to firebase database 'Users' node
        mUsersDatabaseReference.child(firebaseUser.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.d(TAG,"User Data could not be saved " + databaseError.getMessage());
                    Toast.makeText(SignUp2.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG,"User Data saved successfully.");
                    Log.d("Pushed to db",mUsersDatabaseReference.getDatabase().toString());
                    writeDeviceToFirebase(firebaseUser);
                }
            }
        });

    }

    private void writeDeviceToFirebase(final FirebaseUser firebaseUser){
        //push device to firebase database 'Devices' node
        mDevicesDatabaseReference.child(user.getImei()).setValue(device, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.d(TAG,"Device Data could not be saved " + databaseError.getMessage());
                    Toast.makeText(SignUp2.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    deleteDataFromFirebase(firebaseUser);
                }
                else {
                    Log.d(TAG,"Device Data saved successfully.");
                    Log.d("Pushed to db",mDevicesDatabaseReference.getDatabase().toString());
                    writeEmailToFirebase(firebaseUser);
                }
            }
        });
    }

    private void writeEmailToFirebase(final FirebaseUser firebaseUser){
        //push email and mobile no. on root node
        String emailKey = TextUtils.join(",", Arrays.asList(user.getEmail().split("\\."))); //as key in firebase db cannot contain "."
        mEmailDatabaseReference.child(emailKey).setValue(firebaseUser.getUid(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.d(TAG,"Email Data could not be saved " + databaseError.getMessage());
                    Toast.makeText(SignUp2.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    deleteDataFromFirebase(firebaseUser);
                }
                else {
                    Log.d(TAG,"Email Data saved successfully.");
                    Log.d("Pushed to db",mEmailDatabaseReference.getDatabase().toString());
                    writeMobileToFirebase(firebaseUser);
                }
            }
        });
    }

    private void writeMobileToFirebase(final FirebaseUser firebaseUser){
        mMobileDatabaseReference.child(user.getMobile()).setValue(firebaseUser.getUid(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.d(TAG,"Mobile Data could not be saved " + databaseError.getMessage());
                    Toast.makeText(SignUp2.this, "Authentication failed. Please check your internet connection", Toast.LENGTH_SHORT).show();
                    deleteDataFromFirebase(firebaseUser);
                }
                else {
                    Log.d(TAG,"Mobile Data saved successfully.");
                    Log.d("Pushed to db",mMobileDatabaseReference.getDatabase().toString());
                    Toast.makeText(getApplicationContext(), "YOU ARE NOW A SAVIOUR", Toast.LENGTH_LONG).show();
                    Intent sosPage = new Intent(SignUp2.this, sos_page.class);
                    startActivity(sosPage);
                }
            }
        });
    }

    private void deleteDataFromFirebase(FirebaseUser firebaseUser){
        if (firebaseUser != null){
            mUsersDatabaseReference.child(firebaseUser.getUid()).setValue(null);
            mDevicesDatabaseReference.child(imei).setValue(null);
            String emailKey = TextUtils.join(",", Arrays.asList(user.getEmail().split("\\."))); //as key in firebase db cannot contain "."
            mEmailDatabaseReference.child(emailKey).setValue(null);
            mMobileDatabaseReference.child(user.getMobile()).setValue(null);
        }
    }

    private void linkGoogleAccount(GoogleSignInAccount acct) {

        // Link the anonymous user to the email credential
        //showProgressDialog();
        AuthCredential credential= GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        // [START link_credential]
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(SignUp2.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            user.setGoogleAccountLinked(true);
                            // write data to firebase
                            writeDataToFirebase(firebaseUser);
                            //Toast.makeText(LinkAccountActivity.this, "Account Linked Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                throw task.getException();
                            }catch (Exception e){
                                Log.d(TAG, "linkWithCredential:failure"+e.getMessage());
                                Toast.makeText(SignUp2.this, "Authentication failed. Please check your connection", Toast.LENGTH_SHORT).show();
                                signOut();
                            }
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END link_credential]
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getEmail());
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignUp2.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            Log.d(TAG, "onComplete: " + (isNew ? "new user" : "old user"));

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // set imei and uid in firebase
                            device = new Device();
                            device.setUID(user.getUid());
                            mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
                            mDevicesDatabaseReference = mFirebaseDatabase.getReference().child("Devices");
                            mDevicesDatabaseReference.child(imei).setValue(device);
                            mUsersDatabaseReference.child(user.getUid()).child("imei").setValue(imei);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.layout.activity_main), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}





