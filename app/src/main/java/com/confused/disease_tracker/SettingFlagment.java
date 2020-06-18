package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.confused.disease_tracker.authen.Profile;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class SettingFlagment extends Fragment {

    Switch mSwitch;
    Context context;
    Intent intent;
    LocationManager locationManager;
    boolean GpsStatus;

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inf = inflater.inflate(R.menu.fragment_setting, container, false);
        Button myProfileBtn = inf.findViewById(R.id.myProfileBtn);

        //Function Testing Section------------------------------------------------------------------
        mSwitch = (Switch) inf.findViewById(R.id.switch1);
        context = getContext();
        CheckGpsStatus();

        if (CheckGpsStatus() == true){
            mSwitch.setChecked(true);
            //Toast.makeText(this.context, "เปิดระบบนำนาง",Toast.LENGTH_SHORT).show();
        }else {
            mSwitch.setChecked(false);
            //Toast.makeText(this.context, "ปิดระบบนำทาง",Toast.LENGTH_SHORT).show();
        }

        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        //------------------------------------------------------------------------------------------

        myProfileBtn.setOnClickListener(mMyProfileBtn);

        return inf;
    }

    //Test Status GPS ------------------------------------------------------------------------------
    private boolean CheckGpsStatus() {
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(GpsStatus == true) {
            Toast.makeText(this.context, "เปิดระบบนำนาง",Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this.context, "ปิดระบบนำทาง",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //----------------------------------------------------------------------------------------------

    private View.OnClickListener mMyProfileBtn = new View.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(getContext(), Profile.class));
        }
    };


}