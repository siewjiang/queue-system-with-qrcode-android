 package com.example.queuesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.UserApi;

 public class ProfileActivity extends AppCompatActivity {

     private TextView yourusername, youremail, verifymsg;
     private Button verifyemail, reset_password;

     //Firestore connection
     private FirebaseAuth firebaseAuth;
     private FirebaseFirestore fstore;
     private FirebaseUser user;
     String userId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("Profile");

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        yourusername = findViewById(R.id.yourusername);
        youremail = findViewById(R.id.youremail);
        //verifymsg = findViewById(R.id.verifymsg);
        //verifyemail = findViewById(R.id.verifyemail);
        reset_password = findViewById(R.id.reset_password);

        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();

        if(!user.isEmailVerified()) {
            verifymsg.setVisibility(View.VISIBLE);
            verifyemail.setVisibility(View.VISIBLE);
            verifyemail.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    user.sendEmailVerification()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(v.getContext(),
                                            "Verification Email Has Been Sent.",
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", "onFailure: Email not sent"+e.getMessage());
                                }
                            });
                }
            });
        }

        DocumentReference documentReference = fstore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()) {
                    yourusername.setText(value.getString("username"));
                    youremail.setText(value.getString("userEmail"));
                }else {
                    Log.d("tag","onEvent: Document do not exists");
                }
            }
        });

        reset_password.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText resetPassword = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Change Password ?");
                passwordResetDialog.setMessage("Enter New Password (The password must longer than 6 characters)");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPassword = resetPassword.getText().toString();
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this,
                                                "Password Reset Successfully.",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ProfileActivity.this,
                                                "Password Reset Failed.",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }
                                });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog
                    }
                });

                passwordResetDialog.create().show();
            }
        });

    }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//         switch (item.getItemId()){
//             case R.id.organization_home:
//                 //Take users to their profile
//                 if(user != null && firebaseAuth != null){
//                     startActivity(new Intent(ProfileActivity.this, OrganizationActivity.class));
//                     //finish();
//                 }
//                 break;
//         }
         return super.onOptionsItemSelected(item);
     }
 }