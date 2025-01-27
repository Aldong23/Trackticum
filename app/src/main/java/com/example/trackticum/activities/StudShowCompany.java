package com.example.trackticum.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.utils.Constants;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudShowCompany extends AppCompatActivity {

    // For action bar
    private Toolbar toolbar;
    ProgressDialog progressDialog;

    //Fetch Company Information
    private TextView comNameTV, comNatureTV, comLocationTV, comEmailTV, comSlotTV, comContactTV, comBgTV, amTimeInOutTV, pmTimeInOutTV;
    private RoundedImageView comLogoIV;
    SharedPreferences sharedPreferences;
    private ExtendedFloatingActionButton applyBTN;

    //for Skill Requirements
    private FlexboxLayout jobsContainer;

    private String comId, studComID, studIsApproved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_show_company);
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
        toolbar = findViewById(R.id.show_company_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Companies");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);

        //Fetch Company Information
        comId = getIntent().getStringExtra("com_id");
        studComID = getIntent().getStringExtra("stud_com_id");
        studIsApproved = getIntent().getStringExtra("stud_is_approved");
        comLogoIV = findViewById(R.id.com_logo_IV);
        comNameTV = findViewById(R.id.com_name_tv);
        comNatureTV = findViewById(R.id.com_nature_tv);
        comLocationTV = findViewById(R.id.com_location_tv);
        comEmailTV = findViewById(R.id.com_email_tv);
        comSlotTV = findViewById(R.id.com_slot_tv);
        comContactTV = findViewById(R.id.com_contact_tv);
        comBgTV = findViewById(R.id.com_descrip_tv);
        amTimeInOutTV = findViewById(R.id.am_time_in_out);
        pmTimeInOutTV = findViewById(R.id.pm_time_in_out);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        fetchCompanyDetails();

        applyBTN = findViewById(R.id.apply_btn);
        if((!studComID.equals("null") && !studComID.isEmpty()) || studIsApproved.equals("0")){
            applyBTN.setVisibility(View.GONE);
        }else{
            applyBTN.setVisibility(View.VISIBLE);
            checkIfApplied();
        }

        //Setting up the Skill Requirements
        jobsContainer = findViewById(R.id.jobsContainer);
        fetchJobOffer();
    }

    private void setupListeners() {
        applyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StudShowCompany.this);
                builder.setTitle("Confirm Application");
                builder.setMessage("Are you sure you want to apply to this company?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    processApply();
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void processApply() {
        String studID = sharedPreferences.getString("stud_id", null);

        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Applying");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/apply-to-company";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        // Parse JSON response for better feedback
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Application sent successfully!", Toast.LENGTH_SHORT).show();
                            checkIfApplied();
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("student_id", studID);
                params.put("company_id", comId);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void fetchCompanyDetails() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/get-com-details/" + comId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject comDetails = new JSONObject(response);

                int comID = comDetails.getInt("id");
                String imageUrl = comDetails.getString("image_url");
                String comName = comDetails.getString("name");
                String comNature = comDetails.getString("nature");
                String comLocation = comDetails.getString("address");
                String comEmail = comDetails.getString("email");
                String comSlot = comDetails.getString("slot");
                String comContact = comDetails.getString("contact");
                String comBg = comDetails.getString("description");
                String amTimeIn = comDetails.getString("am_time_in");
                String amTimeOut = comDetails.getString("am_time_out");
                String amInOut = amTimeIn + " - " + amTimeOut;
                String pmTimeIn = comDetails.getString("pm_time_in");
                String pmTimeOut = comDetails.getString("pm_time_out");
                String pmInOut = pmTimeIn + " - " + pmTimeOut;

                comNameTV.setText(comName);
                comNatureTV.setText(comNature);
                comLocationTV.setText(comLocation);
                comEmailTV.setText(comEmail);
                comSlotTV.setText(comSlot);
                comContactTV.setText(comContact);
                comBgTV.setText(Html.fromHtml(comBg, Html.FROM_HTML_MODE_LEGACY));
                amTimeInOutTV.setText(!amInOut.equals("null - null") ? amInOut : "N/A");
                pmTimeInOutTV.setText(!pmInOut.equals("null - null") ? pmInOut : "N/A");

                Picasso.get().invalidate(imageUrl);
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

    private void fetchJobOffer() {
        String url = Constants.API_BASE_URL + "/company/get-job-offers/" + comId;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Clear the existing views in jobsContainer
                            jobsContainer.removeAllViews();

                            // Loop through the job offers in the response
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                int jobOfferId = obj.getInt("id");
                                String jobTitle = obj.getString("name");

                                // Create TextView for each job offer
                                TextView jobTextView = new TextView(StudShowCompany.this);
                                jobTextView.setText(jobTitle);
                                jobTextView.setPadding(16, 8, 16, 8);
                                jobTextView.setBackgroundResource(R.drawable.job_offer_style);
                                jobTextView.setTextColor(Color.BLACK);

                                Typeface customFont = ResourcesCompat.getFont(StudShowCompany.this, R.font.sf_rounded_regular);
                                jobTextView.setTypeface(customFont);

                                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(2, 2, 2, 2);
                                jobTextView.setLayoutParams(params);

                                // Add the TextView to the container
                                jobsContainer.addView(jobTextView);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(StudShowCompany.this, "Error processing job offers", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(StudShowCompany.this, "Failed to fetch job offers", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }

    private void checkIfApplied() {
        String studID = sharedPreferences.getString("stud_id", null);
        String url = Constants.API_BASE_URL + "/student/pending-application/" + studID + "/" + comId;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("exists")) {
                                boolean exists = response.getBoolean("exists");
                                if (exists) {
                                    applyBTN.setEnabled(false);
                                    applyBTN.setText("Applied");
                                } else {
                                    applyBTN.setEnabled(true);
                                    applyBTN.setText("Quick Apply");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Unexpected response from server", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Failed to check pending application", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonObjectRequest);
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