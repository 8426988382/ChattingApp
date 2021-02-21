package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText email;
    TextInputEditText pass;
    Button login_btn;

    ProgressDialog dialog;
    FirebaseAuth mAuth;
    TextView register_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email_id);
        pass = findViewById(R.id.password);
        login_btn = findViewById(R.id.login_btn);
        dialog = new ProgressDialog(this);

        register_text = findViewById(R.id.textView14);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = email.getText().toString().trim();
                String userPass = pass.getText().toString().trim();

                if(!TextUtils.isEmpty(userName) || !TextUtils.isEmpty(userPass)){
                    dialog.setTitle("Logging In");
                    dialog.setMessage("Please Wait while we check your credentials");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();


                    loginUser(userName, userPass);
                }

            }
        });

    }

    private void loginUser(String userName, String userPass) {

        mAuth.signInWithEmailAndPassword(userName, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    String user_id = mAuth.getCurrentUser().getUid();

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
                else{
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();

                }
            }
        });

        register_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

}
