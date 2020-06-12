package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AlertHistoryFragment extends Fragment {

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inf = inflater.inflate(R.menu.fragment_alerthistory, container, false);

        BottomNavigationView bottomNav = (BottomNavigationView) inf.findViewById(R.id.top_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.alertHistFragment,
                    new HistoryListFragment()).commit();
        }
        return inf;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_list:
                            selectedFragment = new HistoryListFragment();
                            break;
                    }

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.alertHistFragment,
                            selectedFragment).commit();

                    return true;
                }
            };

}