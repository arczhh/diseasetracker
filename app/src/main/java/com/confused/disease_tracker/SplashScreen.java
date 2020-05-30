package com.confused.disease_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 4000;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseHelper sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Setting.setWindow(this);
        //startService(new Intent(this, BackgroundLocationUpdateService.class));
        sqLiteDatabase = new DatabaseHelper(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent act1 = new Intent(SplashScreen.this, Login.class);
                startActivity(act1);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
