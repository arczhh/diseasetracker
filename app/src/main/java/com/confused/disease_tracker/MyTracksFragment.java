package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.AlgorithmHelper;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyTracksFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseHelper sqLiteDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sqLiteDatabase = new DatabaseHelper(getContext());
        @SuppressLint("ResourceType") View v = inflater.inflate(R.menu.fragment_mytracks, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment == null){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng thailand = new LatLng(14.276868, 100.493645);
        mMap.addMarker(new MarkerOptions().position(thailand).title("Marker in Thailand"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(thailand,5));*/

        userLocationMarker();
    }

    public void userLocationMarker(){
        PolylineOptions userLocationLine = new PolylineOptions();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String dateCheck = String.valueOf(LocalDateTime.now()).substring(0,10);
        int[] color = AlgorithmHelper.getRandomIntegerBetweenRange(0, 255);

        // Animate to Thailand
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.276868, 100.493645),5));

        Cursor alertHistory = sqLiteDatabase.getAlertHistoryDays(user.getUid());
        while (alertHistory.moveToNext()){
            Log.d("Circle",alertHistory.getDouble(5)+", "+alertHistory.getDouble(6) );
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(alertHistory.getDouble(5), alertHistory.getDouble(6)))
                    .radius(50.0)
                    .strokeColor(Color.RED)
                    .strokeWidth(1f)
                    .fillColor(Color.argb(70,150,50,50))
            );
        }

        Cursor row = sqLiteDatabase.getUserLocationDataByDate(user.getUid());
        while(row.moveToNext()){
            if(row.getString(4).substring(0, 10) != dateCheck){
                dateCheck = row.getString(4).substring(0, 10);
                color = AlgorithmHelper.getRandomIntegerBetweenRange(0, 255);
            }
            mMap.addPolyline(userLocationLine
                    .add(new LatLng(row.getDouble(2), row.getDouble(3)))
                    .width(5f)
                    .color(Color.argb(150,color[0],color[1],color[2]))
            );
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(row.getDouble(2), row.getDouble(3)),18));
        }
    }
}