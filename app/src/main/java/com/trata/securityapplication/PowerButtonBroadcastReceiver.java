package com.trata.securityapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerButtonBroadcastReceiver extends BroadcastReceiver {

    String TAG="PowerButtonBroadcastReceiver";
    private static int count=0;
    private static long lastcall;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(TAG,"onReceive with count:"+count);

        long time=System.currentTimeMillis();
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            Log.d(TAG,"Screen turned off at:"+time);


        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            //Take count of the screen on position
            Log.d(TAG,"Screen turned on at:"+time);
        }

        if(time-lastcall<=1100)//checking if difference if less than 1.1s
        {
            count++;
            Log.d(TAG,"Lastcall updated");
        }
        else
        {
            Log.d(TAG,"Count reseted");

            resetcount();
        }
        lastcall=time;

        if(count>=5){
            Log.d(TAG,"Rapidly pressed power buttons more than 5 times..");
            if(!SosPlayer.checkPlaying()){
                Log.d(TAG,"Starting siren...");
                SosPlayer.setCount(count);
                Intent callSos= new Intent(context,SosPlayer.class);
                callSos.putExtra("play",1);
                context.startService(callSos);
            }
        }

    }

    public void resetcount(){
        count=0;
    }

}
