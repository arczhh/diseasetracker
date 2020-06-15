package com.confused.disease_tracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import com.confused.disease_tracker.datatype.LocationChecker;
import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.confused.disease_tracker.service.DataUpdateService;
import com.confused.disease_tracker.service.DetectorService;
import com.confused.disease_tracker.service.LocationService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Setting.setWindow(this);
        AndroidThreeTen.init(this);

        startService();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }

    private void startService(){

        LocationService mLocationService = new LocationService();
        Intent mServiceIntent2 = new Intent(this, mLocationService.getClass());
        if (!isMyServiceRunning(mLocationService.getClass())) {
            startService(mServiceIntent2);
        }

        DetectorService mDetectorService = new DetectorService();
        Intent mServiceIntent3 = new Intent(this, mDetectorService.getClass());
        if (!isMyServiceRunning(mDetectorService.getClass())) {
            startService(mServiceIntent3);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_mytracks:
                            selectedFragment = new MyTracksFragment();
                            break;
                        case R.id.nav_alerthistory:
                            selectedFragment = new AlertHistoryFragment();
                            break;
                        case R.id.nav_setting:
                            selectedFragment = new SettingFlagment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running "+serviceClass.getName());
        return false;
    }
}