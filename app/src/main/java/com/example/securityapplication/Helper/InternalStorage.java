package com.example.securityapplication.Helper;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.securityapplication.GoogleFirebaseSignIn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalStorage {

    private static volatile InternalStorage internalStorageInstance;
    final static private String TAG = "InternalStorage";
    private Context context;

    public static InternalStorage getInstance() {
        //Double check locking pattern
        if (internalStorageInstance == null) { //Check for the first time..if there is no instance available... create new one
            synchronized (InternalStorage.class) { //Check for the second time to make ThreadSafe
                //if there is no instance available... create new one
                if (internalStorageInstance == null) {
                    internalStorageInstance = new InternalStorage();
                    Log.d(TAG,"Created new InternalStorageInstance");
                }
            }
        }
        else {
            Log.d(TAG,"FirebaseHelperInstance Exists");
        }
        return internalStorageInstance;
    }

    //Make singleton from serialize and deserialize operation.
    protected InternalStorage readResolve() {
        return getInstance();
    }

    public void initContext(Context context){this.context = context;}

    public void createDirectoryAndSaveFile(Bitmap imageToSave, String filePath) {

        File direct = new File(filePath);

        if (!direct.exists()) {
            File profilePicDir = new File(filePath);
            profilePicDir.mkdirs();
        }

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri getCaptureImageOutputUri(String email) {
        Uri outputFileUri = null;
        try {
            File getImage = context.getExternalFilesDir("");
            if (getImage != null) {
                outputFileUri = Uri.fromFile(new File(getImage.getPath(), email+"/profile.jpeg"));
            }
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return outputFileUri;
    }
}
