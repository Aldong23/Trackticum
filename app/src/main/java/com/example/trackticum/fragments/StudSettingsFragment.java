package com.example.trackticum.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.trackticum.R;
import com.example.trackticum.activities.StudLogin;

public class StudSettingsFragment extends Fragment {

    public StudSettingsFragment() {
        // Required empty public constructor
    }

    private Button logoutBTN;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stud_settings, container, false);

        // Add code below
        initializeData(view);
        setupListeners(view);

        return view;
    }

    private void initializeData(View view) {
        logoutBTN = view.findViewById(R.id.logout_btn);
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
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(requireActivity(), StudLogin.class);
        startActivity(intent);
        requireActivity().finish();
    }
}