package com.example.securityapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class saviour_fragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saviour,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // init the CardView
        CardView cardView =  getView().findViewById(R.id.card_view);
        cardView.setRadius(20F); // set corner radius value
        // Implement onClickListener event on CardView
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext(),R.style.CustomDialogTheme);
        TextView title = new TextView(getContext());
        // You Can Customise your Title here
        title.setText("Coming Soon ...");
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(20, 60, 20, 60);
        title.setTextSize(80);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(40);

        builder.setCustomTitle(title);
        builder.setCancelable(false);

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    FragmentManager fragmentManager2 = getFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    home_fragment fragment2 = new home_fragment();
                    ;
                    fragmentTransaction2.replace(R.id.savior, fragment2);
                    fragmentTransaction2.commit();
                    BottomNavigationView mBottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
                    mBottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
                }catch (Exception e){
                    Log.d("Error","Some Error Occures");
                }

            }
        });



        builder.show();


        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), recent_cards.class);

                startActivity(i);






                //((saviour_fragment)context).overridePendingTransition(R.anim.fade_entry, R.anim.hold);

                //Toast.makeText(getContext(), "CardView clicked event ", Toast.LENGTH_LONG).show();
            }
        });





        CardView cardView1 = (CardView) getView().findViewById(R.id.card_view1);
        cardView1.setRadius(20F); // set corner radius value

        CardView cardView2 = (CardView) getView().findViewById(R.id.card_view2);

        cardView2.setRadius(20F); // set corner radius value

        CardView cardView3;
        cardView3 = (CardView) getView().findViewById(R.id.card_view3);
        cardView3.setRadius(20F); // set corner radius value
        // Implement onClickListener event on CardView
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getContext(), "CardView 1 clicked event ", Toast.LENGTH_LONG).show();
            }
        });



    }




}
