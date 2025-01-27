package com.example.trackticum.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StudEditProfile extends AppCompatActivity {

    //For Action bar
    private Toolbar toolbar;

    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    private TextInputEditText studContactET, studBirthdayET, studAddressET, studParentET, comDepartmentEt;
    private AutoCompleteTextView studSexET;
    private RoundedImageView studentIV;
    private Button uploadImageBTN;

    private ArrayAdapter<String> adapterItems;

    //date picker
    DatePickerDialog.OnDateSetListener onDateSetListener;
    final Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_edit_profile);
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
        toolbar = findViewById(R.id.stud_edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Information");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        //inputs
        studContactET = findViewById(R.id.stud_contact_et);
        studBirthdayET = findViewById(R.id.stud_birthday_et);
        studAddressET = findViewById(R.id.stud_address_et);
        studSexET = findViewById(R.id.stud_sex_et);
        studentIV = findViewById(R.id.stud_image_tv);
        studParentET = findViewById(R.id.stud_parent_et);
        comDepartmentEt = findViewById(R.id.com_department_et);
        uploadImageBTN = findViewById(R.id.upload_image_btn);

        //set up dropdown inputs
        String[] statusItems = {"Male", "Female"};
        adapterItems = new ArrayAdapter<String>(this,R.layout.dropdown_layout,statusItems);
        studSexET.setAdapter(adapterItems);

        //dateinputPicker
        studBirthdayET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        StudEditProfile.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,onDateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                month = i1+1;
                String date = month+"/"+i2+"/"+i;
                studBirthdayET.setText(date);
            }
        };


        fetchStudOldDetails();
    }

    private void setupListeners() {
        uploadImageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(StudEditProfile.this)
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
        }
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

    private void uploadImageToServer(String base64Image, Uri uri) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String studID = sharedPreferences.getString("stud_id", null);
        String url = Constants.API_BASE_URL + "/student/upload-student-image";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Successful", Toast.LENGTH_LONG).show();
                    studentIV.setImageURI(uri);
                    //request to refresh profile
                    sharedPreferences.edit().putBoolean("refreshStudProfile", true).apply();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stud_id", studID);
                params.put("stud_image", base64Image); // Send Base64 image string
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void fetchStudOldDetails() {
        String studID = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/get-stud-details/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject studDetails = new JSONObject(response);

                String stud_id = studDetails.getString("id");
                String imageUrl = studDetails.getString("image_url");
                String email = studDetails.getString("email");
                String contact = studDetails.getString("contact");
                String sex = studDetails.getString("gender");
                String birthday = studDetails.getString("formatted_birthday");
                String address = studDetails.getString("address");
                String parent = studDetails.getString("parent_guardian");
                String comDepartment = studDetails.getString("department_assigned");

                studContactET.setText(!contact.equals("null") ? contact : "");
                studBirthdayET.setText(!birthday.equals("null") ? birthday : "");
                studAddressET.setText(!address.equals("null") ? address : "");
                studParentET.setText(!parent.equals("null") ? parent : "");
                comDepartmentEt.setText(!comDepartment.equals("null") ? comDepartment : "");
                studSexET.setText(sex, false);

                studSexET.setAdapter(adapterItems);
                if (!imageUrl.isEmpty()) {
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.img_placeholder)
                            .error(R.drawable.img_placeholder)
                            .resize(500, 500)
                            .centerCrop()
                            .into(studentIV);
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void updateStudInfo() {
        String contactPattern = "[0][6-9][0-9]{9}";

        String studID = sharedPreferences.getString("stud_id", null);
        String stud_contact = studContactET.getText().toString().trim();
        String stud_sex = studSexET.getText().toString().trim();
        String stud_birthday = studBirthdayET.getText().toString().trim();
        String stud_address = studAddressET.getText().toString().trim();
        String stud_parent = studParentET.getText().toString().trim();
        String com_department = comDepartmentEt.getText().toString().trim();

        if (stud_contact.isEmpty()) {
            studContactET.setError("Please enter your contact");
            studContactET.requestFocus();
        } else if (!stud_contact.matches(contactPattern)) {
            studContactET.setError("Enter correct contact number");
            studContactET.requestFocus();
        } else if (stud_sex.isEmpty()) {
            studSexET.setError("Please enter your sex");
            studSexET.requestFocus();
        } else if (stud_birthday.isEmpty()) {
            studSexET.setError("Please enter your birthday");
            studSexET.requestFocus();
        } else if (stud_address.isEmpty()) {
            studSexET.setError("Please enter your address");
            studSexET.requestFocus();
        } else if (stud_parent.isEmpty()) {
            studParentET.setError("Please enter the parent/guardian name");
            studParentET.requestFocus();
        } else {
            saveDataToDatabase(studID, stud_contact, stud_sex, stud_birthday, stud_address, stud_parent, com_department);
        }
    }

    private void saveDataToDatabase(String studID, String studContact, String studSex, String studBirthday, String studAddress, String studParent, String comDepartment) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Saving");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/update-stud-details";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        boolean status = jsonResponse.getBoolean("status");
                        Toast.makeText(StudEditProfile.this, message, Toast.LENGTH_LONG).show();

                        // Check if the update was successful before refreshing
                        if (status) {
                            sharedPreferences.edit().putBoolean("refreshStudProfile", true).apply();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(StudEditProfile.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }, error -> {
                    progressDialog.dismiss();
                    Toast.makeText(StudEditProfile.this, "Failed to update", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stud_id", studID);
                params.put("stud_contact", studContact);
                params.put("stud_sex", studSex);
                params.put("stud_birthday", studBirthday);
                params.put("stud_address", studAddress);
                params.put("stud_parent", studParent);
                params.put("com_department", comDepartment);
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
        getMenuInflater().inflate(R.menu.stud_editprofile_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.save_info) {
            updateStudInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}