package com.confused.disease_tracker.datatype;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
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

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

public class User {
    private String userid;
    private ArrayList<MyLocation> locations;

    public User(String UID) {
        this.locations = new ArrayList<>();
        this.userid = UID;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public ArrayList<MyLocation> getLocations() {
        return locations;
    }

    public void addLocations(LatLng location, String timestamp) {
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp);
        this.locations.add(new MyLocation(location, localDateTime));
    }

    public void loc(Context context) {
        Log.d("User Class", this.userid);
        if(locations.size() != 0) {
            for (MyLocation e : locations) {
                Toast.makeText(context, String.format(e.getLatLng()+", TIMESTAMP: "+e.getTimestamp()), Toast.LENGTH_LONG).show();
                Log.d("User Class - Location", e.getLatLng()+","+e.getTimestamp());
            }
        }else{
            Log.d("User Class", "No location found.");
        }

    }
}
