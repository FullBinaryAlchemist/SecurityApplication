package com.trata.securityapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapviewlite.MapImage;
import com.here.sdk.mapviewlite.MapImageFactory;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapMarkerImageStyle;
import com.here.sdk.mapviewlite.MapScene;
import com.here.sdk.mapviewlite.MapStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import com.trata.securityapplication.Helper.FirebaseHelper;
import com.trata.securityapplication.model.AlertDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

import static java.lang.Thread.sleep;

public class recent_cards extends AppCompatActivity implements UpdateSaviourCountCallback{
    private static final String TAG = "RecentCards";
    private MapViewLite mapView;
    // private MapStyle mapStyle;
    Context context = this;
    private TextView name;
    public static double saviourLongitude;
    public static double saviourLatitude;
    public static double victimLongitude;
    public static double victimLatitude;
    private SharedPreferences sharedPreferences;
    public static double alertLongitude;
    public static double alertLatitude;

    ImageView profile_image;



    SQLiteDBHelper mydb = SQLiteDBHelper.getInstance(this);
    GeoCoordinates geoCoordinatesAlert;
    GeoCoordinates geoCoordinatesSaviour;
    GeoCoordinates geoCoordinatesLastLocation;
    LinearLayout linearLayout;
    BottomSheetBehavior bottomSheetBehavior;
    String zone,sub_zone,user_location,victim_location,distance_inbetween;
    private int distance_integer=1000;
    private Routing routing;
    private boolean image_downloaded=false;
    private Bitmap bitmap_profile;
    Timer timer = new Timer();
    TimerTask task,task2,task3;
    private AlertDetails ad;
    MapMarker mapMarkerAlert;
    MapImage mapImageAlert;
    boolean m_move=false,show_image=false;
    public static void setSaviourLocation(double latitude, double longitude) {
        saviourLatitude = latitude;
        saviourLongitude = longitude;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_cards);
        profile_image=findViewById(R.id.imageView3);
        View nestedScrollView = (View) findViewById(R.id.nestedScrollView);
        sharedPreferences=getSharedPreferences("TRATA",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("move",false);
        editor.commit();
        bottomSheetBehavior = BottomSheetBehavior.from(nestedScrollView);
        String uid=getIntent().getStringExtra("uid");
        HashMap<String, AlertDetails> detail=AlertObjects.getAllAlerts();
        ad=detail.get(uid);
        //updateLocation(ad.getLocation());
        String victimLocation = ad.getLocation();
        Log.d("victimLocation","---"+victimLocation);
        AlertDetails ad=detail.get(uid);
        StorageReference image_ref=ad.getImageUrl();

        image_ref.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                //profile_image.setImageBitmap(bitmap);
                bitmap_profile=bitmap;
                image_downloaded=true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                String state = "";

                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING: {
                        state = "DRAGGING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_SETTLING: {
                        state = "SETTLING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        state = "EXPANDED";
                        break;
                    }
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        state = "COLLAPSED";
                        break;
                    }
                    case BottomSheetBehavior.STATE_HIDDEN: {
                        state = "HIDDEN";

                        break;
                    }
                }

                Log.d("bottomsachin",state);
                if(state.equals("COLLAPSED"))
                    bottomSheetBehavior.setPeekHeight(90);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("bottomsachin","onslide");
            }
        });
        /** Get a MapViewLite instance from the layout.*/
        mapView = findViewById(R.id.map_view);
        name = findViewById(R.id.name);
        mapView.onCreate(savedInstanceState);
        name.setText(ad.getName());
        checkGPSPermission();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                Log.d("run","loading Map Scene");
                loadMapScene();

            }
        }, 3000);

        /**Setting Event listeners for accept and reject **/
        Button accept= findViewById(R.id.accept);
        Button decline= findViewById(R.id.decline);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                addSaviourToAlert(ad.getUid());
                EmergencyMessagingService.subscribeTopic("saviours_"+ad.getUid()); //subscribing to topic saviours_#uidVictim to receive live location updates
                //TODO:Remove reject option and updateUI
                accept.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
            }
        });
        //TODO: Remove the alertDetail object and key from AlertObjects . Update the history of the user on firebase and in local database:SEEN
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Cancel the timertask*/
                task.cancel();
                EmergencyMessagingService.unsubscribeTopic("saviours_"+ad.getUid()); //in-case the user got subscribed to topic->unsubscribe them
                //removing the AlertDetail object form AlertObjects
                AlertObjects.getAllAlerts().remove(uid);
                Log.d("saviour"," Alert#"+uid+" was removed from the alerts hashmap");
                //update history
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String strDate= formatter.format(date);
                mydb.addhistory(ad.getName(),strDate);
                //close this activity and recycler view should be updated
                Intent intent=new Intent(recent_cards.this,navigation.class);
                intent.putExtra("saviour",true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });

        EmergencyMessagingService.setUpdateSaviourCountCallback(this);
        task2 = new TimerTask() {
            @Override
            public void run() {
                if(sharedPreferences.getBoolean("move",false) && m_move)
                {
                    Log.d("recent_cards","move to saviour");
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String strDate= formatter.format(date);
                    mydb.addhistory(ad.getName(),strDate);
                    Intent intent=new Intent(recent_cards.this,navigation.class);
                    intent.putExtra("saviour",true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else {
                    Log.d("recent_cards","not move to saviour");
                }
            }
        };
        timer.schedule(task2,500, 10000);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseHelper.getInstance().getAlertsDatabaseReference().child(ad.getUid()).child("saviours").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                    // run some code
                    Log.d("recent_cards","saviour");
                    accept.setVisibility(View.GONE);
                    decline.setVisibility(View.GONE);
                }else {
                    Log.d("recent_cards"," not saviour");
                 Log.d("recent_cards",FirebaseAuth.getInstance().getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        new Thread() {
            boolean threa_image=true;
            public void run() {
                while (threa_image) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(show_image)
                                    {
                                        Log.d("innnnn","showed_thread");
                                        profile_image.setImageBitmap(bitmap_profile);
                                        threa_image=false;

                                    }
                                Log.d("innnnn","showed_thread_not image found");
                            }
                        });
                        Thread.sleep(30*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("innnnn","showed_thread_not image found2");
            }

        }.start();
    }
    //adds Saviour to alerts/{uid}/saviours node on Firebase
    public void addSaviourToAlert(final String uid /*Victim uid*/){
        String ts = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());//timestamp at which request is accepted
        String useruid= FirebaseHelper.getInstance().getFirebaseAuth().getUid();//Gets current user uid
        FirebaseHelper.getInstance().getAlertsDatabaseReference().child(uid).child("saviours").child(useruid).setValue(ts)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("Saviour","Saviour added to firebase for alert uid#"+uid);
                            try{
                                Toasty.success(getApplicationContext(),"Saviour added to firebase for alert uid#"+uid,Toasty.LENGTH_LONG).show();
                            }
                            catch(Exception e){
                                Log.d("toasty",e.getMessage());
                            }
                        }

                        else{
                            AlertObjects.getAllAlerts().remove(uid);
                            Log.d("saviour"," Alert#"+uid+" was removed from the alerts hashmap");
                            //update history
                            Date date = new Date();
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            String strDate= formatter.format(date);
                            mydb.addhistory(ad.getName(),strDate);
                            //close this activity and recycler view should be updated
                            Intent intent=new Intent(recent_cards.this,navigation.class);
                            intent.putExtra("saviour",true);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            Log.d("Saviour","Saviour NOT added to firebase for alert Uid#"+uid);
                            Toasty.error(getApplicationContext(), "Saviour NOT added to firebase for alert Uid#"+uid, Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
    }

    void checkGPSPermission() {
        Log.d("MainActivity", "Inside CheckGPSPermission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {   //permissions not granted
            final int d = Log.d("GPS Access in Main", "Requesting GPS Location");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 102);
        } else {
            //permissions granted
            ContextCompat.startForegroundService(this,new Intent(this,GetGPSCoordinates.class));
//            GpsPermission = true;
        }
    }

    public  void loadMapScene() {

        Log.d("loadMapScene","coordinates  "+ saviourLatitude +","+ saviourLongitude);
        geoCoordinatesAlert = new GeoCoordinates(victimLatitude, victimLongitude); //End point
        geoCoordinatesSaviour = new GeoCoordinates(saviourLatitude, saviourLongitude);//Start point

        // Load a scene from the SDK to render the map with a map style.

        mapView.getMapScene().loadScene(MapStyle.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(MapScene.ErrorCode errorCode) {
                if (errorCode == null) {
                    //Current location view
                    mapView.getCamera().setTarget(geoCoordinatesSaviour);
                    mapView.getCamera().setZoomLevel(14);
                    //Show the marker on map

                    MapMarkerImageStyle mapMarkerImageStyle = new MapMarkerImageStyle();
                    mapMarkerImageStyle.setAnchorPoint(new Anchor2D(0.5F, 1)); //Anchors the marker by the middle to the location
                    mapMarkerImageStyle.setScale(0.06F);                               //Scales the drawable image

                    //Do something after 100ms
                    //handler.postDelayed(this, 10000);
                     task = new TimerTask() {
                        @Override
                        public void run() {
                                //TODO:update geoCoordinates for victim and saviour and
                                // Fetch the image of victim if distance is less than 100m and UpdateUI
                                ad = AlertObjects.getAlert(ad.getUid());
                                if(ad==null)
                                {
                                    task.cancel();
                                    return;

                                }
                                user_location=GetGPSCoordinates.getddLastKnownLocation();
                                victim_location=ad.getLocation();
                                if(user_location!=null && victim_location!=null)
                                {
                                    distance_inbetween=calculatedistance(user_location,victim_location);
                                    distance_integer=Integer.valueOf(distance_inbetween);
                                    Log.d("distance_integer",distance_inbetween);
                                    if(distance_integer<250 && image_downloaded){
                                        //profile_image.setImageBitmap(bitmap_profile);
                                        show_image=true;
                                    }

                                }


                                updateLocation(ad.getLocation());

                                Log.d("run","loading Map Scene");
                                loadMarker(mapMarkerImageStyle);
                        }
                    };
                    timer.schedule(task,500, 60000);

                    //Calls the Routing Class
                    routing = new Routing(recent_cards.this, mapView,geoCoordinatesAlert, geoCoordinatesSaviour);


                } else {
                    Log.d(TAG, "onLoadScene failed: " + errorCode.toString());
                }
            }
        });

    }


    void loadMarker(MapMarkerImageStyle mapMarkerImageStyle){
        //Update the marker

         mapImageAlert = MapImageFactory.fromResource(context.getResources(),R.drawable.alert);
         mapMarkerAlert = new MapMarker(geoCoordinatesAlert);

        MapImage mapImageSaviour = MapImageFactory.fromResource(context.getResources(),R.drawable.saviour);
        MapMarker mapMarkerSaviour = new MapMarker(geoCoordinatesSaviour);

        mapMarkerAlert.addImage(mapImageAlert, mapMarkerImageStyle);
        mapView.getMapScene().addMapMarker(mapMarkerAlert);
        mapMarkerSaviour.addImage(mapImageSaviour, mapMarkerImageStyle);
        mapView.getMapScene().addMapMarker(mapMarkerSaviour);

        routing.addRoute(geoCoordinatesAlert,geoCoordinatesSaviour);

    }


    /**Loads the Route between given co-ordinates*/
 /**   public void addRouteButtonClicked(View view) {
        routing.addRoute(geoCoordinatesAlert,geoCoordinatesSaviour);

    }
    /**Clears the route, marker still present on map*/
 /**   public void clearMapButtonClicked(View view) {
        routing.clearMap();
    }*/



    @Override
    protected void onPause() {
        m_move=false;

        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        m_move=true;
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        task2.cancel();
        super.onDestroy();
        mapView.onDestroy();
    }

    public void updateLocation(String location){

        //Remove previous markers

        removeMarker();
        //Update geocordinatesalert object
        String victimLocation = location;
        Log.d("updateLocation","victimLocation"+victimLocation);
        String[] victimCoordinates = victimLocation.split(",");
         victimLatitude = Double.parseDouble(victimCoordinates[0]);
         victimLongitude = Double.parseDouble(victimCoordinates[1]);
        geoCoordinatesAlert = new GeoCoordinates(victimLatitude,victimLongitude);
        Log.d("updateLocation","geoCoordinatesAlert"+geoCoordinatesAlert);
        //Update geocoordinatessaviour object

        geoCoordinatesSaviour = new GeoCoordinates(saviourLatitude, saviourLongitude);
        Log.d("updateLocation","geoCoordinatesSaviour"+geoCoordinatesSaviour);

        //routing.clearMap();
    }

    private void removeMarker() {
        mapView.getMapScene().removeMapMarker(mapMarkerAlert);
        routing.clearMap();
    }

    public void updateSaviourCount(String newText){
        Log.d(TAG,"updateSaviourCount callback");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView counttext=findViewById(R.id.saviourCountText);
                //String newText="Number of Saviours coming to rescue:"+ad.getSaviourcount();
                Log.d(TAG,newText);
                counttext.setText(newText.toCharArray(),0,newText.toCharArray().length);
                Log.d(TAG,"Get text on saviourCount:"+counttext.getText().toString());
                //counttext.setText(newText.toString());
            }
        });

    }
    private String calculatedistance(String location,String user_location){
        List<String> t_loc = Arrays.asList(location.split(","));
        List<String> u_loc = Arrays.asList(user_location.split(","));
        Location targetLocation = new Location("");//provider name is unnecessary
        targetLocation.setLatitude(Double.valueOf(t_loc.get(0)));//your coords of course
        targetLocation.setLongitude(Double.valueOf(t_loc.get(1)));
        Location myLocation = new Location("");//provider name is unnecessary
        myLocation.setLatitude(Double.valueOf(u_loc.get(0)));//your coords of course
        myLocation.setLongitude(Double.valueOf(u_loc.get(1)));
        float distanceInMeters =  targetLocation.distanceTo(myLocation);
        Log.d("distance_integer",(int)distanceInMeters+" ");
        return String.valueOf((int) distanceInMeters);
    }


}
