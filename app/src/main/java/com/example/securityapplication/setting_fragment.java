package com.example.securityapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class setting_fragment extends Fragment {
    Button sos;
    Button rate_us,scream,invite,privacy,community,aboutus,feedback; //added community
    String str="Check out TRATA, I use it to protect myself and the people I care about. Get it for free at \nhttp://innovatiivecreators.in/download/";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting,container,false);

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rate_us=getActivity().findViewById(R.id.rate_us);
        scream=getActivity().findViewById(R.id.screem);
        invite=getActivity().findViewById(R.id.invite);
        privacy=getActivity().findViewById(R.id.privacy);
        community=getActivity().findViewById(R.id.community);
        feedback=getActivity().findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:sachin.saw.13@gmail.com?subject=" + "Feedback" + "&body=" + "");
                intent.setData(data);
                startActivity(intent);
            }
        });
        aboutus=getActivity().findViewById(R.id.aboutus);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://innovatiivecreators.in/Privacy/");
                Intent priva = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(priva);
            }
        });
        scream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.info(getContext(), "This feature is coming soon...", Toast.LENGTH_SHORT, true).show();
            }
        });
        //Added about us activity intent
        aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aboutus= new Intent(getActivity(),AboutUs.class);
                startActivity(aboutus);
            }
        });
        //NOTE:changed to blog Community
        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://trataindia.blogspot.com"); //TODO:replace uri with blog url
                Intent priva = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(priva);
            }
        });

        rate_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Uri uri = Uri.parse("market://details?id="+"com.example.securityapplication");
                    Intent Rate = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(Rate);
                }
                catch (ActivityNotFoundException e)
                {
                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="+"com.example.securityapplication");
                    Intent Rate = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(Rate);
                }
            }
        });
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,str);
                intent.putExtra(Intent.EXTRA_SUBJECT,"TRATA : THE SECURITY APPLICATION");
                intent.setType("text/plain");
                startActivity(intent);
            }
        });
    }
}
