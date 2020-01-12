package com.trata.securityapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.trata.securityapplication.model.AlertDetails;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class EmergencyMessagingService extends FirebaseMessagingService {
    private static final String TAG="CloudMessagingService";
    private RemoteMessage remoteMessage;
    private String channelId="999";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        this.remoteMessage= remoteMessage;
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ !true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
               // scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

/*
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }
*/

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");

        String keys[]= {"username","liveLocation","uid"};
        // Check if message contains a notification payload.
        //If it is a notification message then it contains first time alert
        //else it is an update message so an AlertDetail must exist.So update it
        if (remoteMessage.getNotification() != null) {
            AlertDetails alertDetails = new AlertDetails();

            alertDetails.setName((String) remoteMessage.getData().get("username"));
            alertDetails.setLocation((String) remoteMessage.getData().get("liveLocation"));
            alertDetails.setUid((String) remoteMessage.getData().get("uid"));
            for (String key : keys) {

                Object value = remoteMessage.getData().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);

            }

            AlertObjects.setAlertDetail(alertDetails.getUid(), alertDetails);
        }
        else{
            if(remoteMessage.getData().containsKey("saviourCount")){
                String count=remoteMessage.getData().get("saviourCount");
                showSaviourCountNotification(Integer.parseInt(count));
            }
            else
            {
                Log.d(TAG, "Update data message received");
                String uid = (String) remoteMessage.getData().get("uid");
                AlertDetails alert = AlertObjects.getAlert(uid);
                String location = (String) remoteMessage.getData().get("liveLocation");
                alert.setLocation(location);
                Log.d(TAG, "Updated alert with uid:" + uid + " location:" + location);
            }
        }
    }
    public static void subscribeTopic(final String topic){
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscription to topic:"+topic/*getString(R.string.msg_subscribed)*/;
                        if (!task.isSuccessful()) {
                            msg += " failed";/*getString(R.string.msg_subscribe_failed)*/;
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static void unsubscribeTopic(final String topic){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Unsubscription from topic:"+topic/*getString(R.string.msg_subscribed)*/;
                        if (!task.isSuccessful()) {
                            msg += " failed";/*getString(R.string.msg_subscribe_failed)*/;
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static String getTopicString(String zone,String sub_zone){
        return "alerts_"+zone.split(",")[0]+"_"+zone.split(",")[1]+"_"+sub_zone.split(",")[0]+"_"+sub_zone.split(",")[1];
    }

    /*private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());
    }
*/

    public  void showSaviourCountNotification(int count){

        String messageBody=count+" saviours are coming to your rescue!";
        String title="Help is on the way";

        if(count==0){
            messageBody="Alerted Saviours nearby. Stay strong !";
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notify)
                        .setColor(getResources().getColor(R.color.cyan))
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setOngoing(true);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Saviours to rescue",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(999, notificationBuilder.build());

    }
}
