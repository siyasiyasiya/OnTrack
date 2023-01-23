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

public class EducatorProfile extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static FragmentManager fragmentManager;
    private Button logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educator_profile);

        System.out.println("this worked 1");

        LinearLayout ll = (LinearLayout) findViewById(R.id.profileDisplay);
        ll.removeAllViews();

        fragmentManager = getSupportFragmentManager();

        if (user != null) {
            String email = user.getEmail();

            System.out.println("this worked 2");

            findId(email);

            System.out.println("this worked 3");

        }

        logOut = (Button) findViewById(R.id.logOut2);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EducatorProfile.this, NewUserLanding.class));
                FirebaseAuth.getInstance().signOut();
            }
        });

        System.out.println("this worked 4");
    }

    //this function is searching for the document id of the educator in the database from the email information that it currently has
    private void findId(String email){
        CollectionReference deliveryRef = db.collection("educators");
        Query nameQuery = deliveryRef.whereEqualTo("email", email);
        nameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                        getInfo(document.getId());
                    }
                }
            }
        });
    }

    //this function uses the document id found in the previous function in order to read the document fields (educator information) from the database
    private void getInfo(String id){
        DocumentReference docRef = db.collection("educators").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        setProfile(document.getData().get("school name").toString(), document.getData().get("name").toString(), document.getData().get("code").toString());
                        printStudents(document.getData().get("school name").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //this is editing the fragment
    private void setProfile(String schoolName, String name, String code){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ProfileDisplay educatorProfile = new ProfileDisplay();
        Bundle b = new Bundle();
        b.putString("name", name);
        b.putString("status", "Educator");
        b.putString("school", schoolName);
        b.putString("code", code);
        educatorProfile.setArguments(b);
        fragmentTransaction.add(R.id.profileDisplay, educatorProfile).commit();
    }

    //this searches for all students from the same school as the educator and prints their name and grade in a list format on the screen.
    private void printStudents(String school){
        LinearLayout ll2 = (LinearLayout) findViewById(R.id.studentDisplay);
        ll2.removeAllViews();
        System.out.println(school);
        db.collection("students")
                .whereEqualTo("school name", school)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            System.out.println("successful");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                StudentName student = new StudentName();
                                Bundle b = new Bundle();
                                b.putString("name", document.getData().get("name").toString());
                                b.putString("grade", document.getData().get("grade").toString());
                                b.putString("email", document.getData().get("email").toString());
                                student.setArguments(b);
                                fragmentTransaction.add(R.id.studentDisplay, student).commit();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}