package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    Toolbar tool;

    TextInputEditText mStatus;
    Button btn;

    DatabaseReference mStatusDatabase;
    FirebaseUser mCurrentUser;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        tool = findViewById(R.id.toolbar);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);







        mStatus = findViewById(R.id.status_id);
        btn = findViewById(R.id.update_status_id);

        mStatus.setText(bundle.getString("status"));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ProgressDialog(StatusActivity.this);

                dialog.setTitle("Saving Changes");
                dialog.setMessage("Please Wait");
                dialog.show();

                String status_txt = mStatus.getText().toString().trim();

                mStatusDatabase.child("status").setValue(status_txt).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            dialog.dismiss();
                        }
                        else{
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Unable to process your request",Toast.LENGTH_SHORT).show();
                        }
                    }
                });




            }
        });


    }
}
