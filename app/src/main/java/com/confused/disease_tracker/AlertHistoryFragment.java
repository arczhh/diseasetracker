package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.confused.disease_tracker.helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class AlertHistoryFragment extends Fragment {
    private DatabaseHelper sqLiteDatabase;
    private ListView listView;
    private ArrayList<String> strings = new ArrayList<>();

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sqLiteDatabase = new DatabaseHelper(getContext());
        View inf = inflater.inflate(R.menu.fragment_alerthistory, container, false);
        listView = (ListView) inf.findViewById(R.id.listView);
        viewLocationData();
        return inf;
    }

    public void viewLocationData(){
        Cursor res = sqLiteDatabase.getUserLocationData();
        if(res.getCount() == 0){
            return;
        }
        while (res.moveToNext()){
            strings.add(res.getDouble(1)+", " + res.getDouble(2));
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(arrayAdapter);
    }
}