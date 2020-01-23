package com.trata.securityapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class deliveryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("deliveryRecevier","Onreceiver");
        switch (getResultCode())
        {
            case Activity.RESULT_OK:
                Toasty.success(context, "SMS delivered", Toast.LENGTH_SHORT, true).show();
                break;
            case Activity.RESULT_CANCELED:
                Toasty.error(context, "SMS not delivered", Toast.LENGTH_LONG, true).show();
                break;
        }


    }
}
