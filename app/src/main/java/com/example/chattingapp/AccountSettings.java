package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.security.AccessControlContext;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettings extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser current_user;

    TextView display_name;
    TextView status;

    CircleImageView pfile_img;

    ImageView btn1;
    ImageView btn2;

    Toolbar toolbar;

    private static final int INTER = 1;


    private StorageReference mImageStorage;

    ProgressDialog dialog;

    private FirebaseAuth mAuth;


    private DatabaseReference mUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        pfile_img = findViewById(R.id.pfile_img_id);
        display_name = findViewById(R.id.textView2);
        status = findViewById(R.id.textView3);
        btn2 = findViewById(R.id.button3);
        toolbar = findViewById(R.id.tool_);
        btn1 = findViewById(R.id.button2);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        current_user = FirebaseAuth.getInstance().getCurrentUser();

        String currentUid = current_user.getUid();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status_ = dataSnapshot.child("status").getValue().toString();
                String thumb_img = dataSnapshot.child("thumb_img").getValue().toString();


                display_name.setText(name);
                status.setText(status_);

                Picasso.with(AccountSettings.this).load(image).into(pfile_img);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_text = status.getText().toString().trim();

                Intent intent = new Intent(AccountSettings.this, StatusActivity.class);
                intent.putExtra("status", status_text);
                startActivity(intent);
            }
        });


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent();
                galleryintent.setType("image/*");
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryintent, "Select Image"), INTER);

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(AccountSettings.this);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTER && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                dialog = new ProgressDialog(AccountSettings.this);
                dialog.setTitle("Uploading...");
                dialog.setMessage("Please Wait..");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                Uri resultUri = result.getUri();



                String uid = current_user.getUid();

                StorageReference filepath = mImageStorage.child("profile_images").child(uid + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            String download_url = task.getResult().getStorage().getDownloadUrl().toString();

                            mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        dialog.dismiss();

                                    }

                                }
                            });

                        } else {
                            dialog.dismiss();
                            Toast.makeText(AccountSettings.this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mUserRef.child("online").setValue(true);

    }

    @Override
    protected void onStop() {
        super.onStop();


    }
}
