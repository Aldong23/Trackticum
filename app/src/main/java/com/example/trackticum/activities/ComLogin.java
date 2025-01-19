package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
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
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ComLogin extends AppCompatActivity {

    private TextInputEditText comemail_et, compassword_et;
    private Button login_btn, student_link_btn, forgot_password;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_login);
        setupWindowInsets();

        //Add code here
        initializeData();
        setupListeners();

    }

    private void initializeData() {
        comemail_et = findViewById(R.id.login_email);
        compassword_et = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_btn);
        student_link_btn = findViewById(R.id.student_link_btn);
        forgot_password = findViewById(R.id.forgot_password_btn);
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
    }

    private void setupListeners() {
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performlogin();
            }
        });
        student_link_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ComLogin.this, StudLogin.class);
                startActivity(intent);
            }
        });
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ComLogin.this, ComForgotPassword.class);
                startActivity(intent);
            }
        });
    }

    private void performlogin() {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String com_email = comemail_et.getText().toString().trim();
        String com_password = compassword_et.getText().toString().trim();

        if (com_email.isEmpty()) {
            comemail_et.setError("Please enter your Email");
            comemail_et.requestFocus();
        } else if (!com_email.matches(emailPattern)) {
            comemail_et.setError("Please enter a valid email");
            comemail_et.requestFocus();
        } else if (com_password.isEmpty()) {
            compassword_et.setError("Please enter your password");
            compassword_et.requestFocus();
        } else {
            progressDialog.setMessage("Logging in please wait");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Constants.API_BASE_URL + "/company/com-login";

            // Retrieve FCM token asynchronously
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(ComLogin.this, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get the FCM token
                        String fcmToken = task.getResult();

                        // Proceed with the login request
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                response -> {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        boolean status = jsonResponse.getBoolean("status");
                                        String message = jsonResponse.getString("message");

                                        if (status) {
                                            int comId = jsonResponse.getInt("com_id");
                                            storeComIdToSession(String.valueOf(comId));
                                            storeSchoolYearToSession();
                                            redirectToMain();
                                            Toast.makeText(ComLogin.this, message, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ComLogin.this, message, Toast.LENGTH_SHORT).show();
                                        }
                                        progressDialog.dismiss();
                                    } catch (JSONException e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(ComLogin.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                                    }
                                }, error -> {
                            progressDialog.dismiss();
                            Toast.makeText(ComLogin.this, "Login failed", Toast.LENGTH_SHORT).show();
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("email", com_email);
                                params.put("password", com_password);
                                params.put("fcm_token", fcmToken); // Add FCM token to parameters
                                return params;
                            }
                        };
                        queue.add(stringRequest);
                    });
        }
    }

    private void storeComIdToSession(String comId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("com_id", comId);
        editor.apply();
    }

    private void storeSchoolYearToSession() {
        String url = Constants.API_BASE_URL + "/school-year/get-default";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                boolean status = jsonObject.getBoolean("status");

                if (status) {
                    String school_year_id = jsonObject.getString("id");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("sy_id", school_year_id);
                    editor.apply();
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Announcement", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Announcement", error.toString());
        });

        queue.add(request);
    }

    private void redirectToMain() {
        Intent intent = new Intent(ComLogin.this, ComMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Volley.newRequestQueue(this).cancelAll(request -> true);
    }
}