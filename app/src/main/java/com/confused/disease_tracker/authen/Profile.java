package com.confused.disease_tracker.authen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.confused.disease_tracker.R;
import com.confused.disease_tracker.Setting;
import com.confused.disease_tracker.service.DataUpdateService;
import com.confused.disease_tracker.service.DetectorService;
import com.confused.disease_tracker.service.LocationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class Profile extends AppCompatActivity {
    private static final int GALLERY_INTENT_CODE = 1023 ;
    private TextView fullName,email,phone, status, logout, editProfile;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;
    private Button resendCodeBtn;
    private Button resetPassLocal;
    private FirebaseUser user;
    private ImageView profileImage;
    private StorageReference storageReference;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Setting.setWindow(this);

        /*backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email = findViewById(R.id.profileEmail);
        //resetPassLocal = findViewById(R.id.resetPasswordLocal);
        status = findViewById(R.id.profileStatusText);
        logout = findViewById(R.id.logoutBtn);

        profileImage = findViewById(R.id.profileImage);

        resendCodeBtn = (Button) findViewById(R.id.resendCode);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

       if(!user.isEmailVerified()) {
           resendCodeBtn.setVisibility(View.VISIBLE);
           findViewById(R.id.msg).setVisibility(View.VISIBLE);

           resendCodeBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           Toast.makeText(getApplicationContext(), "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                       }
                   });
               }
           });
       }

        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LocationService mYourService1 = new LocationService();
                DetectorService mYourService2 = new DetectorService();
                DataUpdateService mYourService3 = new DataUpdateService();
                Intent mServiceIntent1 = new Intent(getApplicationContext(), mYourService1.getClass());
                Intent mServiceIntent2 = new Intent(getApplicationContext(), mYourService2.getClass());
                Intent mServiceIntent3 = new Intent(getApplicationContext(), mYourService3.getClass());
                stopService(mServiceIntent1);
                stopService(mServiceIntent2);
                stopService(mServiceIntent3);
                finish();
                FirebaseAuth.getInstance().signOut();//logout
                startActivity(new Intent(getApplicationContext(), Login.class));

            }
        });

        StorageReference profileRef = storageReference.child("user/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });
        
        DocumentReference documentReference = fStore.collection("user").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    phone.setText(documentSnapshot.getString("phoneNum"));
                    fullName.setText(documentSnapshot.getString("name"));
                    email.setText(documentSnapshot.getString("email"));
                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });


        /*resetPassLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetPassword = new EditText(v.getContext());

                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password ?");
                passwordResetDialog.setMessage("Enter New Password > 6 Characters long.");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String newPassword = resetPassword.getText().toString();
                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Profile.this, "Password Reset Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Profile.this, "Password Reset Failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close
                    }
                });

                passwordResetDialog.create().show();

            }
        });

        /*changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery
                Intent i = new Intent(v.getContext(),EditProfile.class);
                i.putExtra("name",fullName.getText().toString());
                i.putExtra("email",email.getText().toString());
                i.putExtra("phoneNum",phone.getText().toString());
                startActivity(i);
            }
        });*/

        if(user.isEmailVerified()){
            status.setText("ยืนยันอีเมลแล้ว");
            status.setTextColor(Color.argb(200,127, 177, 116));
        }else {
            status.setText("ยังไม่ได้ทำการยืนยันอีเมล");
            status.setTextColor(Color.argb(200, 232, 30, 37));
            findViewById(R.id.uploadLocationSection1).setVisibility(View.GONE);
            findViewById(R.id.uploadLocationSection2).setVisibility(View.GONE);
            findViewById(R.id.deviceAccessSection1).setVisibility(View.GONE);
            findViewById(R.id.deviceAccessSection2).setVisibility(View.GONE);
        }
        editProfileBtn();
    }

    public void editProfileBtn(){
        editProfile = findViewById(R.id.editProfileBtn);

        editProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(),EditProfile.class);
                i.putExtra("name",fullName.getText().toString());
                i.putExtra("email",email.getText().toString());
                i.putExtra("phoneNum",phone.getText().toString());
                i.putExtra("status",status.getText().toString());
                startActivityForResult(i, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                boolean hasBackPressed = data.getBooleanExtra("hasBackPressed", false);
                if(hasBackPressed){
                    finish();
                    startActivity(getIntent());
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
}