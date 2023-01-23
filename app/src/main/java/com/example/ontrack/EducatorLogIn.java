package com.example.ontrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EducatorLogIn extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button logIn;
    private TextView signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educator_log_in);
        mAuth = FirebaseAuth.getInstance();

        email = (EditText) findViewById(R.id.emailInput2);
        password = (EditText) findViewById(R.id.passwordInput2);
        logIn = (Button) findViewById(R.id.logInButton2);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInUser();
            }
        });
        signUp = (TextView) findViewById(R.id.signUpLink2);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EducatorLogIn.this, EducatorSignUp.class));
            }
        });
    }

    private void logInUser() {
        String emailVal = email.getText().toString().trim();
        String passwordVal = password.getText().toString().trim();

        //this is using form validation in order to insure the form information provided is correct
        if (emailVal.isEmpty()) {
            email.setError("Email Is Required!");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
            email.setError("Please Provide Valid Email!");
            email.requestFocus();
            return;
        }

        if (passwordVal.isEmpty()) {
            password.setError("Password Is Required!");
            password.requestFocus();
            return;
        }

        if (passwordVal.length() < 6) {
            password.setError("Min Password Length Is 6 Characters!");
            password.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(emailVal, passwordVal).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(checkIfEmailVerified()){
                        Toast.makeText(EducatorLogIn.this, "Worked", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(EducatorLogIn.this, EducatorProfile.class));
                    }else {
                        mAuth.signOut();
                        Toast.makeText(EducatorLogIn.this, "Verify your email first.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EducatorLogIn.this, "Email or Password is incorrect", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean checkIfEmailVerified() {
        return mAuth.getCurrentUser().isEmailVerified();
    }
}