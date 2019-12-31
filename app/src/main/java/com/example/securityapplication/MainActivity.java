package com.example.securityapplication;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import android.widget.TextView;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.securityapplication.Helper.FirebaseHelper;
import com.example.securityapplication.Helper.InternalStorage;
import com.example.securityapplication.Helper.KeyboardHelper;
import com.example.securityapplication.model.User;
//import com.agrawalsuneet.dotsloader.loaders.TashieLoader;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int ERROR_DIALOG_REQUEST = 9002 ;
    private TelephonyManager telephonyManager;
    private String mImeiNumber;
    private TextView mStatus;
    private EditText mEmail;
    private EditText mPassword;
    private Button mSignInButton;
    private String TAG = "MainActivity";
    private Button signupbttn;

    private FirebaseHelper firebaseHelper;

    //GoogleFirebaseSignIn
    private SignInButton mGoogleSignInButton;
    private GoogleFirebaseSignIn googleFirebaseSignIn;
    private static final int RC_SIGN_IN = 9001;
    ProgressBar pgsBar;
    ProgressBar pgsBar1;

    private User user;
    private String uid;

    //persistent service
    private Intent mSosPlayerIntent;

    //Permissions request code
    int RC;

    private static Hashtable<String,String> userData;
    SQLiteDBHelper db;

    public void pgbarshow()
    {
        mSignInButton.setText("");
        findViewById(R.id.pBar).setVisibility(VISIBLE);
        mSignInButton.getBackground().setAlpha(100);

        mSignInButton.setClickable(false);
        signupbttn.setClickable(false);

        mEmail.setEnabled(false);
        mPassword.setEnabled(false);
        findViewById(R.id.ForgetPassword).setEnabled(false);
        findViewById(R.id.checkbox).setEnabled(false);
        mGoogleSignInButton.setEnabled(false);
    }

    public void pgbarhide()
    {
        findViewById(R.id.pBar).setVisibility(GONE);
        mSignInButton.setText("SIGN IN");
        mSignInButton.getBackground().setAlpha(255);

        signupbttn.setClickable(true);
        mSignInButton.setClickable(true);

        mEmail.setEnabled(true);
        mPassword.setEnabled(true);
        findViewById(R.id.ForgetPassword).setEnabled(true);
        findViewById(R.id.checkbox).setEnabled(true);
        mGoogleSignInButton.setEnabled(true);
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if(i== R.id.signInButton){
            Animation sign_anim= AnimationUtils.loadAnimation(this,R.anim.btn_anim);
            mSignInButton.startAnimation(sign_anim);
            if(firebaseHelper.getFirebaseAuth().getCurrentUser()==null) {
                Log.d(TAG,"Current user is null");
                if(validateForm()){
                    if (!IsInternet.checkInternet(MainActivity.this))
                        return;
                    KeyboardHelper.hideSoftKeyboard(MainActivity.this, v);
                    userData = new Hashtable<>();
                    pgbarshow();
                    userData.put("email",mEmail.getText().toString());
                    userData.put("password",mPassword.getText().toString());
                    userData.put("SignInType", "email");
                    // Check if registered user sign's in using old device or new device using imei number.
                    setDeviceForSignIn(mImeiNumber);
                }
                else {
                    Log.d(TAG,"Invalid credentials");
                    Toasty.error(MainActivity.this, "Enter valid credentials", Toast.LENGTH_SHORT, true).show();

 //                   Toast.makeText(MainActivity.this, "Enter valid credentials",Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (i==R.id.googleSignInButton){
            if (!IsInternet.checkInternet(MainActivity.this))
                return;

            Intent signInIntent = firebaseHelper.getGoogleSignInClient().getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        else if (i==R.id.signUpButton){
            Animation signup_anim=AnimationUtils.loadAnimation(this,R.anim.btn_anim);
            signupbttn.startAnimation(signup_anim);
            signupbttn.setText("");
            findViewById(R.id.pBar1).setVisibility(VISIBLE);
            mSignInButton.setClickable(false);

            signupbttn.getBackground().setAlpha(100);

            Intent signUpIntent = new Intent(this,SignUp1Activity.class);
            startActivityForResult(signUpIntent,1);

        }
        //Added Reset Passowrd Activity
        else if (i==R.id.ForgetPassword){
            Intent forgetPasswordIntent = new Intent(this,ResetPasswordActivity.class);
            startActivity(forgetPasswordIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"Starting MainActivity...........................");

        //initialize Activity
        Log.d("MainActivity","Inside onCreate");
        initViews();
        initOnClickListeners();

        FirebaseApp.initializeApp(this);

        /**  Get FirebaseHelper Instance **/
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.initFirebase();
        firebaseHelper.initContext(MainActivity.this);

        //Grant Device read Permissions
        if (mImeiNumber == null)
            deviceId();

        /**  GOOGLE LOGIN  **/
        firebaseHelper.initGoogleSignInClient(getString(R.string.server_client_id));
        findViewById(R.id.googleSignInButton).setOnClickListener(this);
        //get single instance of user if logged in through google from user defined class GoogleFirebaseSignIn
        googleFirebaseSignIn = GoogleFirebaseSignIn.getInstance();
        //initialize user defined class GoogleFirebaseSignIn with Firebase user instance andTAGusing user defined init method
        initializeGoogleFirebaseSignIn();

        /** DATABASE FORCEFUL CREATION**/
        db=SQLiteDBHelper.getInstance(MainActivity.this);
        //uncomment to force delete database NOTE:Recomment after uncommenting oncce
        //db.deleteDatabase(this);
    }

    public void onStart(){

        Log.d(TAG,"Inside onStart");

 //       final DatabaseReference ddb=FirebaseDatabase.getInstance().getReference().child("Devices").child("356477081682635");

//        final DatabaseReference ddb=FirebaseDatabase.getInstance().getReference().child("Devices").child("356477081682635");

//        ddb.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot dp:dataSnapshot.getChildren()){
//                    String st=dp.getKey();
//                    DatabaseReference d=ddb.child(st);
//                    d.removeValue();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
 //      ddb.removeValue();
   //     Toast.makeText(this, "deletion done", Toast.LENGTH_SHORT).show();

        super.onStart();
        if (mImeiNumber == null)
            deviceId();
        checkPlayServices();
        FirebaseUser currentUser = firebaseHelper.getFirebaseAuth().getCurrentUser();
        updateUI(currentUser);
    }


    private boolean checkGPSPermission() {
        Log.d("MainActivity","Inside CheckGPSPermission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {   //permissions not granted
            Log.d("GPS Access in Main","Requesting GPS Location");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 102);
        }else {
            //permissions granted
            //ContextCompat.startForegroundService(this,new Intent(this,GetGPSCoordinates.class));
        }
         return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
     }

    private void initializeGoogleFirebaseSignIn(){
        if (mImeiNumber == null)
            deviceId();
        else
            googleFirebaseSignIn.init(this, mImeiNumber);
    }

    public void initViews(){
        mEmail=findViewById(R.id.editEmail);
        mPassword=findViewById(R.id.editPassword);
        mStatus= findViewById(R.id.status);
        mSignInButton=findViewById(R.id.signInButton);
        mGoogleSignInButton=findViewById(R.id.googleSignInButton);
        signupbttn = findViewById(R.id.signUpButton);
        pgsBar = findViewById(R.id.pBar);
        pgsBar1= findViewById(R.id.pBar1);
    }

    public void initOnClickListeners(){
        findViewById(R.id.signInButton).setOnClickListener(this);
    }

   private void checkUserStatus(){

       Log.d(TAG,"Checkig user status...");
        if (firebaseHelper.getFirebaseAuth().getCurrentUser() != null) {
            deviceId();
            firebaseHelper.getDevicesDatabaseReference().child(mImeiNumber).setValue(FirebaseAuth.getInstance().getUid(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        firebaseHelper.makeDeviceImeiNull(mImeiNumber);
                        if (databaseError.getCode() == -3) {
                            Toast.makeText(MainActivity.this, "Last logout was unsuccessful. Try to login using the previous Account.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Please check your connection and try again", Toast.LENGTH_LONG).show();
                            Log.d(TAG,databaseError.getMessage());
                            Toast.makeText(MainActivity.this, "Sign in failed"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        LogOutUser();
                    } else {
                        firebaseHelper.getUsersDatabaseReference().child(FirebaseAuth.getInstance().getUid()).child("imei").setValue(mImeiNumber, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    firebaseHelper.makeDeviceImeiNull(mImeiNumber);
                                    if (databaseError.getCode() == -3){
                                        Toast.makeText(MainActivity.this, "Last logout was unsuccessful. Try to login using the previous Account.", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                        Toast.makeText(MainActivity.this, "Please check your connection and try again", Toast.LENGTH_LONG).show();
                                    Log.d(TAG,databaseError.getMessage());
                                    Toast.makeText(MainActivity.this, "Sign in failed."+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                                    // sigin out the user
                                    LogOutUser();
                                } else {
                                    Log.d(TAG, "Updated firebase");
                                    storeData(FirebaseAuth.getInstance().getCurrentUser());
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void deviceId() {
        telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        Log.d(TAG,"Inside deviceID");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
            return;
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mImeiNumber = telephonyManager.getImei(0);
                Log.d("IMEI", "IMEI Number of slot 1 is:" + mImeiNumber);
            }
            else {
                mImeiNumber = telephonyManager.getDeviceId();
            }
            googleFirebaseSignIn = GoogleFirebaseSignIn.getInstance();
            initializeGoogleFirebaseSignIn();
        }

        //Log.d("MAinActivity","SMS intent");
        //check permissions

        checkSMSPermission();
    }

    public  boolean checkSMSPermission(){
        Log.d(TAG,"Checking SMS Permissions");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            //Toasty.error(this, "Permission Required for sending SMS in case of SOS", Toast.LENGTH_SHORT, true).show();
            Log.d("MainActivity", "PERMISSION FOR SEND SMS NOT GRANTED, REQUESTING PERMSISSION...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 103);
            return false;
        }
        if (checkPlayServices())
            checkGPSPermission();
        else
            Toasty.error(this,"Google Play Services not found on your device",Toast.LENGTH_LONG);
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    deviceId();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    finish();
                    Toasty.error(this, "We need these permissions to keep you safe", Toast.LENGTH_SHORT, true).show();

                   // Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            case 102:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GPS In MainActivity","GPS Permissions granted");
                    if (user!=null && checkPlayServices())
                        ContextCompat.startForegroundService(this, new Intent(MainActivity.this, GetGPSCoordinates.class));
                } else {
                    Toasty.error(getApplicationContext(),"We need these permissions to keep you safe",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Inside else of onRequestPermissions denied, Calling finish");
                    finish();
                    //Permission Required Prompt
                }
                break;
            case 103:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGPSPermission();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Toasty.error(this, "We need these permissions to keep you safe", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean validateForm(){
        boolean valid = true;

        String email = mEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Required.");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Required.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            pgbarshow();
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                userData = new Hashtable<String,String>();
                userData.put("email",account.getEmail());
                userData.put("SignInType","google");
                setDeviceForSignIn(mImeiNumber);

            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                Toasty.error(this, "Authentication failed. Try again", Toast.LENGTH_SHORT, true).show();

               // Toast.makeText(MainActivity.this, "Authentication failed. Try again",Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                boolean hasBackPressed = data.getBooleanExtra("hasBackPressed",true);
                Log.d(TAG,"hasBackPressed:"+hasBackPressed);
                if (hasBackPressed){
                    LogOutUser();

                    pgsBar1.setVisibility(GONE);
                    signupbttn.getBackground().setAlpha(255);
                    signupbttn.setText("SIGN UP");
                    mSignInButton.getBackground().setAlpha(255);

                    if(pgsBar.getVisibility()==VISIBLE)
                    {
                        pgsBar.setVisibility(GONE);
                    }
                    mSignInButton.setClickable(true);
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void signIn(final String email, final String password){
         Log.d(TAG,"Signing IN user with email "+email+" and password "+password);

         firebaseHelper.getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            try {
                                boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                                Log.d(TAG, "onComplete: " + (isNew ? "new user" : "old user"));
                            }catch (Exception e){
                                Log.d(TAG,e.getMessage());
                            }

                            final FirebaseUser firebaseUser = firebaseHelper.getFirebaseAuth().getCurrentUser();
                            Log.d(TAG, firebaseHelper.getDevicesDatabaseReference().toString());
                            checkUserStatus();
                        } else {

                            try{
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidCredentialsException e){
                                Log.d(TAG,"Exception:"+e.getMessage());
                                Toasty.error(MainActivity.this, "Invalid Password", Toast.LENGTH_SHORT, true).show();

                                //Toast.makeText(MainActivity.this, "Invalid Password",Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                Log.d(TAG,"Exception:"+e.getMessage());
                                Toasty.error(MainActivity.this, "Authentication failed. Try again"+e.getMessage(), Toast.LENGTH_SHORT, true).show();

                               // Toast.makeText(MainActivity.this, "Authentication failed. Try again"+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }finally {
                                updateUI(null);
                            }
                        }
                    }
                });
    }

    private void LogOutUser(){
        firebaseHelper.firebaseSignOut();
        firebaseHelper.googleSignOut(MainActivity.this);
        pgbarhide();
        updateUI(null);
    }

    public void updateUI(FirebaseUser firebaseUser){
        //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();

        if(firebaseUser==null){
            mStatus.setText(R.string.not_logged);
            mSignInButton.setText(R.string.sign_in_text);
            mEmail.setVisibility(VISIBLE);
            mPassword.setVisibility(VISIBLE);
            mGoogleSignInButton.setVisibility(VISIBLE);
            pgbarhide();
        }
        else {
            Log.d("MainActivty","Inside UpdateUI GPS Permission = "+checkGPSPermission());
            //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();
            if (db.getdb_user().getImei() == null){
                firebaseHelper.firebaseSignOut(mImeiNumber);
                firebaseHelper.googleSignOut(MainActivity.this);
                updateUI(null);
                return;
            }
            //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();

            Log.d(TAG,"current user"+firebaseUser.getEmail());
            //goto next activity only if user exists in firebase db
            /** SosPlayer Service intent**/
            startService(new Intent(this, SosPlayer.class));
            //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();

            Log.d(TAG,"Starting navigation activity");

            if (checkPlayServices()) {
                if (checkGPSPermission()) {
                    /**Location Service intent**/
                    Log.d("MainActivity", "GPS Service starting");
                    Intent mHomeIntent = new Intent(MainActivity.this,navigation.class);
                    startActivity(mHomeIntent);
                    ContextCompat.startForegroundService(this, new Intent(MainActivity.this, GetGPSCoordinates.class));
                    try {
                        finish();
                        //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();

                    }catch (Exception e){
                        Log.d(TAG,"Exception on closing activity:"+e.getMessage());
                        finish();
                    }
                }else {
                    Toasty.error(this, "Location Permission is required to keep the app running", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Inside else of updateUI denied");
                }
            } else {
                //if google playServices are not installed the GPS Service won't run
                Intent mHomeIntent = new Intent(MainActivity.this,navigation.class);
                startActivity(mHomeIntent);
                try {
                    finish();
                    //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();
                }catch (Exception e){
                    Log.d(TAG,"Exception on closing activity:"+e.getMessage());
                    finish();
                }
            }
            //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();
        }
        Log.d(TAG,"UI updated successfully");
        //Toasty.success(MainActivity.this, "Success!", Toast.LENGTH_SHORT, true).show();
    }

    private void setDeviceForSignIn(String imei){
        Log.d(TAG,"Inside setDeviceForSignIn method-Imei no.:"+imei);
        firebaseHelper.getDevicesDatabaseReference().child(imei).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot deviceDataSnapshot) {
                Log.d("Device Data Snapshot:", deviceDataSnapshot.toString());
                if (deviceDataSnapshot.exists()) {
                    uid = deviceDataSnapshot.getValue(String.class);
                } else {
                    uid = null;
                }
                validateBeforeSignIn();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,databaseError.getDetails());
                Toast.makeText(MainActivity.this, "Inside setDeviceForSignIn MainAct:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                pgbarhide();
            }
        });
    }

    private void validateBeforeSignIn() {
        Log.d(TAG,"Inside validateBeforeSignIn");
        // Check if registered user sign's in using old device or new device using imei number.
        if (uid != null) {
            Log.d(TAG,"Current user:"+firebaseHelper.getFirebaseAuth().getCurrentUser()+"device formatted  or user data cleared or app uninstalled");
        }
        //to check if email is registered
        setUidFromFirebaseForSignIn();
    }

    private void setUidFromFirebaseForSignIn(){
        Log.d(TAG,"Inside setUidFromFirebaseForSignIn");
        String email = userData.get("email");
        // replace "." with "," in email id to store in firebase db as key
        String commaSeperatedEmail = TextUtils.join(",", Arrays.asList(email.split("\\.")));
        Log.d(TAG,commaSeperatedEmail);
        Log.d(TAG,firebaseHelper.getEmailDatabaseReference().toString());
        firebaseHelper.getEmailDatabaseReference().child(commaSeperatedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot emailNodeDataSnapshot) {
                Log.d("Email Data Snapshot:", emailNodeDataSnapshot.toString());
                if (emailNodeDataSnapshot.exists()) {
                    //emailNode = emailNodeDataSnapshot.getValue(Email.class);
                    uid = emailNodeDataSnapshot.getValue(String.class);
                    Log.d(TAG,uid);
                } else {
                    uid = null;
                }
                isEmailRegistered();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,databaseError.getDetails());
                Toast.makeText(MainActivity.this, "Inside setUidFromFirebaseForSignIn MainAct:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                pgbarhide();
            }
        });
    }

    private void isEmailRegistered(){
        Log.d(TAG,"Inside isEmailRegistered");
        if (uid == null){
            Log.d(TAG,"Account not registered. Please complete the registration process");
            // prompt user to signUp
            Intent signUpIntent = new Intent(this,SignUp1Activity.class);
            Toasty.warning(this, "Account not registered. Please complete the registration process", Toast.LENGTH_LONG, true).show();
            if (userData.get("SignInType").equals("email")){
                signUpIntent.putExtra("email", userData.get("email"));
            }
            startActivityForResult(signUpIntent,1);
            pgbarhide();
        }
        else {
            Log.d(TAG, uid);
            firebaseHelper.getUsersDatabaseReference().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                    Log.d("User Data Snapshot:", userDataSnapshot.toString());
                    if (userDataSnapshot.exists()) {
                        user = userDataSnapshot.getValue(User.class);
                        if (user.getImei() == null){
                            // user is logged out
                            Log.d(TAG,"No user not logged  in. Login the user");
                            Log.d(TAG,"No user not logged  in. Login the user");
                            crossValidateUserData();
                        }
                        else {
                            firebaseHelper.getDevicesDatabaseReference().child(user.getImei()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot deviceDataSnapshot) {
                                    Log.d("Device Data Snapshot:", deviceDataSnapshot.toString());
                                    if (deviceDataSnapshot.exists()) {
                                        if (user.getImei().equals(mImeiNumber)){
                                            crossValidateUserData();
                                        }
                                        else {
                                            uid = deviceDataSnapshot.getValue(String.class);
                                        /* User not logged out from old device
                                           prompt user to log out from old device
                                           */
                                            Log.d(TAG, "User is LoggedIn in other device");
                                            Toasty.error(MainActivity.this, "You are logged in another device .Please logout from old device to continue", Toast.LENGTH_SHORT, true).show();

                                           /* Toast.makeText(MainActivity.this,
                                                    "You are logged in another device .Please logout from old device to continue", Toast.LENGTH_LONG).show();
                                           */ LogOutUser();
                                        }
                                    }
                                    else {
                                        //log out the user
                                        firebaseHelper.firebaseSignOut(mImeiNumber);
                                        LogOutUser();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d(TAG,databaseError.getDetails());
                                    Toast.makeText(MainActivity.this, "Inside isEmailRegistered deviceDberror:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    pgbarhide();
                                }
                            });
                        }

                    } else {
                        user = null;
                        Log.d(TAG,"User is null");
                        pgbarhide();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    // Getting User failed, log a message
                    Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                    Toast.makeText(MainActivity.this, "Inside isEmailRegistered userDberror:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    pgbarhide();
                }
            });
        }
    }

    private void crossValidateUserData(){
        String email, password = null;
        String SignInType = userData.get("SignInType");

        if (SignInType.equals("email"))
            password = userData.get("password");
        email = userData.get("email");

        if (user != null) {
            //Log.d("password:", user.getPassword());
            if (!user.getEmail().equals(email)) {
                // Case1:Either email entered is invalid or different
                // Case2:prompt user that this device is stored under other user ...ask previous user to logout

                if (SignInType.equals("google")){
                    // logout user from google
                    firebaseHelper.googleSignOut(MainActivity.this);
                }
                pgbarhide();

            } else {
                if (SignInType.equals("email")) {
                    // Login the User through email
                    signIn(email, password);
                }
                else if (SignInType.equals("google")){
                    // login the user through google
                    initializeGoogleFirebaseSignIn();
                    GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                    if (acc != null)
                        googleFirebaseSignIn.firebaseAuthWithGoogle(acc);
                    else {
                        Toast.makeText(MainActivity.this, "Authentication failed. Try again", Toast.LENGTH_SHORT).show();
                        pgbarhide();
                    }
                }
            }
        } else {
            Log.d(TAG, "User:null");
            pgbarhide();
        }
    }


    /**Checks whether service is running or not**/
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
//        stopService(mSosPlayerIntent);
        Log.i("Mainactivity destroyed", "onDestroy!");
        super.onDestroy();

    }

    public void storeData(final FirebaseUser firebaseUser){
        if (firebaseUser != null) {
            final String uid = firebaseUser.getUid();
            firebaseHelper.getUsersDatabaseReference().child(uid).addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
//                    Log.d("Paid12345","schin1"+user.getName()+user.isPaid());
                    //db=new SQLiteDBHelper(MainActivity.this);
                    SQLiteDBHelper db=SQLiteDBHelper.getInstance(MainActivity.this);
                    try {
                        if (db.addUser(user)) {
                            if (user.getSosContacts() != null)
                                db.addsosContacts(user.getSosContacts(),1); //to fetch SOSContacts from Firebase
                            downloadProfilePic(uid);
                        }
                        else {
                            firebaseHelper.firebaseSignOut(mImeiNumber);
                            firebaseHelper.googleSignOut(MainActivity.this);
                            updateUI(null);
                            Toasty.error(MainActivity.this, "Authentication failed :sqlite error occurred",Toasty.LENGTH_LONG, true).show();
                            return;
                        }

                    }catch (Exception e){
                        Log.d(TAG,e.getMessage());
                        firebaseHelper.firebaseSignOut(mImeiNumber);
                        firebaseHelper.googleSignOut(MainActivity.this);
                        updateUI(null);
                        Toasty.error(MainActivity.this, "Sqlite error occurred MainAct:"+e.getMessage(),Toasty.LENGTH_LONG, true).show();
                        return;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG,databaseError.getDetails());
                    Toasty.error(MainActivity.this, "Inside storeData MainAct:"+databaseError.getMessage(), Toasty.LENGTH_LONG, true).show();
                    firebaseHelper.firebaseSignOut(mImeiNumber);
                    firebaseHelper.googleSignOut(MainActivity.this);
                    pgbarhide();
                }
            });
        }
    }

    private void downloadProfilePic(String uid){
        // display image from internal storage
        final InternalStorage internalStorage = InternalStorage.getInstance();
        internalStorage.initContext(MainActivity.this);
        File imgPath = internalStorage.getImagePathFromStorage(user.getEmail());
        try{
            BitmapFactory.decodeStream(new FileInputStream(imgPath));
            updateUI(FirebaseAuth.getInstance().getCurrentUser());
        }catch (IOException e){
            //Toast.makeText(getContext(), "Profile picture not found", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Profile picture not found");
            // download from firebase
            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference imgRef = storageReference.child(uid).child("images/profile_pic");
                Log.d(TAG,"imagePath"+imgRef.getPath()+imgRef.getParent());

                final long ONE_MEGABYTE = 1024 * 1024;
                imgRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        try {
                            internalStorage.saveImageToInternalStorage(bitmap, user.getEmail());
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Unable to store image",Toast.LENGTH_SHORT).show();
                        }
                        updateUI(FirebaseAuth.getInstance().getCurrentUser());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.d(TAG,"Profile pic doesn't exists");
                        updateUI(FirebaseAuth.getInstance().getCurrentUser());
                    }
                });
            }catch (Exception ec){
                Log.d(TAG, "error while downloading image:"+ec.getMessage());
            }
        }
    }

    private boolean checkPlayServices() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "checkPlayServices: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "checkPlayServices: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}