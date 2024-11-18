package com.example.trackticum.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.activities.ComEditProfile;
import com.example.trackticum.activities.StudLogin;
import com.example.trackticum.utils.Constants;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class ComProfileFragment extends Fragment {

    public ComProfileFragment() {
        // Required empty public constructor
    }

    private static final int REQUEST_CODE_EDIT_PROFILE = 1001;

    // For action bar
    private Toolbar toolbar;

    //Fetch Company Information
    private TextView comNameTV, comStatusTV, comLocationTV, comEmailTV, comSlotTV, comBgTV;
    SharedPreferences sharedPreferences;

    //for Skill Requirements
    private FlexboxLayout jobsContainer;

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
        //For action bar
        toolbar = view.findViewById(R.id.com_profile_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Profile");

        //Fetch Company Information
        comNameTV = view.findViewById(R.id.com_name_tv);
        comStatusTV = view.findViewById(R.id.com_status_tv);
        comLocationTV = view.findViewById(R.id.com_location_tv);
        comEmailTV = view.findViewById(R.id.com_email_tv);
        comSlotTV = view.findViewById(R.id.com_slot_tv);
        comBgTV = view.findViewById(R.id.com_descrip_tv);
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        fetchCompanyDetails();

        //Setting up the Skill Requirements
        jobsContainer = view.findViewById(R.id.jobsContainer);
        List<String> jobOffers = Arrays.asList(
                "Software Engineer",
                "UI/UX Designer",
                "Marketing Specialist",
                "Data Analyst",
                "Product Manager"
        );

        for (String job : jobOffers) {
            TextView jobTextView = new TextView(requireActivity());
            jobTextView.setText(job);
            jobTextView.setPadding(16, 8, 16, 8);
            jobTextView.setBackgroundResource(R.drawable.job_offer_style);
            jobTextView.setTextColor(Color.BLACK);

            Typeface customFont = ResourcesCompat.getFont(requireContext(), R.font.sf_rounded_regular);
            jobTextView.setTypeface(customFont);

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(2, 2, 2, 2);
            jobTextView.setLayoutParams(params);

            jobsContainer.addView(jobTextView);
        }
    }

    private void setupListeners(View view) {

    }

    private void fetchCompanyDetails() {
        String comId = sharedPreferences.getString("com_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/company/get-com-details/" + comId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject comDetails = new JSONObject(response);

                int comID = comDetails.getInt("id");
                String comName = comDetails.getString("com_name");
                String comStatus = comDetails.getString("com_status");
                String comLocation = comDetails.getString("com_address");
                String comEmail = comDetails.getString("com_email");
                String comSlot = comDetails.getString("com_slot");
                String comBg = comDetails.getString("com_description");

                comNameTV.setText(comName);
                comStatusTV.setText(comStatus);
                comLocationTV.setText(comLocation);
                comEmailTV.setText(comEmail);
                comSlotTV.setText(comSlot);
                comBgTV.setText(comBg);

            } catch (JSONException e) {
                Toast.makeText(requireActivity(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            fetchCompanyDetails();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.com_profile_actionbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.edit_info) {
            Intent intent = new Intent(getActivity(), ComEditProfile.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}