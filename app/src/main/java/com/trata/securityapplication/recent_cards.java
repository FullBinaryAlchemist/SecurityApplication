package com.trata.securityapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class recent_cards extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_cards);
    }
}
