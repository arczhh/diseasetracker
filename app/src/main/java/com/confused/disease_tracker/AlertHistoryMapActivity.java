package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AlertHistoryMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseHelper sqLiteDatabase;

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqLiteDatabase = new DatabaseHelper(getApplicationContext());
        Setting.setWindow(this);
        setContentView(R.menu.activity_alert_history_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        alertLocation();
    }

    public void alertLocation(){
        Cursor row = sqLiteDatabase.getAlertHistory(getIntent().getExtras().getInt("aid"));
        if(row.getCount() == 0) Log.d("Database", "Data not found.");
        while (row.moveToNext()) {
            LatLng usrLoc = new LatLng(row.getDouble(3), row.getDouble(4));

            //User
            mMap.addMarker(new MarkerOptions()
                    .position(usrLoc)
                    .title("ตำแหน่งของคุณ")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
            mMap.addCircle(new CircleOptions()
                    .center(usrLoc)
                    .radius(50.0)
                    .strokeColor(Color.GREEN)
                    .strokeWidth(1f)
                    .fillColor(Color.argb(70,50,150,50))
            );

            // Patient
            LatLng patLoc = new LatLng(row.getDouble(5), row.getDouble(6));
            mMap.addMarker(new MarkerOptions()
                    .position(patLoc)
                    .title("ตำแหน่งของผู้ป่วย")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );
            mMap.addCircle(new CircleOptions()
                    .center(patLoc)
                    .radius(50.0)
                    .strokeColor(Color.RED)
                    .strokeWidth(1f)
                    .fillColor(Color.argb(70,150,50,50))
            );

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng((usrLoc.latitude+patLoc.latitude)/2, (usrLoc.longitude+patLoc.longitude)/2),19));
        }
    }

}