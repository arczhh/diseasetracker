package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.confused.disease_tracker.authen.Profile;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class SettingFragment extends Fragment {

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inf = inflater.inflate(R.menu.fragment_setting, container, false);
        Button myProfileBtn = inf.findViewById(R.id.myProfileBtn);

        myProfileBtn.setOnClickListener(mMyProfileBtn);

        return inf;
    }

    private View.OnClickListener mMyProfileBtn = new View.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(getContext(), Profile.class));
        }
    };

}
