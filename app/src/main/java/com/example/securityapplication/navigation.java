package com.example.securityapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securityapplication.model.Device;
import com.example.securityapplication.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.facebook.FacebookSdk.getApplicationContext;

public class navigation extends AppCompatActivity {

    int count=0;
    static User newUser=new User();
    Boolean is_home=true;

    SQLiteDBHelper db=new SQLiteDBHelper(navigation.this);
    public static Boolean test=false;

    Menu optionsMenu;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mDevicesDatabaseReference;
    private User user;
    private Device device;
    private String TAG = "NavigatonFragment";
    private String mImeiNumber;
    private TelephonyManager telephonyManager;

    private ValueEventListener mUsersDatabaseReferenceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListner);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_continer,new home_fragment()).commit();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        // initialise database references
        initDataBaseReferences();

        //sqlite db code here
        Log.d("cchecking","Oncreate : Loaded"+is_home);
        if(db.numberOfRows()==0)
            getData(1);
        else
        {
            Log.d("checking","oncreate option menu 3 is running");
            getData(2);
            if(db.getTestmode())
            {
                Log.d("checking","oncreate option menu 2 is running");
                test=true;
            }
            }



    }

    private void initDataBaseReferences(){
        //Initialize Database references
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        mDevicesDatabaseReference = mFirebaseDatabase.getReference().child("Devices");
    }

    public void getData(final int check){
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference();
            mUsersDatabaseReferenceListener = databaseReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    newUser = dataSnapshot.getValue(User.class);
                    // check if user signed in from two devices
                    if (FirebaseAuth.getInstance().getCurrentUser() != null)
                        if (dataSnapshot.getValue(User.class).getImei() != "null")
                            recheckUserAuthentication(FirebaseAuth.getInstance().getCurrentUser());

                    if (check == 1) {
                        db.addUser(newUser);
                        Log.d("FirebaseUsername", newUser.getName() + " 1 " + newUser.getEmail());
                    } else if (check == 2) {
                        Log.d("FirebaseUsername", newUser.getName() + " 2 " + newUser.getEmail());
                        db.updateUser(newUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);
        optionsMenu=menu;
        MenuItem titem=optionsMenu.findItem(R.id.testmode);
        test=db.getTestmode();
        Log.d("checking","oncreate option menu is running"+db.getTestmode());
        if(test)
            titem.setChecked(true);
        else
            titem.setChecked(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.testmode:
                    if (item.isChecked()) {
                        item.setChecked(false);
                        test = false;
                        db.updatetestmode(test);
                        //Log.d("checking1", String.valueOf(db.getTestmode()) + "home" + is_home);
                        Toast.makeText(this, "Test mode Off", Toast.LENGTH_SHORT).show();
                        if (is_home) {
                            TextView tv = (TextView) findViewById(R.id.textView3);
                            tv.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        item.setChecked(true);
                        test = true;
                        db.updatetestmode(test);
                        //Log.d("checking2", String.valueOf(db.getTestmode()));
                        Toast.makeText(this, "Test mode On", Toast.LENGTH_SHORT).show();
                        if (is_home) {
                            TextView tv = (TextView) findViewById(R.id.textView3);
                            tv.setVisibility(View.VISIBLE);
                        }
                    }


                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        catch (Exception e)
        {
            item.setChecked(db.getTestmode());
            Toast.makeText(this, "Loading.....please wait for a second", Toast.LENGTH_LONG).show();
        }
        finally {
            return true;
        }

    }
    Fragment selectedFragment = null;
    private BottomNavigationView.OnNavigationItemSelectedListener navListner =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch(menuItem.getItemId()){
                        case R.id.home:
                            is_home=true;
                            selectedFragment = new home_fragment();
                            break;
                        case R.id.setting:
                            is_home=false;
                            selectedFragment = new setting_fragment();
                            break;
                        case R.id.save:
                            is_home=false;
                            selectedFragment = new saviour_fragment();
                            break;
                        case R.id.profile:
                            is_home=false;
                            selectedFragment = new profile_fragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_continer,
                            selectedFragment).commit();

                    return true;
                }
            };
    @Override
    public void onBackPressed(){
        AlertDialog.Builder a_builder = new AlertDialog.Builder(navigation.this);
        a_builder.setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alert = a_builder.create();
        alert.setTitle("Message");
        alert.show();
    }

    public void sos(View view) {
        startActivity(new Intent(this,sos_page.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImei();
                } else {
                    closeNow();
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void closeNow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            finishAffinity();
        }
        else{
            finish();
        }
    }

    private void recheckUserAuthentication(final FirebaseUser firebaseUser){
        Log.d(TAG,FirebaseAuth.getInstance().getCurrentUser().getEmail());
        Log.d(TAG,firebaseUser.getEmail());
        Log.d(TAG,"Inside recheckUserAuthentication");
        getImei();
        mUsersDatabaseReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                Log.d("User Data Snapshot:", userDataSnapshot.toString());
                if (userDataSnapshot.exists()) {
                    user = userDataSnapshot.getValue(User.class);
                    Log.d(TAG,"Imei of device:"+mImeiNumber);
                    Log.d(TAG,"Imei from firebase:"+user.getImei());
                    if (!user.getImei().equals(mImeiNumber)){
                        // same user trying to login from multiple devices -> logout the user
                        Log.d(TAG, "User is LoggedIn in other device");
                        //Toast.makeText(navigation.this,"You are logged in another device .Please logout from old device to continue", Toast.LENGTH_LONG).show();
                        signOut();
                    }
                    else{/* nothing to do*/}
                } else {/* this should not be the case*/}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getImei(){
        telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
            return;
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mImeiNumber = telephonyManager.getImei(0);
                Log.d("IMEI", "IMEI Number of slot 1 is:" + mImeiNumber);
            } else {
                mImeiNumber = telephonyManager.getDeviceId();
            }
        }
    }

    private void signOut(){

        // first make uid under imei null in Devices and imei under uid null in Users
        device = new Device();
        device.setUID("null");
        mDevicesDatabaseReference.child(mImeiNumber).setValue(device);

        //Firebase signOut
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out from Firebase", Toast.LENGTH_SHORT).show();
        }
        mUsersDatabaseReference.removeEventListener(mUsersDatabaseReferenceListener);
        //Google signOut
        /*if(GoogleSignIn.getLastSignedInAccount(this) != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //updateUI(null);
                            //Toast.makeText(MainActivity.this,"Logged Out from Google",Toast.LENGTH_SHORT).show();
                        }
                    });
        }*/
        //Clear the back stack and re-directing to the sign-up page
        Intent mLogOutAndRedirect= new Intent(getApplicationContext(),MainActivity.class);
        mLogOutAndRedirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mLogOutAndRedirect);
        //finishing the navigation activity
        try {
            closeNow();
            Log.d(TAG,"closed activity successfully");
        }catch (Exception e){
            Log.d(TAG,"Closing app exception:"+e.getMessage());
            finish();
        }
    }
}
