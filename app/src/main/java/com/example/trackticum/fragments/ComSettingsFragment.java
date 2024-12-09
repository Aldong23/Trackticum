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
import com.example.trackticum.activities.ComLogin;
import com.example.trackticum.activities.StudLogin;

public class ComSettingsFragment extends Fragment {

    public ComSettingsFragment() {
        // Required empty public constructor
    }

    private Button logoutBTN;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_com_settings, container, false);

        initializeData(view);
        setupListeners(view);
        return view;
    }

    private void initializeData(View view) {
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(requireActivity(), ComLogin.class);
        startActivity(intent);
        requireActivity().finish();
    }
}