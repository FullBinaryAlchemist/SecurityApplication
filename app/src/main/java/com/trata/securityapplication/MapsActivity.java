package com.trata.securityapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapviewlite.MapImage;
import com.here.sdk.mapviewlite.MapImageFactory;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapMarkerImageStyle;
import com.here.sdk.mapviewlite.MapScene;
import com.here.sdk.mapviewlite.MapStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import com.trata.securityapplication.Routing;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity {
    private static final String TAG = "HereMaps";
    private MapViewLite mapView;
    // private MapStyle mapStyle;
    Context context = this;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /** Get a MapViewLite instance from the layout.*/
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
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
                            Log.d("run","loading Map Scene");
                            loadMarker(mapMarkerImageStyle);
                        }
                    };
                    timer.schedule(task,500, 60000);


                 /**       }
                    }, 5000);*/
                    //Calls the Routing Class
                    routing = new Routing(MapsActivity.this, mapView,geoCoordinatesAlert, geoCoordinatesSaviour);



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
