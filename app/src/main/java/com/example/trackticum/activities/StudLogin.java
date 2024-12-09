package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

public class StudLogin extends AppCompatActivity {

    private TextInputEditText studno_et, studpassword_et;
    private Button login_btn, company_link_btn;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_login);
        setupWindowInsets();

        // Add code below
        initializeData();
        setupListeners();
    }

    private void initializeData() {
        studno_et = findViewById(R.id.login_studno);
        studpassword_et = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_btn);
        company_link_btn = findViewById(R.id.company_link_btn);
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
    }

    private void setupListeners() {
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });
        company_link_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudLogin.this, ComLogin.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String stud_no = studno_et.getText().toString().trim();
        String stud_password = studpassword_et.getText().toString().trim();

        if (stud_no.isEmpty()) {
            studno_et.setError("Please enter your Student ID");
            studno_et.requestFocus();
        } else if (stud_password.isEmpty()) {
            studpassword_et.setError("Please enter your password");
            studpassword_et.requestFocus();
        } else {
            progressDialog.setMessage("Logging in please wait");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = Constants.API_BASE_URL + "/student/stud-login";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.getBoolean("status");
                        String message = jsonResponse.getString("message");
                        int stud_id = jsonResponse.getInt("stud_id");

                        if (status) {
                            storeStudIdToSession(String.valueOf(stud_id));
                            redirectToMain();
                        }
                        progressDialog.dismiss();
                        Toast.makeText(StudLogin.this, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        Toast.makeText(StudLogin.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    progressDialog.dismiss();
                    Toast.makeText(StudLogin.this, "Login failed", Toast.LENGTH_SHORT).show();
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("student_number", stud_no);
                        params.put("password", stud_password);
                        return params;
                    }
                };
            queue.add(stringRequest);
        }
    }

    private void storeStudIdToSession(String studUserid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("stud_id", studUserid);
        editor.apply();
    }

    private void redirectToMain() {
        Intent intent = new Intent(StudLogin.this, StudMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String studUserid = sharedPreferences.getString("stud_id", null);
        String comId = sharedPreferences.getString("com_id", null);
        if (studUserid != null && comId == null) {
            Intent intent = new Intent(StudLogin.this, StudMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (studUserid == null && comId != null) {
            Intent intent = new Intent(StudLogin.this, ComMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}