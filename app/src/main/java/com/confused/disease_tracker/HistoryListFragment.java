package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.confused.disease_tracker.datatype.LocationChecker;
import com.confused.disease_tracker.datatype.MyLocation;
import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HistoryListFragment extends Fragment {
    private DatabaseHelper sqLiteDatabase;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView listView;
    private ArrayList<String> strings = new ArrayList<>();
    private User myUser;
    private ArrayList<Patient> patients;
    private int min = 60;
    private double distance = 0.5;

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sqLiteDatabase = new DatabaseHelper(getContext());
        View inf = inflater.inflate(R.menu.history_list, container, false);
        listView = (ListView) inf.findViewById(R.id.listView);
        myUser = user();
        patients = patient();
        //viewPatientLocationData();
        //viewCalculateData(distance, min);
        patientListPure();
        return inf;
    }

    public void viewLocationData(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Cursor res = sqLiteDatabase.getUserLocationDataByDate(user.getUid());
        if(res.getCount() == 0){
            return;
        }
        while (res.moveToNext()){
            strings.add("User:"+res.getString(1)+" {"+res.getDouble(2)+", " + res.getDouble(3)+" ("+res.getString(4)+") } "+res.getString(5));
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(arrayAdapter);
    }

    public static ArrayList<Patient> filterPatient(User user, ArrayList<Patient> patients, double distance, int min){
        for(Patient e : patients) {
            e.filterByDate(user, min);
        }
        for(Patient e : patients) {
            e.filterByDistance(user, distance, min);
        }
        return patients;
    }

    public static Patient filterEachPatient(User user, Patient patient, double distance, int min){
        patient.filterByDate(user, min);
        patient.filterByDistance(user, distance, min);
        return patient;
    }

    public void viewCalculateData(double distance, int min){
        for(Patient pat : patients){
            strings.add(pat.getPatientName()+", "+pat.getPatientDisease()+", "+pat.getPatientStatus());
            strings.add("ข้อมูลตำแหน่งผู้ป่วย 14 วันที่ที่ผ่านมา");
            for(MyLocation e : pat.getLocations()){
                strings.add(pat.getPatientName()+"{ "+e.getLatLng()+" TIMESTAMP: "+ e.getTimestamp() +"}");
            }
            pat = filterEachPatient(myUser, pat, distance, min);
            strings.add("ข้อมูลเมื่อคำนวณแล้ว");
            for(LocationChecker locationChecker : pat.getLocationChecker()){
                strings.add(locationChecker.getMsg());
            }
            strings.add("-------------------");
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(arrayAdapter);
    }

    public User user(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Cursor res = sqLiteDatabase.getUserLocationDataByDate(firebaseUser.getUid());
        User usr = new User(firebaseUser.getUid());
        usr.addLocations(new LatLng(16.47061,102.827754), "2020-05-27T06:00");
        usr.addLocations(new LatLng(16.464226,102.82829), "2020-05-27T09:17");
        usr.addLocations(new LatLng(16.425247,102.80424), "2020-05-27T13:06");
        usr.addLocations(new LatLng(16.474898,102.822701), "2020-05-27T14:33");
        usr.addLocations(new LatLng(16.443783,102.812688), "2020-05-27T18:30");
        usr.addLocations(new LatLng(16.463539,102.829479), "2020-05-27T21:42");
        if (res.getCount() != 0) {
            while (res.moveToNext()) {
                LatLng loc = new LatLng(res.getDouble(2), res.getDouble(3));
                usr.addLocations(loc, res.getString(4));
            }
        }else{
            //Toast.makeText(getContext(), "No user location data.", Toast.LENGTH_LONG).show();
        }
        return usr;
    }

    @SuppressLint("LongLogTag")
    public ArrayList<Patient> patient(){
        Patient patient;
        ArrayList<Patient> patients = new ArrayList<>();;
        Cursor pat = sqLiteDatabase.getPatientData();
        if(pat.getCount() < 0){
            Log.d("Database/Patient","No data found.");
        }
        while (pat.moveToNext()) {
            patient = new Patient(pat.getString(1),pat.getString(2),pat.getString(3));
            Cursor patLoc = sqLiteDatabase.getPatientLocationData(pat.getInt(0));
            while (patLoc.moveToNext()){
                Log.d("Database/Patient location", patLoc.getString(0) + ", " + patLoc.getString(1) + ", " + patLoc.getString(2)+ ", " + patLoc.getString(3)+ ", " + patLoc.getString(4));
                patient.addLocations(patLoc.getDouble(2), patLoc.getDouble(3), patLoc.getString(4));
            }
            patients.add(patient);
            Log.d("Database/Patient", pat.getString(1) + ", " + pat.getString(2) + ", " + pat.getString(3));
        }
        return patients;
    }

    @SuppressLint("LongLogTag")
    public void patientListPure(){
        Cursor pat = sqLiteDatabase.getPatientData();
        if(pat.getCount() < 0){
            Log.d("Database/Patient","No data found.");
        }
        while (pat.moveToNext()) {
            strings.add((pat.getString(1)+", "+pat.getString(2)+", "+pat.getString(3)));
            Cursor patLoc = sqLiteDatabase.getPatientLocationData(pat.getInt(0));
            while (patLoc.moveToNext()){
                strings.add(patLoc.getString(0) + ", " + patLoc.getString(1) + ", " + patLoc.getString(2)+ ", " + patLoc.getString(3)+ ", " + patLoc.getString(4));
            }
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(arrayAdapter);
    }
}
