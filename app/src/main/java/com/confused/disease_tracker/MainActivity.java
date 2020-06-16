package com.confused.disease_tracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.location.LocationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.confused.disease_tracker.service.DetectorService;
import com.confused.disease_tracker.service.LocationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button mSwitch;
    Context context;
    Intent intent1;
    LocationManager locationManager;
    boolean GpsStatus;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Setting.setWindow(this);
        AndroidThreeTen.init(this);
        startService();

        //Function Testing Section------------------------------------------------------------------
        mSwitch = (Button)findViewById(R.id.switch1);
        context = getApplicationContext();
        CheckGpsStatus();
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent1);
            }
        });
        //------------------------------------------------------------------------------------------

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

    }

        //Test Status GPS --------------------------------------------------------------------------
        private void CheckGpsStatus() {
            locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            assert locationManager != null;
            GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(GpsStatus == true) {
                Toast.makeText(this, "GPS is Enabled",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS is Disabled",Toast.LENGTH_SHORT).show();
            }
        }
        //------------------------------------------------------------------------------------------

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

}