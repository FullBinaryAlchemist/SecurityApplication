package com.example.securityapplication;

import android.graphics.drawable.AnimatedStateListDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getContext(), "CardView clicked event ", Toast.LENGTH_LONG).show();
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
