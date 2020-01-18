package com.trata.securityapplication;

import android.app.AlarmManager;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.google.firebase.storage.StorageReference;

import com.trata.securityapplication.Helper.FirebaseHelper;
import com.trata.securityapplication.model.AlertDetails;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import javax.net.ssl.HttpsURLConnection;

import es.dmoral.toasty.Toasty;

public class EmergencyMessagingService extends FirebaseMessagingService {
    private static final String TAG="CloudMessagingService";
    private RemoteMessage remoteMessage;
    private String channelId="999";
    private static HashMap<String,Boolean> subscribed=new HashMap<String, Boolean>(10); //a Hashmap to keep track of all subscribed topics
    String useruid;
    private static UpdateSaviourCountCallback updateSaviourCountCallback;
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
        try {
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                if (/* Check if data needs to be processed by long running job */ !true) {
                    // For long-running tasks (10 seconds or more) use WorkManager.
                    // scheduleJob();
                } else {
                    // Handle message within 10 seconds
                    handleNow();
                }
                showToast();
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG,"Error occured inside onMessageReceived:"+e.getMessage());
        }

        // Check if message contains a notification payload.


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    public void showToast(){
        Handler handler=new Handler(getApplication().getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (remoteMessage.getNotification() != null) {
                    Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

                    Toasty.warning(getBaseContext(),"An alert came.Please check Saviours tab",Toasty.LENGTH_LONG).show();
                }
                else{ //added new toasts
                    if(remoteMessage.getData().containsKey("safe")){
                        String username=(String) remoteMessage.getData().get("username");
                        Toasty.success(getBaseContext(),username+" is now safe.Thanks for your help!",Toasty.LENGTH_LONG).show();
                    }
                    else
                    {

                        String uid = (String) remoteMessage.getData().get("uid");
                        Toast.makeText(getBaseContext(),"Location updates were received. For uid:"+uid,Toast.LENGTH_LONG).show();//TODO:remove toast

                    }
                }

            }
        });


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
        if (remoteMessage.getNotification() != null && !remoteMessage.getData().containsKey("safe")) {
            AlertDetails alertDetails = new AlertDetails();

            alertDetails.setName((String) remoteMessage.getData().get("username"));
            alertDetails.setLocation((String) remoteMessage.getData().get("liveLocation"));
            alertDetails.setUid((String) remoteMessage.getData().get("uid"));
            FirebaseHelper firebaseHelper=FirebaseHelper.getInstance();
            StorageReference storageReference=firebaseHelper.getStorageReference_ofuid((String) remoteMessage.getData().get("uid"));
            alertDetails.setImageUrl(storageReference);
            for (String key : keys) {

                Object value = remoteMessage.getData().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);

            }
            //TODO:check if victim uid same as user uid. In that case don't add to AlertObject
            AlertObjects.setAlertDetail(alertDetails.getUid(), alertDetails);
            //do destroy after 30 min
            someFunctino(alertDetails.getUid());


        }
        else{
            if(remoteMessage.getData().containsKey("saviourCount")){
                String count=remoteMessage.getData().get("saviourCount");
                String uid=remoteMessage.getData().get("uid");
                AlertObjects.getAlert(uid).setSaviourcount(count);

                String newText="No of Saviours to rescue:"+count;
                updateSaviourCountCallback.updateSaviourCount(newText);
                //show "saviour on the way..." notification only if the person is subscribed to victim topic
                if(EmergencyMessagingService.subscribed.containsKey("victim_"+uid))
                    showSaviourCountNotification(Integer.parseInt(count));
                else
                    Log.d(TAG,"Not subscribed to victim_"+uid+" so not showing notification");

            }
            else if(remoteMessage.getData().containsKey("sendCount")){
                Log.d(TAG,"sendCount message received");
                String uid=remoteMessage.getData().get("uid");
                useruid=FirebaseHelper.getInstance().getFirebaseAuth().getUid();
                //TODO:http call
                String params="?callerUid="+useruid+"&targetUid="+uid;
                Log.d(TAG,"params:"+params);
                try{
                    callUrl(params);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(remoteMessage.getData().containsKey("testCount")){
                Log.d(TAG,"testCount message received");
                String uid=remoteMessage.getData().get("uid");
                useruid= FirebaseHelper.getInstance().getFirebaseAuth().getUid();
                if(useruid.equals(uid)){
                    home_fragment.incrementTestCount();
                    showSaviourCountNotification(home_fragment.getTestCount());
                }
            }
            else if(remoteMessage.getData().containsKey("safe")){
                Log.d(TAG,"Safety message received");
                //removing the alert object
                String uid = (String) remoteMessage.getData().get("uid");
                String username=(String) remoteMessage.getData().get("username");
                AlertObjects.getAllAlerts().remove(uid);
                EmergencyMessagingService.unsubscribeTopic("saviours_"+uid);// unsubscribe to prevent future live updates from receiving
                //TODO:redirect to saviours fragment and update recycler view and history

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
                            Log.d(TAG, msg);
                            return;
                        }
                        Log.d(TAG, msg);
                        subscribed.put(topic,true);//add topic to subscribed Hashmap
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
                            Log.d(TAG, msg);
                            return;
                        }
                        Log.d(TAG, msg);
                        subscribed.remove(topic);//remove the topic from subscribed list
                        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void unsubscribeAll(){
        Log.d(TAG,"unsubscribeAll called");
        for(Map.Entry<String, Boolean> entry : subscribed.entrySet()) {
            String key = entry.getKey();
            EmergencyMessagingService.unsubscribeTopic(key);
        }
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

    public static void callUrl(String params) throws IOException {
        Log.d("callUrl","CloudMessagingService called");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url = null;
        try {
            url = new URL("https://us-central1-securityapplication-b990e.cloudfunctions.net/testCount"+params);
            Log.d("callUrl","Url received:"+"https://us-central1-securityapplication-b990e.cloudfunctions.net/testCount"+params);
        } catch (MalformedURLException e) {
            Log.d("callUrl","MalformedUrlException");
            e.printStackTrace();
            return;
        }
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Log.d("callUrl","urlConnection called");
        } finally {
            urlConnection.disconnect();
        }
    }

    public static void setUpdateSaviourCountCallback(UpdateSaviourCountCallback cb) {
        updateSaviourCountCallback = cb;
    }
    public void someFunctino(String uid) {
        // set the timeout
        // this will stop this function in 30 minutes
        Log.d("sa1234567","just start");
        long in30Minutes = 30 * 60 * 1000;
        Timer timer = new Timer();
        timer.schedule( new TimerTask(){
            public void run() {
                AlertObjects.alerts.remove(uid);
            }
        },  in30Minutes );

        // do the work...
        Log.d("EmergencyMessagging","remove called ");
    }
}
