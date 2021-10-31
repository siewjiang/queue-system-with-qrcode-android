package com.example.queuesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import util.UserApi;

public class LoginActivity<UserCallbacks> extends AppCompatActivity {
    private Button loginButton, createAccButton;
    private ProgressBar progressBar;
    private AutoCompleteTextView loginemail;
    private EditText loginpassword;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private TextView forgotpassword;
    String userId;

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress);
        loginButton = findViewById(R.id.email_sign_in_button);
        createAccButton = findViewById(R.id.create_acc_button_login);
        loginemail = findViewById(R.id.email);
        loginpassword = findViewById(R.id.password);
        forgotpassword = findViewById(R.id.forgot_password);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null){
                    //user is already logged in
                    checkUserType(currentUser.getUid());
                }else{
                    //no user yet
                }
            }
        };

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password ?");
                passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        firebaseAuth.sendPasswordResetEmail(mail)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(LoginActivity.this,
                                                "Reset Link Sent To Your Email",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this,
                                                "Error! Reset Link is Not Sent" + e.getMessage(),
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

        createAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkEmail = loginemail.getText().toString().trim();
                String checkPassword = loginpassword.getText().toString().trim();

                //form validation
                if(TextUtils.isEmpty(checkEmail)){
                    loginemail.setError("Email is Required!");
                }
                if(TextUtils.isEmpty(checkPassword)){
                    loginpassword.setError("Password is Required!");
                }else{
                    if(checkPassword.length() < 6){
                        loginpassword.setError("Password Must be >= 6 Characters!");
                    }
                }

                loginEmailPasswordUser(loginemail.getText().toString().trim(),
                        loginpassword.getText().toString().trim());
            }
        });
    }

    private void loginEmailPasswordUser(String email, String password){
        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(LoginActivity.this,
                                        "Login Successfully",
                                        Toast.LENGTH_LONG)
                                        .show();
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                assert user != null;
                                final String currentUserId = user.getUid();
                                userId = firebaseAuth.getCurrentUser().getUid();


                                collectionReference
                                    .whereEqualTo("userId",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if(error != null){
                                            }
                                            assert value != null;
                                            if(!value.isEmpty()){
                                                progressBar.setVisibility(View.INVISIBLE);
                                                for(QueryDocumentSnapshot snapshot : value){
                                                    UserApi userApi = UserApi.getInstance();
                                                    userApi.setUsername(snapshot.getString("username"));
                                                    userApi.setUserId(snapshot.getString("userId"));
                                                    userApi.setUserEmail(snapshot.getString("userEmail"));
                                                    userApi.setUserRole(snapshot.getString("userRole"));

                                                    //check user type and direct to the right page
                                                    checkUserType(task.getResult().getUser().getUid());

                                                    //Go to activity
//                                                    startActivity(new Intent(LoginActivity.this,
//                                                            ShopperActivity.class));
                                                }
                                            }

                                        }

                                    });
                            }else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this,
                                        "Something went wrong" + task.getException().getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this,
                                    "Failed to Log In" + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
        }else {
            progressBar.setVisibility(View.INVISIBLE);
//            Toast.makeText(LoginActivity.this,
//                    "Please enter email and password",
//                    Toast.LENGTH_LONG)
//                    .show();
        }
    }

    private void checkUserType(String uid) {
        DocumentReference documentReference = db.collection("users").document(uid);
        //extract the data from the document
        documentReference.get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //identify the user type
                    if(documentSnapshot.getString("userRole").equals("Shopper")){
                        startActivity(new Intent(LoginActivity.this,
                                OrganizationActivity.class));
                        finish();
                    }
                    if(documentSnapshot.getString("userRole").equals("User")){
                        startActivity(new Intent(LoginActivity.this,
                                ClientActivity.class));
                        finish();
                    }
                }
            });
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

}