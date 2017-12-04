package com.example.maxwell.boxchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mpassword;
    private Button mcreateBtn;
    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private DatabaseReference mdatabase;
    @SuppressWarnings("deprecation")
    private ProgressDialog mRegProgressDialog;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        mtoolbar = (Toolbar)findViewById(R.id.reg_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //progress dialog
        mRegProgressDialog = new ProgressDialog(this);

        mDisplayName = (TextInputLayout)findViewById(R.id.reg_name);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mpassword = (TextInputLayout)findViewById(R.id.reg_password);
        mcreateBtn = (Button)findViewById(R.id.creat_acc_btn);


        mcreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    String display_name = mDisplayName.getEditText().getText().toString();
                    String display_email = mEmail.getEditText().getText().toString();
                    String password = mpassword.getEditText().getText().toString();


                    if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(display_email)
                            || !TextUtils.isEmpty(password))
                    {

                        mRegProgressDialog.setTitle("Registering User");
                        mRegProgressDialog.setMessage("Please wait while we create your account");
                        mRegProgressDialog.setCanceledOnTouchOutside(false);
                        mRegProgressDialog.show();
                        register_user(display_name, display_email, password);
                    }

                else
                {
                    Toast t = Toast.makeText(RegisterActivity.this, "Please check your connectivity", Toast.LENGTH_LONG);
                    t.show();
                }

            }
        });


    }

    private void register_user(final String display_name, String display_email, String password) {
        mAuth.createUserWithEmailAndPassword(display_email, password).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            FirebaseUser curr_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = curr_user.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mdatabase = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("device_token", deviceToken);
                            userMap.put("name", display_name);
                            userMap.put("status", "Hi there i'm using Unit Box App");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mdatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        mRegProgressDialog.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        Toast t = Toast.makeText(RegisterActivity.this, "Created", Toast.LENGTH_SHORT);
                                        t.show();
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });


                        }
                        else
                        {
                            mRegProgressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot sign in, please check the" +
                                            " form and try again" ,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );


    }


    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnected();
    }

}
