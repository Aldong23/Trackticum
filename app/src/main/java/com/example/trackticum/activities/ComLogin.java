package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class ComLogin extends AppCompatActivity {

    private TextInputEditText comemail_et, compassword_et;
    private Button login_btn, student_link_btn;
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

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean status = jsonResponse.getBoolean("status");
                            String message = jsonResponse.getString("message");
                            int comId = jsonResponse.getInt("com_id");

                            if (status) {
                                storeComIdToSession(String.valueOf(comId));
                                redirectToMain();
                            }
                            progressDialog.dismiss();
                            Toast.makeText(ComLogin.this, message, Toast.LENGTH_SHORT).show();
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
                    return params;
                }
            };
            queue.add(stringRequest);
        }
    }

    private void storeComIdToSession(String comId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("com_id", comId);
        editor.apply();
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
}