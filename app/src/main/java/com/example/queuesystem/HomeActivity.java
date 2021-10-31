package com.example.queuesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.platforminfo.UserAgentPublisher;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;

import util.UserApi;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    public static final int code = 123456789;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        FirebaseDatabase.getInstance().getReference("CurrentQueueInLine").setValue(0);
        FirebaseDatabase.getInstance().getReference("CurrentQueueSize").setValue(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_profile:
                //Take users to their profile
                if(user != null && firebaseAuth != null){
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    //finish();
                }
                break;
            case R.id.action_signout:
                //sign user out
                if(user != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    Toast.makeText(HomeActivity.this,
                            "Logged Out Successfully",
                            Toast.LENGTH_LONG)
                            .show();
                    //finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // method to move to organization screen
    public void moveToOrganization(View view){
        Intent intent = new Intent(this, OrganizationActivity.class);
        startActivity(intent);
    }

    // method to move to organization screen
    public void moveToClient(View view){
        Intent intent = new Intent(this, ClientActivity.class);
        startActivity(intent);
    }
}