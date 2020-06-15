package com.confused.disease_tracker.service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.confused.disease_tracker.R;
import com.confused.disease_tracker.authen.Login;
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

import org.threeten.bp.LocalDateTime;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DetectorService extends Service {
    private DatabaseHelper sqLiteDatabase;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private User myUser;
    private ArrayList<Patient> patients;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int min = 60;
    private int period = 1000*60*1;
    private double dist1 = 0.15;
    private double dist2 = 0.075;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate() {
        sqLiteDatabase = new DatabaseHelper(getApplication());
        if(!user.isAnonymous()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startMyOwnForeground();
            else
                startForeground(1, new Notification());

            Timer timer = new Timer ();
            TimerTask hourlyTask = new TimerTask () {
                @Override
                public void run () {
                    // your code here...
                    myUser = user();
                    patients = patient();
                    computeToInsert(dist1, dist2, min);
                }
            };

            // schedule the task to run starting now and then every hour...
            timer.schedule (hourlyTask, 0l, period);   // 1000*10*60 every 10 minut

        }else{
            stopService(new Intent(this, DetectorService.class));
            startActivity(new Intent(this, Login.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.confused.disease_tracker";
        String channelName = "Detector";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App detector service")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_map_black_24dp)
                .build();
        startForeground(2, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long[] unixTimestamp() throws ParseException {
        long[] timestamp;
        DecimalFormat formatter = new DecimalFormat("00");
        Date date1 = new Date();
        LocalDateTime minusDate = LocalDateTime.now().minusDays(14);
        Date date2 = new Date(minusDate.getYear(), minusDate.getMonthValue(), minusDate.getDayOfMonth(),minusDate.getHour(), minusDate.getMinute());
        Log.d("Unix", "Time1: " + date1);
        Log.d("Unix", "Time2: " + date2);
        timestamp = new long[]{date1.getTime(), date2.getTime()};
        return timestamp;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void computeToInsert(double dist1, double dist2, int min){
        for(Patient pat : patients){
            pat = filterEachPatient(myUser, pat, dist1, dist2, min);
            for(LocationChecker locationChecker : pat.getLocationChecker()){
                Cursor row = sqLiteDatabase.getAlertHistory(myUser.getUserid(), pat.getPatientID(),
                        locationChecker.getUsrLocation().getLatLng().latitude, locationChecker.getUsrLocation().getLatLng().longitude,
                        locationChecker.getPatLocation().getLatLng().latitude, locationChecker.getPatLocation().getLatLng().longitude);
                if(row.getCount() == 0){
                    sqLiteDatabase.insertAlertHistory(myUser.getUserid(), pat.getPatientID(),
                            locationChecker.getUsrLocation().getLatLng().latitude, locationChecker.getUsrLocation().getLatLng().longitude,
                            locationChecker.getPatLocation().getLatLng().latitude, locationChecker.getPatLocation().getLatLng().longitude,
                            String.valueOf(locationChecker.getUsrLocation().getTimestamp()), locationChecker.getMsg(), locationChecker.getRisk());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Patient filterEachPatient(User user, Patient patient, double dist1, double dist2, int min){
        patient.filterByDate(user, min);
        patient.filterByDistance(user, dist1, dist2, min);
        return patient;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public User user(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Cursor res = sqLiteDatabase.getUserLocationDataByDate(firebaseUser.getUid());
        User usr = new User(firebaseUser.getUid());
        usr.addLocations(new LatLng(16.47061,102.827754), "2020-06-10T06:00");
        usr.addLocations(new LatLng(16.464226,102.82829), "2020-06-10T09:17");
        usr.addLocations(new LatLng(16.425247,102.80424), "2020-06-10T13:06");
        usr.addLocations(new LatLng(16.474898,102.822701), "2020-06-10T14:33");
        usr.addLocations(new LatLng(16.443783,102.812688), "2020-06-10T18:30");
        usr.addLocations(new LatLng(16.463539,102.829479), "2020-06-10T21:42");
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("LongLogTag")
    public ArrayList<Patient> patient(){
        Patient patient;
        ArrayList<Patient> patients = new ArrayList<>();;
        Cursor pat = sqLiteDatabase.getPatientData();
        if(pat.getCount() < 0){
            Log.d("Database/Patient","No data found.");
        }
        while (pat.moveToNext()) {
            patient = new Patient(pat.getString(0),pat.getString(1),pat.getString(2),pat.getString(3));
            Cursor patLoc = sqLiteDatabase.getPatientLocationData(pat.getString(0));
            while (patLoc.moveToNext()){
                Log.d("Database/Patient location", patLoc.getString(0) + ", " + patLoc.getString(1) + ", " + patLoc.getString(2)+ ", " + patLoc.getString(3)+ ", " + patLoc.getString(4));
                patient.addLocations(patLoc.getDouble(2), patLoc.getDouble(3), patLoc.getString(4));
            }
            patients.add(patient);
            Log.d("Database/Patient", pat.getString(1) + ", " + pat.getString(2) + ", " + pat.getString(3));
        }
        return patients;
    }

}
