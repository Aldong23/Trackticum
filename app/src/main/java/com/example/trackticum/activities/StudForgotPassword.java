package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudForgotPassword extends AppCompatActivity {

    private Toolbar toolbar;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    private TextInputEditText emailET;
    Button submitBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_forgot_password);
        setupWindowInsets();
        //setting up the status bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.deepTeal));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0); // Clear the flag for light status bar
        }

        //Add code here
        initializeData();
        setupListeners();
    }

    private void initializeData() {
        //set up action bar
        toolbar = findViewById(R.id.com_forgot_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Forgot Password");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        emailET = findViewById(R.id.email_et);
        submitBTN = findViewById(R.id.submit_btn);
    }

    private void setupListeners() {
        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                String email = emailET.getText().toString().trim();

                if (email.isEmpty()) {
                    emailET.setError("Email is required");
                    emailET.requestFocus();
                } else if (!email.matches(emailPattern)) {
                    emailET.setError("Please enter a valid email");
                    emailET.requestFocus();
                } else {
                    processForgotPassword(email);
                }
            }
        });
    }

    private void processForgotPassword(String email) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Processing");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/stud-process-forgot-password";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        // Parse the response as JSON
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.getBoolean("status");
                        String message = jsonResponse.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        if (status) {
                            String studID = jsonResponse.getString("stud_id");
                            Intent intent = new Intent(StudForgotPassword.this, StudPasswordReset.class);
                            intent.putExtra("stud_id", studID);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to process", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, // Timeout in milliseconds (30 seconds)
                0, // No retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Backoff multiplier
        ));

        queue.add(stringRequest);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}