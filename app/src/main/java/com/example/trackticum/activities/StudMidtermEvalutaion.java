package com.example.trackticum.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudMidtermEvalutaion extends AppCompatActivity {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    String studID;

    private RadioGroup[] aGroups = new RadioGroup[5]; // For a1 - a5
    private RadioGroup[] bGroups = new RadioGroup[10]; // For b1 - b10
    private TextInputEditText strongPointsInput, improvementsInput;
    private CheckBox discussedCheckbox;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_midterm_evalutaion);
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
        toolbar = findViewById(R.id.stud_midterm_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Midterm Evaluation");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        studID = getIntent().getStringExtra("stud_id");

        // Initialize RadioGroups for a1 - a5
        aGroups[0] = findViewById(R.id.a1);
        aGroups[1] = findViewById(R.id.a2);
        aGroups[2] = findViewById(R.id.a3);
        aGroups[3] = findViewById(R.id.a4);
        aGroups[4] = findViewById(R.id.a5);

        // Initialize RadioGroups for b1 - b10
        bGroups[0] = findViewById(R.id.b1);
        bGroups[1] = findViewById(R.id.b2);
        bGroups[2] = findViewById(R.id.b3);
        bGroups[3] = findViewById(R.id.b4);
        bGroups[4] = findViewById(R.id.b5);
        bGroups[5] = findViewById(R.id.b6);
        bGroups[6] = findViewById(R.id.b7);
        bGroups[7] = findViewById(R.id.b8);
        bGroups[8] = findViewById(R.id.b9);
        bGroups[9] = findViewById(R.id.b10);

        // Initialize other inputs
        strongPointsInput = findViewById(R.id.strong_points_input);
        improvementsInput = findViewById(R.id.improvements_input);
        discussedCheckbox = findViewById(R.id.discussed_checkbox);
        submitButton = findViewById(R.id.submit_button);

        fetchEvaluation();
    }

    private void setupListeners() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    new AlertDialog.Builder(StudMidtermEvalutaion.this)
                            .setTitle("Confirm Submission")
                            .setMessage("Are you sure you want to submit your evaluation?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Proceed to save data
                                storeToDataBase();
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                // Dismiss dialog
                                dialog.dismiss();
                            })
                            .show();

                }
            }
        });
    }

    // Validate the form
    private boolean validateForm() {
        // Check if all radio groups in aGroups are selected
        for (int i = 0; i < aGroups.length; i++) {
            if (aGroups[i].getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select a rating for all questions", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Check if all radio groups in bGroups are selected
        for (int i = 0; i < bGroups.length; i++) {
            if (bGroups[i].getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select a rating for all questions", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Check if strongPointsInput is filled
        if (strongPointsInput.getText().toString().trim().isEmpty()) {
            strongPointsInput.setError("This field is required");
            strongPointsInput.requestFocus();
            return false;
        }

        // Check if improvementsInput is filled
        if (improvementsInput.getText().toString().trim().isEmpty()) {
            improvementsInput.setError("This field is required");
            improvementsInput.requestFocus();
            return false;
        }

        return true;
    }

    private void storeToDataBase() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/add-midterm-evaluation";

        // Show a progress dialog while submitting the data
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
                            fetchEvaluation();
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

                // Student ID (example value, replace with actual logic to fetch it)
                params.put("stud_id", studID);

                // Add ratings from aGroups (a1 to a5)
                for (int i = 0; i < aGroups.length; i++) {
                    int selectedId = aGroups[i].getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    params.put("a" + (i + 1), selectedRadioButton.getText().toString());
                }

                // Add ratings from bGroups (b1 to b10)
                for (int i = 0; i < bGroups.length; i++) {
                    int selectedId = bGroups[i].getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    params.put("b" + (i + 1), selectedRadioButton.getText().toString());
                }

                // Add strong points and improvements
                params.put("strong_points", strongPointsInput.getText().toString().trim());
                params.put("improvements", improvementsInput.getText().toString().trim());

                // Add discussed checkbox value (1 if checked, 0 if unchecked)
                params.put("discussed", discussedCheckbox.isChecked() ? "1" : "0");

                return params;
            }
        };

        queue.add(stringRequest);
    }


    private void fetchEvaluation() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/get-midterm-evaluation/" + studID;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                // Check if data exists
                if (jsonResponse.getBoolean("status")) {
                    JSONObject data = jsonResponse.getJSONObject("data");

                    // Map descriptive fields to aGroups (a1 - a5)
                    setRadioGroupValue(aGroups[0], data.getInt("knowledge_of_work"));
                    setRadioGroupValue(aGroups[1], data.getInt("quality_and_quantity_of_work"));
                    setRadioGroupValue(aGroups[2], data.getInt("punctuality_attendance"));
                    setRadioGroupValue(aGroups[3], data.getInt("communication_skill"));
                    setRadioGroupValue(aGroups[4], data.getInt("rapport_with_employee"));

                    // Map descriptive fields to bGroups (b1 - b10)
                    setRadioGroupValue(bGroups[0], data.getInt("physical_appearance_grooming"));
                    setRadioGroupValue(bGroups[1], data.getInt("ability_to_follow_direction"));
                    setRadioGroupValue(bGroups[2], data.getInt("courtesy"));
                    setRadioGroupValue(bGroups[3], data.getInt("initiative"));
                    setRadioGroupValue(bGroups[4], data.getInt("drive_and_leadership"));
                    setRadioGroupValue(bGroups[5], data.getInt("interest_motivation"));
                    setRadioGroupValue(bGroups[6], data.getInt("reliability"));
                    setRadioGroupValue(bGroups[7], data.getInt("mental_maturity"));
                    setRadioGroupValue(bGroups[8], data.getInt("emotional_maturity"));
                    setRadioGroupValue(bGroups[9], data.getInt("interpersonal_maturity"));

                    // Populate other inputs
                    strongPointsInput.setText(data.getString("student_strong_points"));
                    improvementsInput.setText(data.getString("student_need_improvements"));
                    discussedCheckbox.setChecked(data.getInt("is_discussed") == 1);

                    // Disable all inputs and the submit button
                    disableAllInputs();

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

    private void setRadioGroupValue(RadioGroup radioGroup, int value) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            if (Integer.parseInt(radioButton.getText().toString()) == value) {
                radioButton.setChecked(true);
                break;
            }
        }
    }


    private void disableAllInputs() {
        // Disable RadioGroups for a1 - a5
        for (RadioGroup group : aGroups) {
            for (int i = 0; i < group.getChildCount(); i++) {
                group.getChildAt(i).setEnabled(false);
            }
        }

        // Disable RadioGroups for b1 - b10
        for (RadioGroup group : bGroups) {
            for (int i = 0; i < group.getChildCount(); i++) {
                group.getChildAt(i).setEnabled(false);
            }
        }

        // Disable other inputs
        strongPointsInput.setEnabled(false);
        improvementsInput.setEnabled(false);
        discussedCheckbox.setEnabled(false);

        // Disable submit button
        submitButton.setEnabled(false);
    }


    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

}