package com.confused.disease_tracker.authen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.confused.disease_tracker.R;
import com.confused.disease_tracker.Setting;
import com.confused.disease_tracker.config.Config;
import com.confused.disease_tracker.helper.DatabaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText profileFullName,profileEmail,profilePhone;
    ImageView profileImageView;
    TextView changeProfilePictureBtn, profileStatusText;
    Button saveBtn,cancelProfileInfo,resetPassLocal;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Setting.setWindow(this);

        Intent data = getIntent();
        String fullName = data.getStringExtra("name");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phoneNum");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        profileFullName = findViewById(R.id.profileFullName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        profileImageView = findViewById(R.id.profileImage);
        profileStatusText = findViewById(R.id.profileStatusText);

        changeProfilePictureBtn = findViewById(R.id.changeProfilePictureBtn);

        resetPassLocal = findViewById(R.id.changePasswordBtn);
        saveBtn = findViewById(R.id.saveProfileInfo);
        cancelProfileInfo = findViewById(R.id.cancelProfileInfo);

        StorageReference profileRef = storageReference.child("user/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImageView);
            }
        });

        changeProfilePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        cancelProfileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profileFullName.getText().toString().isEmpty() || profileEmail.getText().toString().isEmpty() || profilePhone.getText().toString().isEmpty()){
                    //Toast.makeText(EditProfile.this, "One or Many fields are empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String newEmail = profileEmail.getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = fStore.collection("user").document(user.getUid());
                        Map<String,Object> edited = new HashMap<>();
                        edited.put("email",newEmail);
                        edited.put("name",profileFullName.getText().toString());
                        edited.put("phoneNum",profilePhone.getText().toString());
                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                Intent returnIntent = new Intent();
                                if(!newEmail.equals(email)){
                                    returnIntent.putExtra("emailChanged",true);
                                }
                                DatabaseHelper sqLiteDatabase = new DatabaseHelper(getApplicationContext());
                                Cursor userData = sqLiteDatabase.getUserData(user.getUid());
                                while (userData.moveToNext()){
                                    sqLiteDatabase.updateUser(user.getUid(), userData.getString(2), userData.getString(1));
                                    FirebaseFirestore.getInstance().collection("patient")
                                            .whereEqualTo("patientName", userData.getString(2))
                                            .whereEqualTo("patientDisease", Config.getDisease())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                                            /*FirebaseFirestore.getInstance().collection("patient").document(document.getId())
                                                                    .update("patientName", profileFullName.getText(),
                                                                            "username", newEmail);*/
                                                        }
                                                    } else {
                                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });

                                }
                                Cursor uploadDate = sqLiteDatabase.getUserData(user.getUid());
                                while (uploadDate.moveToNext()){
                                    sqLiteDatabase.updateUploadDate(user.getUid(), uploadDate.getString(2));
                                }
                                finish();
                            }
                        });
                        //Toast.makeText(EditProfile.this, "Email is changed.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this,   e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        resetPassLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = v.getContext();
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText resetPassword = new EditText(v.getContext());
                resetPassword.setHint("รหัสผ่าน");
                resetPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                layout.addView(resetPassword); // Notice this is an add method

                final EditText rePassword = new EditText(v.getContext());
                rePassword.setHint("กรุณากรอกรหัสผ่านอีกครั้ง");
                rePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                layout.addView(rePassword); // Another add method

                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("คุณต้องการเปลี่ยนรหัสผ่านใช่หรือไม่");
                passwordResetDialog.setView(layout);


                passwordResetDialog.setPositiveButton("เปลี่ยนรหัสผ่าน", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String newPassword = resetPassword.getText().toString();
                        String repassword = rePassword.getText().toString();

                        if(TextUtils.isEmpty(newPassword)){
                            Toast.makeText(getApplicationContext(), "กรุณากรอกรหัสผ่าน", Toast.LENGTH_SHORT).show();
                        }

                        if(newPassword.length() < 6){
                            Toast.makeText(getApplicationContext(), "รหัสต้องมีความยาวมากกว่าหรือเท่ากับ 6 อักขระ", Toast.LENGTH_SHORT).show();
                        }

                        if(TextUtils.isEmpty(repassword) || !newPassword.equals(repassword)){
                            Toast.makeText(getApplicationContext(), "กรุณากรอกรหัสผ่านให้ตรงกัน", Toast.LENGTH_SHORT).show();
                        }

                        if(!TextUtils.isEmpty(newPassword) && !(newPassword.length() < 6) && !TextUtils.isEmpty(repassword) && newPassword.equals(repassword)){
                            user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "เปลี่ยนรหัสผ่านสำเร็จ", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(), "กรุณาเข้าสู่ระบบใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("passwordChanged",true);
                                    setResult(Activity.RESULT_OK,returnIntent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "เปลี่ยนรหัสผ่านไม่สำเร็จ: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d("Error",e.getMessage());
                                }
                            });
                        }
                    }
                });

                passwordResetDialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                passwordResetDialog.create().show();
            }
        });

        profileEmail.setText(email);
        profileFullName.setText(fullName);
        profilePhone.setText(phone);
        if(user.isEmailVerified()){
            profileStatusText.setText("ยืนยันอีเมลแล้ว");
            profileStatusText.setTextColor(Color.argb(200,127, 177, 116));

        }else {
            profileStatusText.setText("ยังไม่ได้ทำการยืนยันอีเมล");
            profileStatusText.setTextColor(Color.argb(200, 232, 30, 37));
        }

        Log.d(TAG, "onCreate: " + fullName + " " + email + " " + phone);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();

                //profileImage.setImageURI(imageUri);

                uploadImageToFirebase(imageUri);

            }
        }

    }

    private void uploadImageToFirebase(Uri imageUri) {
        // uplaod image to firebase storage
        final StorageReference fileRef = storageReference.child("user/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(!user.isEmailVerified()){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("hasBackPressed",true);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }else{
            finish();
        }
    }

}