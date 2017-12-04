package com.example.maxwell.boxchat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUserList;
    private DatabaseReference mUserDatabase;
   // private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar =(Toolbar)findViewById(R.id.user_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      //  mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
      //  String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserList = (RecyclerView)findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                        Users.class, R.layout.users_single_layout,UsersViewHolder.class,
                        mUserDatabase
                ) {
                    @Override
                    protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                        viewHolder.setName(model.getName());
                        viewHolder.setStatus(model.getStatus());
                        viewHolder.setImage(model.getThumb_image(), getApplicationContext());

                        final String user_id = getRef(position).getKey();

                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("user_id", user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }
                };

        mUserList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {

        View mview;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
        }

        public  void setName(String name)
        {
            TextView mUserNameView = (TextView) mview.findViewById(R.id.user_single_name);
            mUserNameView.setText(name);
        }
        public void setStatus(String stats)
        {
            TextView mUserStatus = (TextView)mview.findViewById(R.id.user_single_status);
            mUserStatus.setText(stats);
        }
        public void setImage(String img, Context ctx)
        {
            CircleImageView mUserImage = (CircleImageView) mview.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(img).placeholder(R.mipmap.avatar).into(mUserImage);
        }
    }
}
