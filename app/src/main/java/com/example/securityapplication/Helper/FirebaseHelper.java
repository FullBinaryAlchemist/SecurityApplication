package com.example.securityapplication.Helper;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.securityapplication.GoogleFirebaseSignIn;
import com.example.securityapplication.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDevicesDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mEmailDatabaseReference;
    private static final String TAG = "FirebaseHelper";
    private Context context;
    private GoogleSignInClient mGoogleSignInClient;
    private static volatile FirebaseHelper firebaseHelperInstance;
    private FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    public void initFirebase(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        /** Get FirebaseStorage instance **/
        firebaseStorage = FirebaseStorage.getInstance();
        /** Get FirebaseStorage reference **/
        storageReference = firebaseStorage.getReference();
        initDataBaseReferences();
        Log.d(TAG,"Firebase Initialization complete");
    }

    public void initContext(Context context){
        this.context = context;
    }

    public static FirebaseHelper getInstance() {
        //Double check locking pattern
        if (firebaseHelperInstance == null) { //Check for the first time..if there is no instance available... create new one
            synchronized (GoogleFirebaseSignIn.class) { //Check for the second time to make ThreadSafe
                //if there is no instance available... create new one
                if (firebaseHelperInstance == null) {
                    firebaseHelperInstance = new FirebaseHelper();
                    Log.d(TAG,"Created new FirebaseHelperInstance");
                }
            }
        }
        else {
            Log.d(TAG,"FirebaseHelperInstance Exists");
        }
        return firebaseHelperInstance;
    }

    //Make singleton from serialize and deserialize operation.
    protected FirebaseHelper readResolve() {
        return getInstance();
    }

    private void initDataBaseReferences(){
        Log.d(TAG,"Initializing Databse References... ");
        //Initialize Database
        mDevicesDatabaseReference = mFirebaseDatabase.getReference().child("Devices");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        mEmailDatabaseReference = mFirebaseDatabase.getReference().child("Email");
    }

    public void initGoogleSignInClient(String server_client_id){
        Log.d(TAG,"Initializing Google SignIN Client");
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(server_client_id)
                .requestEmail()
                .requestProfile()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void makeDeviceImeiNull(String imei){
        Log.d(TAG,"Inside makeDeviceImeiNull");
        Log.d(TAG, imei+mAuth.getUid());
        // first make uid under imei null in Devices and imei under uid null in Users
        if (mAuth.getCurrentUser() != null)
            mDevicesDatabaseReference.child(imei).setValue(null, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Log.d(TAG, databaseError.getMessage());
                    }
                }
            });
    }

    public void makeUserImeiNull(){
        Log.d(TAG,"Inside makeUserImeiNull");
        Log.d(TAG, mAuth.getUid());
        if (mAuth.getCurrentUser() != null)
            mUsersDatabaseReference.child(mAuth.getUid()).child("imei").setValue(null, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Log.d(TAG, databaseError.getMessage());
                    }
                    else
                        mAuth.signOut();
                }
            });
    }

    public void firebaseSignOut(String imei){
        Log.d(TAG,"Firebase SignOut(String imei) called");

        //Firebase signOut
        if (mAuth.getCurrentUser() != null) {
            makeDeviceImeiNull(imei);
            makeUserImeiNull();
            Log.d(TAG,"Logged Out from Firebase"); //Removed Toasty and added log
        }
    }

    public void firebaseSignOut(){
        Log.d(TAG,"Firebase SignOut() called");
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    public void googleSignOut(Activity activity){
        Log.d(TAG,"Google SignOut called");
        if(GoogleSignIn.getLastSignedInAccount(context) != null && mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //updateUI(null);
                        }
                    });
        }
    }

    public DatabaseReference getUsersDatabaseReference(){
        return mUsersDatabaseReference;
    }

    public DatabaseReference getDevicesDatabaseReference(){
        return mDevicesDatabaseReference;
    }

    public DatabaseReference getEmailDatabaseReference(){
        return mEmailDatabaseReference;
    }

    public GoogleSignInClient getGoogleSignInClient(){
        return mGoogleSignInClient;
    }

    public FirebaseAuth getFirebaseAuth() { return mAuth; }

    public  FirebaseDatabase getFirebaseDatabase() { return mFirebaseDatabase; }

    public StorageReference getStorageReference() {return storageReference.child(FirebaseAuth.getInstance().getUid());}

    public StorageReference getImageStorageRef() {return storageReference.child("images");}

    public void addsos_infirebase(String uid,String c1,String c2,String c3,String c4,String c5){
        DatabaseReference dr= mUsersDatabaseReference.child(uid).child("sosContacts");
        DatabaseReference dr1=mUsersDatabaseReference.child(uid).child("sosContacts").child("c1");
        DatabaseReference dr2=mUsersDatabaseReference.child(uid).child("sosContacts").child("c2");
        DatabaseReference dr3=mUsersDatabaseReference.child(uid).child("sosContacts").child("c3");
        DatabaseReference dr4=mUsersDatabaseReference.child(uid).child("sosContacts").child("c4");
        DatabaseReference dr5=mUsersDatabaseReference.child(uid).child("sosContacts").child("c5");
        dr1.setValue(c1);
        dr2.setValue(c2);
        dr3.setValue(c3);
        dr4.setValue(c4);
        dr5.setValue(c5);

    }
    public void updateuser_infirebase(String uid,User user){
        DatabaseReference dr1=mUsersDatabaseReference.child(uid).child("dob");
        DatabaseReference dr2=mUsersDatabaseReference.child(uid).child("gender");
        DatabaseReference dr3=mUsersDatabaseReference.child(uid).child("location");
        DatabaseReference dr4=mUsersDatabaseReference.child(uid).child("mobile");
        DatabaseReference dr5=mUsersDatabaseReference.child(uid).child("name");
        dr1.setValue(user.getDob());
        dr2.setValue(user.getGender());
        dr3.setValue(user.getLocation());
        dr4.setValue(user.getMobile());
        dr5.setValue(user.getName());

    }
}