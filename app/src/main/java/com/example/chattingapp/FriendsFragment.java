package com.example.chattingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class FriendsFragment extends Fragment {


    RecyclerView mlistView;

    FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private DatabaseReference mFriendDatabase, mUsersDatabase;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v;

        v = inflater.inflate(R.layout.fragment_friends, container, false);


        mlistView = v.findViewById(R.id.friend_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mlistView.setHasFixedSize(true);
        mlistView.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendDatabase, Friends.class)
                        .build();


        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
//                holder.setStatus(model.getDate());


                final String user_id = getRef(position).getKey();

                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();

                        String online = null;

                        if(dataSnapshot.hasChild("online")){

                           online  = dataSnapshot.child("online").getValue().toString();


                            holder.setImage(online);
                        }



                        holder.setName(userName);
                        holder.setStatus(userStatus);

                        final String finalOnline = online;
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which == 0){
                                            Intent intent = new Intent(getContext(), ProfileActivtiy.class);
                                            intent.putExtra("uid", user_id);
                                            startActivity(intent);
                                        }
                                        if(which == 1){

                                            Intent chatintent = new Intent(getContext(), ChatActivity.class);
                                            chatintent.putExtra("uid", user_id);
                                            chatintent.putExtra("userName", userName);
                                            chatintent.putExtra("online" , finalOnline);
                                            startActivity(chatintent);

                                        }
                                    }
                                });

                                builder.show();

                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }


        };

        firebaseRecyclerAdapter.startListening();
        mlistView.setAdapter(firebaseRecyclerAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }


        public void setName(String name) {
            TextView userName = mView.findViewById(R.id.textView4);
            userName.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatus = mView.findViewById(R.id.textView5);
            userStatus.setText(status);
        }

        public void setImage(String online){
            ImageView online_image = mView.findViewById(R.id.imageView);

            if(online.equals("true")){
                online_image.setVisibility(View.VISIBLE);
            }else{
                online_image.setVisibility(View.GONE);
            }
        }
    }
}
