package com.confused.disease_tracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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

    private static int SPLASH_TIME_OUT = 4000;
    private DatabaseHelper sqLiteDatabase;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        DataUpdateService mDataUpdateService = new DataUpdateService();
        Intent mServiceIntent1 = new Intent(this, mDataUpdateService.getClass());
        if (!isMyServiceRunning(mDataUpdateService.getClass())) {
            startService(mServiceIntent1);
        }
        setContentView(R.layout.activity_splash_screen);
        Setting.setWindow(this);
        sqLiteDatabase = new DatabaseHelper(this);
        hospitalLocation();
        final FirebaseAuth fAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = fAuth.getCurrentUser();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent act1 = new Intent(SplashScreen.this, Login.class);
                Intent act2 = new Intent(SplashScreen.this, Profile.class);
                Intent act3 = new Intent(SplashScreen.this, MainActivity.class);
                if(user == null){
                    startActivity(act1);
                    finish();
                }else if(!user.isEmailVerified()){
                    startActivity(act2);
                    finish();
                }else{
                    startActivity(act3);
                    finish();
                }

            }
        }, SPLASH_TIME_OUT);
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

    private void hospitalLocation() {
        sqLiteDatabase.dropHospital();
        db.collection("application").document("hospital")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        db.collection("hospital")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (final QueryDocumentSnapshot hospital : task.getResult()) {
                                                db.collection("hospital/" + hospital.getId() + "/responsible")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    String respDisease = "โรคที่รับผิดชอบ: ";
                                                                    int strLength = respDisease.length();
                                                                    for (QueryDocumentSnapshot resp : task.getResult()) {
                                                                        respDisease += resp.getData().get("diseaseName")+", ";
                                                                    }
                                                                    if(respDisease.length() == strLength){
                                                                        respDisease = "ไม่พบข้อมูล";
                                                                    }else{
                                                                        respDisease = respDisease.substring(0, respDisease.length() - 2);
                                                                    }
                                                                    sqLiteDatabase.insertHospital((String) hospital.getData().get("hospitalName"), (double) hospital.getData().get("lat"), (double) hospital.getData().get("lng"), respDisease);
                                                                } else {
                                                                    Log.d("TAG", "Error getting documents: ", task.getException());
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            Log.d("TAG", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

}
