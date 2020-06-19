package com.confused.disease_tracker;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.config.Config;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.confused.disease_tracker.service.DataUpdateService;
import com.confused.disease_tracker.service.DetectorService;
import com.confused.disease_tracker.service.LocationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Timer;
import java.util.TimerTask;


public class AgreementActivity  extends AppCompatActivity {
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstlaunch);
        Setting.setWindow(this);

        // Start DataUpdateService
        DataUpdateService mDataUpdateService = new DataUpdateService();
        Intent mServiceIntent1 = new Intent(getApplicationContext(), mDataUpdateService.getClass());
        if (!isMyServiceRunning(mDataUpdateService.getClass())) {
            startService(mServiceIntent1);
        }

        // Elements in layout
        acceptButton();
        cancelButton();
        storeUserData();
    }

    public void acceptButton(){
        Button button = findViewById(R.id.acceptBtn);
        CheckBox checkBox = findViewById(R.id.checkBox1);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!checkBox.isChecked()){
                    Toast.makeText(getApplicationContext(), "กรุณายอมรับข้อตกลงก่อนการใช้งาน", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    // Check user location if not dialog to allow will show
                    Setting.checkUserLocationPermission(AgreementActivity.this);

                    Timer timer = new Timer ();
                    TimerTask hourlyTask = new TimerTask () {
                        @Override
                        public void run () {
                            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                                new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message message) {
                                        Toast.makeText(getApplicationContext(), "อนุญาตแล้ว", Toast.LENGTH_SHORT ).show();
                                    }
                                };
                                startServices();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                                timer.cancel();
                            }
                        }
                    };

                    // schedule the task to run starting now and then every hour...
                    timer.schedule (hourlyTask, 0l, 1000);
                }
            }
        });
    }

    public void cancelButton(){
        Button button = findViewById(R.id.cancelBtn);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
                FirebaseAuth.getInstance().signOut();//logout
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }

    public void storeUserData(){
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseHelper sqLiteDatabase = new DatabaseHelper(getApplicationContext());

        DocumentReference documentReference = fStore.collection("user").document(user.getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    Cursor row = sqLiteDatabase.getUserData(user.getUid());
                    if(row.getCount() == 0){
                        sqLiteDatabase.insertUser(user.getUid(),documentSnapshot.getString("email"), documentSnapshot.getString("name"));
                    }
                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });
    }

    public void startServices(){

        // Start LocationService
        LocationService mLocationService = new LocationService();
        Intent mServiceIntent2 = new Intent(getApplicationContext(), mLocationService.getClass());
        if (!isMyServiceRunning(mLocationService.getClass())) {
            startService(mServiceIntent2);
        }

        // Start DetectorService
        DetectorService mDetectorService = new DetectorService();
        Intent mServiceIntent3 = new Intent(getApplicationContext(), mDetectorService.getClass());
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
        Log.i ("Service status", "Not running "+serviceClass.getName());
        return false;
    }

}
