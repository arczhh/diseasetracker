package com.confused.disease_tracker.datatype;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.confused.disease_tracker.helper.AlgorithmHelper;
import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;

public class Patient {
    private String patientID;
    private String patientName;
    private String patientDisease;
    private String patientStatus;
    private ArrayList<MyLocation> locations;
    ArrayList<LocationChecker> locationChecker;

    public Patient(String patientID, String patientName, String patientDisease, String patientStatus) {
        this.patientID = patientID;
        this.patientName = patientName;
        this.patientDisease = patientDisease;
        this.patientStatus = patientStatus;
        locations = new ArrayList<>();
        this.locationChecker = new ArrayList<>();
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientDisease() {
        return patientDisease;
    }

    public void setPatientDisease(String patientDisease) {
        this.patientDisease = patientDisease;
    }

    public String getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }

    public void setLocations(ArrayList<MyLocation> locations) {
        this.locations = locations;
    }

    public ArrayList<LocationChecker> getLocationChecker() {
        return locationChecker;
    }

    public void setLocationChecker(ArrayList<LocationChecker> locationChecker) {
        this.locationChecker = locationChecker;
    }

    public ArrayList<MyLocation> getLocations() {
        return locations;
    }

    public void addLocations(double lat, double lng, String timestamp) {
        LatLng location = new LatLng(lat, lng);
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp);
        this.locations.add(new MyLocation(location, localDateTime));
    }

    public void log(){
        Log.d("Patient", this.patientName+","+this.patientDisease+","+this.patientStatus);
        for (MyLocation e : locations) {
            Log.d("Patient", e.getLatLng()+","+e.getTimestamp());
        }
    }

    public Patient filterByDate(User user, int range){
        ArrayList<MyLocation> newLocation = new ArrayList<>();
        for(MyLocation usr : user.getLocations()) {
            for(MyLocation pat : locations) {
                boolean  flag = AlgorithmHelper.isInRange(usr.getTimestamp(), pat.getTimestamp(), range);
                if(flag) {
                    if(newLocation.size() != 0) {
                        if(!newLocation.contains(pat)) {
                            newLocation.add(pat);
                        }
                    }else {
                        newLocation.add(pat);
                    }

                }
            }
        }
        this.locations = newLocation;
        return this;
    }

    public Patient filterByDistance(User user, double dist1, double dist2, int min){
        ArrayList<MyLocation> newLocation = new ArrayList<>();
        for(MyLocation usr : user.getLocations()) {
            for(MyLocation pat : locations) {
                double dist = AlgorithmHelper.calDistance(usr.getLatLng().latitude, usr.getLatLng().longitude, pat.getLatLng().latitude, pat.getLatLng().longitude);
                boolean  flag1 = dist <= dist1 && dist > dist2;
                boolean  flag2 = dist <= dist2;
                if(flag1 && AlgorithmHelper.isInRange(usr.getTimestamp(), pat.getTimestamp(), min)) {
                    if(newLocation.size() != 0) {
                        if(!newLocation.contains(pat)) {
                            newLocation.add(pat);
                            locationChecker.add(new LocationChecker(this, usr, pat, dist, 0));
                        }
                    }else {
                        locationChecker.add(new LocationChecker(this, usr, pat, dist, 0));
                        newLocation.add(pat);
                    }
                }else if(flag2 && AlgorithmHelper.isInRange(usr.getTimestamp(), pat.getTimestamp(), min)){
                    if(newLocation.size() != 0) {
                        if(!newLocation.contains(pat)) {
                            newLocation.add(pat);
                            locationChecker.add(new LocationChecker(this, usr, pat, dist, 1));
                        }
                    }else {
                        locationChecker.add(new LocationChecker(this, usr, pat, dist, 1));
                        newLocation.add(pat);
                    }
                }
            }
        }
        this.locations = newLocation;
        return this;
    }

}
