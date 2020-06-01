package com.confused.disease_tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.confused.disease_tracker.helper.CustomInfoWindowAdapter;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.confused.disease_tracker.helper.DialogPopup;
import com.confused.disease_tracker.helper.FontManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 500;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    private Marker currentUserLocationMarker;
    private final static int Request_User_Location_Code = 99;
    private View locationButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseHelper sqLiteDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        @SuppressLint("ResourceType") View view = inflater.inflate(R.menu.fragment_home, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment == null){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        sqLiteDatabase = new DatabaseHelper(getContext());
        locationButton = (View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent();
        // Change the visibility of my location button
        if(locationButton != null){
            locationButton.setVisibility(View.GONE);
        }
        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        @SuppressLint("WrongViewCast") FontManager infoBtn = (FontManager) view.findViewById(R.id.popup_info);
        infoBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DialogPopup dialogPopup = new DialogPopup();
                dialogPopup.show(getActivity().getSupportFragmentManager(), "Dialog Popup");
            }
        });
        @SuppressLint("WrongViewCast") ImageView gpsBtn = (ImageView) view.findViewById(R.id.gpsBtn);
        gpsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mMap != null)
                {
                    if(locationButton != null)
                        locationButton.findViewById(Integer.parseInt("2")).callOnClick();
                }
            }
        });
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.276868,100.493645 ),5));
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getContext()));
            patientLocation();
            getHospitalData();
        }
    }


    protected void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case  Request_User_Location_Code:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if(googleApiClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(getContext(), "Permission Denied...", Toast.LENGTH_SHORT);
                }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1100);
        mLocationRequest.setFastestInterval(1100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        /*if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if(currentUserLocationMarker != null){
            currentUserLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //insertLocation(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        if(googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void insertLocation(double lat, double lng){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Cursor lastLoc = sqLiteDatabase.getUserLastLocationData(user.getUid());
        if(lastLoc.getCount() == 0){
            sqLiteDatabase.insertUserLocation(user.getUid(), lat, lng, 1);
        }else{
            while (lastLoc.moveToNext()){
                double lastLat = Setting.covertDecimal(lastLoc.getDouble(2),4);
                double lastLng = Setting.covertDecimal(lastLoc.getDouble(3),4);
                double currentLat = Setting.covertDecimal(lat,4);
                double currentLng = Setting.covertDecimal(lng,4);
                if(lastLat-currentLat != 0 && lastLng-currentLng != 0){
                    sqLiteDatabase.insertUserLocation(user.getUid(), lat, lng, 1);
                }else{
                    if(Setting.covertDecimal(lastLoc.getDouble(2),6)-Setting.covertDecimal(lat,6) != 0 && Setting.covertDecimal(lastLoc.getDouble(3),6)-Setting.covertDecimal(lng,6) != 0){
                        sqLiteDatabase.insertUserLocation(user.getUid(), lat, lng, 0);
                    }
                }
            }
        }
    }

    private void patientLocation() {
        db.collection("patient")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("patient/" + document.getId() + "/location")
                                        .orderBy("timestamp")
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot location : task.getResult()) {

                                                        mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng((double) location.getData().get("lat"), (double) location.getData().get("lng")))
                                                                .title((String) location.getData().get("timestamp"))
                                                                .snippet("Test")
                                                                .icon(Setting.bitmapDescriptorFromVector(getContext(), R.drawable.ic_patient)));
                                                    }
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

    public void getHospitalData(){
        Cursor res = sqLiteDatabase.getHospitalData();
        if(res.getCount() == 0){
            return;
        }
        while (res.moveToNext()){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(res.getDouble(2), res.getDouble(3)))
                    .title(res.getString(1))
                    .snippet(res.getString(4))
                    .icon(Setting.bitmapDescriptorFromVector(getContext(), R.drawable.ic_hospital)));
        }
    }
}
