package com.example.ontrack;

import static android.content.ContentValues.TAG;

import static java.lang.Math.random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class EducatorSignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText name, email, password, school;
    private Button signUp;
    private TextView logIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educator_sign_up);
        mAuth = FirebaseAuth.getInstance();

        name = (EditText) findViewById(R.id.educatorNameInput);
        email = (EditText) findViewById(R.id.educatorEmailInput);
        password = (EditText) findViewById(R.id.educatorPasswordInput);
        school = (EditText) findViewById(R.id.educatorSchoolInput);

        signUp = (Button) findViewById(R.id.signUpButton2);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        logIn = (TextView) findViewById(R.id.logInLink2);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EducatorSignUp.this, EducatorLogIn.class));
            }
        });
    }

    private void registerUser(){
        String emailVal = email.getText().toString().trim();
        String passwordVal = password.getText().toString().trim();
        String nameVal = name.getText().toString().trim();
        String schoolVal = school.getText().toString().trim();
        int n;
        n = 10000000 + (int)(random() * 90000000);
        String codeVal = String.valueOf(n);

        //this is using form validation in order to insure the form information provided is in the correct format
        if(nameVal.isEmpty() || !nameVal.contains(" ")){
            name.setError("Full Name Is Required!");
            name.requestFocus();
            return;
        }

        if(emailVal.isEmpty()){
            email.setError("Email Is Required!");
            email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()){
            email.setError("Please Provide Valid Email!");
            email.requestFocus();
            return;
        }

        if(passwordVal.isEmpty()){
            password.setError("Password Is Required!");
            password.requestFocus();
            return;
        }

        if(passwordVal.length() < 6){
            password.setError("Min Password Length Is 6 Characters!");
            password.requestFocus();
            return;
        }

        if(schoolVal.isEmpty()){
            school.setError("School Name Is Required!");
            school.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailVal, passwordVal)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> educator = new HashMap<>();
                            educator.put("name", nameVal);
                            educator.put("email", emailVal);
                            educator.put("password", passwordVal);
                            educator.put("school name", schoolVal);
                            educator.put("code", codeVal);

                            //this is writing the information the student provided during the creation of the account to the database
                            db.collection("educators")
                                    .add(educator)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                            sendVerificationEmail();
                                            Toast.makeText(EducatorSignUp.this, "Verify your email",
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(EducatorSignUp.this, EducatorLogIn.class));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                        }
                    }
                });
    }

    //this sends a verification email to the user's email address
    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                                       {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful())
                                               {
                                                   FirebaseAuth.getInstance().signOut();
                                               }
                                           }
                                       }
                );
    }
}