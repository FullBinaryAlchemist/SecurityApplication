package com.example.securityapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.location.Location;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import es.dmoral.toasty.Toasty;

public class GetGPSCoordinates extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    private static String lastKnownLocation=null;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private GoogleApiClient client;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static String getLastKnownLocation(){
        return lastKnownLocation;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        Log.d("GPSService", "Oncreate");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5*60*1000); //NOTE:changed to 5 mins
        locationRequest.setFastestInterval(3*60*1000); //NOTE:changed to 2 mins
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



        /*if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) ==PackageManager.PERMISSION_GRANTED))
        {
            // Permission is granted
            listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Intent i = new Intent("location_update");
                    GetGPSCoordinates.lastKnownLocation = ddToDms(location.getLatitude(), location.getLongitude());
                    i.putExtra("coordinates", location.getLatitude() + "," + location.getLongitude());
                    Log.d("GPSService", "coordinates" + location.getLatitude() + "," + location.getLongitude());
                    Toast.makeText(getApplicationContext(), "coordinates " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT);
                    sendBroadcast(i);
                }

                @Override
                public void onProviderDisabled(String s) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            };
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            //noinspection MissingPermission

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, listener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);

        }else {
            //Permission not Granted
        }*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("GPS Service","Inside OnStart");
        //do heavy work on a background thread

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.d("GPS Service","LocationResult is "+locationResult);
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            GetGPSCoordinates.lastKnownLocation = ddToDms(location.getLatitude(), location.getLongitude());
                            Log.d("GPS Service Running", "Coordinates = Latitude = " + location.getLatitude() + " Longitude = " + location.getLongitude());
                        } else {
                            Log.d("GPS Service","Location Fetch failed");
                            Toast.makeText(getApplicationContext(), "Location Fetch Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    //super.onLocationAvailability(locationAvailability);
                    Log.d("GPS Service","OnLocationAvailabilty IsLocationAvailable "+ locationAvailability.isLocationAvailable());
                    if (locationAvailability.isLocationAvailable()){
                        Log.d("onLocationAvailabilty","Location Available will show updates");
                        //Location Available resume service
                    }else {
                        Log.d("onLocationAvailabilty","Location Unavailable deploying intent");
                        Toasty.warning(getApplicationContext(),"Please turn on location to help us serve you better",Toasty.LENGTH_LONG).show();
                        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }
            };

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());

            Log.d("GPSService", "Notification ON");
            String input = "You are being protected";
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notify)
                    .setColor(getResources().getColor(R.color.cyan))
                    .setContentIntent(pendingIntent)
                    .setContentTitle("TRATA")
                    .setContentText(input)
                    .build();

            startForeground(1, notification);
        }
        else {
            Log.d("GPS Service","Inside else, Permissions denied");
            Toast.makeText(this,"Location Permissions not granted",Toast.LENGTH_LONG).show();
        }


        return START_STICKY;
    }

    /*
    public int[] degreeToDMS(double coordinate){
        coordinate=Math.abs(coordinate);
        int c_degrees= (int)coordinate ;

        double minutes_seconds= coordinate-c_degrees;
        double minutes= (minutes_seconds*60);
        int c_minutes= (int)minutes;

        double seconds= (minutes%1)*60;
        int c_seconds=(int)seconds;

    }*/

    public String ddToDms(double ilat,double ilng) {

        double lat = ilat;
        double lng = ilng;
        String latResult, lngResult;
        String DMS_coordinates; //degree-minute-second converted decimal
        // Make sure that you are working with numbers.
        // This is important in case you are working with values

        // Check the correspondence of the coordinates for latitude: North or South.

        String Strlat= Location.convert(ilat,Location.FORMAT_SECONDS);
        String[] split_Strlat=Strlat.split(":");
        latResult=split_Strlat[0]+"°"+split_Strlat[1]+"\'"+split_Strlat[2]+"\'\'";
        latResult += (lat >= 0)? "N" : "S";

        // Check the correspondence of the coordinates for longitude: East or West.
        String Strlon= Location.convert(ilng,Location.FORMAT_SECONDS);
        String[] split_Strlon=Strlon.split(":");
        lngResult=split_Strlon[0]+"°"+split_Strlon[1]+"\'"+split_Strlon[2]+"\'\'";
        lngResult += (lng >= 0)? "E" : "W";

        DMS_coordinates=latResult+"+"+lngResult;
        Log.d("GPSService/Conversion","INPUT:"+ilat+","+ilng+" result:"+DMS_coordinates);

        return DMS_coordinates;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toasty.error(getApplicationContext(), "GPS service destroyed", Toast.LENGTH_SHORT, true).show();

        //Toast.makeText(getApplicationContext(),"GPS service destroyed",Toast.LENGTH_SHORT);
        Log.d("GPSService","OnDestroy");
        if (locationCallback!=null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void initListener(){
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                /*Log.d("GPS Service","Fetching Location Through Network ");
                GetGPSCoordinates.lastKnownLocation = ddToDms(location.getLatitude(), location.getLongitude());
                Log.d("Network Location ","Latitude = "+location.getLatitude()+" Longitude = "+location.getLongitude());*/
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("GPS Service","Service provider disabled");
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };
    }
}
