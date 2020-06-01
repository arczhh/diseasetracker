package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class HistoryListFragment extends Fragment {
    private DatabaseHelper sqLiteDatabase;
    private ListView listView;
    private ArrayList<String> strings = new ArrayList<>();

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sqLiteDatabase = new DatabaseHelper(getContext());
        View inf = inflater.inflate(R.menu.history_list, container, false);
        listView = (ListView) inf.findViewById(R.id.listView);
        viewLocationData();
        return inf;
    }

    public void viewLocationData(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Cursor res = sqLiteDatabase.getUserLocationTrack(user.getUid());
        if(res.getCount() == 0){
            return;
        }
        while (res.moveToNext()){
            strings.add("User:"+res.getString(1)+" {"+res.getDouble(2)+", " + res.getDouble(3)+" ("+res.getString(4)+") } "+res.getString(5));
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(arrayAdapter);
    }
}
