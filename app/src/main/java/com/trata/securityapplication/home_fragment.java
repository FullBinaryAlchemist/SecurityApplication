package com.trata.securityapplication;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.trata.securityapplication.Helper.FirebaseHelper;
import com.trata.securityapplication.model.Alert;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static com.trata.securityapplication.navigation.test;

public class home_fragment extends Fragment {

//    private static FirebaseHelper ;
    public Button alert;
    public Button emergency;
    public Button informsafety;
    static public boolean check=false;
    int RC;
    Boolean is_paid = false;//NOTE: DO NOT CHANGE TO TRUE
    private static Alert alertobj;//This is Model Object to push to firebase
    private static FirebaseHelper firebaseHelper= FirebaseHelper.getInstance();
    //NOTE: Button bt has been removed. Now using Button emergency. Event listeners also moved to emergency
    private static boolean alertExists=false;
    public static int testcount=0; //store the count of saviours when emergency pressed in testmode
    public static Context context;
    static String ts; //NOTE:was earlier in timestamp function
    Context c2;
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context=getActivity().getApplicationContext();
        alert = Objects.requireNonNull(getActivity()).findViewById(R.id.alert);
        emergency = getActivity().findViewById(R.id.emergency);
        informsafety = getActivity().findViewById(R.id.inform);

        firebaseHelper= FirebaseHelper.getInstance(); //NOTE:Added FirebaseHelper

        final android.support.v7.widget.Toolbar toolbar1 = (Toolbar) getActivity().findViewById(R.id.toolbar);





        Log.d("Paid1234hello2","paid: "+UserObject.user.isPaid());
        if(UserObject.user.isPaid()){
            is_paid=true;
        }
        else{
            is_paid=false;
        }
        Log.d("Paid1234hello2","ispaid: "+is_paid);

        alert.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                Animation alert_anim=AnimationUtils.loadAnimation(getContext(),R.anim.btn_anim);
                alert.startAnimation(alert_anim);
                if(!checkSMSPermission()) {

                }
                else
                {
                    Toasty.error(getContext(), "sms permisssion not enabled", Toast.LENGTH_SHORT);

                    //Toast.makeText(getContext(),"sms permisssion not enabled",Toast.LENGTH_LONG);
                }

                check=true;

                Intent mSosPlayerIntent = new Intent(getContext(), SendSMSService.class);
                mSosPlayerIntent.putExtra("alert",1);


                if (!isMyServiceRunning(SendSMSService.class)){
                    Objects.requireNonNull(getContext()).startService(mSosPlayerIntent);

                }
            }
        });

        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.btn_anim);
                emergency.startAnimation(myAnim);
                if (is_paid || true) { //temporarily made ALWAYS TRUE

                    //Toasty.info(getContext(), "You are Premium member", Toast.LENGTH_SHORT, true).show(); //Commented-out

                    //Code: TO play siren and send emergency message and alert
                    //emergency.setBackgroundColor(getResources().getColor(R.drawable.buttonshape_emer));
                    final Context c2 = getContext();
                    check=true;

                    //change the interval to 1 min.
                    GetGPSCoordinates.speedyLocationRequest();

                    Intent emergencyintent1=new Intent(getContext(), BackgroundSosPlayerService.class);

                    if (c2 != null) {
                        c2.startService(emergencyintent1);
                    }

                    Intent emergencyintent2 = new Intent(getContext(), SendSMSService.class);
                    emergencyintent2.putExtra("emergency",1);
                    assert c2 != null;
                    c2.startService(emergencyintent2);

                    final String uid=firebaseHelper.getFirebaseAuth().getUid();
                    EmergencyMessagingService.subscribeTopic("victim_"+uid);

                    //check if not test mode.Otherwise don't raise entry on firebase
                    if(!navigation.test){
                        try {
                            startAlertCreation(uid);
                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("home_fragment","startAlertCreation error");
                        }
                    }
                    //Showing testmode count
                    else {
                        sendTestMessage(firebaseHelper.getFirebaseAuth().getUid());
                    }

                } else {
                    //if user using free services only
                    new AlertDialog.Builder(getContext()).setMessage("Upgrade to Premium to Use this function")
                            .setPositiveButton("Purchased", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                        intent.setData(Uri.parse("http://www.w3schools.com"));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        Toasty.error(getContext(), "Something went wrong", Toast.LENGTH_SHORT, true).show();

                                      //  Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).setNegativeButton("Cancel", null).setCancelable(false).create().show();
                }
            }
        });

        informsafety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation inf_anim=AnimationUtils.loadAnimation(getContext(),R.anim.btn_anim);
                informsafety.startAnimation(inf_anim);

                //unsubscribe after inform safety has been pressed. This way Saviour count will no longer be received. So notification will be cancelled
                String uid=firebaseHelper.getFirebaseAuth().getUid();
                EmergencyMessagingService.unsubscribeTopic("victim_"+uid);
                NotificationManager notificationManager =
                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(999); //Cancelling the SaviourCount notification

                resetTestCount();

                //TODO:check if Alert node exists and it does then delete it 
                exists(uid,"",false);

                try {

                    if (SendSMSService.getAlert() == 0 && SendSMSService.getEmergency() == 0) {
                        Toasty.warning(getContext(), "Emergency/Alert Not Raised", Toast.LENGTH_SHORT,true).show();  //changed to warning-toasty from toast
                    }



                else{
                        //reset the interval to 10 mins.
                        GetGPSCoordinates.resetLocationRequest();

                        Context c3 = getContext();

                        Intent stopsms = new Intent(getContext(), SendSMSService.class);
                        stopsms.putExtra("safe", 1);
                        if (c3 != null) {
                            c3.startService(stopsms);
                        }


                        if (isMyServiceRunning(SendSMSService.class)) {


                            if (c3 != null) {
                                c3.stopService(stopsms);
                            }
                        }

                        if (isMyServiceRunning(BackgroundSosPlayerService.class)) {
                            //stopping the sosplay variable and resetting count in SosPlayer.java
                            SosPlayer.stopPlaying();

                            Intent stopemergency = new Intent(getContext(), BackgroundSosPlayerService.class);
                            if (c3 != null) {
                                c3.stopService(stopemergency);

                                check = false;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Toasty.warning(getContext(), "Emergency/Alert Not Raised", Toast.LENGTH_SHORT,true).show();  //changed to warning-toasty from toast

                    Log.d("home_fragment", "catch raised");
                }

                //TODO:Check if emergency was created on Firebase .If it was then , delete the entry and update history


            }
        });

//        if (navigation.test) {
//
//            TextView tv = getActivity().findViewById(R.id.textView3);
//            tv.setVisibility(View.VISIBLE);
//        } else {
//            TextView tv = getActivity().findViewById(R.id.textView3);
//            tv.setVisibility(View.INVISIBLE);
//        }



    }

    public  boolean checkSMSPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            Toasty.info(getContext(), "Permission Required for sending SMS in case of SOS", Toast.LENGTH_SHORT, true).show();

           // Toast.makeText(getContext(), "Permission Required for sending SMS in case of SOS", Toast.LENGTH_LONG).show();
            Log.d("MainActivity", "PERMISSION FOR SEND SMS NOT GRANTED, REQUESTING PERMSISSION...");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.SEND_SMS}, RC);
            return false;// added return false
        }
        final boolean b = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        return b;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        Context c1 = getContext();
        ActivityManager manager = (ActivityManager) c1.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
    public static void timeStamp(String uid, String location, String ts){
//        String location=GetGPSCoordinates.getddLastKnownLocation();
        Log.d("alert_history","Alert History working    "+uid);
        ts= TextUtils.join(":", Arrays.asList(ts.split("\\.")));

        firebaseHelper.getUsersDatabaseReference().child(uid).child("alert_history").child(ts).setValue(location)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("home_fragment_timestamp","Firebase:TimeStamp added in Firebase");
                            try{
                            Toasty.success(context,"Alert added in FIrebase",Toasty.LENGTH_LONG).show();
                          }
                            catch(Exception e){
                                Log.d("toasty",e.getMessage());
                            }
//                            timeStamp(uid,ddLastKnownLocation);
                        }

                        else{
                            Log.d("home_fragment_timestamp","Firebase:TimeStamp NOT ADDED in Firebase");
                            Toasty.error(context, "Firebase entry failed", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
        ;
    }

    public static void exists(String uid, String ddLastKnownLocation,boolean createOrdelete /**Adds if true . Delete if false*/){
        Log.d("Exists","Calling Exists..................");
        firebaseHelper.getAlertsDatabaseReference().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Log.d("Exists", "Return True");
                    setAlertExists();
                    if(!createOrdelete){
                        deleteAlert(uid);
                    }
                } else {
                    resetAlertExists();
                    if(createOrdelete){
                        addAlert(uid, ddLastKnownLocation);
                    }
                    Log.d("Not Exists", "Return False");
                    resetAlertExists();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void startAlertCreation(String uid) throws NullPointerException{
        String formattedSubZone= GetGPSCoordinates.getFormattedZoning(GetGPSCoordinates.getSub_zone());
        final String ddLastKnownLocation =GetGPSCoordinates.getddLastKnownLocation(); //for Location
        ts = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());//timestamp NOTE:was earlier in timestamp function

        Log.d("home_fragment","Emergency: formattedSubZone:"+formattedSubZone+"\nuid:"+uid+"\nlocation:"+ddLastKnownLocation);
        alertobj= new Alert();
        alertobj.setLocation(ddLastKnownLocation);
        alertobj.setSubzone(formattedSubZone);
        alertobj.setTs(ts);
        //Temporary code to test saviours live location update
        EmergencyMessagingService.subscribeTopic("saviours_"+uid); //TODO:Remove after Saviour fragment complete
        //TODO:check whether an Emergency has already been raised by User. If there already exists then don't create another entry
        Log.d("Exists","Calling Calling Exists..................");
        exists(uid,ddLastKnownLocation,true);//creates
    }

    public static void addAlert(final String uid, final String ddLastKnownLocation){
        try {
            firebaseHelper.getAlertsDatabaseReference().child(uid).setValue(alertobj)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("home_fragment","Firebase:Alert added in Firebase");
                                Toasty.success(context,"Alert added in FIrebase",Toasty.LENGTH_LONG).show();
                                timeStamp(uid,ddLastKnownLocation,ts); //function definition changed to include Timestamp as well
                                setAlertExists();//sets Alert exists to true
                            }
                            else{
                                Log.d("home_fragment","Firebase:Alert NOT ADDED in Firebase");
                                Toasty.error(context, "Firebase entry failed", Toast.LENGTH_SHORT, true).show();
                            }
                        }
                    });
        }catch (Exception e){
            Log.d("home_fragment","Emergency creation on firebase failed");
            e.printStackTrace();
            Toasty.error(context, "Emergency creation on firebase failed"+e.getMessage(), Toast.LENGTH_SHORT, true).show();

        }
    }

    public static void deleteAlert(final String uid){
        try {
            firebaseHelper.getAlertsDatabaseReference().child(uid).setValue(null)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                resetAlertExists(); //Reset the alertExists boolean variable
                                Log.d("home_fragment","Firebase:Alert Removed from Firebase");
                                Toasty.success(context,"Alert Removed from FIrebase",Toasty.LENGTH_LONG).show();
                            }
                            else{
                                Log.d("home_fragment","Firebase:Alert NOT REMOVED in Firebase");
                                Toasty.error(context, "Firebase deletion failed", Toast.LENGTH_SHORT, true).show();
                            }
                        }
                    });
        }catch (Exception e){
            Log.d("home_fragment","Emergency creation on firebase failed");
            e.printStackTrace();
            Toasty.error(context, "Emergency creation on firebase failed"+e.getMessage(), Toast.LENGTH_SHORT, true).show();

        }
    }
    //TODO:create an HTTP endpoint using cloud function and call that endpoint
    public void sendTestMessage(final String uid){
        Log.d("TestMessage","Will send a test mode message");
        String topic=EmergencyMessagingService.getTopicString(GetGPSCoordinates.getZone(),GetGPSCoordinates.getSub_zone());
        String params="?callerUid="+uid+"&targetUid="+uid+"&topic="+topic;
        Log.d("TestMessage","params:"+params);

        try {
            EmergencyMessagingService.callUrl(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean getAlertExists(){ return alertExists; }
    public static void setAlertExists(){ alertExists=true; }
    public static void resetAlertExists(){alertExists=false;}

    public static void incrementTestCount(){ Log.d("incrementTestCount","Testcount:"+(++testcount)); }
    public static void resetTestCount(){testcount=0;}
    public static int getTestCount(){return testcount;}
}


