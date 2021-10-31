package com.example.queuesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.reflect.KFunction;

public class OrganizationActivity extends AppCompatActivity {

    private int queueSize = 0;
    int lineNum = 1;
    private int currentTime;
    private Button setEstimatedTime;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    public static final int code = 123456789;
    ImageView image;
    final Context c = this;
    private static final String TAG = "ORGANIZATION_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization);
        setEstimatedTime = findViewById(R.id.setEstimatedTime);
        getSupportActionBar().setTitle("Home");

        //grant permission from user for excel
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        setEstimatedTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference("WaitingTime").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long getwaitingtime = (long) dataSnapshot.getValue();
                        int currentTime=Integer.parseInt(String.valueOf(getwaitingtime));
                        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                        View mView = layoutInflaterAndroid.inflate(R.layout.set_time_dialog, null);
                        TextView currentWaitTime = (TextView)mView.findViewById(R.id.currentWaitTime);
                        currentWaitTime.setText("Current wait time range for an individual : "+currentTime);
                        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                        alertDialogBuilderUserInput.setView(mView);

                        final EditText inputTime = (EditText) mView.findViewById(R.id.userInputDialog);
                        alertDialogBuilderUserInput
                                .setCancelable(false)
                                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        //ToDo get user input here
                                        String checkInput = inputTime.getText().toString().trim();
                                        if (!TextUtils.isEmpty(checkInput)) {
                                            String startNewTime = inputTime.getText().toString();
                                            int newTime=Integer.parseInt(startNewTime);
                                            FirebaseDatabase.getInstance().getReference("WaitingTime").setValue(newTime);
                                            Toast.makeText(OrganizationActivity.this,
                                                    "Updated Successfully",
                                                    Toast.LENGTH_LONG)
                                                    .show();
                                        } else {
                                            //currentWaitTime.setError("This Field cannot be empty!");
                                        }
                                    }
                                })

                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogBox, int id) {
                                                dialogBox.cancel();
                                            }
                                        });

                        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                        alertDialogAndroid.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            private int getCurrentWaitingTime(int currentTime) {
                FirebaseDatabase.getInstance().getReference("WaitingTime").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long getwaitingtime = (long) dataSnapshot.getValue();
                        int currentTime=Integer.parseInt(String.valueOf(getwaitingtime));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return currentTime;
            }
        });

        // code for QR code
        image = (ImageView) findViewById(R.id.image);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(""+code, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            image.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // when the current queue changes update the UI
        FirebaseDatabase.getInstance().getReference("CurrentQueueInLine").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long number = (long)dataSnapshot.getValue();
                // if the queue is empty

                if(number==0){
                    TextView displayNum = (TextView)findViewById(R.id.textView);
                    displayNum.setText("0");
                }else{
                    TextView displayNum = (TextView)findViewById(R.id.textView);
                    displayNum.setText(String.valueOf(number));
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
                TextView sizeQueue = (TextView)findViewById(R.id.numQueue);
                sizeQueue.setText(size+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("CurrentQueueInLine")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long currentNumber = (long)snapshot.getValue();
                    FirebaseDatabase.getInstance().getReference("CurrentQueueSize")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                long totalNumber = (long)snapshot2.getValue();
                                FirebaseDatabase.getInstance().getReference("WaitingTime")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot3) {
                                            long getTimeFromDB = (long)snapshot3.getValue();
                                            long time = (totalNumber-currentNumber)*getTimeFromDB;
                                            FirebaseDatabase.getInstance().getReference("EstimatedTime").setValue(time);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        FirebaseDatabase.getInstance().getReference("WaitingTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long getwaitingtime = (long) snapshot.getValue();
                int currentTime=Integer.parseInt(String.valueOf(getwaitingtime));
                LayoutInflater inflater = (LayoutInflater)getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                View vi = inflater.inflate(R.layout.set_time_dialog, null);
                TextView currentWaitTime = (TextView)vi.findViewById(R.id.currentWaitTime);
                currentWaitTime.setText("Current wait time range for an individual : "+getwaitingtime+" mins");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // will be called when a customer leaves
    public void moveToNextInLine(View view) {
        ++lineNum;
        ++queueSize;
        // only move move to the next client if they queue allows
        if(lineNum<=queueSize && lineNum!=2){
            FirebaseDatabase.getInstance().getReference("CurrentQueueInLine").setValue(lineNum);

        }else{
            FirebaseDatabase.getInstance().getReference("CurrentQueueInLine").setValue(lineNum-1);

        }
        // increase the size

        FirebaseDatabase.getInstance().getReference("CurrentQueueInLine")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long currentNumber = (long)snapshot.getValue();
                    FirebaseDatabase.getInstance().getReference("CurrentQueueSize")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                long totalNumber = (long)snapshot2.getValue();
                                FirebaseDatabase.getInstance().getReference("WaitingTime")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot3) {
                                            long getTimeFromrealtimeDB = (long)snapshot3.getValue();
                                            long time = (totalNumber-currentNumber)*getTimeFromrealtimeDB;
                                            FirebaseDatabase.getInstance().getReference("EstimatedTime").setValue(time);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    // for closing up shop
    public void closeQueueing(View view) throws IOException {

        //set date and time "EEE, MMM d, ''yy"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        //date and time with another format
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm aaa, EEE, dd-MM-yyyy", Locale.getDefault());
        String currentDateAndTime2 = sdf2.format(new Date());

        //store queue size as the report to firebase
        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser currentuser = firebaseAuth.getCurrentUser();
        final String userNow = currentuser.getUid();
        DocumentReference ref = database.collection("QueueReport").document(currentDateAndTime);
        FirebaseDatabase.getInstance().getReference("CurrentQueueSize")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long getCurrentQueueSize = (long) snapshot.getValue();
                    String str = Long.toString(getCurrentQueueSize);

                    Map<String,String> storeQueueSize = new HashMap<>();
                    storeQueueSize.put("QueueSize",str);
                    storeQueueSize.put("DateTime",currentDateAndTime2);
                    ref.set(storeQueueSize);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        lineNum = 0;
        queueSize = 0;
        FirebaseDatabase.getInstance().getReference("CurrentQueueInLine").setValue(0);
        FirebaseDatabase.getInstance().getReference("CurrentQueueSize").setValue(0);
        FirebaseDatabase.getInstance().getReference("EstimatedTime").setValue(0);

        Toast.makeText(OrganizationActivity.this,
                "End Queue Successfully, Report Generated.",
                Toast.LENGTH_LONG)
                .show();
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
                startActivity(new Intent(OrganizationActivity.this, ProfileActivity.class));
                if(user != null && firebaseAuth != null){
                    startActivity(new Intent(OrganizationActivity.this, ProfileActivity.class));
                    //finish();
                }
                break;
            case R.id.action_report:
                //Take users to the report page
                startActivity(new Intent(OrganizationActivity.this, ReportActivity.class));
                break;
            case R.id.action_signout:
                //sign user out
                firebaseAuth.signOut();
                startActivity(new Intent(OrganizationActivity.this, LoginActivity.class));
                Toast.makeText(OrganizationActivity.this,
                        "Logged Out Successfully",
                        Toast.LENGTH_LONG)
                        .show();
                finish();
                if(user != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(OrganizationActivity.this, LoginActivity.class));
                    Toast.makeText(OrganizationActivity.this,
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