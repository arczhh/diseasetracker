package com.confused.disease_tracker.datatype;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;

public class Patient {
    private String patientName;
    private String patientDisease;
    private String patientStatus;
    private ArrayList<MyLocation> locations;

    public Patient(String patientName, String patientDisease, String patientStatus) {
        this.patientName = patientName;
        this.patientDisease = patientDisease;
        this.patientStatus = patientStatus;
        locations = new ArrayList<>();
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

    public ArrayList<Patient> getPatientLoc(User usr){
        for (MyLocation usrLoc : usr.getLocations()) {
            Log.d("UsrLoc", usrLoc.getLatLng()+","+usrLoc.getTimestamp());
            for (MyLocation patLoc : locations ) {
                Log.d("PatLoc", patLoc.getLatLng()+","+patLoc.getTimestamp());
            }
        }
        return null;
    }

}
