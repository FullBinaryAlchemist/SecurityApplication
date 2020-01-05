package com.trata.securityapplication;

import android.app.job.JobParameters;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.app.job.JobService;

public class RestartJobService extends JobService{
        private static String TAG= JobService.class.getSimpleName();
        private static RestartServiceBroadcastReceiver restartSensorServiceReceiver;
        private static JobService instance;
        private static JobParameters jobParameters;

        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            Log.d(TAG,"inside onStartJob");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //startForegroundService(new Intent(this,SosPlayer.class)); //was startForeground before . Temporarily disabled till a better solution obtained
                startService(new Intent(this,SosPlayer.class));
            } else {
                startService(new Intent(this,SosPlayer.class));
            }
            instance= this;
            RestartJobService.jobParameters= jobParameters;

            return false;
        }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
