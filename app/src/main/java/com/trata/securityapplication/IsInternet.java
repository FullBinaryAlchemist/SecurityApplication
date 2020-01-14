package com.trata.securityapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class IsInternet extends AppCompatActivity {


    public static boolean isNetworkAvaliable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }

    // check internet connectivity
    public static Boolean checkInternet(Context context){
        if (IsInternet.isNetworkAvaliable(context)) {
            return true;
        }
        else {
            Toasty.error(context, "Please check your Internet Connectivity", Toast.LENGTH_LONG, true).show();

           // Toast.makeText(context, "Please check your Internet Connectivity", Toast.LENGTH_LONG).show();
            return false;
        }
    }

}
