package com.example.securityapplication;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.securityapplication.Helper.FirebaseHelper;
import com.example.securityapplication.Helper.KeyboardHelper;
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
import es.dmoral.toasty.Toasty;

public class SignUp2 extends AppCompatActivity {
    private final AppCompatActivity activity = SignUp2.this;


    private TextInputEditText input_mobile;
    private AutoCompleteTextView input_location;

    private Button btn_submit;
    private Intent ReturnIntent;
    private InputValidation inputValidation;
    private Validation validation = new Validation();
    private SQLiteDBHelper DBHelper;
    private User user;
    private String blockcharset = "~#^|$%&*!,.";
    //adding requestCode variable for requestPermission
    private int RC;

    private FirebaseHelper firebaseHelper;
    private GoogleFirebaseSignIn googleFirebaseSignIn;
    private ValueEventListener mUsersDatabaseReferenceListener;

    private String uid;

    private String TAG = "SignUp2";
    private String imei;

    private TextView text_view;
    private RadioGroup gender_grp;
    private RadioButton Radio_Gender;
    private DatePickerDialog datePickerDialog;
    private TextInputEditText textinputName,textinputDOB,date; // was earlier TextInputLayout

    private String password;
    public static ProgressBar Spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);
        Spinner = (ProgressBar) findViewById(R.id.progress_bar);
        Spinner.setVisibility(View.INVISIBLE);

        initViews();
        initListeners();
        initObjects();

        /**  Get FirebaseHelper Instance **/
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.initFirebase();
        firebaseHelper.initContext(SignUp2.this);
        firebaseHelper.initGoogleSignInClient(getString(R.string.server_client_id));

        Resources res = getResources();
        String[] Locality = res.getStringArray(R.array.Locality);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Locality);
        input_location.setAdapter(adapter);
//        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // check if user is signed in to google or facebook
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                if (personName != null) {
                    textinputName.setText(personName);
                }
            }
        } else {
            Log.d("isLoggedinGoogle", "Not logged in");
        }

        //get single instance of user if logged in through google from user defined class GoogleFirebaseSignIn
        googleFirebaseSignIn = GoogleFirebaseSignIn.getInstance();
        initializeGoogleFirebaseSignIn();
    }

    private void initializeGoogleFirebaseSignIn(){
        //deviceId();
        googleFirebaseSignIn.init(SignUp2.this, imei);
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
        DBHelper = SQLiteDBHelper.getInstance(activity);
        ReturnIntent = new Intent();
        user = getIntent().getParcelableExtra("User"); //getting the User object from previous signup activity
        password = getIntent().getStringExtra("password");
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
                Animation sub_anim= AnimationUtils.loadAnimation(SignUp2.this,R.anim.btn_anim);
                btn_submit.startAnimation(sub_anim);
                KeyboardHelper.hideSoftKeyboard(SignUp2.this, view);
                Toast.makeText(SignUp2.this, "Please stay here", Toast.LENGTH_LONG).show();

                if (!(validation.validateName(textinputName) & validation.validateGender(gender_grp,text_view) & validation.validateDob(textinputDOB))){
                    Toasty.error(SignUp2.this, "Enter Valid Credentials", Toast.LENGTH_SHORT, true).show();

                    //Toast.makeText(SignUp2.this,"Enter Valid Credentials",Toast.LENGTH_SHORT).show();
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
                    if (!IsInternet.checkInternet(SignUp2.this))
                        return;

                    KeyboardHelper.hideSoftKeyboard(SignUp2.this, view);

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
                            Toast.makeText(SignUp2.this, "Please fill the details",Toast.LENGTH_SHORT).show();

                            // go back to signUp1
                            finishActivity(2);
                        }

                        // set user object
                        setUser(gender);

                        // user details can be pushed to db
                        if (!IsInternet.checkInternet(SignUp2.this))
                            return;
                        AddUser();
                    } else {
                        Log.d("SignUp2", "User exists ");
                        Toasty.error(getApplicationContext(), "MOBILE NO. ALREADY EXISTS", Toast.LENGTH_LONG, true).show();

                        //Toast.makeText(getApplicationContext(), "MOBILE NO. ALREADY EXISTS", Toast.LENGTH_LONG).show();
                        Spinner.setVisibility(View.GONE);
                        Enable();
                        btn_submit.setText("SIGNUP");

                    }
                }
                }
            }

        );
    }

    public void onStart(){
        super.onStart();
        if (btn_submit != null) {
            KeyboardHelper.hideSoftKeyboard(SignUp2.this, btn_submit);
        }
    }

    private void setUser(String gender){
        user.setName(textinputName.getText().toString().trim());
        user.setGender(gender);
        user.setDob(textinputDOB.getText().toString().trim());
        user.setMobile(input_mobile.getText().toString().trim());
        user.setLocation(input_location.getText().toString().trim());
        user.setImei(imei);//setting IMEI
        user.setPaid(false);
        //user.setSosContacts(setSosContacts());
        Log.d(TAG,user.getName());
        Log.d(TAG,user.getEmail());
        Log.d(TAG,user.getGender());
        Log.d(TAG,user.getDob());
        Log.d(TAG,user.getImei());
        Log.d(TAG,user.getLocation());
        Log.d(TAG,user.getMobile());
        Log.d(TAG,"isPaid?:"+user.isPaid());
    }

    private void AddUser(){

        Log.d("User Email:",user.getEmail());
        Log.d("User imei:",user.getImei());

        // create the User
         firebaseHelper.getFirebaseAuth().createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(SignUp2.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            Log.d(TAG,"new user ? ->"+task.getResult().getAdditionalUserInfo().isNewUser());
                            FirebaseUser firebaseUser = firebaseHelper.getFirebaseAuth().getCurrentUser();
                            writeDataToFirebase(firebaseUser);

                        } else {
                            try{
                                throw task.getException();
                            }
                            catch (FirebaseAuthUserCollisionException e){
                                // user exists -> sign in the user
                                firebaseHelper.getFirebaseAuth().signInWithEmailAndPassword(user.getEmail(), password)
                                        .addOnCompleteListener(SignUp2.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser firebaseUser = firebaseHelper.getFirebaseAuth().getCurrentUser();
                                                    writeDataToFirebase(firebaseUser);
                                                } else {
                                                    try {
                                                        throw task.getException();
                                                    } catch (Exception e) {
                                                        Log.d(TAG, "Exception:" + e.getMessage());
                                                        Toasty.error(SignUp2.this, "Authentication failed.", Toast.LENGTH_LONG, true).show();
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
                                    Toasty.error(SignUp2.this, "Authentication failed.", Toast.LENGTH_LONG, true).show();
                                }
                            }
                        }
                    });
    }

    private void writeDataToFirebase(FirebaseUser firebaseUser){
        //check internet connection
        if (!IsInternet.checkInternet(SignUp2.this)) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                firebaseHelper.firebaseSignOut();
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            writeEmailToFirebase(firebaseUser);
        else
            Toast.makeText(SignUp2.this, "Please signup again.", Toast.LENGTH_SHORT).show();
    }

    private void writeEmailToFirebase(final FirebaseUser firebaseUser){
        Log.d(TAG,"Inside writeEmail");
        //push email and mobile no. on root node
        String emailKey = TextUtils.join(",", Arrays.asList(user.getEmail().split("\\."))); //as key in firebase db cannot contain "."
        firebaseHelper.getEmailDatabaseReference().child(emailKey).setValue(firebaseUser.getUid(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.d(TAG,"Email Data could not be saved " + databaseError.getMessage());
                    Toast.makeText(SignUp2.this, "error in writeEmailtofirebase Signup2:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    if (databaseError.getCode() == -3) {
                        // -3 : Permission denied to write in firebase
                        Toasty.error(SignUp2.this, "Account already registered", Toast.LENGTH_LONG, true).show();
                    }else Toast.makeText(SignUp2.this,"In email:"+databaseError.getMessage(),Toast.LENGTH_LONG).show();
                    firebaseHelper.firebaseSignOut();
                    firebaseHelper.googleSignOut(SignUp2.this);
                    redirectToMainActivity();
                }
                else {
                    Log.d(TAG,"Email Data saved successfully.");
                    writeDeviceToFirebase(firebaseUser);
                }
            }
        });
    }

    private void writeDeviceToFirebase(final FirebaseUser firebaseUser){
        Log.d(TAG,"Inside writeDevice");
        firebaseHelper.getDevicesDatabaseReference().child(user.getImei()).setValue(firebaseUser.getUid(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.d(TAG,"Device Data could not be saved " + databaseError.getMessage());
                    if (databaseError.getCode() == -3)
                        Toast.makeText(SignUp2.this, "Permission denied in device", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(SignUp2.this, "error in writeDevicetofirebase Signup2:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    deleteDataFromFirebase(firebaseUser);
                }
                else {
                    Log.d(TAG,"Device Data saved successfully.");
                    writeUserToFirebase(firebaseUser);
                }
            }
        });
    }

    private void writeUserToFirebase(final FirebaseUser firebaseUser){
        Log.d(TAG,"Inside writeUser");
        //push user to firebase database 'Users' node
        firebaseHelper.getUsersDatabaseReference().child(firebaseUser.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.d(TAG,"User Data could not be saved " + databaseError.getMessage());
                    if (databaseError.getCode() == -3)
                        Toast.makeText(SignUp2.this, "Permission denied in user", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(SignUp2.this, "error in writeUsertofirebase Signup2:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    deleteDataFromFirebase(firebaseUser);
                }
                else {
                    Log.d(TAG,"User Data saved successfully.");
                    try {
                        if (DBHelper.addUser(user)) {
                            gotoNextActivity();
                        }
                        else {
                            Toasty.error(getApplicationContext(), "Data not stored in sqlite Signup2", Toast.LENGTH_LONG, true).show();
                            Toast.makeText(SignUp2.this, "Authentication failed. Try to login", Toast.LENGTH_SHORT).show();
                            LogOutUser();
                        }
                    }catch (Exception e){
                        Log.d(TAG,e.getMessage());
                        Toasty.error(getApplicationContext(), "Sqlite error occurred Signup2", Toast.LENGTH_LONG, true).show();
                        Toast.makeText(SignUp2.this, "Authentication failed. Try to login", Toast.LENGTH_SHORT).show();
                        LogOutUser();
                    }
                }
            }
        });
    }

    public void LogOutUser(){
        firebaseHelper.firebaseSignOut(imei);
        firebaseHelper.googleSignOut(SignUp2.this);
        redirectToMainActivity();
    }

    public void redirectToMainActivity(){

        //delete user records from SQLite
        if (DBHelper.getdb_user() != null)
            DBHelper.deleteDatabase(SignUp2.this);

        Intent redirect = new Intent(SignUp2.this,MainActivity.class);
        redirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(redirect);
        finish();
    }

    private void deleteDataFromFirebase(FirebaseUser firebaseUser){
        Log.d(TAG,"Inside deleteDataFromFirebase");
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            try {
                firebaseHelper.getUsersDatabaseReference().child(firebaseUser.getUid()).removeValue();
                firebaseHelper.getDevicesDatabaseReference().child(imei).removeValue();
                String emailKey = TextUtils.join(",", Arrays.asList(user.getEmail().split("\\."))); //as key in firebase db cannot contain "."
                firebaseHelper.getEmailDatabaseReference().child(emailKey).removeValue();
            }catch (Exception e){
                Log.d(TAG,"Exception while deleting:"+e.getMessage());
                Toast.makeText(SignUp2.this, "Exception while deleting:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        Spinner.setVisibility(View.GONE);
        Enable();
        btn_submit.setText("SIGNUP");
        Toast.makeText(SignUp2.this, "Authentication failed", Toast.LENGTH_SHORT).show();
    }

    private void gotoNextActivity(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            Toast.makeText(getApplicationContext(), "YOU ARE NOW A SAVIOUR", Toast.LENGTH_LONG).show();
            // start next activity
            // check if user is signed in to google or facebook
            if (GoogleSignIn.getLastSignedInAccount(SignUp2.this) != null){
                Log.d(TAG,"Logged in to google");
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(SignUp2.this);
                googleFirebaseSignIn.linkGoogleAccount(acct);
            }
            startSosContactActivity();
        }
        else {
            Toast.makeText(SignUp2.this, "Authentication failed. Try to login", Toast.LENGTH_SHORT).show();
            // redirect user to MainActivity
            LogOutUser();
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

    private void startSosContactActivity(){
        ReturnIntent.putExtra("ResultIntent",user);
        //Log.d("SignUp2 ","Returned Completed User Object"+user.getMobile()+user.getLocation());
        setResult(10,ReturnIntent);//to finish sing up 1 activity
        activity.finish();
        //Clear the back stack and re-directing to the sospage
        Intent sosPage = new Intent(SignUp2.this, sos_page.class);
        sosPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        sosPage.putExtra("btn","1");
        startActivity(sosPage);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"Inside onBackPressed");
        if (!(btn_submit.getAlpha()==1)){
            Log.d(TAG,"Inside if condition");
            // give alert to wait
            AlertDialog.Builder a_builder = new AlertDialog.Builder(SignUp2.this);
            a_builder.setMessage("Please wait")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

            AlertDialog alert = a_builder.create();
            alert.show();
        }
        else
            super.onBackPressed();
    }
}