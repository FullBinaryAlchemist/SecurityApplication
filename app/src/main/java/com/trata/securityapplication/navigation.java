package com.trata.securityapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trata.securityapplication.Helper.FirebaseHelper;
import com.trata.securityapplication.model.User;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;



import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class navigation extends AppCompatActivity implements ForceUpdateChecker.OnUpdateNeededListener{

    int count=0,aa;
    static User newUser=UserObject.user;
    Boolean is_home=true;
    SQLiteDBHelper db;
    public static Boolean test=false;//NOTE: DO NOT CHANGE TO TRUE
    public static TextView tmode1;

    private int flag=0;
    private String TAG = "NavigatonFragment";
    private String mImeiNumber;
    private TelephonyManager telephonyManager;
    FirebaseRemoteConfig firebaseRemoteConfig;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //check the Location Access setting state
        checkLocationAccess();
        db=SQLiteDBHelper.getInstance(navigation.this);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListner);
        Intent intent=getIntent();
        boolean saviour=intent.getBooleanExtra("saviour",false);
        if(saviour)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_continer,new saviour_fragment()).commit();
            bottomNav.setSelectedItemId(R.id.save);
        }
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_continer,new home_fragment()).commit();
        }
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.initFirebase();
        firebaseHelper.initContext(this);

        Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //toolbar.setNavigationIcon(R.drawable.ic_toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        //toolbar.setLogo(R.drawable.ic_toolbar);
        
        ImageView logo = findViewById(R.id.imageView2);


        Animation animRotate = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.rotate);

        logo.startAnimation(animRotate);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent aboutus= new Intent(getApplicationContext(),AboutUs.class);
                startActivity(aboutus);

            }
        });


        async();
        //sqlite db code here
        Log.d("checking11","oncreate option menu 3 is running");
        Log.d("checking11", "oncreate "+db.getTestmode());


        if(db.getTestmode()) {
            Log.d("checking", "oncreate option menu 2 is running");
            test = true;
            flag = 1;
        }

        tmode1=(TextView)findViewById(R.id.testmode);
        if(flag==1)
        {
            tmode1.setText("TEST MODE : ON");
            tmode1.setTextColor(Color.GREEN);

        }

        tmode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag==0){
                    flag=1;
                    tmode1.setText("TEST MODE : ON ");
                    db.updatetestmode(true);
                    tmode1.setTextColor(Color.GREEN);
                    Log.d("checking11", "oncreate onc "+db.getTestmode());
                }
                else {
                    flag=0;
                    tmode1.setTextColor(Color.WHITE);
                    tmode1.setText("TEST MODE : OFF");
                    db.updatetestmode(false);

                }
                //update testmode value in db
                test= (flag==1); //using the global static variable instead of the local variable
                db.updatetestmode(test);
            }
        });

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
       // Log.d("remoteaaa","rohan"+firebaseRemoteConfig.getString("force_update_store_url"));
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
    }
    private void async() {
        checkFirstSosContact();
        if(db.get_user_row().getCount()==0){
            Log.d("iamrun","me21");
            //db.deleteDatabase(this.getApplicationContext());
            // Signout Code Here
            LogOutAndStartMainActivity();
        }
        if(db.getSosContacts().getCount()!=0) {
            UserObject.user=db.getdb_user();
            HashMap<String,String> sosContacts=UserObject.user.getSosContacts();
            Log.d("soscontactchecking1","c1"+ sosContacts.get("c1")+" c2: "+sosContacts.get("c2"));
            Log.d("soscontactchecking2",UserObject.user.toString());
            SendSMSService.initContacts(); //to initialise SOS Contacts as soon as the database is ready
        }

        Log.d("Paid1234hello11","userobj"+UserObject.user.isPaid()+db.getdb_user().getName());
        Log.d("Paid1234hello111","userobj2"+UserObject.print());

    }


    private void checkFirstSosContact(){
        // check if first sos contact is added
        SQLiteDBHelper mydb = db;
        Cursor res=mydb.getSosContacts();
        if (res.getCount() == 0){
            //Toast.makeText(getApplicationContext(), "No SOS Contact records Found", Toast.LENGTH_LONG).show();
            Log.d("SOS Activity","No Contact Data found ");
            Intent sosPage = new Intent(navigation.this, sos_page.class);
            sosPage.putExtra("btn","1");
            startActivityForResult(sosPage,1);
        }
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
                        Toast.makeText(this, "Test mode Off", Toast.LENGTH_SHORT).show();
                    } else {
                        item.setChecked(true);
                        test = true;
                        db.updatetestmode(test);
                        Toast.makeText(this, "Test mode On", Toast.LENGTH_SHORT).show();
                    }


                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        catch (Exception e)
        {
            item.setChecked(db.getTestmode());
            Toasty.warning(this, "Loading.....please wait for a second", Toast.LENGTH_LONG, true).show();

           // Toast.makeText(this, "Loading.....please wait for a second", Toast.LENGTH_LONG).show();
        }
        finally {
            return true;
        }

    }
    Fragment selectedFragment = null;
    private BottomNavigationView.OnNavigationItemSelectedListener navListner =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem){

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
                    try {

                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_continer,
                                selectedFragment).commit();
                    }
                    catch (Exception e){

                    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==10 && requestCode==1)
            try {
                finish();
            }catch (Exception e){
                Log.d(TAG,"Exception on closing activity:"+e.getMessage());
                finish();
            }
    }

    public void sos(View view) {
        Intent intent=new Intent(navigation.this,sos_page.class);
        intent.putExtra("btn","2");
        startActivity(intent);
    }

    public void LogOutAndStartMainActivity(){
        firebaseHelper.getUsersDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(User.class).getImei() != null)
                    firebaseHelper.makeDeviceImeiNull(mImeiNumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,"Imei not made null");
            }
        });
        firebaseHelper.firebaseSignOut();
        firebaseHelper.googleSignOut(navigation.this);
        //delete user records from SQLite
        //db.delete_table();
        db.deleteDatabase(this);

        Intent mLogOutAndRedirect= new Intent(navigation.this, MainActivity.class);
        mLogOutAndRedirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mLogOutAndRedirect);
        //finishing the navigation activity
        finish();
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue reposting.")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }


    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //Method to check the LocationAccess state of user's device
    private void checkLocationAccess() {
        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            ShowDialog();
        }
    }
    //display a dialog to user.
    private void ShowDialog() {
        new android.app.AlertDialog.Builder(this)
                .setMessage(R.string.dialog_message)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        //context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                })
                .setCancelable(false)
                .show();
    }

}