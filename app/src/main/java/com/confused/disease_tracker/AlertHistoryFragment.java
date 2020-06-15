package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.confused.disease_tracker.datatype.LocationChecker;
import com.confused.disease_tracker.datatype.MyLocation;
import com.confused.disease_tracker.datatype.Patient;
import com.confused.disease_tracker.datatype.User;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.confused.disease_tracker.service.DetectorService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AlertHistoryFragment extends Fragment {
    private DatabaseHelper sqLiteDatabase;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView listView;
    private ArrayList<String> strings = new ArrayList<>();
    private ArrayList<Integer> risk = new ArrayList<>();
    private ArrayList<Integer> aid = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sqLiteDatabase = new DatabaseHelper(getContext());
        View inf = inflater.inflate(R.menu.history_list, container, false);
        listView = (ListView) inf.findViewById(R.id.listView);
        alertHistoryListView();
        return inf;
    }

    public void alertHistoryListView(){
        Cursor row = sqLiteDatabase.getAlertHistory(FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(row.getCount() < 0){
            Log.d("Database/Alert Hist","No data found.");
        }else{
            Log.d("Database/Alert Hist","Size "+row.getCount());
        }
        while (row.moveToNext()) {
            risk.add(row.getInt(9));
            aid.add(row.getInt(0));
            strings.add(row.getString(1)+", "+row.getString(2)+", "+row.getString(3)+", "+row.getString(4)+", "+row.getString(5)+", "+row.getString(6)+", "+row.getString(7)+", "+row.getString(8)+", "+row.getString(9)+", "+row.getString(10));
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, strings){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                if(risk.get(position) == 0){
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                }else{
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                }
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), AlertHistoryMapActivity.class);
                intent.putExtra("aid", aid.get(position));
                startActivity(intent);
            }
        });
    }

}