package com.example.ontrack;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class StudentSignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText name, email, password, code;
    private Button signUp;
    private TextView logIn;
    private Spinner grade;
    private boolean codeGood = false;
    private boolean finished = false;


    private static final String[] gradeOptions = {"Grade", "9", "10", "11", "12"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sign_up);
        mAuth = FirebaseAuth.getInstance();

        name = (EditText) findViewById(R.id.studentNameInput);
        email = (EditText) findViewById(R.id.studentEmailInput);
        password = (EditText) findViewById(R.id.studentPasswordInput);
        code = (EditText) findViewById(R.id.studentCodeInput);
        grade = (Spinner) findViewById(R.id.studentGradeDropdown);

        signUp = (Button) findViewById(R.id.signUpButton);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    checkCredentials();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        logIn = (TextView) findViewById(R.id.logInLink);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StudentSignUp.this, StudentLogIn.class));
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(StudentSignUp.this,
                android.R.layout.simple_spinner_item, gradeOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        grade.setAdapter(adapter);
    }

    private void checkCredentials() throws InterruptedException {
        String emailVal = email.getText().toString().trim();
        String passwordVal = password.getText().toString().trim();
        String nameVal = name.getText().toString().trim();
        String codeVal = code.getText().toString().trim();
        String gradeVal = grade.getSelectedItem().toString().trim();
//        validateCode(codeVal);

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

        if(gradeVal.isEmpty() || gradeVal.equals("Grade")){
            code.setError("Grade Is Required!");
            return;
        }

        if(codeVal.isEmpty()){
            code.setError("School Code Is Required!");
            code.requestFocus();
            return;
        }


        validateCode(codeVal, new UserCallback() {
            @Override
            public void onCodeExists(Boolean exists) {
                if (!exists) {
                    System.out.println("Which ran first");
                    code.setError("Enter A Valid Code!");
                    code.requestFocus();
                }
                else{
                    registerUser(nameVal, emailVal, passwordVal, gradeVal, codeVal);
                }
            }
        } );
    }

    //this is searching the database in order to ensure the school code the student provided actually exists
    private void validateCode(String code, final UserCallback callback){
        CollectionReference educatorsRef = db.collection("educators");
        Query hasCode = educatorsRef.whereEqualTo("code", code);
        AggregateQuery countQuery = hasCode.count();
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Count fetched successfully
                    AggregateQuerySnapshot snapshot = task.getResult();
                    Log.d(TAG, "Count: " + snapshot.getCount());
                    if (snapshot.getCount() > 0) {
                        callback.onCodeExists(true);
                        System.out.println("i ran this code");
                    } else {
                        callback.onCodeExists(false);
                    }
                } else {
                    Log.d(TAG, "Count failed: ", task.getException());
                    callback.onCodeExists(false);
                }
            }
        });
    }

    public interface UserCallback {
        void onCodeExists(Boolean exists);
    }

    private void registerUser(String nameVal, String emailVal, String passwordVal, String gradeVal, String codeVal){
        mAuth.createUserWithEmailAndPassword(emailVal, passwordVal)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> student = new HashMap<>();
                            student.put("name", nameVal);
                            student.put("email", emailVal);
                            student.put("password", passwordVal);
                            student.put("grade", gradeVal);

                            db.collection("students")
                                    .add(student)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                                            //this is writing the information the student provided during the creation of the account to the database
                                            db.collection("educators")
                                                    .whereEqualTo("code", codeVal)
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                                                    Map<String, Object> schoolName = new HashMap<>();
                                                                    schoolName.put("school name", document.getData().get("school name"));
                                                                    db.collection("students").document(documentReference.getId()).set(schoolName, SetOptions.merge());
                                                                }
                                                            } else {
                                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                                            }
                                                        }
                                                    });

                                            sendVerificationEmail();
                                            Toast.makeText(StudentSignUp.this, "Verify your email",
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(StudentSignUp.this, StudentLogIn.class));
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