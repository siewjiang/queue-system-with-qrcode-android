package com.example.queuesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import util.UserApi;

public class CreateAccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final String TAG = "CREATE_ACCOUNT_ACTIVITY";
    private Button loginButton, createAccButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("users");
    //private CollectionReference collectionReference;

    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private EditText usernameEditText;
    private Spinner spinner;
    String item;
    //String[] role = {"Select A Role","Shopper","User"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();

        createAccButton = findViewById(R.id.create_acc_button);
        progressBar = findViewById(R.id.create_acc_progress);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);
        usernameEditText = findViewById(R.id.username_account);

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> userType = new ArrayList<String>();
        userType.add("Select A Role");
        userType.add("Shopper");
        userType.add("User");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,userType);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

//        //can work but only for demo
//        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
//        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(CreateAccountActivity.this,
//                android.R.layout.simple_list_item_1,
//                getResources().getStringArray(R.array.names));
//        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mySpinner.setAdapter(myAdapter); //allow the adapter to show the data inside the spinner

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

        createAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String role = spinner.getSelectedItem().toString().trim();

                //form validation
                if(TextUtils.isEmpty(email)){
                    emailEditText.setError("Email is Required!");
                }
                if(TextUtils.isEmpty(password)){
                    passwordEditText.setError("Password is Required!");
                }
                if(password.length() < 6){
                    passwordEditText.setError("Password Must be >= 6 Characters!");
                }
                if(TextUtils.isEmpty(username)){
                    usernameEditText.setError("Username is Required!");
                }
                if(role == "Select A Role"){
                    TextView errorText = (TextView)spinner.getSelectedView();
                    errorText.setError("");
                    errorText.setText("Select A Role!");//changes the selected item text to this
                }

                if(!TextUtils.isEmpty(emailEditText.getText().toString())
                        && !TextUtils.isEmpty(passwordEditText.getText().toString())
                        && !TextUtils.isEmpty(usernameEditText.getText().toString())){

                    createUserEmailAccount(email,password,username,role);
                }
            }
        });
    }

    private void createUserEmailAccount(String email,String password,final String username,String role){

        //paid tutorial
        if (!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(username)
                && role != "Select A Role"){

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            //we take user to next page
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            final String currentUserId = currentUser.getUid();

//                                //send verification link
//                                currentUser.sendEmailVerification()
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                Toast.makeText(CreateAccountActivity.this,
//                                                        "Verification Email Has Been Sent.",
//                                                        Toast.LENGTH_LONG)
//                                                        .show();
//                                            }
//                                        })
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Log.d(TAG, "onFailure: Email nont sent"+e.getMessage());
//                                            }
//                                        });

                            DocumentReference documentReference = db.collection("users").document(currentUserId);

                            //create a user Map so that we can create a user in the user collection
                            Map<String,String> userObj = new HashMap<>();
                            userObj.put("userId",currentUserId);
                            userObj.put("username",username);
                            userObj.put("userEmail",email);
                            userObj.put("userRole",role);

                            documentReference.set(userObj)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"onSuccess:user Profile is created for "+currentUserId);
                                        Toast.makeText(CreateAccountActivity.this,
                                                "Account successfully created, please verify your email.",
                                                Toast.LENGTH_LONG)
                                                .show();
                                        startActivity(new Intent(CreateAccountActivity.this,
                                                LoginActivity.class));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                            //HomeActivity.class = link to next page or main page

                            //save to our firebase database
//                            collectionReference.add(userObj)
//                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                    @Override
//                                    public void onSuccess(DocumentReference documentReference) {
//                                        documentReference.get()
//                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                    if (Objects.requireNonNull(task).getResult().exists()){
//                                                        progressBar.setVisibility(View.INVISIBLE);
////                                                        String name = task.getResult()
////                                                                .getString("username");
////
////                                                        UserApi userApi = UserApi.getInstance(); //Global API
////                                                        userApi.setUserId(currentUserId);
////                                                        userApi.setUsername(name);
////
////                                                        Intent intent = new Intent(CreateAccountActivity.this,
////                                                                OrganizationActivity.class);
////                                                        intent.putExtra("username",name);
////                                                        intent.putExtra("userEmail",email);
////                                                        intent.putExtra("userId",currentUserId);
////                                                        intent.putExtra("userRole",role);
////                                                        startActivity(intent);
//
//                                                        //create a user Map so that we can create a user in the user collection
//                                                        Map<String,String> userObj = new HashMap<>();
//                                                        userObj.put("userId",currentUserId);
//                                                        userObj.put("username",username);
//                                                        userObj.put("userEmail",email);
//                                                        userObj.put("userRole",role);
//
////                                                        collectionReference
////                                                            .whereEqualTo("userId",currentUserId)
////                                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
////                                                                @Override
////                                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
////                                                                    if(error != null){
////                                                                    }
////                                                                    assert value != null;
////                                                                    if(!value.isEmpty()){
////                                                                        for(QueryDocumentSnapshot snapshot : value){
////                                                                            if(snapshot.getString("userRole") == "Shopper"){
////                                                                                Intent intent = new Intent(CreateAccountActivity.this,
////                                                                                        OrganizationActivity.class);
////                                                                                intent.putExtra("username",name);
////                                                                                intent.putExtra("userEmail",email);
////                                                                                intent.putExtra("userId",currentUserId);
////                                                                                intent.putExtra("userRole",role);
////                                                                                startActivity(intent);
////                                                                            }
////                                                                            if(snapshot.getString("userRole") == "User"){
////                                                                                Intent intent = new Intent(CreateAccountActivity.this,
////                                                                                        ClientActivity.class);
////                                                                                intent.putExtra("username",name);
////                                                                                intent.putExtra("userEmail",email);
////                                                                                intent.putExtra("userId",currentUserId);
////                                                                                intent.putExtra("userRole",role);
////                                                                                startActivity(intent);
////                                                                            }
////                                                                        }
////                                                                    }
////                                                                }
////                                                            });
//
////                                                            Toast.makeText(CreateAccountActivity.this,
////                                                                    "Please verify your email before login!",
////                                                                    Toast.LENGTH_LONG)
////                                                                    .show();
//
//                                                    }else {
//                                                        progressBar.setVisibility(View.INVISIBLE);
//                                                    }
//                                                }
//                                            });
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                    }
//                                });

                        }else {
                            //something went wrong
                            Toast.makeText(CreateAccountActivity.this,
                                    "Something went wrong!" + task.getException().getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        }else {
            Toast.makeText(CreateAccountActivity.this,
                "Something went wrong!",
                Toast.LENGTH_LONG)
                .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        item = spinner.getSelectedItem().toString();
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
                            startActivity(new Intent(CreateAccountActivity.this,
                                    OrganizationActivity.class));
                            finish();
                        }
                        if(documentSnapshot.getString("userRole").equals("User")){
                            startActivity(new Intent(CreateAccountActivity.this,
                                    ClientActivity.class));
                            finish();
                        }
                    }
                });
    }
}