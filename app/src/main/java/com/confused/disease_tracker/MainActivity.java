package com.confused.disease_tracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.authen.Profile;
import com.confused.disease_tracker.config.Config;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.confused.disease_tracker.helper.LoadingFragment;
import com.confused.disease_tracker.service.DataUpdateService;
import com.confused.disease_tracker.service.DetectorService;
import com.confused.disease_tracker.service.LocationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper sqLiteDatabase = new DatabaseHelper(getApplicationContext());
        //sqLiteDatabase.dropUpload();
        //sqLiteDatabase.dropAlertHistory();

        setContentView(R.layout.activity_main);
        Setting.setWindow(this);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        startServices();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new LoadingFragment()).commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //I added this if statement to keep the selected fragment when rotating the device
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                // Start DetectorService
                DetectorService mDetectorService = new DetectorService();
                Intent mServiceIntent3 = new Intent(getApplicationContext(), mDetectorService.getClass());
                if (!isMyServiceRunning(mDetectorService.getClass())) {
                    startService(mServiceIntent3);
                }
            }
        }, Config.getHomeFragmentSplashTimeOut());
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
                            selectedFragment = new AccountFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                boolean passwordChanged = data.getBooleanExtra("passwordChanged", false);
                boolean emailChanged = data.getBooleanExtra("emailChanged", false);
                if(passwordChanged | emailChanged ){
                    Setting.stopAllServices(getApplicationContext(), this);
                    finish();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void startServices(){
        // Start DataUpdateService
        DataUpdateService mDataUpdateService = new DataUpdateService();
        Intent mServiceIntent1 = new Intent(getApplicationContext(), mDataUpdateService.getClass());
        if (!isMyServiceRunning(mDataUpdateService.getClass())) {
            startService(mServiceIntent1);
        }

        // Start LocationService
        LocationService mLocationService = new LocationService();
        Intent mServiceIntent2 = new Intent(getApplicationContext(), mLocationService.getClass());
        if (!isMyServiceRunning(mLocationService.getClass())) {
            startService(mServiceIntent2);
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
        Log.i ("Service status", "Not running "+serviceClass.getName());
        return false;
    }

}