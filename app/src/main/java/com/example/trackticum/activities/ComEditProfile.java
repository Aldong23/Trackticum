package com.example.trackticum.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ComEditProfile extends AppCompatActivity {

    //For Action bar
    private Toolbar toolbar;

    //Inputs
    private TextInputEditText comNameET, comAddressET, comSlotET, comBgET;
    RoundedImageView comLogoIV;
    ImageButton uploadLogoBtn;

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
        comAddressET = findViewById(R.id.com_address_ET);
        comSlotET = findViewById(R.id.com_slot_ET);
        comBgET = findViewById(R.id.com_bg_ET);
        comLogoIV = findViewById(R.id.com_logo_IV);
        uploadLogoBtn = findViewById(R.id.upload_logo_btn);
        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        fetchOldComDetails();


    }

    private void setupListeners() {
        uploadLogoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ComEditProfile.this)
                        .cropSquare()
                        .galleryOnly()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            String base64Image = convertImageToBase64(uri);
            if (base64Image != null) {
                uploadImageToServer(base64Image, uri);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR && data != null) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToServer(String base64Image, Uri uri) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/upload-com-logo";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Successful", Toast.LENGTH_LONG).show();
                    comLogoIV.setImageURI(uri);
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_id", comId);
                params.put("logo", base64Image); // Send Base64 image string
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error encoding image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void fetchOldComDetails() {
        String comId = sharedPreferences.getString("com_id", null);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/get-com-details/" + comId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject comDetails = new JSONObject(response);

                int comID = comDetails.getInt("id");
                String imageUrl = comDetails.getString("image_url");
                String comName = comDetails.getString("name");
                String comLocation = comDetails.getString("address");
                String comSlot = comDetails.getString("slot");
                String comBg = comDetails.getString("description");

                comNameET.setText(comName);
                comAddressET.setText(comLocation);
                comSlotET.setText(comSlot);
                comBgET.setText(comBg);

                if (!imageUrl.isEmpty()) {
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.img_placeholder)
                            .error(R.drawable.img_placeholder)
                            .resize(500, 500)
                            .centerCrop()
                            .into(comLogoIV);
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void updateCompanyInfo() {
        String comId = sharedPreferences.getString("com_id", null);
        String com_name = comNameET.getText().toString().trim();
        String com_address = comAddressET.getText().toString().trim();
        String com_slot = comSlotET.getText().toString().trim();
        String com_bg = comBgET.getText().toString().trim();

        if (com_name.isEmpty()) {
            comNameET.setError("Please enter your Name");
            comNameET.requestFocus();
        } else if (com_address.isEmpty()) {
            comAddressET.setError("Please enter your Address");
            comAddressET.requestFocus();
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
                    saveDataToDatabase(comId, com_name, com_address, com_slot, com_bg);
                }
            } catch (NumberFormatException e) {
                comSlotET.setError("Slot must be a valid number");
                comSlotET.requestFocus();
            }
        }
    }

    private void saveDataToDatabase(String comId, String comName, String comAddress, String comSlot, String comBg) {
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
                params.put("com_address", comAddress);
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