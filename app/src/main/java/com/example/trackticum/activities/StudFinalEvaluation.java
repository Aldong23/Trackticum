package com.example.trackticum.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class StudFinalEvaluation extends AppCompatActivity {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    String studID;

    TextInputEditText q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, q11, q12, q13, q14, q15;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_final_evaluation);
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
        toolbar = findViewById(R.id.stud_final_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Final Evaluation");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        studID = getIntent().getStringExtra("stud_id");

        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);
        q5 = findViewById(R.id.q5);
        q6 = findViewById(R.id.q6);
        q7 = findViewById(R.id.q7);
        q8 = findViewById(R.id.q8);
        q9 = findViewById(R.id.q9);
        q10 = findViewById(R.id.q10);
        q11 = findViewById(R.id.q11);
        q12 = findViewById(R.id.q12);
        q13 = findViewById(R.id.q13);
        q14 = findViewById(R.id.q14);
        q15 = findViewById(R.id.q15);

        // Initialize buttons
        submitButton = findViewById(R.id.submit_button);

        fetchFinalEvaluation();
    }

    private void setupListeners() {

        // Submit button click listener with validation
        submitButton.setOnClickListener(v -> {
            if (validateFields()) {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Submission")
                        .setMessage("Are you sure you want to submit your evaluation?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Proceed to save data
                            saveToDatabase();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Dismiss dialog
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    private void saveToDatabase() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/add-final-evaluation";

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting data...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        boolean status = jsonResponse.getBoolean("status");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (status) {
                            fetchFinalEvaluation();
                            Toast.makeText(this, "Submission Successful!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Collecting data from all fields (q1 to q15)
                params.put("student_id", studID);
                params.put("knowledge_of_the_work_assigned", q1.getText().toString().trim());
                params.put("efficiency", q2.getText().toString().trim());
                params.put("quality_of_work", q3.getText().toString().trim());
                params.put("cleanliness_and_orderliness_of_work", q4.getText().toString().trim());
                params.put("handling_of_office_equipments", q5.getText().toString().trim());
                params.put("personality", q6.getText().toString().trim());
                params.put("attendance", q7.getText().toString().trim());
                params.put("punctuality", q8.getText().toString().trim());
                params.put("honesty", q9.getText().toString().trim());
                params.put("initiative", q10.getText().toString().trim());
                params.put("courtesy_and_respect", q11.getText().toString().trim());
                params.put("rapport_with_co_workers", q12.getText().toString().trim());
                params.put("uniform_dress_code", q13.getText().toString().trim());
                params.put("reliability", q14.getText().toString().trim());
                params.put("sense_of_humor", q15.getText().toString().trim());

                return params;
            }
        };

        queue.add(stringRequest);
    }


    private boolean validateFields() {
        // Validate q1 (not >20, not <0)
        if (!validateInput(q1, 20)) return false;

        // Validate q2 and q3 (not >10, not <0)
        if (!validateInput(q2, 10)) return false;
        if (!validateInput(q3, 10)) return false;

        // Validate q4 to q15 (not >5, not <0)
        if (!validateInput(q4, 5)) return false;
        if (!validateInput(q5, 5)) return false;
        if (!validateInput(q6, 5)) return false;
        if (!validateInput(q7, 5)) return false;
        if (!validateInput(q8, 5)) return false;
        if (!validateInput(q9, 5)) return false;
        if (!validateInput(q10, 5)) return false;
        if (!validateInput(q11, 5)) return false;
        if (!validateInput(q12, 5)) return false;
        if (!validateInput(q13, 5)) return false;
        if (!validateInput(q14, 5)) return false;
        if (!validateInput(q15, 5)) return false;

        return true;
    }

    // Helper method for validation with error messages
    private boolean validateInput(TextInputEditText field, int maxValue) {
        String inputText = field.getText().toString().trim();

        // Check if empty
        if (inputText.isEmpty()) {
            field.setError("This field cannot be empty");
            field.requestFocus();
            return false;
        }

        try {
            // Parse the input to a number
            int value = Integer.parseInt(inputText);

            // Check if value is within the allowed range
            if (value < 0 || value > maxValue) {
                field.setError("Value must be between 0 and " + maxValue);
                field.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            // Catch invalid numbers
            field.setError("Please enter a valid number");
            field.requestFocus();
            return false;
        }

        // Clear error if validation passed
        field.setError(null);
        return true;
    }

    private void fetchFinalEvaluation() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/get-final-evaluation/" + studID;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                // Check if data exists
                if (jsonResponse.getBoolean("status")) {
                    JSONObject data = jsonResponse.getJSONObject("data");

                    // Map the response data to fields
                    q1.setText(data.getString("knowledge_of_the_work_assigned"));
                    q2.setText(data.getString("efficiency"));
                    q3.setText(data.getString("quality_of_work"));
                    q4.setText(data.getString("cleanliness_and_orderliness_of_work"));
                    q5.setText(data.getString("handling_of_office_equipments"));
                    q6.setText(data.getString("personality"));
                    q7.setText(data.getString("attendance"));
                    q8.setText(data.getString("punctuality"));
                    q9.setText(data.getString("honesty"));
                    q10.setText(data.getString("initiative"));
                    q11.setText(data.getString("courtesy_and_respect"));
                    q12.setText(data.getString("rapport_with_co_workers"));
                    q13.setText(data.getString("uniform_dress_code"));
                    q14.setText(data.getString("reliability"));
                    q15.setText(data.getString("sense_of_humor"));

                    // Disable all inputs and submit button
                    disableInputs();
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error fetching evaluation data", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(this, "Failed to fetch evaluation data", Toast.LENGTH_SHORT).show();
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void disableInputs() {
        q1.setEnabled(false);
        q2.setEnabled(false);
        q3.setEnabled(false);
        q4.setEnabled(false);
        q5.setEnabled(false);
        q6.setEnabled(false);
        q7.setEnabled(false);
        q8.setEnabled(false);
        q9.setEnabled(false);
        q10.setEnabled(false);
        q11.setEnabled(false);
        q12.setEnabled(false);
        q13.setEnabled(false);
        q14.setEnabled(false);
        q15.setEnabled(false);
        // Assuming you have a submit button
        submitButton.setEnabled(false);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}