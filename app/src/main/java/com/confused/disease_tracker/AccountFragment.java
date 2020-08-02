package com.confused.disease_tracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.confused.disease_tracker.authen.EditProfile;
import com.confused.disease_tracker.authen.Login;
import com.confused.disease_tracker.config.Config;
import com.confused.disease_tracker.helper.AlertHistoryListview;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.confused.disease_tracker.service.DataUpdateService;
import com.confused.disease_tracker.service.DetectorService;
import com.confused.disease_tracker.service.LocationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.protobuf.StringValue;
import com.squareup.picasso.Picasso;

import org.threeten.bp.LocalDateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {
    private static final int GALLERY_INTENT_CODE = 1023 ;
    private boolean shouldRefreshOnResume = false;
    private TextView fullName,email,phone, logout, editProfile, risk1, risk2, risk3, uploadDate, popup_info, uploadBtn;
    private Switch switch1, switch2;
    private FirebaseFirestore fStore;
    private FirebaseUser user;
    private ImageView profileImage;
    private StorageReference storageReference;
    private View inf;
    private DatabaseHelper sqLiteDatabase;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inf = inflater.inflate(R.layout.activity_profile, container, false);

        profileData();
        logoutBtn();
        editProfileBtn();
        riskNotification();
        uploadLocation();
        deviceAccess();

        return inf;
    }

    public void profileData(){
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user.isEmailVerified()){
            inf.findViewById(R.id.cardView1).setVisibility(View.GONE);
            inf.findViewById(R.id.cardView2).setVisibility(View.VISIBLE);
        }

        // Text
        phone = inf.findViewById(R.id.profilePhone);
        fullName = inf.findViewById(R.id.profileName);
        email = inf.findViewById(R.id.profileEmail);

        // ImageView
        profileImage = inf.findViewById(R.id.profileImage);

        // Loading Profile A Picture
        StorageReference profileRef = storageReference.child("user/" + user.getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        // Loading User Data
        DocumentReference documentReference = fStore.collection("user").document(user.getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    phone.setText(documentSnapshot.getString("phoneNum"));
                    fullName.setText(documentSnapshot.getString("name"));
                    email.setText(documentSnapshot.getString("email"));
                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });
    }

    public void logoutBtn(){
        logout = inf.findViewById(R.id.logoutBtn);

        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LocationService mYourService1 = new LocationService();
                DetectorService mYourService2 = new DetectorService();
                DataUpdateService mYourService3 = new DataUpdateService();
                Intent mServiceIntent1 = new Intent(getContext(), mYourService1.getClass());
                Intent mServiceIntent2 = new Intent(getContext(), mYourService2.getClass());
                Intent mServiceIntent3 = new Intent(getContext(), mYourService3.getClass());
                getActivity().stopService(mServiceIntent1);
                getActivity().stopService(mServiceIntent2);
                getActivity().stopService(mServiceIntent3);

                getActivity().finish();
                FirebaseAuth.getInstance().signOut();//logout
                startActivity(new Intent(getContext(), Login.class));

            }
        });
    }

    public void editProfileBtn(){
        editProfile = inf.findViewById(R.id.editProfileBtn);

        editProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),EditProfile.class);
                i.putExtra("name",fullName.getText().toString());
                i.putExtra("email",email.getText().toString());
                i.putExtra("phoneNum",phone.getText().toString());
                startActivityForResult(i, 1);
            }
        });
    }

    public void riskNotification(){
        sqLiteDatabase = new DatabaseHelper(getContext());
        risk1 = inf.findViewById(R.id.risk1);
        risk1.setText(String.valueOf(sqLiteDatabase.getAlertHistory(user.getUid(), 0).getCount()));
        risk2 = inf.findViewById(R.id.risk2);
        risk2.setText(String.valueOf(sqLiteDatabase.getAlertHistory(user.getUid(), 1).getCount()));
        risk3 = inf.findViewById(R.id.risk3);
        risk3.setText(String.valueOf(sqLiteDatabase.getAlertHistory(user.getUid()).getCount()));

        // Risk clickable
        inf.findViewById(R.id.low_risk).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AlertHistoryListview.class);
                i.putExtra("risk", "low_risk");
                startActivity(i);
            }
        });
        inf.findViewById(R.id.high_risk).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AlertHistoryListview.class);
                i.putExtra("risk", "high_risk");
                startActivity(i);
            }
        });
        inf.findViewById(R.id.all_risk).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AlertHistoryListview.class);
                i.putExtra("risk", "all_risk");
                startActivity(i);
            }
        });
    }

    public void uploadLocation(){
        // Alert dialog upload location info.
        popup_info = inf.findViewById(R.id.popup_info);
        popup_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder uploadInfo = new AlertDialog.Builder(getContext());
                uploadInfo.setTitle("การอัปโหลดตำแหน่ง");
                uploadInfo.setMessage("หากท่านเป็นผู้มีโอกาสติดเชื้อเนื่องจากได้รับการตรวจแล้ว หรือพบว่ามีอาการเหล่านี้ ท่านสามารถอัปโหลดข้อมูลตำแหน่งของท่านเพื่อแจ้งเตือนผู้ใช้งานท่านอื่น");
                uploadInfo.setNegativeButton("ปิด", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });

                uploadInfo.create().show();
            }
        });

        // Show last upload date
        uploadDate = inf.findViewById(R.id.uploadDate);
        Cursor row = sqLiteDatabase.getUploadDate(user.getUid());
        while (row.moveToNext()){
            uploadDate.setText(row.getString(3));
        }

        uploadBtn = inf.findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder uploadInfo = new AlertDialog.Builder(getContext());
                uploadInfo.setTitle("การอัปโหลดตำแหน่ง");
                uploadInfo.setMessage("เมื่อท่านกดอัปโหลดตำแหน่งที่ท่านเคยไปจะถูกอัปโหลดไปยังฐานข้อมูล แต่จะไม่ได้ระบุตัวตนของท่าน ตำแหน่งของท่านจะถูกนำไปแจ้งเตือนกับผู้ใช้งานท่านอื่น");
                uploadInfo.setPositiveButton("อัปโหลด", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // First time upload
                        if(sqLiteDatabase.getUploadDate(user.getUid()).getCount() == 0){
                            Map<String, Object> docData = new HashMap<>();
                            docData.put("patientDisease", Config.getDisease());
                            docData.put("patientName", fullName.getText());
                            docData.put("patientStatus", Config.getDefaultStausUpload());
                            docData.put("username", user.getEmail());
                            db.collection("patient")
                                    .add(docData)
                                    .addOnSuccessListener(new OnSuccessListener() {
                                        @Override
                                        public void onSuccess(Object o) {
                                            db.collection("patient")
                                                    .whereEqualTo("patientDisease", Config.getDisease())
                                                    .whereEqualTo("username", email.getText())
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> snaps) {
                                                            if (snaps.isSuccessful()) {
                                                                for (final QueryDocumentSnapshot snap : snaps.getResult()) {
                                                                    Map<String, Object> diseaseId = new HashMap<>();
                                                                    db.collection("user").document(user.getUid()).update("covid19_ID", snap.getId());
                                                                    Cursor row = sqLiteDatabase.getUserLocationDataByDate(user.getUid());
                                                                    while(row.moveToNext()){
                                                                        Map<String, Object> docData = new HashMap<>();
                                                                        docData.put("desc", "");
                                                                        docData.put("lat", row.getDouble(2));
                                                                        docData.put("lng", row.getDouble(3));
                                                                        docData.put("timestamp", row.getString(4).split("T")[0]+" "+row.getString(4).split("T")[1]);
                                                                        LocalDateTime localDateTime = LocalDateTime.parse(row.getString(4));
                                                                        Date date = new Date(localDateTime.getYear()-1900, localDateTime.getMonthValue()-1, localDateTime.getDayOfMonth(),localDateTime.getHour(), localDateTime.getMinute());
                                                                        docData.put("unixTimestamp", date.getTime());
                                                                        db.collection("patient/"+snap.getId()+"/location").add(docData);
                                                                        sqLiteDatabase.insertUploadDate(user.getUid(), snap.getId(), String.valueOf(LocalDateTime.now()).substring(0,16), row.getInt(0));
                                                                    }
                                                                }
                                                            } else {
                                                                Log.d("TAG", "Error getting documents: ", snaps.getException());
                                                            }
                                                            Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                                                            FragmentTransaction fragTransaction =   (getActivity()).getSupportFragmentManager().beginTransaction();
                                                            fragTransaction.detach(currentFragment);
                                                            fragTransaction.attach(currentFragment);
                                                            fragTransaction.commit();
                                                            Toast.makeText(getContext(), "อัปโหลดข้อมูลเสร็จสิ้น", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            Log.d("TAG", "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "อัปโหลดข้อมูลผิดพลาด: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.w("TAG", "Error writing document", e);
                                        }
                                    });
                        }else{
                            Cursor row = sqLiteDatabase.getUploadDate(user.getUid());
                            while(row.moveToNext()){
                                Cursor userLoc = sqLiteDatabase.getUserLocationDataByLID(user.getUid(), row.getInt(4));
                                Log.d("INSERT/PatientLoc", "Size loc: "+userLoc.getCount()+", last LID: "+row.getInt(4));
                                while(userLoc.moveToNext()){
                                    Map<String, Object> docData = new HashMap<>();
                                    docData.put("desc", "");
                                    docData.put("lat", userLoc.getDouble(2));
                                    docData.put("lng", userLoc.getDouble(3));
                                    docData.put("timestamp", userLoc.getString(4).split("T")[0]+" "+userLoc.getString(4).split("T")[1]);
                                    LocalDateTime localDateTime = LocalDateTime.parse(userLoc.getString(4));
                                    Date date = new Date(localDateTime.getYear()-1900, localDateTime.getMonthValue()-1, localDateTime.getDayOfMonth(),localDateTime.getHour(), localDateTime.getMinute());
                                    docData.put("unixTimestamp", date.getTime());
                                    db.collection("patient/"+row.getString(2)+"/location").add(docData);
                                    Log.i ("INSERT/PatientLoc", "last LID: "+row.getInt(4)+", LID: "+userLoc.getString(0));
                                    sqLiteDatabase.updateUploadDate(user.getUid(), row.getString(2), String.valueOf(LocalDateTime.now()).substring(0,16), userLoc.getInt(0));
                                }
                                Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                                FragmentTransaction fragTransaction =   (getActivity()).getSupportFragmentManager().beginTransaction();
                                fragTransaction.detach(currentFragment);
                                fragTransaction.attach(currentFragment);
                                fragTransaction.commit();
                                if(userLoc.getCount() != 0){
                                    Log.d("tag", "Data size by lid: "+userLoc.getCount());
                                    Toast.makeText(getContext(), "อัปโหลดข้อมูลเสร็จสิ้น", Toast.LENGTH_SHORT).show();
                                }else{
                                    Log.d("tag", "Data size by lid: "+userLoc.getCount());
                                    Toast.makeText(getContext(), "อัปโหลดข้อมูลไม่สำเร็จ เนื่องจากไม่มีตำแหน่งใหม่", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    }
                });
                uploadInfo.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                        dialog.dismiss();
                    }
                });
                uploadInfo.create().show();
            }
        });
    }

    public void deviceAccess(){
        switch1 = inf.findViewById(R.id.switch1);
        if(isGPSEnabled()){
            switch1.setChecked(true);
        }
        switch1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        switch2 = inf.findViewById(R.id.switch2);
        if(isMyServiceRunning(LocationService.class)){
            switch2.setChecked(true);
        }
        switch2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(switch2.isChecked()){
                    LocationService mYourService1 = new LocationService();
                    Intent mServiceIntent1 = new Intent(getContext(), mYourService1.getClass());
                    getActivity().startService(mServiceIntent1);
                    Toast.makeText(getContext(),"อนุญาตให้เก็บข้อมูลตำแหน่ง", Toast.LENGTH_SHORT).show();
                }else {
                    LocationService mYourService1 = new LocationService();
                    Intent mServiceIntent1 = new Intent(getContext(), mYourService1.getClass());
                    getActivity().stopService(mServiceIntent1);
                    Toast.makeText(getContext(),"ปิดการอนุญาตให้เก็บข้อมูลตำแหน่ง", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean isGPSEnabled (){
        LocationManager locationManager = (LocationManager)
                getContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running "+serviceClass.getName());
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                boolean passwordChanged = data.getBooleanExtra("passwordChanged", false);
                boolean emailChanged = data.getBooleanExtra("emailChanged", false);
                if(passwordChanged){
                    Setting.stopAllServices(getContext(), getActivity());
                    getActivity().finish();
                    startActivity(new Intent(getContext(), Login.class));
                }
                if(emailChanged){
                    Toast.makeText(getContext(), "เปลี่ยนอีเมลสำเร็จ", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getContext(), "กรุณาเข้าสู่ระบบใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
                    Setting.stopAllServices(getContext(), getActivity());
                    getActivity().finish();
                    startActivity(new Intent(getContext(), Login.class));
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

}
