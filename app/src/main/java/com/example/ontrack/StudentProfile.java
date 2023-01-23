package com.example.ontrack;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class StudentProfile extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button logActivity, logOut;
    private TextView change;
    private String email = "";
    private boolean educatorVersion;
    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        LinearLayout ll = (LinearLayout) findViewById(R.id.profileDisplay2);
        ll.removeAllViews();

        fragmentManager = getSupportFragmentManager();

        logActivity = (Button) findViewById(R.id.logButton);
        logActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(educatorVersion){
                    startActivity(new Intent(StudentProfile.this, EducatorProfile.class));
                } else {
                    startActivity(new Intent(StudentProfile.this, LogActivities.class));
                }
            }
        });

        logOut = (Button) findViewById(R.id.logOut1);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StudentProfile.this, NewUserLanding.class));
                FirebaseAuth.getInstance().signOut();
            }
        });

        Bundle extras = getIntent().getExtras();
        change = (TextView) findViewById(R.id.labelAct);
        System.out.println(extras != null);
        if (extras != null) {
            educatorVersion = true;
            System.out.println("hello");
            email = extras.getString("email");
            System.out.println(extras.getString("email"));
            logActivity.setText("Go Back");
            change.setText("Student Activity");
        } else {
            educatorVersion = false;
            logActivity.setText("Log Activity");
            change.setText("My Activity");
        }

        if (user != null) {
            if(!educatorVersion){
                email = user.getEmail();
            }
            findId(email);
            System.out.println("i ran");
        }
    }

    private void findId(String email){
        CollectionReference deliveryRef = db.collection("students");
        Query nameQuery = deliveryRef.whereEqualTo("email", email);
        nameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                        getSchool(document.getId());
                    }
                }
            }
        });
    }

    private void getSchool(String id){
        DocumentReference docRef = db.collection("students").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        setProfile(document.getData().get("school name").toString(), document.getData().get("name").toString(), document.getData().get("grade").toString());
                        printActivities(document.getId());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setProfile(String schoolName, String name, String grade){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ProfileDisplay educatorProfile = new ProfileDisplay();
        Bundle b = new Bundle();
        b.putString("name", name);
        b.putString("status", "Student");
        b.putString("school", schoolName);
        b.putString("grade", grade);
        educatorProfile.setArguments(b);
        fragmentTransaction.add(R.id.profileDisplay2, educatorProfile).commit();
    }

    //this prints all activities in the students document collection in the database
    private void printActivities(String id){
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.activityDisplay);
        ll2.removeAllViews();
        db.collection("students").document(id).collection("activities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                ActivityInfo activity = new ActivityInfo();
                                Bundle b = new Bundle();
                                b.putString("org", document.getData().get("organization/agency name").toString());
                                b.putString("name", document.getData().get("contact name").toString());
                                b.putString("email", document.getData().get("contact email").toString());
                                b.putString("number", document.getData().get("contact phone").toString());
                                b.putString("hours", document.getData().get("hours").toString());
                                b.putString("docId", document.getId());
                                System.out.println("SP: " + document.getId());
                                b.putString("studentId", id);
                                b.putString("studentEmail", email);
                                activity.setArguments(b);
                                fragmentTransaction.add(R.id.activityDisplay, activity).commit();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
}