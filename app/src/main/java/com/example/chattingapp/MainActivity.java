package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    ViewPager viewPager;
    SectionPagerAdapter pagerAdapter;

    TabLayout tabLayout;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat App");

        if(mAuth.getCurrentUser() != null)
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        viewPager = findViewById(R.id.viewPager);

        pagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.main_tab);
        tabLayout.setupWithViewPager(viewPager);



    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();
        }else{

            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mAuth.getCurrentUser() != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
         //   mUserRef.child("lastseen").setValue(ServerValue.TIMESTAMP);
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_log_out){
            FirebaseAuth.getInstance().signOut();

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();
        }

        if(item.getItemId() == R.id.main_setting){

            Intent intent = new Intent(MainActivity.this, AccountSettings.class);
            startActivity(intent);
        }

        if(item.getItemId() == R.id.main_user){
            Intent intent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(intent);
        }

        return true;
    }
}
