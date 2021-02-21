package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private static final int GALLERY_PIC = 1;
    String mChatUser;
    String userName, online_status;

    Toolbar toolbar;

    FirebaseAuth mAuth;

    DatabaseReference mRootRef, mUserOnlineDatabase;

    private String Current_user_id;
    private StorageReference mImageStorage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PIC && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "message/" + Current_user_id + "/" + mChatUser;
            final String chat_user_re = "message/" + mChatUser + "/" + Current_user_id;

            DatabaseReference user_message_push = mRootRef.child("message").child(Current_user_id).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String download_url = Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", Current_user_id);

                        messagetype.setText("");

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_re + "/" + push_id , messageMap);

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    Log.e("CHAT ERROR ", databaseError.getMessage());
                                }
                            }
                        });

                }

                }
            });
        }
    }

    private ImageView sendbtn, addbtn;
    EditText messagetype;
    private SwipeRefreshLayout swipeRefreshLayout;


    RecyclerView chat_list;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private DatabaseReference mMessageDatabase;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private int itempos = 0;
    private String mLastKey = "";
    private String mPreKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle bundle = getIntent().getExtras();

        mChatUser = bundle.getString("uid");
        userName = bundle.getString("userName");
        online_status = bundle.getString("online");


        sendbtn = findViewById(R.id.send_id);
        addbtn = findViewById(R.id.add_id);
        messagetype = findViewById(R.id.message_id);
        swipeRefreshLayout = findViewById(R.id.swipe_);


        mAuth = FirebaseAuth.getInstance();
        Current_user_id = mAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(userName);


        mUserOnlineDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mImageStorage = FirebaseStorage.getInstance().getReference();
//        ActionBar actionBar = getSupportActionBar();
//
//        assert actionBar != null;
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowCustomEnabled(true);
//
//
//        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        assert inflater != null;
//        View action_bar_view = inflater.inflate(R.layout.custom_chat_app_bar, null);
//
//        actionBar.setCustomView(action_bar_view);


//


        // retrieving the messages

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mAdapter = new MessageAdapter(messagesList);

        chat_list = findViewById(R.id.chat_message);
        mLinearLayout = new LinearLayoutManager(this);

        chat_list.setHasFixedSize(true);
        chat_list.setLayoutManager(mLinearLayout);



        chat_list.setAdapter(mAdapter);

        loadMessages();





        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();

                if(online.equals("true")){

                }else{

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRootRef.child("chat").child(Current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map ChataddMap = new HashMap();

                    ChataddMap.put("seen", false);
                    ChataddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map ChatUserMap = new HashMap();
                    ChatUserMap.put("chat/" + Current_user_id + "/" + mChatUser, ChataddMap);
                    ChatUserMap.put("chat/" + mChatUser + "/" + Current_user_id, ChataddMap);

                    mRootRef.updateChildren(ChatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Log.e("CHAT ERROR", databaseError.getMessage().toString());
                            }


                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



//        addbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//
//
//                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PIC);
//            }
//        });





        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage ++;

                itempos = 0;

                loadMoreMessages();



            }
        });


    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("message").child(Current_user_id).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messagekey = dataSnapshot.getKey();




                if(!mPreKey.equals(messagekey)){
                    messagesList.add(itempos++, message);
                }else{

                    mPreKey = mLastKey;

                }

                if(itempos == 1){
                    mLastKey = messagekey;

                }



                mAdapter.notifyDataSetChanged();


                swipeRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("message").child(Current_user_id).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);


                itempos ++;

                if(itempos == 1){
                    String messagkey = dataSnapshot.getKey();
                    mLastKey = messagkey;
                    mPreKey = messagkey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                chat_list.scrollToPosition(messagesList.size() - 1);
                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(){

        String message = messagetype.getText().toString().trim();

        if(!TextUtils.isEmpty(message)){

            String curr_user_ref = "message/" + Current_user_id + "/" + mChatUser;
            String chat_user_ref = "message/" + mChatUser + "/" + Current_user_id;

            DatabaseReference user_message_push = mRootRef.child("message").child(Current_user_id).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", Current_user_id);

            messagetype.setText("");

            Map messageUserMap = new HashMap();
            messageUserMap.put(curr_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id , messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.e("CHAT ERROR ", databaseError.getMessage());
                    }
                }
            });
        }


    }
}
