package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivtiy extends AppCompatActivity {


    DatabaseReference mDatabase, mFriendsRequestDatabase, mFriendDatabase, notificationDatabase, mRootRef;
    private FirebaseUser mCurrentUser;

    TextView display_name, status, total_friends;
    Button friends_btn, cancel_btn;

    private ProgressDialog dialog;

    private String mCurrent_state;
    private FirebaseAuth mAuth;


    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();

        final String uid = bundle.getString("uid");

        dialog = new ProgressDialog(ProfileActivtiy.this);

        assert uid != null;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mFriendsRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();



        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        display_name = findViewById(R.id.textView6);
        status = findViewById(R.id.textView7);
        total_friends = findViewById(R.id.textView8);
        friends_btn = findViewById(R.id.button4);
        cancel_btn = findViewById(R.id.button5);


        mCurrent_state = "not_friends";

        cancel_btn.setVisibility(View.GONE);


        dialog.setTitle("Loading Data");
        dialog.setMessage("Please Wait");
        dialog.setCanceledOnTouchOutside(false);





        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("name").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();

                display_name.setText(userName);
                status.setText(userStatus);


                // Friend List Request Feature

                mFriendsRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(uid)){

                            String request_type = dataSnapshot.child(uid).child("request_type").getValue().toString();

                            if(request_type.equals("received")){

                                mCurrent_state = "req_received";
                                friends_btn.setText("Accept Friend Request");
                                cancel_btn.setVisibility(View.VISIBLE);
                            }
                            else if(request_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                friends_btn.setText("Cancel Friend Request");

                                cancel_btn.setVisibility(View.GONE);
                            }

                            dialog.dismiss();
                        }else{
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(uid)){
                                        mCurrent_state = "friends";
                                        friends_btn.setText("UnFriend this Person" );

                                        cancel_btn.setVisibility(View.GONE);
                                    }

                                    dialog.dismiss();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    dialog.dismiss();
                                }


                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        friends_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friends_btn.setEnabled(false);

                // Not Friend State

                if(mCurrent_state.equals("not_friends")){


                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(uid).push();
                    String new_notification_id = newNotificationref.getKey();

                    Map RequestMap = new HashMap();

                    RequestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + uid+  "/request_type", "sent");
                    RequestMap.put("Friend_req/" + uid + "/" + mCurrentUser.getUid() + "/request_type" , "received");
                    RequestMap.put("notifications/" + uid + "/" + new_notification_id, notificationData);

                    mRootRef.updateChildren(RequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(ProfileActivtiy.this, "Some Error Occurred" , Toast.LENGTH_SHORT).show();
                            }

                            friends_btn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            friends_btn.setText("Cancel Friend Request");

                        }
                    });
                }

                if(mCurrent_state.equals("req_received")){


                    final String curr_date = DateFormat.getDateTimeInstance().format(new Date());

                    Map FriendMap = new HashMap();
                    FriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + uid + "/date", curr_date);
                    FriendMap.put("Friends/" + uid + "/" + mCurrentUser.getUid() + "/date", curr_date);

                    FriendMap.put("Friend_req" + mCurrentUser.getUid() + "/" + uid, null);
                    FriendMap.put("Friend_req" + uid + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(FriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null) {

                                friends_btn.setEnabled(true);
                                mCurrent_state = "friends";
                                friends_btn.setText("UnFriend this person");

                                cancel_btn.setVisibility(View.GONE);

                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivtiy.this, error , Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }


                if(mCurrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + uid, null);
                    unfriendMap.put("Friends/" + uid + "/" + mCurrentUser.getUid(), null);


                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null) {


                                mCurrent_state = "not_friends";
                                friends_btn.setText("Send Friend Request");

                                cancel_btn.setVisibility(View.GONE);

                            }else{
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivtiy.this, error , Toast.LENGTH_SHORT).show();
                            }

                            friends_btn.setEnabled(true);

                        }
                    });

                }

            }
        });


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
