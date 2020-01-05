package com.trata.securityapplication;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;


public class BackgroundSosPlayerService extends Service {
    private static final String TAG = null;
    MediaPlayer player;
    AudioManager mAudioManager;
    SettingsContentObserver mSettingsContentObserver;
    public IBinder onBind(Intent arg0) {

        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager= (AudioManager)getSystemService(AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        mAudioManager.setSpeakerphoneOn(true);

        player = MediaPlayer.create(this, R.raw.idil);
        player.setLooping(true); // Set looping
        //player.setVolume(100,100);



    }
    public int onStartCommand(Intent intent, int flags, int startId) {
       player.start();
        mSettingsContentObserver = new SettingsContentObserver(this,new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );
        return 1;
    }

    public void onStart(Intent intent, int startId) {
        // TO DO
    }
    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void onStop() {

    }
    public void onPause() {

    }
    @Override
    public void onDestroy() {
        getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(false);
        player.stop();
        player.release();
    }

    @Override
    public void onLowMemory() {

    }
}
