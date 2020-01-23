package com.trata.securityapplication;

import android.content.Context;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

import com.trata.securityapplication.Helper.FirebaseHelper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DeleteAlertWorker extends Worker {

    public DeleteAlertWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String uid=getInputData().getString("uid");
        String useruid= FirebaseHelper.getInstance().getFirebaseAuth().getUid();
        //TODO:http call
        String params="?callerUid="+useruid+"&targetUid="+uid;
        try {
            callUrl(params);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }
    private void callUrl(String params) throws IOException {
        Log.d("callUrl","DeleteAlertWorker called");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url = null;
        try {
            url = new URL("https://us-central1-securityapplication-b990e.cloudfunctions.net/deleteAlert"+params);
            Log.d("callUrl","Url received:"+"https://us-central1-securityapplication-b990e.cloudfunctions.net/deleteAlert"+params);
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

}
