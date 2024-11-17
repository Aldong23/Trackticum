package com.example.trackticum.fragments;

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

    private Toolbar toolbar;
    private FlexboxLayout jobsContainer;
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
        toolbar = view.findViewById(R.id.com_profile_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Profile");

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
            Toast.makeText(getContext(), "Edit clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}