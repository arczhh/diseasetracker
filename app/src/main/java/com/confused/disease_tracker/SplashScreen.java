package com.confused.disease_tracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.authen.Profile;
import com.confused.disease_tracker.config.Config;
import com.confused.disease_tracker.datatype.MyLocation;
import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.confused.disease_tracker.service.DataUpdateService;
import com.confused.disease_tracker.service.DetectorService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;


public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_splash_screen);
        Setting.setWindow(this);
        final FirebaseAuth fAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = fAuth.getCurrentUser();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent act1 = new Intent(SplashScreen.this, Login.class);
                Intent act2 = new Intent(SplashScreen.this, Profile.class);
                Intent act3 = new Intent(SplashScreen.this, AgreementActivity.class);
                Intent act4 = new Intent(SplashScreen.this, MainActivity.class);

                if(user == null){
                    startActivity(act1);
                    finish();
                }else if(!user.isEmailVerified()){
                    startActivity(act2);
                    finish();
                }else if(user.isEmailVerified() && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                    startActivity(act3);
                    finish();
                }else{
                    startActivity(act4);
                    finish();
                }

            }
        }, Config.getSplashTimeOut());
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
