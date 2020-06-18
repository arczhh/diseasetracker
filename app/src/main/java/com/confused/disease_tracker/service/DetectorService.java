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
import com.confused.disease_tracker.config.Config;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        sqLiteDatabase = new DatabaseHelper(getApplicationContext());
        //createFakeUserLocation();
        if(!user.isAnonymous()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startMyOwnForeground();
            else
                startForeground(0, new Notification());

            Timer timer = new Timer ();
            TimerTask hourlyTask = new TimerTask () {
                @Override
                public void run () {
                    // your code here...
                    myUser = user();
                    Log.d("User","location size: "+myUser.getLocations().size());
                    patients = patient();
                    Log.d("Patient","location size: "+patients.size());
                    computeToInsert(Config.getCond1_distance(), Config.getCond2_distance(), Config.getRange_min());
                }
            };

            // schedule the task to run starting now and then every hour...
            timer.schedule (hourlyTask, 0l, Config.getDetectorService_period_timework());

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
        String NOTIFICATION_CHANNEL_ID = "com.confused.disease_tracker.service.DetectorService";
        String channelName = "DetectorService";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        @SuppressLint("WrongConstant") Notification notification = notificationBuilder.setOngoing(true)
                //.setContentTitle("App detector service")
                //.setPriority(NotificationManager.IMPORTANCE_MIN)
                //.setCategory(Notification.CATEGORY_SERVICE)
                //.setSmallIcon(R.drawable.ic_map_black_24dp)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .build();
        startForeground(1, notification);
    }

    public static long[] unixTimestamp() throws ParseException {
        long[] timestamp;
        Date date1 = new Date();
        LocalDateTime minusDate = LocalDateTime.now().minusDays(14);
        Date date2 = new Date(minusDate.getYear()-1900, minusDate.getMonthValue()-1, minusDate.getDayOfMonth(),minusDate.getHour(), minusDate.getMinute());
        Log.d("Unix", "Time1: " + date1.getTime());
        Log.d("Unix", "Time2: " + date2.getTime());
        timestamp = new long[]{date1.getTime(), date2.getTime()};
        return timestamp;
    }

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

    public static Patient filterEachPatient(User user, Patient patient, double dist1, double dist2, int min){
        patient.filterByDate(user, min);
        patient.filterByDistance(user, dist1, dist2, min);
        return patient;
    }

    public User user(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Cursor res = sqLiteDatabase.getUserLocationDataByDate(firebaseUser.getUid());
        User usr = new User(firebaseUser.getUid());
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
        Cursor user = sqLiteDatabase.getUserData(myUser.getUserid());
        while(user.moveToNext()){
            Cursor pat = sqLiteDatabase.getPatientData(user.getString(2));
            Log.d("Database/Patient","Patient location size: "+pat.getCount());
            if(pat.getCount() == 0){
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
        }
        return patients;
    }

    private void createFakeUserLocation(){
        ArrayList<MyLocation> myLocations = new ArrayList<>();

        // Day1
        myLocations.add(new MyLocation(new LatLng(16.4796388479, 102.803494934), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"08:25")));
        myLocations.add(new MyLocation(new LatLng(16.4796105965, 102.803846139), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"08:40")));
        myLocations.add(new MyLocation(new LatLng(16.4796743625, 102.804324257), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"08:50")));
        myLocations.add(new MyLocation(new LatLng(16.4800693059, 102.80433541), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"08:55")));
        myLocations.add(new MyLocation(new LatLng(16.4809494748, 102.804904595), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"09:05")));
        myLocations.add(new MyLocation(new LatLng(16.4810542117, 102.804828286), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"09:10")));
        myLocations.add(new MyLocation(new LatLng(16.4809001273, 102.80632218), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"14:25")));
        myLocations.add(new MyLocation(new LatLng(16.4806038055, 102.808280452), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"15:00")));
        myLocations.add(new MyLocation(new LatLng(16.4806912539, 102.80697263), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"15:05")));
        myLocations.add(new MyLocation(new LatLng(16.4810507982, 102.804879574), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"15:10")));
        myLocations.add(new MyLocation(new LatLng(16.4799849125, 102.804878909), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"18:00")));
        myLocations.add(new MyLocation(new LatLng(16.4793779146, 102.805222232), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"18:05")));
        myLocations.add(new MyLocation(new LatLng(16.4804133112, 102.80526707), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"18:30")));
        myLocations.add(new MyLocation(new LatLng(16.4810442377, 102.804796885), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(2)).substring(0,10)+"T"+"18:35")));

        // Day2
        myLocations.add(new MyLocation(new LatLng(16.4810445134, 102.80478476), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"07:30")));
        myLocations.add(new MyLocation(new LatLng(16.4804397884, 102.809134179), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"07:35")));
        myLocations.add(new MyLocation(new LatLng(16.4799747447, 102.813959706), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"07:40")));
        myLocations.add(new MyLocation(new LatLng(16.4797034358, 102.816635409), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"07:45")));
        myLocations.add(new MyLocation(new LatLng(16.4775619524, 102.820123087), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"07:50")));
        myLocations.add(new MyLocation(new LatLng(16.4778001778, 102.82232163), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"07:55")));
        myLocations.add(new MyLocation(new LatLng(16.4777523241, 102.822913785), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"08:00")));
        myLocations.add(new MyLocation(new LatLng(16.477297409, 102.823126527), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"08:30")));
        myLocations.add(new MyLocation(new LatLng(16.4765997486, 102.823284566), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"08:35")));
        myLocations.add(new MyLocation(new LatLng(16.4765740279, 102.823123633), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"08:45")));
        myLocations.add(new MyLocation(new LatLng(16.476540591, 102.82328993), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"11:45")));
        myLocations.add(new MyLocation(new LatLng(16.4765611675, 102.82305926), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"11:55")));
        myLocations.add(new MyLocation(new LatLng(16.4766074647, 102.823233032), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"16:05")));
        myLocations.add(new MyLocation(new LatLng(16.4773944522, 102.823238349), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"16:10")));
        myLocations.add(new MyLocation(new LatLng(16.4776928104, 102.822358584), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"16:15")));
        myLocations.add(new MyLocation(new LatLng(16.478497644, 102.819015879), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"16:25")));
        myLocations.add(new MyLocation(new LatLng(16.4779807639, 102.819651238), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"16:30")));
        myLocations.add(new MyLocation(new LatLng(16.4791748578, 102.818118407), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"17:25")));
        myLocations.add(new MyLocation(new LatLng(16.4798127206, 102.814921214), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"17:30")));
        myLocations.add(new MyLocation(new LatLng(16.4800819907, 102.812354565), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"17:35")));
        myLocations.add(new MyLocation(new LatLng(16.4804833389, 102.809178583), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"17:45")));
        myLocations.add(new MyLocation(new LatLng(16.4806287574, 102.807363738), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"17:55")));
        myLocations.add(new MyLocation(new LatLng(16.4810052128, 102.804824654), LocalDateTime.parse(String.valueOf(LocalDateTime.now().minusDays(1)).substring(0,10)+"T"+"18:00")));

        // Date fixed
        myLocations.add(new MyLocation( new LatLng(16.47061,102.827754), LocalDateTime.parse("2020-06-10T06:00")));
        myLocations.add(new MyLocation(new LatLng(16.464226,102.82829), LocalDateTime.parse("2020-06-10T09:17")));
        myLocations.add(new MyLocation(new LatLng(16.425247,102.80424), LocalDateTime.parse("2020-06-10T13:06")));
        myLocations.add(new MyLocation(new LatLng(16.474898,102.822701), LocalDateTime.parse("2020-06-10T14:33")));
        myLocations.add(new MyLocation(new LatLng(16.443783,102.812688), LocalDateTime.parse("2020-06-10T18:30")));
        myLocations.add(new MyLocation(new LatLng(16.463539,102.829479), LocalDateTime.parse("2020-06-10T21:42")));

        for(MyLocation e : myLocations){
            Cursor row = sqLiteDatabase.checkLocationInUserLocation(user.getUid(), e.getLatLng().latitude, e.getLatLng().longitude);
            if (row.getCount() != 0) {
                sqLiteDatabase.deleteFakeUserLocation(e.getLatLng().latitude, e.getLatLng().longitude);
            }else{
                sqLiteDatabase.insertUserLocationWithTime(user.getUid(), e.getLatLng().latitude, e.getLatLng().longitude, String.valueOf(e.getTimestamp()));
            }
        }

    }

}
