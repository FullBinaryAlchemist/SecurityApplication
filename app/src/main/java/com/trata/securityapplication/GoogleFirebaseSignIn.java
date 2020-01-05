package com.trata.securityapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.trata.securityapplication.Helper.FirebaseHelper;
import com.trata.securityapplication.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

import es.dmoral.toasty.Toasty;

public class GoogleFirebaseSignIn implements Serializable {

    private Activity activity;
    private static final String TAG = "GoogleSignIn";
    //private static final int RC_SIGN_IN = 9001;

    private static volatile GoogleFirebaseSignIn googleFirebaseInstance;

    private String mImeiNumber;
    private FirebaseHelper firebaseHelper;
    private FirebaseUser firebaseUser;

    //private constructor
    private GoogleFirebaseSignIn(){
        //Prevent form the reflection api.
        if (googleFirebaseInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public void init(Activity activity, String imei){

        this.activity = activity;
        this.mImeiNumber = imei;
        firebaseHelper = FirebaseHelper.getInstance();
        firebaseHelper.initFirebase();
    }

    public static GoogleFirebaseSignIn getInstance() {
        //Double check locking pattern
        if (googleFirebaseInstance == null) { //Check for the first time..if there is no instance available... create new one
            synchronized (GoogleFirebaseSignIn.class) { //Check for the second time to make ThreadSafe
                //if there is no instance available... create new one
                if (googleFirebaseInstance == null) {
                    googleFirebaseInstance = new GoogleFirebaseSignIn();
                    Log.d(TAG,"Created new googleFirebaseInstance");
                }
            }
        }
        else {
            Log.d(TAG,"googleFirebaseInstance Exists");
        }
        return googleFirebaseInstance;
    }

    //Make singleton from serialize and deserialize operation.
    protected GoogleFirebaseSignIn readResolve() {
        return getInstance();
    }

    public void linkGoogleAccount(GoogleSignInAccount acct) {
        Log.d(TAG,"Inside linkGoogleAccount");
        if (acct == null)
            return;
        // Link the anonymous user to the email credential

        AuthCredential credential= GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        // [START link_credential]
        firebaseHelper.getFirebaseAuth().getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            Toast.makeText(activity, "Account Linked Successfully", Toast.LENGTH_SHORT).show();

                        } else {
                            String[] exceptionSplitted = task.getException().toString().split(":");
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(activity, exceptionSplitted[exceptionSplitted.length-1], Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END link_credential]
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d(TAG,"Inside firebaseAuthWithGoogle");
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getEmail());
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseHelper.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            try {
                                boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                                Log.d(TAG, "onComplete: " + (isNew ? "new user" : "old user"));
                            }catch (Exception e){
                                Log.d(TAG,e.getMessage());
                            }

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser firebaseUser = firebaseHelper.getFirebaseAuth().getCurrentUser();

                            // set imei and uid in firebase
                            setImeiInFirebase(firebaseUser);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.layout.activity_main), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            setUser(null);
                        }
                    }
                });
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //setUser(null);
        }
    }

    private void setUser(FirebaseUser firebaseUser){
        this.firebaseUser = firebaseUser;
        if (firebaseUser != null){
            Intent mHomeIntent = new Intent(activity,navigation.class);
            activity.startActivity(mHomeIntent);
        }
        else {
            firebaseHelper.googleSignOut(activity);
            Intent mLogOutAndRedirect = new Intent(activity, MainActivity.class);
            mLogOutAndRedirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(mLogOutAndRedirect);
            Toasty.error(activity, "Sign in failed", Toast.LENGTH_SHORT, true).show();
        }
        //finishing the navigation activity
        activity.finish();
    }

    private void storeDataInSql(final FirebaseUser firebaseUser){
        if (firebaseUser != null) {
            final String uid = firebaseUser.getUid();
            firebaseHelper.getUsersDatabaseReference().child(uid).addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Log.d("Paid12345","schin1"+user.getName()+user.isPaid());
                    SQLiteDBHelper db=SQLiteDBHelper.getInstance(activity);

                    try {
                        Log.d("Paid12345","schin1"+user.getName()+user.isPaid());
                    }catch (Exception e){
                        Log.d(TAG, e.getMessage());
                    }

                    try {
                        if (db.addUser(user)) {
                            if (user.getSosContacts() != null)
                                db.addsosContacts(user.getSosContacts(),1); //to fetch SOSContacts from Firebase
                            setUser(firebaseUser);
                        }
                        else {
                            firebaseHelper.firebaseSignOut(mImeiNumber);
                            setUser(null);
                            Toast.makeText(activity, "Authentication failed :In GoogleFirebaseSignin sqlite error occurred",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }catch (Exception e){
                        Log.d(TAG,e.getMessage());
                        firebaseHelper.firebaseSignOut(mImeiNumber);
                        setUser(null);
                        Toast.makeText(activity, "Authentication failed :In GoogleFirebaseSignin sqlite error occurred"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        return;
                    }
//                    Log.d("Paid12345","schin"+user.getName()+ user.isPaid());
//                    if(dataSnapshot.getValue(User.class).isPaid()){
//                        Log.d("Paid12345","i am here");
//                        home_fragment.setpaid(true);
//                    }
//                    else{
//                        home_fragment.setpaid(false);
//                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG,databaseError.getDetails());
                    Toast.makeText(activity, "Inside storeDataInSql:"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    firebaseHelper.firebaseSignOut(mImeiNumber);
                    setUser(null);
                }
            });
        }
    }

    private void setImeiInFirebase(final FirebaseUser firebaseUser){

        firebaseHelper.getDevicesDatabaseReference().child(mImeiNumber).setValue(firebaseUser.getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseHelper.getUsersDatabaseReference().child(firebaseUser.getUid()).child("imei").setValue(mImeiNumber).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        storeDataInSql(firebaseUser);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        firebaseHelper.makeDeviceImeiNull(mImeiNumber);
                        Log.d(TAG,e.getMessage());
                        // sigin out the user
                        firebaseHelper.firebaseSignOut();
                        setUser(null);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,e.getMessage());
                // sign out the user
                firebaseHelper.firebaseSignOut();
                setUser(null);
            }
        });
    }
}
