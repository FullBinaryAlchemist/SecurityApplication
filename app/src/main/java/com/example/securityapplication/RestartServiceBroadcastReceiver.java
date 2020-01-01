package com.example.securityapplication;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {
    private static JobScheduler jobScheduler;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(RestartServiceBroadcastReceiver.class.getSimpleName(), "Restarting service on stopped service");
        //Toasty.info(context, "Service restarted", Toast.LENGTH_SHORT, true).show();//TODO:Remove toasty

        //Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
        scheduleJob(context);
   }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        if (jobScheduler == null) {
            jobScheduler = (JobScheduler) context
                    .getSystemService(JOB_SCHEDULER_SERVICE);
        }
        ComponentName componentName = new ComponentName(context,
                RestartJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                // setOverrideDeadline runs it immediately - you must have at least one constraint
                // https://stackoverflow.com/questions/51064731/firing-jobservice-without-constraints
                .setOverrideDeadline(0)
                .setPersisted(true).build();
        jobScheduler.schedule(jobInfo);
    }
}
