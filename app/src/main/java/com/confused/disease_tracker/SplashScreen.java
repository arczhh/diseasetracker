package com.confused.disease_tracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.authen.Profile;
import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.DatabaseHelper;
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

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 4000;
    private DatabaseHelper sqLiteDatabase;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Setting.setWindow(this);
        sqLiteDatabase = new DatabaseHelper(this);
        downloadPatient();
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

    public void downloadPatient(){
        sqLiteDatabase.dropPatient();
        sqLiteDatabase.dropPatientLocation();
        db.collection("patient")
                .whereEqualTo("patientDisease","โควิด-19")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot patientSnap : task.getResult()) {
                                Log.d("Patient/Download", patientSnap.getId() + "," + patientSnap.getString("patientName") + ", " + patientSnap.getString("patientDisease") + ", " + patientSnap.getString("patientStatus"));
                                sqLiteDatabase.insertPatient(Integer.parseInt(patientSnap.getId()), patientSnap.getString("patientName"), patientSnap.getString("patientDisease"), patientSnap.getString("patientStatus"));
                                db.collection("patient/" + patientSnap.getId() + "/location")
                                        //.whereArrayContains(String.valueOf(java.time.LocalDate.now()), "timestamp")
                                        .orderBy("timestamp")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> locTask) {
                                                if (locTask.isSuccessful()) {
                                                    for (final QueryDocumentSnapshot patientLoc : locTask.getResult()) {
                                                        String[] split = patientLoc.getString("timestamp").split(" ");
                                                        String timestamp = split[0]+"T"+split[1];
                                                        sqLiteDatabase.insertPatientLocation(Integer.parseInt(patientSnap.getId()), Integer.parseInt(patientLoc.getId()),patientLoc.getDouble("lat"), patientLoc.getDouble("lng"), timestamp);
                                                    }
                                                } else {
                                                    Log.d("TAG", "Error getting documents: ", locTask.getException());
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
