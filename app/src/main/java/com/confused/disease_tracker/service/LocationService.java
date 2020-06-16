package com.confused.disease_tracker.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.confused.disease_tracker.MainActivity;
import com.confused.disease_tracker.R;
import com.confused.disease_tracker.Setting;
import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.datatype.LocationChecker;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    private DatabaseHelper sqLiteDatabase;
    private long refreshTime = 60*1000*1;
    private int minDistance = 10;
    private int[] majorDec = {3, 4};
    private int[] minorDec = {3, 3};
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if(!user.isAnonymous()){
            sqLiteDatabase = new DatabaseHelper(getApplication());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startMyOwnForeground();
            else
                startForeground(1, new Notification());
            listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Intent i = new Intent("location_update");
                    i.putExtra("coordinates", location.getLongitude() + " " + location.getLatitude());
                    insertLocation(location.getLatitude(),location.getLongitude());
                    sendBroadcast(i);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            };

            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            //noinspection MissingPermission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, refreshTime, minDistance, listener);
        }else{
            stopService(new Intent(this, LocationService.class));
            startActivity(new Intent(this, Login.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.confused.disease_tracker.service.LocationService";
        String channelName = "LocationService";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_map_black_24dp)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentText(user.getEmail())
                .build();
        startForeground(1, notification);
    }

    private void insertLocation(double lat, double lng){
        Cursor lastLoc = sqLiteDatabase.getUserLastLocationData(user.getUid());
        if(lastLoc.getCount() == 0){
            sqLiteDatabase.insertUserLocation(user.getUid(), lat, lng, 1);
        }else{
            if(isMajorFarFromLastDistance(lat, lng)){
                sqLiteDatabase.insertUserLocation(user.getUid(), lat, lng, 1);
                Toast.makeText(this, "Major Point: "+lat+", "+lng, Toast.LENGTH_SHORT).show();
            }else if(isMinor(lat, lng)){
                sqLiteDatabase.insertUserLocation(user.getUid(), lat, lng, 0);
                Toast.makeText(this, "Minor Point: "+lat+", "+lng, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isMajor(double lat, double lng){
        Cursor lastLoc = sqLiteDatabase.getUserLastLocationData(user.getUid());
        boolean flag = false;
        while (lastLoc.moveToNext()){
            flag = Setting.covertDecimal(lastLoc.getDouble(2), majorDec[0])-Setting.covertDecimal(lat, majorDec[0]) != 0 && Setting.covertDecimal(lastLoc.getDouble(3),majorDec[1])-Setting.covertDecimal(lng, majorDec[1]) != 0;
        }
        return flag;
    }

    private boolean isMinor(double lat, double lng){
        Cursor lastLoc = sqLiteDatabase.getUserLastLocationData(user.getUid());
        boolean flag = false;
        while (lastLoc.moveToNext()){
            flag = Setting.covertDecimal(lastLoc.getDouble(2), minorDec[0])-Setting.covertDecimal(lat, minorDec[0]) != 0 && Setting.covertDecimal(lastLoc.getDouble(3), minorDec[1])-Setting.covertDecimal(lng, minorDec[1]) != 0;
        }
        return flag;
    }

    private boolean isMajorFarFromLastDistance(double lat, double lng){
        Cursor lastMajorLoc = sqLiteDatabase.getUserLastMajorLocationData(user.getUid());
        boolean flag = false;
        while (lastMajorLoc.moveToNext()){
            flag = Setting.covertDecimal(lastMajorLoc.getDouble(2), majorDec[0])-Setting.covertDecimal(lat, majorDec[0]) != 0 && Setting.covertDecimal(lastMajorLoc.getDouble(3),majorDec[1])-Setting.covertDecimal(lng, majorDec[1]) != 0;
        }
        return flag;
    }
}