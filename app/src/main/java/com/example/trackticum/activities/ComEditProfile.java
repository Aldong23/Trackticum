package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class ComEditProfile extends AppCompatActivity {

    //For Action bar
    private Toolbar toolbar;

    //Inputs
    private TextInputEditText comNameET, comAddressET, comEmailET, comSlotET, comBgET;
    private AutoCompleteTextView comStatusET;
    private ArrayAdapter<String> adapterItems;

    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_edit_profile);
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
        toolbar = findViewById(R.id.com_edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Information");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        //initialized inputs
        comNameET = findViewById(R.id.com_name_ET);
        comStatusET = findViewById(R.id.com_status_ET);
        comAddressET = findViewById(R.id.com_address_ET);
        comEmailET = findViewById(R.id.com_email_ET);
        comSlotET = findViewById(R.id.com_slot_ET);
        comBgET = findViewById(R.id.com_bg_ET);
        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        //set up dropdown inputs
        String[] statusItems = {"Open", "Close"};
        adapterItems = new ArrayAdapter<String>(this,R.layout.dropdown_layout,statusItems);
        comStatusET.setAdapter(adapterItems);

        fetchOldComDetails();


    }

    private void setupListeners() {

    }

    private void fetchOldComDetails() {
        String comId = sharedPreferences.getString("com_id", null);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/get-com-details/" + comId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject comDetails = new JSONObject(response);

                int comID = comDetails.getInt("id");
                String comName = comDetails.getString("com_name");
                String comStatus = comDetails.getString("com_status");
                String comLocation = comDetails.getString("com_address");
                String comEmail = comDetails.getString("com_email");
                String comSlot = comDetails.getString("com_slot");
                String comBg = comDetails.getString("com_description");

                comNameET.setText(comName);
                comStatusET.setText(comStatus, false);
                comAddressET.setText(comLocation);
                comEmailET.setText(comEmail);
                comSlotET.setText(comSlot);
                comBgET.setText(comBg);

                comStatusET.setAdapter(adapterItems);

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void updateCompanyInfo() {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String comId = sharedPreferences.getString("com_id", null);
        String com_name = comNameET.getText().toString().trim();
        String com_status = comStatusET.getText().toString().trim();
        String com_address = comAddressET.getText().toString().trim();
        String com_email = comEmailET.getText().toString().trim();
        String com_slot = comSlotET.getText().toString().trim();
        String com_bg = comBgET.getText().toString().trim();

        if (com_name.isEmpty()) {
            comNameET.setError("Please enter your Name");
            comNameET.requestFocus();
        } else if (com_status.isEmpty()) {
            comStatusET.setError("Please enter your Status");
            comStatusET.requestFocus();
        } else if (com_address.isEmpty()) {
            comAddressET.setError("Please enter your Address");
            comAddressET.requestFocus();
        } else if (com_email.isEmpty()) {
            comEmailET.setError("Please enter your Email");
            comEmailET.requestFocus();
        } else if (!com_email.matches(emailPattern)) {
            comEmailET.setError("Please enter a valid email");
            comEmailET.requestFocus();
        } else if (com_slot.isEmpty()) {
            comSlotET.setError("Please enter Slot");
            comSlotET.requestFocus();
        } else {
            try {
                int slot = Integer.parseInt(com_slot);
                if (slot < 0) {
                    comSlotET.setError("Slot must be a positive number");
                    comSlotET.requestFocus();
                } else if (com_bg.isEmpty()) {
                    comBgET.setError("Please enter Background information");
                    comBgET.requestFocus();
                } else {
                    // All validations passed, proceed with the data
                    saveDataToDatabase(comId, com_name, com_status, com_address, com_email, com_slot, com_bg);
                }
            } catch (NumberFormatException e) {
                comSlotET.setError("Slot must be a valid number");
                comSlotET.requestFocus();
            }
        }
    }

    private void saveDataToDatabase(String comId, String comName, String comStatus, String comAddress, String comEmail, String comSlot, String comBg) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Saving");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/update-com-details";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(ComEditProfile.this, response, Toast.LENGTH_LONG).show();
                }, error -> {
                    progressDialog.dismiss();
                    Toast.makeText(ComEditProfile.this, "Failed to update", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("com_id", comId);
                params.put("com_name", comName);
                params.put("com_status", comStatus);
                params.put("com_address", comAddress);
                params.put("com_email", comEmail);
                params.put("com_slot", comSlot);
                params.put("com_bg", comBg);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.com_editprofile_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
            return true;
        } else if (id == R.id.save_info) {
            updateCompanyInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}