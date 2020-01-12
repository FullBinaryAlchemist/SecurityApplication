package com.trata.securityapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

public class recent_cards extends AppCompatActivity {
    private static final String TAG = "HereMaps";
    private MapViewLite mapView;
    // private MapStyle mapStyle;
    Context context = this;
    private TextView name;
    public static double alertLongitude;
    public static double alertLatitude;

    GeoCoordinates geoCoordinatesAlert;
    GeoCoordinates geoCoordinatesSaviour;

    String zone,sub_zone;
    private Routing routing;
    Timer timer = new Timer();
    public static void setLatitude(double latitude) {
        alertLatitude = latitude;
    }

    public static void setLongitude(double longitude) {
        alertLongitude = longitude;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_cards);
        String uid=getIntent().getStringExtra("uid");
        HashMap<String, AlertDetails> detail=AlertObjects.getAllAlerts();
        AlertDetails ad=detail.get(uid);

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
                addSaviourToAlert(ad.getUid());
                EmergencyMessagingService.subscribeTopic("saviours_"+ad.getUid()); //subscribing to topic saviours_#uidVictim to receive live location updates
                //TODO:Remove reject option and updateUI
            }
        });
        //TODO: Remove the alertDetail object and key from AlertObjects . Update the history of the user on firebase and in local database
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmergencyMessagingService.unsubscribeTopic("saviours_"+ad.getUid()); //in-case the user got subscribed to topic->unsubscribe them
                //removing the AlertDetail object form AlertObjects
                AlertObjects.getAllAlerts().remove(uid);
                Log.d("saviour"," Alert#"+uid+" was removed from the alerts hashmap");
                //update history

                //close this activity and recycler view should be updated
                finish();

            }
        });
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

        Log.d("loadMapScene","coordinates  "+alertLatitude+","+alertLongitude );
        geoCoordinatesAlert = new GeoCoordinates(alertLatitude, alertLongitude); //End point
        geoCoordinatesSaviour = new GeoCoordinates(19.404046299999998, 72.8284918);//Start point

        // Load a scene from the SDK to render the map with a map style.

        mapView.getMapScene().loadScene(MapStyle.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(MapScene.ErrorCode errorCode) {
                if (errorCode == null) {
                    //Current location view
                    mapView.getCamera().setTarget(geoCoordinatesSaviour);
                    mapView.getCamera().setZoomLevel(14);
                    //Show the marker on map
                    /**   MapImage mapImageAlert = MapImageFactory.fromResource(context.getResources(),R.drawable.alert);
                     MapMarker mapMarkerAlert = new MapMarker(geoCoordinatesAlert);
                     MapImage mapImageSaviour = MapImageFactory.fromResource(context.getResources(),R.drawable.saviour);
                     MapMarker mapMarkerSaviour = new MapMarker(geoCoordinatesSaviour); */

                    MapMarkerImageStyle mapMarkerImageStyle = new MapMarkerImageStyle();
                    mapMarkerImageStyle.setAnchorPoint(new Anchor2D(0.5F, 1)); //Anchors the marker by the middle to the location
                    mapMarkerImageStyle.setScale(0.06F);                               //Scales the drawable image

                    /**  mapMarkerAlert.addImage(mapImageAlert, mapMarkerImageStyle);
                     mapView.getMapScene().addMapMarker(mapMarkerAlert);
                     mapMarkerSaviour.addImage(mapImageSaviour, mapMarkerImageStyle);
                     mapView.getMapScene().addMapMarker(mapMarkerSaviour);*/

                    /**   final Handler handler = new Handler();
                     handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {*/
                    //Do something after 100ms
                    //handler.postDelayed(this, 10000);
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            //TODO:update geoCoordinates for victim and saviour and
                            // Fetch the image of victim if distance is less than 100m and UpdateUI
                            Log.d("run","loading Map Scene");
                            loadMarker(mapMarkerImageStyle);
                        }
                    };
                    timer.schedule(task,500, 60000);


                    /**       }
                     }, 5000);*/
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
        /**Alert*/

        MapImage mapImageAlert = MapImageFactory.fromResource(context.getResources(),R.drawable.alert);
        MapMarker mapMarkerAlert = new MapMarker(geoCoordinatesAlert);
        /**Saviour*/
        MapImage mapImageSaviour = MapImageFactory.fromResource(context.getResources(),R.drawable.saviour);
        MapMarker mapMarkerSaviour = new MapMarker(geoCoordinatesSaviour);

        /**Alert*/
        mapMarkerAlert.addImage(mapImageAlert, mapMarkerImageStyle);
        mapView.getMapScene().addMapMarker(mapMarkerAlert);
        /**Saviour*/
        mapMarkerSaviour.addImage(mapImageSaviour, mapMarkerImageStyle);
        mapView.getMapScene().addMapMarker(mapMarkerSaviour);


    }
    /**Loads the Route between given co-ordinates*/
    public void addRouteButtonClicked(View view) {
        routing.addRoute(geoCoordinatesAlert,geoCoordinatesSaviour);

    }
    /**Clears the route, marker still present on map*/
    public void clearMapButtonClicked(View view) {
        routing.clearMap();
    }



    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
