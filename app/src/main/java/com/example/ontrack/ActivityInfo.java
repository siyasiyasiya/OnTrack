package com.example.ontrack;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActivityInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivityInfo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ActivityInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActivityInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static ActivityInfo newInstance(String param1, String param2) {
        ActivityInfo fragment = new ActivityInfo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_info, container, false);

        TextView organization = view.findViewById(R.id.orgName);
        TextView name = view.findViewById(R.id.contactName2);
        TextView email = view.findViewById(R.id.contactEmail2);
        TextView phone = view.findViewById(R.id.contactPhone2);
        TextView hours = view.findViewById(R.id.duration);
        Button edit = view.findViewById(R.id.button4);

        Bundle bundle = getArguments();
        String org = bundle.getString("org");
        organization.setText("Org. Name: " + org);
        String cName = bundle.getString("name");
        name.setText("Contact Name: " + cName);
        String cEmail = bundle.getString("email");
        email.setText("Contact Email Address: " + cEmail);
        String cPhone = bundle.getString("number");
        phone.setText("Contact Phone Number: " + cPhone);
        String hoursVal = bundle.getString("hours");
        hours.setText("Duration: " + hoursVal + " Hours");
        String docId = bundle.getString("docId");
        System.out.println("AI: " + docId);
        String studentId = bundle.getString("studentId");
        String sEmail = bundle.getString("studentEmail");

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LogActivities.class);
                intent.putExtra("orgName", org);
                intent.putExtra("name", cName);
                intent.putExtra("email", cEmail);
                intent.putExtra("phone", cPhone);
                intent.putExtra("hours", hoursVal);
                intent.putExtra("studentId", studentId);
                intent.putExtra("docId", docId);
                intent.putExtra("sEmail", sEmail);
                startActivity(intent);
            }
        });
        return view;
    }
}