package com.trata.securityapplication;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ResetPasswordActivity extends AppCompatActivity {

    TextInputEditText emailEditText;
    Button btn_reset;
    TextView resetText;
    Validation validate;
    ProgressBar pgsBar;

    public void pgbarshow()
    {
        btn_reset.setText("");
        findViewById(R.id.pBar).setVisibility(VISIBLE);
        btn_reset.getBackground().setAlpha(100);

    }

    public void pgbarhide()
    {

        findViewById(R.id.pBar).setVisibility(GONE);


        btn_reset.getBackground().setAlpha(255);


        btn_reset.setText("RESET PASSWORD");

    }




    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        FirebaseApp.initializeApp(this);
        final FirebaseAuth mAuth= FirebaseAuth.getInstance();

        emailEditText=findViewById(R.id.editTextEmail);
        btn_reset=findViewById(R.id.resetPasswordButton);
        resetText=findViewById(R.id.resetPasswordText);



        pgsBar = findViewById(R.id.pBar);

        validate= new Validation();

        btn_reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v) {
                Animation reset_anim= AnimationUtils.loadAnimation(ResetPasswordActivity.this,R.anim.btn_anim);
                btn_reset.startAnimation(reset_anim);
                if (!IsInternet.checkInternet(ResetPasswordActivity.this))
                    return;

                String email= emailEditText.getEditableText().toString().trim();
                if(validate.validateEmail(emailEditText)){
                    pgbarshow();
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            InputMethodManager inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                            if(task.isSuccessful()){
                                Toasty.success(ResetPasswordActivity.this, "EMAIL SENT. PLEASE CHECK YOUR MAIL", Toast.LENGTH_SHORT, true).show();

                               // Toast.makeText(ResetPasswordActivity.this,"EMAIL SENT. PLEASE CHECK YOUR MAIL",Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(ResetPasswordActivity.this,MainActivity.class));



                            }
                            else
                            {
                                String error = task.getException().getMessage().split("\\.")[0];
                                Toasty.error(ResetPasswordActivity.this, error, Toast.LENGTH_LONG, true).show();

                                //Toast.makeText(ResetPasswordActivity.this,error,Toast.LENGTH_LONG).show();

                            }
                            pgbarhide();
                        }
                    });


                }

            }
        });

    }


}