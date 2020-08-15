package com.confused.disease_tracker.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.confused.disease_tracker.AlertHistoryMapActivity;
import com.confused.disease_tracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AlertHistoryListview extends AppCompatActivity {
    private DatabaseHelper sqLiteDatabase;
    private ListView listView;
    private ArrayList<String> strings = new ArrayList<>();
    private ArrayList<Integer> risk = new ArrayList<>();
    private ArrayList<Integer> aid = new ArrayList<>();
    private ArrayList<Integer> isRead = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqLiteDatabase = new DatabaseHelper(getApplicationContext());
        setContentView(R.menu.history_list);
        TextView title = findViewById(R.id.title);
        listView = (ListView) findViewById(R.id.listView);

        if(getIntent().getStringExtra("risk").equals("low_risk")){
            title.setText("การแจ้งเตือนความเสี่ยงน้อย");
            low_risk();
        }else if(getIntent().getStringExtra("risk").equals("high_risk")){
            title.setText("การแจ้งเตือนความเสี่ยงมาก");
            high_risk();
        }else if(getIntent().getStringExtra("risk").equals("all_risk")){
            title.setText("การแจ้งเตือนทั้งหมด");
            all_risk();
        }
    }

    public void low_risk() {
        Cursor row = sqLiteDatabase.getAlertHistory(FirebaseAuth.getInstance().getCurrentUser().getUid(), 0);
        if (row.getCount() < 0) {
            Log.d("Database/Alert Hist", "No data found.");
        } else {
            Log.d("Database/Alert Hist", "Size " + row.getCount());
        }
        while (row.moveToNext()) {
            risk.add(row.getInt(9));
            aid.add(row.getInt(0));
            //String str = row.getString(1)+", "+row.getString(2)+", "+row.getString(3)+", "+row.getString(4)+", "+row.getString(5)+", "+row.getString(6)+", "+row.getString(7)+", "+row.getString(8)+", "+row.getString(9)+", "+row.getString(10);
            String str = row.getString(8);
            strings.add(str);
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, strings) {
            @SuppressLint("ResourceType")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (risk.get(position) == 0) {
                    view.setBackgroundColor(Color.argb(150,255,165,82));
                }
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
        //listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), AlertHistoryMapActivity.class);
                intent.putExtra("aid", aid.get(position));
                intent.putExtra("risk", getIntent().getStringExtra("risk"));
                startActivity(intent);
                finish();
            }
        });
    }

    public void high_risk() {
        Cursor row = sqLiteDatabase.getAlertHistory(FirebaseAuth.getInstance().getCurrentUser().getUid(), 1);
        if (row.getCount() < 0) {
            Log.d("Database/Alert Hist", "No data found.");
        } else {
            Log.d("Database/Alert Hist", "Size " + row.getCount());
        }
        while (row.moveToNext()) {
            risk.add(row.getInt(9));
            aid.add(row.getInt(0));
            //String str = row.getString(1)+", "+row.getString(2)+", "+row.getString(3)+", "+row.getString(4)+", "+row.getString(5)+", "+row.getString(6)+", "+row.getString(7)+", "+row.getString(8)+", "+row.getString(9)+", "+row.getString(10);
            String str = row.getString(8);
            strings.add(str);
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, strings) {
            @SuppressLint("ResourceType")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (risk.get(position) == 1) {
                    view.setBackgroundColor(Color.argb(150,255,56,56));
                }
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
        //listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), AlertHistoryMapActivity.class);
                intent.putExtra("aid", aid.get(position));
                intent.putExtra("risk", getIntent().getStringExtra("risk"));
                startActivity(intent);
                finish();
            }
        });
    }

    public void all_risk() {
        Cursor row = sqLiteDatabase.getAlertHistory(FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(row.getCount() < 0){
            Log.d("Database/Alert Hist","No data found.");
        }else{
            Log.d("Database/Alert Hist","Size "+row.getCount());
        }
        while (row.moveToNext()) {
            risk.add(row.getInt(9));
            aid.add(row.getInt(0));
            isRead.add(row.getInt(10));
            //String str = row.getString(1)+", "+row.getString(2)+", "+row.getString(3)+", "+row.getString(4)+", "+row.getString(5)+", "+row.getString(6)+", "+row.getString(7)+", "+row.getString(8)+", "+row.getString(9)+", "+row.getString(10);
            String str = row.getString(8);
            strings.add(str);
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, strings){
            @SuppressLint("ResourceType")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                if(risk.get(position) == 0){
                    if(isRead.get(position) == 0){
                        view.setBackgroundColor(Color.argb(200,255,165,82));
                    }
                    if(isRead.get(position) == 1){
                        view.setBackgroundColor(Color.argb(150,255,165,82));
                    }
                }else{
                    if(isRead.get(position) == 0){
                        view.setBackgroundColor(Color.argb(200,255,56,56));
                    }else{
                        view.setBackgroundColor(Color.argb(150,255,56,56));
                    }
                }
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
        //listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), AlertHistoryMapActivity.class);
                intent.putExtra("aid", aid.get(position));
                intent.putExtra("risk", getIntent().getStringExtra("risk"));
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }
}
