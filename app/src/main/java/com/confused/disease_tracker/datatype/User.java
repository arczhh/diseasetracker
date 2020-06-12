package com.confused.disease_tracker.datatype;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;


public class User {
    private String userid;
    private ArrayList<MyLocation> locations;

    public User(String UID) {
        this.locations = new ArrayList<>();
        this.userid = UID;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public ArrayList<MyLocation> getLocations() {
        return locations;
    }

    public void addLocations(LatLng location, String timestamp) {
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp);
        this.locations.add(new MyLocation(location, localDateTime));
    }

    public void log() {
        Log.d("User Class", this.userid);
        if(locations.size() != 0) {
            for (MyLocation e : locations) {
                Log.d("User Class - Location", e.getLatLng()+","+e.getTimestamp());
            }
        }else{
            Log.d("User Class", "No location found.");
        }

    }
}
