package com.trata.securityapplication;

import android.util.Log;

public class ZoneFetching {
    private static String newZonesList[]=new String[9];//CMS long,lat
    private static String newSubzonesList[]= new String[9];

    public static String[] getNewZonesList() {
        return newZonesList;
    }

    public static String[] getNewSubzonesList() {
        return newSubzonesList;
    }

    public static int getCount(){
        if(newZonesList[0]!=null){
            return newZonesList.length;
        }
        return -1;
    }

    public static int getNewMinute(int deg, int min, int diff){
        return (min+diff)%60;
    }
    //DOES NOT HANDLE THE CASE WHEN DEGREE IS 0
    public static int getNewDegree(int deg, int min, int diff){
        if(getNewMinute(deg,min,diff)!=(min+diff))
            deg= deg+ Math.abs(diff)/diff;
        return deg;
    }
    //input as comma separated string "77,57"-long,lat in deg , "59,57" long,lat in min
    public static void fetchAllSub(String zone, String subzone){
        int zones[]= new int[2];
        int subzones[]=new int[2];
        for(int i=0;i<zone.split(",").length;i++){
            zones[i]=Integer.parseInt(zone.split(",")[i]);
            subzones[i]=Integer.parseInt(subzone.split(",")[i]);

            if(subzones[i]%2!=0){
                subzones[i]-=1;
            }
        }
        int diff[]= {+2,0,-2};
        int ind=-1;
        for(int i=0;i<diff.length;i++){
            for(int j=0;j<diff.length;j++){
                String newZone=getNewDegree(zones[0],subzones[0],diff[i])+","+getNewDegree(zones[1], subzones[1], diff[j]);
                String newSubZone=getNewMinute(zones[0],subzones[0],diff[i])+","+getNewMinute(zones[1], subzones[1], diff[j]);
                Log.d("ZoneFetching","Diffs:"+diff[i]+","+diff[j]);
                Log.d("ZoneFetching","newZone:"+newZone);
                Log.d("ZoneFetching","newSubZone:"+newSubZone);

                newZonesList[++ind]=newZone;
                newSubzonesList[ind]=newSubZone;
            }

        }
    }
}
