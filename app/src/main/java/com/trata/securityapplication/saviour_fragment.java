package com.trata.securityapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trata.securityapplication.Helper.FirebaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class saviour_fragment extends Fragment {
    private RecyclerView mRecyclerView,mRecyclerView2;
    private ExampleAdapter mAdapter,mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager;
    private MaterialCardView m1,m2;
    private HashMap<String,String> hashMap;
    FirebaseHelper firebaseHelper;
    SQLiteDBHelper mydb = SQLiteDBHelper.getInstance(getContext());
    final ArrayList<exampleitem> exampleList = new ArrayList<>();
    ArrayList<exampleitem> exampleList2 = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saviour,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView2 = getActivity().findViewById(R.id.recyclerView2);
        mRecyclerView = getActivity().findViewById(R.id.recyclerView);
        m1=getActivity().findViewById(R.id.card_view);
        m2=getActivity().findViewById(R.id.card_view2);
  //add user here for active alerts
   //    active("sachin2 sav","5");


if(exampleList.size()==0) {
    //exampleList.add(new exampleitem("No-One", "All Are Safe in Your Regione :)"));
    m1.setVisibility(View.GONE);
    mRecyclerView.setVisibility(View.GONE);
}else {
    m1.setVisibility(View.VISIBLE);
    mRecyclerView.setVisibility(View.VISIBLE);
}

        hashMap=mydb.fetch_history();
        for ( Map.Entry<String, String> entry : hashMap.entrySet()) {
            String name = entry.getKey();
            String date = entry.getValue();
            history(name, date);
        }

        if(exampleList2.size()==0){
            exampleList2.add(new exampleitem("No History Present",""));
        }

        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new ExampleAdapter(exampleList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ExampleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(getContext(), "Item "+ position +" is clicked", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getContext(),recent_cards.class);
                history.exampleList=exampleList;
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });


        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter2 = new ExampleAdapter(exampleList2);
        mRecyclerView2.setLayoutManager(mLayoutManager);
        mRecyclerView2.setAdapter(mAdapter2);
        mAdapter2.setOnItemClickListener(new ExampleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(getContext(), "History item "+position+" is clicked ", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void active(String name, String distance) {
        exampleList.add(new exampleitem(name, "Distance: "+distance+" miter"));
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String strDate= formatter.format(date);
        mydb.addhistory(name,strDate);
        update_firebase(name,strDate);

    }

    private void update_firebase(String name, String strDate) {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.child("Users").child(firebaseUser.getUid()).child("history").child(name).setValue(strDate);
        Log.d("SOSActivity","Firebase : Data updated in firebase");
    }

    private void history(String name, String date) {
        exampleList2.add(new exampleitem(name, "Date: "+date));
    }

}
