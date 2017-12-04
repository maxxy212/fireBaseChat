package com.example.maxwell.boxchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReq;
    private ImageView mProfileImage;
    private DatabaseReference mUserDatabase;
    private String mcurrent_state;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrent_user;
    private Button mDeclineBtn;
    @SuppressWarnings("deprecation")
    private ProgressDialog mProgressDialog;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mProfileImage = (ImageView)findViewById(R.id.profile_display_image);
        mProfileName = (TextView)findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_friendlist);
        mProfileSendReq = (Button)findViewById(R.id.profile_send_request_button);
        mDeclineBtn = (Button)findViewById(R.id.profile_decline_button);

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mcurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.mipmap.avatar).into(mProfileImage);

                //-----------------friend list && request feature-----------

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type")
                                    .getValue().toString();
                            if (req_type.equals("received"))
                            {
                                mcurrent_state = "req_received";
                                mProfileSendReq.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }
                            else if (req_type.equals("sent"))
                            {
                                mcurrent_state = "req_sent";
                                mProfileSendReq.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }

                        else
                        {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id))
                                    {
                                        mcurrent_state = "friends";
                                        mProfileSendReq.setText("UNFRIEND");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReq.setEnabled(false);


//----------------Not Friends-------------------------------------------------------


                if (mcurrent_state.equals("not_friends"))
                {
                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    final HashMap<String, String> notificationData = new HashMap<String, String>();
                    notificationData.put("from" , mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/"+ mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener(){

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if (databaseError !=null)
                           {
                               Toast.makeText(ProfileActivity.this, "There was error in sending request", Toast.LENGTH_LONG).show();
                           }

                           mProfileSendReq.setEnabled(true);
                            mcurrent_state ="req_sent";
                            mProfileSendReq.setText("CANCEL FRIEND REQUEST");
                        }
                    });
                }

                //-------------------Cancel Request---------------------------------------

                if (mcurrent_state.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReq.setEnabled(true);
                                    mcurrent_state ="not_friends";
                                    mProfileSendReq.setText("SEND FRIEND REQUEST");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });

                }

                //-----------------Request received state----------------
                if (mcurrent_state.equals("req_received"))
                {
                    final String currentDate = java.text.DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" +user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" +mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" +user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener(){

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null)
                            {
                                mProfileSendReq.setEnabled(true);
                                mcurrent_state = "friends";
                                mProfileSendReq.setText("UNFRIEND");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //*************************Unfriend******************************************

                if (mcurrent_state.equals("friends"))
                {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener(){

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null)
                            {
                                mcurrent_state = "not_friends";
                                mProfileSendReq.setText("SEND FRIEND REQUEST");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReq.setEnabled(true);
                        }
                    });


                }

            }
        });

        //*******************************Decline Friend request****************************
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map unfriendMap = new HashMap();
                unfriendMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                unfriendMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener(){

                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError == null)
                        {
                            mcurrent_state = "not_friends";
                            mProfileSendReq.setText("SEND FRIEND REQUEST");

                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);
                        }
                        else
                        {
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                        mProfileSendReq.setEnabled(true);
                    }
                });

            }
        });

    }


}
