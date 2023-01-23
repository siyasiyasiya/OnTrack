package com.example.ontrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class NewUserLanding extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_landing);
    }

    public void startStudent(View v){
        startActivity(new Intent(NewUserLanding.this, StudentLogIn.class));
    }

    public void startEducator(View v){
        startActivity(new Intent(NewUserLanding.this, EducatorLogIn.class));
    }
}