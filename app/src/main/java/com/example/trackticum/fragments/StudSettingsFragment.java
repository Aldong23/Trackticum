package com.example.trackticum.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.activities.StudLogin;
import com.example.trackticum.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudSettingsFragment extends Fragment {

    private Toolbar toolbar;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    TextInputEditText oldPasswordET, newPasswordET, confirmPasswordET, emailET;
    Button submitBTN, verifyBTN;
    ImageView statusIV;

    public StudSettingsFragment() {
        // Required empty public constructor
    }

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
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(requireContext());

        //For action bar
        toolbar = view.findViewById(R.id.stud_settings_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Settings");

        oldPasswordET = view.findViewById(R.id.old_password);
        newPasswordET = view.findViewById(R.id.new_password);
        confirmPasswordET = view.findViewById(R.id.confirm_password);
        submitBTN = view.findViewById(R.id.submit_btn);

        emailET = view.findViewById(R.id.email_et);
        verifyBTN = view.findViewById(R.id.verify_btn);
        statusIV = view.findViewById(R.id.status_IV);

        fetchEmailStatus();
    }

    private void fetchEmailStatus() {
        String studID = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/company/get-com-verified/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject comDetails = new JSONObject(response);

                String email = comDetails.getString("email");
                String isVerified = comDetails.getString("is_verified");

                emailET.setText(email);
                statusIV.setVisibility(isVerified.equals("1") ? View.VISIBLE : View.GONE);

            } catch (JSONException e) {
                Toast.makeText(requireActivity(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void setupListeners(View view) {
        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFields();
            }
        });
    }

    private void validateFields() {
        String oldPassword = oldPasswordET.getText().toString().trim();
        String newPassword = newPasswordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();

        // Check if fields are empty
        if (oldPassword.isEmpty()) {
            oldPasswordET.setError("Old password is required");
            oldPasswordET.requestFocus();
        } else if (newPassword.isEmpty()) {
            newPasswordET.setError("New password is required");
            newPasswordET.requestFocus();
        } else if (newPassword.length() < 8) { // Check password length
            newPasswordET.setError("New password must be at least 8 characters");
            newPasswordET.requestFocus();
        } else if (confirmPassword.isEmpty()) {
            confirmPasswordET.setError("Confirm password is required");
            confirmPasswordET.requestFocus();
        } else if (!newPassword.equals(confirmPassword)) {
            confirmPasswordET.setError("Passwords do not match");
            confirmPasswordET.requestFocus();
        } else {
            processChangePassword(oldPassword, newPassword, confirmPassword);
        }
    }

    private void processChangePassword(String oldPassword, String newPassword, String confirmPassword) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Processing");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String studID = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/student/stud-change-password";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        // Parse the response as JSON
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.getBoolean("status");
                        String message = jsonResponse.getString("message");

                        // Show the appropriate message
                        if (status) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            oldPasswordET.setText("");
                            newPasswordET.setText("");
                            confirmPasswordET.setText("");
                        } else {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stud_id", studID);
                params.put("old_password", oldPassword);
                params.put("new_password", newPassword);
                params.put("confirm_password", confirmPassword);
                return params;
            }
        };

        queue.add(stringRequest);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.stud_settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.logout) {
            redirectToLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Volley.newRequestQueue(requireContext()).cancelAll(request -> true);
    }
}