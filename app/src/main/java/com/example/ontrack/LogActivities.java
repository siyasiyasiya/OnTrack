package com.example.ontrack;

import static android.content.ContentValues.TAG;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.HashMap;
import java.util.Map;

public class LogActivities extends AppCompatActivity {

    private EditText orgName, hours, contactName, contactEmail, contactPhone;
    private Button submitForm, deleteButton;
    private boolean editing = false;
    private String docId = "";
    private String studentId = "";
    private String studentEmail = "";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Spinner howMany;


    private static final String[] recurring = {"Weekly", "Monthly", "Yearly", "Once"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_activities);

        orgName = (EditText) findViewById(R.id.organizationName);
        hours = (EditText) findViewById(R.id.numHours);
        contactName = (EditText) findViewById(R.id.contactName);
        contactEmail = (EditText) findViewById(R.id.contactEmail);
        contactPhone = (EditText) findViewById(R.id.contactPhone);

        submitForm = (Button) findViewById(R.id.submitFormButton);
        submitForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCredentials();
                Intent intent = new Intent(LogActivities.this, StudentProfile.class);
                if(editing){
                    intent.putExtra("educator", true);
                    intent.putExtra("email", studentEmail);
                }
                startActivity(intent);
            }
        });

        Bundle extras = getIntent().getExtras();
        deleteButton = (Button) findViewById(R.id.delete);

        //the existence of the bundle allows the program to determine whether the user is trying to edit an existing activity or log a new activity
        if (extras != null) {
            editing = true;
            orgName.setText(extras.getString("orgName"));
            contactName.setText(extras.getString("name"));
            contactEmail.setText(extras.getString("email"));
            contactPhone.setText(extras.getString("phone"));
            hours.setText(extras.getString("hours"));
            docId = extras.getString("docId");
            studentId = extras.getString("studentId");
            studentEmail = extras.getString("sEmail");
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            editing = false;
            deleteButton.setVisibility(View.INVISIBLE);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteActivity();
                startActivity(new Intent(LogActivities.this, StudentProfile.class));
            }
        });

        howMany = (Spinner) findViewById(R.id.hourDropdown);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LogActivities.this,
                android.R.layout.simple_spinner_item, recurring);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        howMany.setAdapter(adapter);
    }

    private void checkCredentials(){
        String orgNameVal = orgName.getText().toString().trim();
        String hoursVal = hours.getText().toString().trim();
        String oftenVal = howMany.getSelectedItem().toString().trim();
        String contactNameVal = contactName.getText().toString().trim();
        String contactEmailVal = contactEmail.getText().toString().trim();
        String contactPhoneVal = contactPhone.getText().toString().trim();

        //this is using form validation in order to insure the form information provided is in the correct format
        if(orgNameVal.isEmpty()){
            orgName.setError("Organization/Agency Name Is Required!");
            orgName.requestFocus();
            return;
        }

        if(hoursVal.isEmpty() || hoursVal.equals("0")){
            hours.setError("Duration Is Required!");
            hours.requestFocus();
            return;
        }

        if(contactNameVal.isEmpty() || !contactNameVal.contains(" ")){
            contactName.setError("Full Name Of Contact Is Required!");
            contactName.requestFocus();
            return;
        }

        if(contactEmailVal.isEmpty()){
            contactEmail.setError("Email Of Contact Is Required!");
            contactEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(contactEmailVal).matches()){
            contactEmail.setError("Please Provide Valid Email!");
            contactEmail.requestFocus();
            return;
        }

        if(contactPhoneVal.isEmpty()) {
            contactPhone.setError("Phone Number Of Contact Is Required!");
            contactPhone.requestFocus();
            return;
        }

        if(oftenVal.equals("once")){
            oftenVal = "";
        }

        hoursVal += " " + oftenVal;

        if (editing) {
            setData(studentId, orgNameVal, hoursVal, contactNameVal, contactEmailVal, contactPhoneVal);
        } else if (user != null) {
            String email = user.getEmail();
            findId(email, orgNameVal, hoursVal, contactNameVal, contactEmailVal, contactPhoneVal);
        }
    }

    //this function is searching for the document id of the student in the database from the email information that it currently has
    private void findId(String email, String orgName, String hours, String contactName, String contactEmail, String contactPhone){
        CollectionReference deliveryRef = db.collection("students");
        Query nameQuery = deliveryRef.whereEqualTo("email", email);
        nameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                        setData(document.getId(), orgName, hours, contactName, contactEmail, contactPhone);
                    }
                }
            }
        });
    }

    //this function uses the document id found in the previous function in order to write the activity information to the database under the student
    private void setData(String id, String orgName, String hours, String contactName, String contactEmail, String contactPhone){
        Map<String, Object> activity = new HashMap<>();
        activity.put("organization/agency name", orgName);
        activity.put("hours", hours);
        activity.put("contact name", contactName);
        activity.put("contact email", contactEmail);
        activity.put("contact phone", contactPhone);

        if(!editing){
            db.collection("students").document(id).collection("activities")
                    .add(activity)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        } else {
            System.out.println(studentId);
            System.out.println(docId);
            db.collection("students").document(studentId).collection("activities").document(docId)
                    .set(activity)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });

        }

//        DocumentReference docRef = db.collection("students").document(id);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                        db.collection("students").document(id).collection("activities").add(activity);
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
    }

    //this gives the user the opportunity to delete their activity which removes it from the database
    private void deleteActivity(){
        db.collection("students").document(studentId).collection("activities").document(docId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }
}