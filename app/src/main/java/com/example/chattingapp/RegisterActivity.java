package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText name,email,pass;
    Button btn;
    FirebaseAuth mAuth;

    ProgressDialog dialog;
    DatabaseReference database;

    TextView signintext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.reg_name);
        email = findViewById(R.id.reg_email);
        pass = findViewById(R.id.reg_password);
        btn = findViewById(R.id.button);
        signintext = findViewById(R.id.textView12);

        dialog = new ProgressDialog(this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int flag = 1;

                final String userName = name.getText().toString().trim();
                final String userEmail = email.getText().toString().trim();
                String userPass = pass.getText().toString().trim();

                if(userName.equals("")){
                    name.setError("Field cannot be empty");

                    flag = 0;
                }
                if(userPass.equals("")){
                    email.setError("Field cannot be empty");
                    flag = 0;
                }
                if(userEmail.equals("")){
                    pass.setError("Field cannot be empty");
                    flag = 0;
                }

                if(flag == 1){
                    Log.e("sFsfs", userName + userEmail + userPass);

                    dialog.setTitle("Registering User");
                    dialog.setMessage("Please wait while we create your account");
                    dialog.show();


                    mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {


                                        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                                        assert current_user != null;
                                        String uid = current_user.getUid();

                                        database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                                        String device_token = null;





                                        HashMap<String, String> userMap = new HashMap<>();

                                        userMap.put("name", userName);
                                        userMap.put("status","Hey! I am using Chat App");
                                        userMap.put("image", "default");
                                        userMap.put("thumb_img", "default");

                                        database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){
                                                    dialog.dismiss();
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d("SUCCESS", "createUserWithEmail:success");
                                                    //   FirebaseUser user = mAuth.getCurrentUser();

                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }

                                            }
                                        });

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("EXCEPTION", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }



            }
        });


        signintext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


    }




}
