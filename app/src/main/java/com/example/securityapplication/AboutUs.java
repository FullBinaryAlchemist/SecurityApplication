package com.example.securityapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class AboutUs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*View aboutPage = new AboutPage(this)
                .isRTL(false)
                //.addItem(getAppNameElement())
                .setImage(R.mipmap.ic_launcher)
                .setDescription("Trata\nAn application to keep your loved ones safe")
                .addItem(new Element().setTitle("Version 1.0.0"))
                .addGroup("Connect with us")
                .addEmail("pranavdave@innovatiivecreators.in")
                .addWebsite("http://www.innovatiivecreators.in/")
                .addFacebook("innovatiivecreators")
                .addItem(getCopyRightsElement())
                .create();*/

        setContentView(R.layout.activity_about_us);
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        TextView copyright= findViewById(R.id.copyright);
        copyright.setText(copyrights);
    }
    /*Element getAppNameElement(){
        Element appNameElement= new Element();
        appNameElement.setTitle("Trata");
        return appNameElement;
    }
    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), copyrights, Toast.LENGTH_SHORT).show();
            }
        });
        return copyRightsElement;
    }*/

}
