package com.trata.securityapplication;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

import static com.trata.securityapplication.SignUp1Activity.Btn_Submit;
import static com.trata.securityapplication.SignUp1Activity.cnfpass_outer;
import static com.trata.securityapplication.SignUp1Activity.pass_outer;
import static com.trata.securityapplication.SignUp1Activity.spinner;
import static com.trata.securityapplication.SignUp1Activity.t1;


public class VerifyEmail {

    private String TAG = "VerifyEmailClass";
    private FirebaseUser firebaseUser;
    private Context context;
    private FirebaseAuth firebaseAuth;

    public VerifyEmail(FirebaseUser firebaseUser, Context context){
        this.firebaseUser = firebaseUser;
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public boolean isEmailIdVerified(){
        Log.d(TAG,"Inside verify email");

        if (firebaseUser.isEmailVerified()) {
            Log.d(TAG, "User Verified");
            return true;
        }
        else {
            Log.d(TAG, "User not verified");
            return false;
        }
    }

    public void sendVerificationEmail(final Activity activity){
        Log.d(TAG,"Inside sendEmailVerification");
        if (firebaseUser == null){
            Log.d(TAG,"User is null inside sendVerificationEmail");
            return;
        }
        /*final String url = "https://securityapplication.page.link/Tbeh?uid=" + user.getUid();
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                .setHandleCodeInApp(false)
                .setAndroidPackageName("com.example.securityapplication.MainActivity",true,null)
                .build();

        firebaseUser.sendEmailVerification(actionCodeSettings)*/

        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toasty.success(context, "Verification email sent to "+ firebaseUser.getEmail(), Toast.LENGTH_LONG, true).show();

                            /*Toast.makeText(context,
                                    "Verification email sent to " + firebaseUser.getEmail(),
                                    Toast.LENGTH_LONG).show();*/
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toasty.warning(context, "Email already sent", Toast.LENGTH_LONG, true).show();

                            Toast.makeText(context,
                                    "Email already sent",
                                    Toast.LENGTH_LONG).show();
                        }
                        signOut();
                        //spinner
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        spinner.setVisibility(View.GONE);
                        t1.setAlpha(1);
                        cnfpass_outer.setAlpha(1);
                        pass_outer.setAlpha(1);
                        Btn_Submit.setAlpha(1);
                        Btn_Submit.setText("PROCEED");
                        //spinner end
                    }
                });
    }

    private void signOut(){
        if (firebaseUser != null) {
            firebaseAuth.signOut();
            //Toast.makeText(context, "Logged Out from Firebase", Toast.LENGTH_SHORT).show();
        }
    }
}
