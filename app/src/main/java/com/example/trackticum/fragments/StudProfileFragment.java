package com.example.trackticum.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.activities.StudLogin;
import com.example.trackticum.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class StudProfileFragment extends Fragment {

    public StudProfileFragment() {
        // Required empty public constructor
    }

    private TextView nameTV, emailTV;
    private Button logoutBTN;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_com_profile, container, false);

        // Add code below
        initializeData(view);
        setupListeners(view);

        return view;
    }

    private void initializeData(View view) {
        nameTV = view.findViewById(R.id.user_name_tv);
        emailTV = view.findViewById(R.id.user_email_tv);
        logoutBTN = view.findViewById(R.id.logout_btn);
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String studUserid = sharedPreferences.getString("stud_userid", null);

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/student/get-stud-details/" + studUserid;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject studDetails = new JSONObject(response);

                int studId = studDetails.getInt("stud_id");
                String studFirstname = studDetails.getString("stud_firstname");
                String studMiddlename = studDetails.getString("stud_middlename");
                String studLastname = studDetails.getString("stud_lastname");
                String studEmail = studDetails.getString("stud_email");
                String studFullname = studFirstname + " " + studMiddlename + " " + studLastname;

                nameTV.setText(studFirstname);
                emailTV.setText(studEmail);

            } catch (JSONException e) {
                Toast.makeText(requireActivity(), "Error: " + e, Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Volley Error", error.toString());
        });

        queue.add(request);
    }

    private void setupListeners(View view) {
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(requireActivity(), StudLogin.class);
        startActivity(intent);
        requireActivity().finish();
    }
}