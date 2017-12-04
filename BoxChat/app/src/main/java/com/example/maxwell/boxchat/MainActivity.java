package com.example.maxwell.boxchat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private ViewPager mviewPager;
    private SectioAdapter sectioAdapter;
    private TabLayout mtabLayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mtoolbar = (Toolbar)findViewById(R.id.main_bar);
        setSupportActionBar(mtoolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("BoxChat");

        if (mAuth.getCurrentUser() !=null)
        {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
        }

        //tabs
        mviewPager = (ViewPager)findViewById(R.id.tabPager);
        sectioAdapter = new SectioAdapter(getSupportFragmentManager());
        mviewPager.setAdapter(sectioAdapter);

        mtabLayout = (TabLayout)findViewById(R.id.main_tab);
        mtabLayout.setupWithViewPager(mviewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentuser = mAuth.getCurrentUser();

        if (currentuser == null)
        {
            sendToStart();
        }
        else
        {
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser !=null)
        {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.main_logout:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.main_accSettings:
                Intent accSettings = new Intent(MainActivity.this, AccountSettings.class);
                startActivity(accSettings);
                break;
            case R.id.main_allUsers:
                Intent userSettings = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(userSettings);
        }
/*
        if (item.getItemId() == R.id.main_logout)
        {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if (item.getItemId() == R.id.main_accSettings)
        {
            Intent accSettings = new Intent(MainActivity.this, AccountSettings.class);
            startActivity(accSettings);
        }*/

        return super.onOptionsItemSelected(item);
    }


}
