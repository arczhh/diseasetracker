package com.confused.disease_tracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.confused.disease_tracker.service.DataUpdateService;
import com.confused.disease_tracker.service.DetectorService;
import com.confused.disease_tracker.service.LocationService;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Setting {

    private static final int PERMISSION_CALLBACK_CONSTANT = 105;
    private SharedPreferences permissionStatus;
    String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    public static void setWindow(Activity activity){
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, Activity activity, int vectorDrawableResourceId) {
        //Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_transparent);
        //background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(activity.getApplicationContext(), vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() , vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static boolean checkUserLocationPermission(Activity activity){
        String[] permissionsRequired = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
        if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
           if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
               ActivityCompat.requestPermissions(activity, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }else{
               ActivityCompat.requestPermissions(activity, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }
            return false;
        }else{
            return true;
        }
    }

    public static double covertDecimal(double src, int n){
        String srcToStr = Double.toString(src);
        String[] split = srcToStr.split("\\.");
        String dec = split[1]+"000000000000".substring(0,n);
        return Double.parseDouble(split[0]+"."+dec);
    }

    public static void stopAllServices(Context context, Activity activity){
        LocationService mYourService1 = new LocationService();
        DetectorService mYourService2 = new DetectorService();
        DataUpdateService mYourService3 = new DataUpdateService();
        Intent mServiceIntent1 = new Intent(context, mYourService1.getClass());
        Intent mServiceIntent2 = new Intent(context, mYourService2.getClass());
        Intent mServiceIntent3 = new Intent(context, mYourService3.getClass());
        activity.stopService(mServiceIntent1);
        activity.stopService(mServiceIntent2);
        activity.stopService(mServiceIntent3);
    }

}

