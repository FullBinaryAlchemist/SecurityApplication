package com.trata.securityapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class sentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("sentRecevier","Onreceiver");
        switch (getResultCode())
        {
            case Activity.RESULT_OK:
                Toasty.success(context, "SMS sent", Toast.LENGTH_SHORT, true).show();

                /*Toast.makeText(context, "SMS sent",
                        Toast.LENGTH_LONG).show();*/
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toasty.error(context, "Error: Message not sent", Toast.LENGTH_LONG, true).show(); //changed from generic failure

               /* Toast.makeText(context, "Generic failure",
                        Toast.LENGTH_LONG).show();*/
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Toasty.error(context,  "No service", Toast.LENGTH_LONG, true).show();

               /* Toast.makeText(context, "No service",
                        Toast.LENGTH_LONG).show();*/
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toasty.error(context,  "Null PDU", Toast.LENGTH_LONG, true).show();

                /*Toast.makeText(context, "Null PDU",
                        Toast.LENGTH_LONG).show();*/
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toasty.warning(context,  "Radio off", Toast.LENGTH_LONG, true).show();

                /*Toast.makeText(context, "Radio off",
                        Toast.LENGTH_LONG).show();*/
                break;
        }

    }
}
