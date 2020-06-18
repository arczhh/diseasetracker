package com.confused.disease_tracker.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.confused.disease_tracker.R;
import com.confused.disease_tracker.Setting;
import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.config.Config;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

public class DataUpdateService  extends Service {
    private DatabaseHelper sqLiteDatabase;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String name;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        sqLiteDatabase = new DatabaseHelper(getApplication());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startMyOwnForeground();
            else
                startForeground(1, new Notification());

            Timer timer = new Timer ();
            TimerTask hourlyTask = new TimerTask () {
                @Override
                public void run () {
                    // your code here...
                    downloadPatient();
                }
            };

            // schedule the task to run starting now and then every hour...
            timer.schedule (hourlyTask, 0l, Config.getDataUpdateService_period_timework());   // 1000*10*60 every 10 minute
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.confused.disease_tracker.service.DataUpdateService";
        String channelName = "DataUpdateService";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        @SuppressLint("WrongConstant") Notification notification = notificationBuilder.setOngoing(true)
                //.setContentTitle("App data update service")
                //.setPriority(NotificationManager.IMPORTANCE_MIN)
                //.setCategory(Notification.CATEGORY_SERVICE)
                //.setSmallIcon(R.drawable.ic_map_black_24dp)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .build();
        startForeground(0, notification);
    }

    public void downloadPatient(){
            sqLiteDatabase.dropPatient();
            sqLiteDatabase.dropPatientLocation();
            db.collection("patient")
                    .whereEqualTo("patientDisease",Config.getDisease())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (final QueryDocumentSnapshot patientSnap : task.getResult()) {
                                    Log.d("Patient/Download", patientSnap.getId() + "," + patientSnap.getString("patientName") + ", " + patientSnap.getString("patientDisease") + ", " + patientSnap.getString("patientStatus"));
                                    sqLiteDatabase.insertPatient(patientSnap.getId(), patientSnap.getString("patientName"), patientSnap.getString("patientDisease"), patientSnap.getString("patientStatus"));
                                    try {
                                        long[] unixTimestamp = DetectorService.unixTimestamp();
                                        Log.d("Patient/Unix time", unixTimestamp[0]+", "+unixTimestamp[1]);
                                        db.collection("patient/" + patientSnap.getId() + "/location")
                                                .orderBy("unixTimestamp")
                                                .whereLessThanOrEqualTo("unixTimestamp", unixTimestamp[0])
                                                .whereGreaterThanOrEqualTo("unixTimestamp", unixTimestamp[1])
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> locTask) {
                                                        if (locTask.isSuccessful()) {
                                                            for (final QueryDocumentSnapshot patientLoc : locTask.getResult()) {
                                                                String[] split = patientLoc.getString("timestamp").split(" ");
                                                                String timestamp = split[0]+"T"+split[1];
                                                                Log.d("PatientLoc/Download", patientSnap.getId()+","+patientLoc.getId()+","+patientLoc.getDouble("lat")+","+ patientLoc.getDouble("lng")+","+timestamp);
                                                                sqLiteDatabase.insertPatientLocation(patientSnap.getId(), patientLoc.getId(),patientLoc.getDouble("lat"), patientLoc.getDouble("lng"), timestamp);
                                                            }
                                                        } else {
                                                            Log.d("TAG", "Error getting documents: ", locTask.getException());
                                                        }
                                                    }
                                                });
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
}

