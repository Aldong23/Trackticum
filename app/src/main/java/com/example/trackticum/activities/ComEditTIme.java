package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ComEditTIme extends AppCompatActivity {

    private Toolbar toolbar;

    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    TextInputEditText amIn, amOut, pmIn, pmOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_edit_time);
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
        toolbar = findViewById(R.id.com_edit_time_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Time");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        amIn = findViewById(R.id.amTimeInInput);
        amOut = findViewById(R.id.amTimeOutInput);
        pmIn = findViewById(R.id.pmTimeInInput);
        pmOut = findViewById(R.id.pmTimeOutInput);

        fetchTime();

    }

    private void setupListeners() {
        View.OnClickListener timeClickListener = v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);  // 24-hour format for initialization
            int minute = calendar.get(Calendar.MINUTE);

            // Using TimePickerDialog in 12-hour format with AM/PM selection
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfDay) -> {
                // Convert and display time in 12-hour format with AM/PM
                String amPm = (hourOfDay < 12) ? "AM" : "PM";
                int displayHour = (hourOfDay == 0) ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay);

                // Set the formatted time back to the input field
                String time = String.format("%02d:%02d %s", displayHour, minuteOfDay, amPm);
                ((TextInputEditText) v).setText(time);
            }, hour, minute, false); // 'false' enables the AM/PM selection in the dialog

            timePickerDialog.show();
        };


        amIn.setOnClickListener(timeClickListener);
        amOut.setOnClickListener(timeClickListener);
        pmIn.setOnClickListener(timeClickListener);
        pmOut.setOnClickListener(timeClickListener);
    }

    private void fetchTime() {
        String comId = sharedPreferences.getString("com_id", null);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/get-com-details/" + comId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject comDetails = new JSONObject(response);

                int comID = comDetails.getInt("id");
                String amTimeIn = comDetails.getString("am_time_in");
                String amTimeOut = comDetails.getString("am_time_out");
                String pmTimeIn = comDetails.getString("pm_time_in");
                String pmTimeOut = comDetails.getString("pm_time_out");

                amIn.setText(!amTimeIn.equals("null") ? amTimeIn : "");
                amOut.setText(!amTimeOut.equals("null") ? amTimeOut : "");
                pmIn.setText(!pmTimeIn.equals("null") ? pmTimeIn : "");
                pmOut.setText(!pmTimeOut.equals("null") ? pmTimeOut : "");

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void saveTime() {
        String am_in = amIn.getText().toString().trim();
        String am_out = amOut.getText().toString().trim();
        String pm_in = pmIn.getText().toString().trim();
        String pm_out = pmOut.getText().toString().trim();

        if (am_in.isEmpty() || am_in.equals("null") || am_out.isEmpty() || am_out.equals("null") ||
                pm_in.isEmpty() || pm_in.equals("null") || pm_out.isEmpty() || pm_out.equals("null")) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setMessage("Please wait...");
            progressDialog.setTitle("Saving");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            String comId = sharedPreferences.getString("com_id", null);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Constants.API_BASE_URL + "/company/update-time";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            // Parse the JSON response to get the message
                            JSONObject jsonResponse = new JSONObject(response);
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                            //request to refresh profile
                            sharedPreferences.edit().putBoolean("refreshComProfile", true).apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("com_id", comId);
                    params.put("am_in", am_in);
                    params.put("am_out", am_out);
                    params.put("pm_in", pm_in);
                    params.put("pm_out", pm_out);
                    return params;
                }
            };
            queue.add(stringRequest);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.com_edit_time_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.save_info) {
            saveTime();
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