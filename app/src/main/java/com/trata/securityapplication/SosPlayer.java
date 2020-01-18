package com.trata.securityapplication;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import com.trata.securityapplication.Helper.FirebaseHelper;

import java.util.Timer;
import java.util.TimerTask;

public class SosPlayer extends Service {
    private MediaSessionCompat mediaSession;
    private static int soskeyscount; //was previously non static
    private  static boolean sosplay; //was previously non static
    private int prev_direction;
    private boolean timerStarted;
    private CountDownTimer wtimer;
    public int counter=0;

    private int stop=0;
    private int play=0;
    private String TAG="SOS Player";

    PowerButtonBroadcastReceiver mPowerButtonBroadcastReceiver;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
            super.onStartCommand(intent, flags, startId);
           // startWaitTimer(); //Commented out as no longer used
            try {
                stop = intent.getIntExtra("stop", 0);
            }catch(Exception e){
                stop=0; //assigning a default value
                Toast.makeText(getApplicationContext(),"SOS player Exception"+e.getMessage(),Toast.LENGTH_LONG);
            }
            Log.d(TAG,"Inside onStartCOmmand : Stop ="+stop);

        if(stop==1){
            stopPlaying();
            //stopping BackgroundSOSPlayer in case it was running
            Intent stopSirenIntent= new Intent(this,BackgroundSosPlayerService.class);
            boolean stoppedSiren=stopService(stopSirenIntent);
            Log.d(TAG,"Stop=1 stopping BackgroundSOSPlayer service in case it was running :"+stoppedSiren);
                boolean stopped=stopService(new Intent(getApplicationContext(),SosPlayer.class)); //stops the service when calling intent has stop=1
                Log.d(TAG,"Stop=1 so calling stopService() :"+stopped);
        }
        else{
            //initialise the VolumeProviderCompact
            detectSosPattern();


        }
        try{
        play=intent.getIntExtra("play",0);
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"SosPlayer Exception"+e.getMessage(),Toast.LENGTH_LONG);
        }
        Log.d(TAG,"Inside onStartCOmmand : PLay ="+play);

        if(play==1){
            updateCount(0); //call updateCount and then from there call BackgroundSosPlayer
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        try {
            super.onCreate();

            //initialising count
            resetCount();
            //initialising sosplaying variable
            sosplay = false;
            prev_direction = 0;

            timerStarted = false;
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //code moved to detectSosPattern which is now called from onStartCommand if stop==0
        //Register PowerButtonBroadcastReceiver screen on-off
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mPowerButtonBroadcastReceiver = new PowerButtonBroadcastReceiver();
        getApplicationContext().registerReceiver(mPowerButtonBroadcastReceiver,filter);
    }

    public void detectSosPattern(){
        Log.d(TAG,"detectSOSPattern initialised");
        /*mediaSession = new MediaSessionCompat(this, "SosPlayer");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build());

        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        VolumeProviderCompat myVolumeProvider =
                new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, *//*max volume*//*100, *//*initial volume level*//*50) {
                    @Override
                    public void onAdjustVolume(int direction) {
                *//*
                -1 -- volume down
                1 -- volume up
                0 -- volume button released
                 *//*
                        Log.v("Player","Direction is:"+direction);
                        updateCount(direction);
                        setCurrentVolume(getCurrentVolume()+direction);
                    }
                };

        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);
*/
    }
    public void startWaitTimer(){
        timerStarted=true;
        Log.d("SOS Timer","Timer started");
        //Toast.makeText(getApplicationContext(), "SOS timer started", Toast.LENGTH_LONG).show();
        wtimer=new CountDownTimer(10*1000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.d("SOS Timer" ,"Time remaining:" +millisUntilFinished / 1000);
            }


            public void onFinish(){
                Log.d("SOS Timer" ,"Timeoout reached" );
                resetCount();
                timerStarted=false;
          //      Toast.makeText(getApplicationContext(), "SOS timer reset", Toast.LENGTH_LONG).show();

            }
        }.start();
    }

    public static void resetCount(){
        soskeyscount=0;
    }
    //allows other class to initialise soskeycount
    public static void setCount(int n){
        soskeyscount=n;
    }

    public void updateCount(int direction){
        Log.d(TAG,"updateCount:");
        if(soskeyscount>=5 && !checkPlaying()){
            //create Alert on firebase
            String uid= FirebaseHelper.getInstance().getFirebaseAuth().getUid();
            home_fragment.startAlertCreation(uid);

            Log.d(TAG,"STARTING siren and sms...");
            startPlaying();
            sendSosSMS();
        }
        if(soskeyscount==0 && prev_direction==0){
            Log.d(TAG,"Initialsing prev_direction");

            prev_direction=direction;
            return;
        }
        if (direction!=0){
            //start timer on first key press
            if(soskeyscount==0 && !timerStarted){
                startWaitTimer();

            }

            if(compareDirection(prev_direction,direction)){
                soskeyscount++;
            }

            else{
                resetCount();

                //cancel the timer counting and deinitialize it to restart counting from start
                if(wtimer!=null){
                    wtimer.cancel();
                    wtimer=null;
                }
            }

            Log.d("New soskeycount","Count"+soskeyscount+" Direction"+direction+" Prev direction"+prev_direction);
            prev_direction=direction;
        }

        Log.d(TAG,"Passing on 0 direction");
    }

    private void sendSosSMS( ){
        Intent serviceIntent = new Intent(this,SendSMSService.class);
        startService(serviceIntent);
    }

    public boolean compareDirection(int p_direction, int n_direction){
        int d_p_direction= (p_direction>0)?1:-1;
        int d_n_direction= (n_direction>0)?1:-1;

        return d_p_direction!=d_n_direction;
    }
    public static boolean checkPlaying(){
        Log.d("SosPlayer","checkPlaying : "+SosPlayer.sosplay);
        return SosPlayer.sosplay;
    }

    public void startPlaying(){
        Intent svc=new Intent(this, BackgroundSosPlayerService.class);
        startService(svc);
        sosplay=true;
    }

    public static void stopPlaying(){
        Log.d("SosPlayer","stopPlaying");
        sosplay=false;
        resetCount();

    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i("SOSPlayer Service", "onTaskRemoved called");
        // restart the never ending service
        if(stop==0) {

            Log.d("SOS PLAYER","Stop==0 so calling Restartservice");
            Intent broadcastIntent = new Intent(this, RestartServiceBroadcastReceiver.class);
            sendBroadcast(broadcastIntent);
        }
        // do not call stoptimertask because on some phones it is called asynchronously
        // after you swipe out the app and therefore sometimes
        // it will stop the timer after it was restarted
        // stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("SOS PLAYER", "ondestroy!");

        sosplay=false;

        if(stop==0) {
            Log.d("SOS PLAYER","Stop==0 so calling Restartservice");
            Intent broadcastIntent = new Intent(this, RestartServiceBroadcastReceiver.class);
            sendBroadcast(broadcastIntent);
        }
        stoptimertask();
        //added try-catch as app crashes if paid set to true
        try {
            mediaSession.release();
        }catch (Exception e){
            e.printStackTrace();
        }

        getApplicationContext().unregisterReceiver(mPowerButtonBroadcastReceiver);

    }
}
