package com.example.queuesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ClientActivity extends AppCompatActivity {

    private boolean hasSignedIn = false;
    private long clientID = 1;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    public static final int code = 123456789;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        getSupportActionBar().setTitle("Home");
    }

    public void FireBaseListener(){
        // setting the position of the client in the queue
        FirebaseDatabase.getInstance().getReference("CurrentQueueSize").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clientID= (long)(dataSnapshot.getValue())+1;
                TextView displayNum = (TextView)findViewById(R.id.textView);
                displayNum.setText(String.valueOf(clientID));
                // increase the size of the queue
                FirebaseDatabase.getInstance().getReference("CurrentQueueSize").setValue(clientID);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // save the spot of the client
        FirebaseDatabase.getInstance().getReference("CurrentQueueInLine").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long number = (long)dataSnapshot.getValue();
                TextView displayCurrentlyCalling = (TextView)findViewById(R.id.clientLineNum);
                displayCurrentlyCalling.setText(number+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //display estimated time
        FirebaseDatabase.getInstance().getReference("EstimatedTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long result = (long)dataSnapshot.getValue();
                TextView timeEstimate = (TextView)findViewById(R.id.timeEstimate);
                if (result < 0 ){
                    timeEstimate.setText(0);
                }
                else {
                    timeEstimate.setText(result+"");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("CurrentQueueSize").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long size = (long)dataSnapshot.getValue();
                TextView totalInLine = (TextView)findViewById(R.id.totalInLine);
                totalInLine.setText(size+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // method to schedule job to check if it is the customers turn in line
    public void checkInBackground(){
        // adding a boolean to let the job know if its work is done
        PersistableBundle isTurn = new PersistableBundle();
        isTurn.putLong("clientID", clientID);
        // scheduling the job
        JobScheduler scheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        ComponentName service = new ComponentName(this, JobSchedulerService.class);
        // creating the info of the job with specific guidelines
        // checks for updates every 10 seconds, for a max of 5 hours

        JobInfo inf = new JobInfo.Builder((int)(Math.random()*101), service).setExtras(isTurn).setMinimumLatency(1000).build();
        scheduler.schedule(inf);
    }

    final Activity activity = this;

    public void openCamera(View view){
        if(!hasSignedIn){
            IntentIntegrator integrator = new IntentIntegrator(activity);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt("Scan QR Code");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // only let them do this once
        if(!hasSignedIn){
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if (result.getContents() == null||!result.getContents().equals(code+"")) {
                    Toast.makeText(this, "Scan Failed/Wrong Code", Toast.LENGTH_LONG).show();
                } else {
                    hasSignedIn = true;
                    Toast.makeText(this, "Scan Successful!" +result.getContents(), Toast.LENGTH_LONG).show();
                    FireBaseListener();
                    checkInBackground();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_profile:
                //Take users to their profile
                startActivity(new Intent(ClientActivity.this, ProfileActivity.class));
                if(user != null && firebaseAuth != null){
                    startActivity(new Intent(ClientActivity.this, ProfileActivity.class));
                    //finish();
                }
                break;
            case R.id.action_signout:
                //sign user out
                AlertDialog.Builder SignOutDialog = new AlertDialog.Builder(this);
                SignOutDialog.setTitle("Sign Out");
                SignOutDialog.setMessage("Are you sure to sign out and exit the queue.");

                SignOutDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseAuth.signOut();
                        startActivity(new Intent(ClientActivity.this, LoginActivity.class));
                        Toast.makeText(ClientActivity.this,
                                "Logged Out Successfully",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
                SignOutDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog
                    }
                });
                SignOutDialog.create().show();

                if(user != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(ClientActivity.this, LoginActivity.class));
                    Toast.makeText(ClientActivity.this,
                            "Logged Out Successfully",
                            Toast.LENGTH_LONG)
                            .show();
                    //finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}