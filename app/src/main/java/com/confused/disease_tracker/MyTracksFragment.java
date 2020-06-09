package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.confused.disease_tracker.datatype.MyLocation;
import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyTracksFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseHelper sqLiteDatabase;
    private User myUser;
    private ArrayList<Patient> patients;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sqLiteDatabase = new DatabaseHelper(getContext());
        @SuppressLint("ResourceType") View v = inflater.inflate(R.menu.fragment_mytracks, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment == null){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        myUser = user();
        patients = patient();
        Log.d("TAG", "Patients: "+patients.size());
        Log.d("TAG", "Usr on CreateView: "+myUser.getUserid());
        myUser.loc(getContext());
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng thailand = new LatLng(14.276868, 100.493645);
        mMap.addMarker(new MarkerOptions().position(thailand).title("Marker in Thailand"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(thailand,5));*/

        userLocationMarker();
    }

    public void userLocationMarker(){
        PolylineOptions userLocationLine = new PolylineOptions();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        double[] animateMove = {0,0};
        Cursor res = sqLiteDatabase.getUserLocationData(user.getUid());
        Cursor track = sqLiteDatabase.getUserLocationTrack(user.getUid());
        if(res.getCount() == 0){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.276868, 100.493645),5));
        }
        while (res.moveToNext()){
            /*mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(res.getDouble(2), res.getDouble(3)))
                    .icon(Setting.bitmapDescriptorFromVector(getContext(), R.drawable.ic_transparent))
            );*/
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(res.getDouble(2), res.getDouble(3)))
                    .radius(50.0)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(1f)
                    .fillColor(Color.argb(70,50,150,50))
            );
        }
        if(track.getCount() == 0){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.276868, 100.493645),5));
        }else{
            while (track.moveToNext()) {
                animateMove[0] = track.getDouble(2);
                animateMove[1] = track.getDouble(3);
                mMap.addPolyline(userLocationLine
                        .add(new LatLng(track.getDouble(2), track.getDouble(3)))
                        .width(3f)
                        .color(Color.RED)
                );

            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(animateMove[0], animateMove[1]), 15));
        }
    }

    public User user(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Cursor res = sqLiteDatabase.getUserLocationDataByDate(firebaseUser.getUid());
        User usr = new User(firebaseUser.getUid());
        Log.d("user func", "Data/"+res.getCount());
        if (res.getCount() != 0) {
            while (res.moveToNext()) {
                LatLng loc = new LatLng(res.getDouble(2), res.getDouble(3));
                Log.d("user func", "LatLng/" + loc + " TIMESTAMP/"+ res.getString(4));
                usr.addLocations(loc, res.getString(4));
            }
        }
        return usr;
    }

    public ArrayList<Patient> patient(){
        Cursor pat = sqLiteDatabase.getPatientData();
        Cursor patloc = sqLiteDatabase.getPatientLocationData();
        Patient patient;
        ArrayList<Patient> patients = new ArrayList<>();;
        Log.d("TAG", "Data count: "+pat.getCount());
        if(pat.getCount() == 0){
            return patients;
        }else{
            while (pat.moveToNext()){
                Log.d("Patient location", "Data count: "+patloc.getCount());
                if(patloc.getCount() == 0){
                    return patients;
                }else{
                    patient = new Patient(pat.getString(1),pat.getString(2),pat.getString(3));
                    while (patloc.moveToNext()){
                        patient.addLocations(patloc.getDouble(2), patloc.getDouble(3), patloc.getString(4));
                    }
                    patients.add(patient);
                }
            }
            //sqLiteDatabase.dropPatient();
            //sqLiteDatabase.dropPatientLocation();
            return patients;
        }
    }
}